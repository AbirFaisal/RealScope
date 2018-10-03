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
 * 代理触发电平等的判断和绘图
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
	 * 画触发参数标签，显示高亮或虚线效果
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

			// 怎么取出头一个？
			// line代表点选，选中才高亮
			// ts = it.next();
			// ts.paintTriggerSet(wfm, pctlc);
		}
	}

	public void changeTrgLabel(int chl, TrgCheckType type, int del,
			WaveFormManager wfm) {
		changeTrgLabel(chl, type, del, wfm, true);
	}

	/**
	 * 改变触发参数值
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
		 * 把反相作为单独adc控制，假设其它都不变，只是界面和操作上反了，反相就在拖拽的增量上也反一下
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
	 * 判断附近是否有触发参数
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