package com.owon.uppersoft.dso.delegate;

import java.beans.PropertyChangeSupport;

import javax.swing.SwingUtilities;

import com.owon.uppersoft.dso.function.PersistentDisplay;
import com.owon.uppersoft.dso.global.CoreControl;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.model.trigger.TriggerSet;
import com.owon.uppersoft.dso.model.trigger.VoltsensableTrigger;
import com.owon.uppersoft.dso.model.trigger.common.Thredshold;
import com.owon.uppersoft.dso.model.trigger.common.Voltsensor;
import com.owon.uppersoft.dso.source.comm.effect.SubmitorFactory;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.vds.core.aspect.control.Pos0_VBChangeInfluence;
import com.owon.uppersoft.vds.core.comm.effect.Submitable;

public class DefaultPVI implements Pos0_VBChangeInfluence {
	private CoreControl cc;
	private PropertyChangeSupport pcs;

	public DefaultPVI(CoreControl cc, PropertyChangeSupport pcs) {
		this.cc = cc;
		this.pcs = pcs;
	}

	@Override
	public void resetPersistence() {
		pcs.firePropertyChange(PersistentDisplay.PERSISTENCE_RESET, null, null);
	}

	@Override
	public void notifyChannelUpdate() {
		pcs.firePropertyChange(PropertiesItem.APPLY_CHANNELS, null, null);
	}

	protected boolean shouldSyncVoltsense_UPPLOW() {
		return !cc.isRunMode_slowMove();
	}

	/**
	 * Changes along with the voltage scale change
	 * 
	 * @param chlidx
	 *            channel index
	 * @param pos0
	 *            zero-point position, with the vertical center of the screen as zero
	 * @param vb0
	 *            voltage scale before the change
	 * @param vb1
	 *            voltage scale after the change
	 */
	public void thredshold_voltsense_ByVoltBase(int chlidx, int pos0, int vb0,
			int vb1, Runnable r, int destpos0) {
		TriggerControl tc = cc.getTriggerControl();
		final TriggerSet ts = tc.getTriggerSetOrNull(chlidx);
		if (ts == null)
			return;

		double intRatio = cc.getMachineInfo().getVoltagesRatio(vb0, vb1);

		// System.err.println(String.format("%d, %d, %d, %f", chlidx, pos0,
		// destpos0, intRatio));

		byte alpha = tc.getChannelModeAlpha();
		if (ts.isCurrentTrigger_Slope()) {
			Thredshold ths = ts.slope.getThredshold();

			/** The calculation uses the relative distance between the trigger level and the zero-point position */

			// int[] vbs = controlManager.getMachineInfo().intVOLTAGE[0];
			// System.out.println(pos0 + ", " + vs + ", " + vbs[vb0] + ", "
			// +
			// vbs[vb1]);
			long vs = destpos0
					+ (int) ((double) (ths.getLowest() - pos0) * intRatio);
			long vs2 = destpos0
					+ (int) ((double) (ths.getUppest() - pos0) * intRatio);
			if (tc.isTrgEnable() && (ths.setLowest(vs) || (ths.setUppest(vs2)))) {
				Submitable sbm = SubmitorFactory.reInit();
				ts.slope.submitUpper_Lower(alpha, chlidx, sbm);
				sbm.apply_trgThen(r);

				pcs.firePropertyChange(PropertiesItem.UPDATE_UPP_LOW, -1,
						chlidx);
			}
		} else if (ts.isVoltsenseSupport()) {
			VoltsensableTrigger vt = (VoltsensableTrigger) ts.getTrigger();
			/** The calculation uses the relative distance between the trigger level and the zero-point position */
			Voltsensor vs = vt.getVoltsensor();

			long value = vs.getVoltsense() - pos0;
			// int[] vbs = controlManager.getMachineInfo().intVOLTAGE[0];
			// System.out.println(pos0 + ", " + vs + ", " + vbs[vb0] + ", "
			// +
			// vbs[vb1]);

			// System.err.println(String
			// .format("%d, %d", vs.getVoltsense(), value));

			value = destpos0 + (int) ((double) value * intRatio);

			// System.err.println(value);

			if (tc.isTrgEnable() && vs.setVoltsense(value)) {
				Submitable sbm = SubmitorFactory.reInit();
				vt.submitVoltsense(alpha, chlidx, sbm);
				sbm.apply_trgThen(r);

				/** Here the changed trigger level value is refreshed; refreshing is optional, it only makes the refreshed value more accurate */
				pcs.firePropertyChange(PropertiesItem.UPDATE_VOLTSENSE, -1,
						chlidx);
			}
		}
	}

	/**
	 * Changes along with the zero-point position change
	 * 
	 * @param chlidx
	 *            channel index
	 * @param dp
	 *            zero-point change difference
	 */
	public Runnable thredshold_voltsense_ByPos0(final int chlidx, int dp,
			final boolean commit) {
		TriggerControl tc = cc.getTriggerControl();
		final TriggerSet ts = tc.getTriggerSetOrNull(chlidx);
		if (ts == null)
			return null;

		final byte alpha = tc.getChannelModeAlpha();
		Runnable r = null;
		if (ts.isCurrentTrigger_Slope()) {
			Thredshold ths = ts.slope.getThredshold();
			if (tc.isTrgEnable() && (ths.setUppest(ths.getUppest() + dp))
					|| (ths.setLowest(ths.getLowest() + dp))) {

				/** Here the changed trigger level value is refreshed; refreshing is optional, it only makes the refreshed value more accurate */
				pcs.firePropertyChange(PropertiesItem.UPDATE_UPP_LOW, -1,
						chlidx);

				r = new Runnable() {
					@Override
					public void run() {
						// System.err.println("thredshold_voltsense_ByPos0");
						if (commit) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									Submitable sbm = SubmitorFactory.reInit();
									ts.slope.submitUpper_Lower(alpha, chlidx, sbm);
									sbm.apply_trg();
								}
							});
						}
					}
				};
			}
		} else if (ts.isVoltsenseSupport()) {
			final VoltsensableTrigger vt = (VoltsensableTrigger) ts
					.getTrigger();
			Voltsensor vs = vt.getVoltsensor();
			long value = vs.getVoltsense();
			vs.setVoltsense(value + dp);
			if (tc.isTrgEnable()) {

				/** Here the changed trigger level value is refreshed; refreshing is optional, it only makes the refreshed value more accurate */
				pcs.firePropertyChange(PropertiesItem.UPDATE_VOLTSENSE, -1,
						chlidx);

				r = new Runnable() {
					@Override
					public void run() {
						// System.err.println("thredshold_voltsense_ByPos0");
						if (commit) {
							SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
									// Change the control task that changes the trigger level along with the zero point to start from the UI thread instead of the command queue
									Submitable sbm = SubmitorFactory.reInit();
									//System.out
									//		.println("sbm.presetTrg_cur_chl: "
									//				+ chlidx);
									vt.submitVoltsense(alpha, chlidx, sbm);
									sbm.apply_trg();
								}
							});
						}
					}
				};
			}
		}
		return r;
	}
}