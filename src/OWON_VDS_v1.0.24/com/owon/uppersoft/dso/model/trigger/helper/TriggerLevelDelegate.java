package com.owon.uppersoft.dso.model.trigger.helper;

import java.awt.Graphics2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.owon.uppersoft.dso.control.TrgLevelCheckHandler;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.model.trigger.TrgCheckType;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.model.trigger.TriggerSet;
import com.owon.uppersoft.dso.model.trigger.VoltsensableTrigger;
import com.owon.uppersoft.dso.source.pack.VTPatchable;
import com.owon.uppersoft.dso.wf.ChartScreenSelectModel;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.paint.ScreenContext;
import com.owon.uppersoft.vds.data.LocRectangle;

/**
 * Delegates the judgment and drawing of the trigger level etc.
 * 
 */
public class TriggerLevelDelegate {
	private TriggerControl trgc;

	private PaintChannelTrgLabelContext pctlc = new PaintChannelTrgLabelContext();

	public TriggerLevelDelegate(TriggerControl trgc) {
		this.trgc = trgc;
	}

	public void preparePaintChannelTrgLabelContext(Graphics2D g2d,
			ScreenContext pc, LocRectangle lr, ChartScreenSelectModel cssm) {
		pctlc.prepare(g2d, pc, lr, cssm);
	}

	/**
	 * Draw the trigger parameter label, showing a highlight or dashed-line effect
	 * 
	 * @param g2d
	 * @param pc
	 * @param cm
	 * @param lr
	 * @param wfm
	 * @param dh
	 */
	public void paintTrgLabel(ControlManager cm, WaveFormManager wfm) {
		if (cm.isTrgLevelDisable())
			return;
		if (trgc.isSingleTrg()) {
			TriggerSet ts = trgc.getSingleTriggerSet();
			ts.paintTriggerSet(wfm, pctlc);
		} else {
			List<TriggerSet> lts = getAltTrgslist();
			ListIterator<TriggerSet> it = lts.listIterator(lts.size());
			TriggerSet ts;
			while (it.hasPrevious()) {
				ts = it.previous();
				ts.paintTriggerSet(wfm, pctlc);
			}

			// how to take out the first one?
			// line represents click selection, highlighted only when selected
			// ts = it.next();
			// ts.paintTriggerSet(wfm, pctlc);
		}
	}

	public void changeTrgLabel(int chl, TrgCheckType type, int del,
			WaveFormManager wfm) {
		changeTrgLabel(chl, type, del, wfm, true);
	}

	/**
	 * Change the trigger parameter value
	 * 
	 * @param chl
	 * @param type
	 * @param del
	 */
	public void changeTrgLabel(int chl, TrgCheckType type, int del,
			WaveFormManager wfm, boolean commit) {
		ControlManager cm = wfm.getDataHouse().controlManager;
		WaveForm wf = wfm.getWaveForm(chl);
		if (wf == null)
			return;

		/**
		 * Treat inversion as a separate adc control; assuming everything else is unchanged and only the UI and operation are reversed, inversion simply flips the drag increment as well
		 */
		if (wf.isInverted()) {
			del = -del;
		}

		int len = cm.getSupportChannelsNumber();
		TriggerSet ts = null;
		if (chl >= 0 && chl < len) {
			if (trgc.isSingleTrg()) {
				int singleTrgChannel = trgc.getSingleTrgChannel();
				if (chl == singleTrgChannel) {
					ts = trgc.getSingleTriggerSet();
				} else {
					logln("trgc changeTrgLevel chl != singleTrgChannel!!!");
				}
			} else {
				ts = find(chl);
			}
		} else {
			logln("trgc changeTrgLevel (chl >= 0 && chl < len) can not satisfy!!!");
		}

		if (ts != null) {
			boolean changed = ts.handelIncr(del, type);

			if (changed) {
				VTPatchable vtp = trgc.getVTPatchable();
				switch (type) {
				case UppOver:
				case LowOver:
					if (commit)
						vtp.submiteUpper_Lower(chl, ts.slope, type);
					break;
				case VoltsenseOver:
					if (commit)
						vtp.submiteVoltsense(chl, (VoltsensableTrigger) ts
								.getTrigger());
					break;
				default:
					return;
				}
				cm.pcs.firePropertyChange(type.fireItem, -1, chl);
			}
		}
	}

	private void logln(String string) {
	}

	private List<TriggerSet> altTrgslist;

	public List<TriggerSet> getAltTrgslist() {
		if (altTrgslist == null) {
			altTrgslist = new LinkedList<TriggerSet>();
			int len = trgc.getChannelsNumber();
			for (int i = 0; i < len; i++) {
				TriggerSet ts = trgc.getAlternateTriggerSet(i);
				altTrgslist.add(ts);
			}
		}

		return altTrgslist;
	}

	private TriggerSet find(int idx) {
		List<TriggerSet> lts = getAltTrgslist();
		ListIterator<TriggerSet> it = lts.listIterator();
		TriggerSet ts = null;
		int i = 0;
		while (it.hasNext()) {
			ts = it.next();

			if (ts.getChannel() == idx) {
				if (i != 0) {
					it.remove();
					lts.add(0, ts);
				}
				return ts;
			}
			i++;
		}
		return null;
	}

	/**
	 * Determine whether there is a trigger parameter nearby
	 * 
	 * @param mousey
	 * @param upborder
	 * @param downborder
	 * @param hc
	 * @param screenMode_3
	 * @param checkHandler
	 */
	public void checkAllAroundTrgLabel(TrgLevelCheckHandler checkHandler,
			WaveFormManager wfm) {
		boolean found = false;
		if (trgc.isSingleTrg()) {
			TriggerSet ts = trgc.getSingleTriggerSet();
			int idx = ts.getChannel();
			WaveForm wf = wfm.getWaveForm(idx);
			if (wf == null || !wf.isOn())
				return;

			checkHandler.setWaveForm(wf);
			found = ts.checkTrgLevel(checkHandler);
			if (found) {
				return;
			}

			//
		} else {
			Iterator<TriggerSet> it = getAltTrgslist().iterator();
			while (it.hasNext()) {
				TriggerSet ts = it.next();
				int idx = ts.getChannel();

				WaveForm wf = wfm.getWaveForm(idx);
				if (!wf.isOn())
					continue;

				checkHandler.setWaveForm(wf);
				found = ts.checkTrgLevel(checkHandler);
				if (found) {
					return;
				}
			}

			//
		}

		// found == false;
	}

}