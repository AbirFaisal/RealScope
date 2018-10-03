package com.owon.uppersoft.dso.delegate;

import java.beans.PropertyChangeSupport;

import com.owon.uppersoft.dso.function.PersistentDisplay;
import com.owon.uppersoft.dso.global.CoreControl;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.mode.control.TimeControlDelegate;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.vds.core.aspect.control.ITimeControl;
import com.owon.uppersoft.vds.core.aspect.control.TimeConfProvider;
import com.owon.uppersoft.vds.ui.prompt.FadeIOShell;

public final class DefaultTimeControlDelegate implements TimeControlDelegate {
	private CoreControl cc;
	private PropertyChangeSupport pcs;

	public DefaultTimeControlDelegate(CoreControl cc, PropertyChangeSupport pcs) {
		this.cc = cc;
		this.pcs = pcs;
	}

	@Override
	public void onHorTrgPosChange() {
	}

	public void resetPersistence() {
		pcs.firePropertyChange(PersistentDisplay.PERSISTENCE_RESET, null, null);
	}

	@Override
	public Runnable getResetPersistenceRunnable() {
		return new Runnable() {
			@Override
			public void run() {
				resetPersistence();
			}
		};
	}

	@Override
	public void nofiyResetPersistence() {
		resetPersistence();
	}

	@Override
	public void onHorTrgPosChangedByTimebase(int tbidx, int htp) {
		pcs.firePropertyChange(ITimeControl.onHorTrgPosChangedByTimebase,
				tbidx, htp);
	}

	@Override
	public void notifyInitSlowMove(boolean slowMove) {
		cc.getTriggerControl().setTrgEnable(!slowMove);
	}

	private int trgmode = -1;

	@Override
	public void onTimebaseChange(int oldidx, int newidx, boolean effect) {
		TriggerControl tc = cc.getTriggerControl();
		TimeConfProvider tcp = cc.getTimeConfProvider();

		boolean newIsSlow = tcp.isOnSlowMoveTimebase(newidx);
		boolean oldIsSlow = tcp.isOnSlowMoveTimebase(oldidx);
		/** 初始化设置的时候就不控制当前的获取数据间隔了，让它自适配 */
		if (oldidx < 0) {
			tc.setTrgEnable(!newIsSlow);
			return;
		}

		if (effect)
			pcs.firePropertyChange(ITimeControl.onTimebaseEffect, oldidx,
					newidx);
		// pcs.firePropertyChange(ITimeControl.onTimebaseChanged, oldidx,
		// newidx);
		pcs.firePropertyChange(ITimeControl.onTimebaseUpdated, null, null);
		if (newIsSlow && !oldIsSlow) {
			/** 保存自动切换前的触发模式 */
			if (tc.isSingleTrg()) {
				trgmode = tc.getSingleTriggerSet().getTrigger().getSweep();
				tc.behaveWhenSwitch2SlowMove();
			}

			if (!cc.getFFTControl().isFFTon()
					&& Platform.getControlManager().isRuntime()) {
				String mes = I18nProvider.bundle().getString("M.Trg.Hide");
				FadeIOShell fs = new FadeIOShell();
				fs.prompt(mes, Platform.getMainWindow().getFrame());
			}

			pcs.firePropertyChange(PropertiesItem.SWITCH_SLOWMOVE, null, null);
			tc.setTrgEnable(false);
		} else if (!newIsSlow && oldIsSlow) {
			pcs.firePropertyChange(PropertiesItem.SWITCH_NormalMOVE, null, null);
			tc.setTrgEnable(true);

			/** 应用自动切换前的触发模式 */
			if (trgmode >= 0 && tc.isSingleTrg()) {
				tc.getSingleTriggerSet().getTrigger().c_setSweep(trgmode, tc);
				trgmode = -1;
			}

			tc.doSubmit();
		}
	}

	@Override
	public void notifyUpdateCurrentSampleRate() {
		cc.updateCurrentSampleRate();
	}

	@Override
	public void notifyUpdateHorTrgPosRange() {
		cc.updateHorTrgPosRange();
	}
}