package com.owon.uppersoft.vds.socket.provider;

import static com.owon.uppersoft.vds.socket.ScpiPool.CH;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.model.trigger.AbsTrigger;
import com.owon.uppersoft.dso.model.trigger.TrgTypeDefine;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.model.trigger.TriggerDefine;
import com.owon.uppersoft.dso.model.trigger.TriggerSet;
import com.owon.uppersoft.dso.model.trigger.VideoTrigger;
import com.owon.uppersoft.dso.model.trigger.VoltsensableTrigger;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.vds.socket.NB;
import com.owon.uppersoft.vds.socket.ScpiPool;

public class TrgScpiProvider {

	private ControlManager cm;
	private WaveFormManager wfm;
	private TriggerControl tc;
	private final String[] trgTypes = { "SINGle", "ALTernate" };
	private final String[] trgSweepMode = { "AUTO", "NORMal", "SINGle" };

	public TrgScpiProvider(ControlManager ctrm) {
		this.cm = ctrm;
		wfm = Platform.getDataHouse().getWaveFormManager();
		tc = cm.getTriggerControl();
	}

	private void updateAllTrgUI(int trgTyp) {
		MainWindow mw = Platform.getMainWindow();
		mw.getTitlePane().updateBtnSingle();
		updateTrgInfoPane();
		cm.pcs.firePropertyChange(PropertiesItem.APPLY_TRIGGER, null, trgTyp);
	}

	private void updateTrgInfoPane() {
		MainWindow mw = Platform.getMainWindow();
		mw.re_paint();
		mw.getToolPane().getTrgInfoPane().updateInfos(tc);
	}

	// private void applyTrgMode(int chl, int trgTyp, TrgTypeDefine trgMod) {
	// // 设触发模式 TrgTypeDefine.Edge,Video;
	// if (cm.getPrinciple().singleVideoAlow(trgMod, chl, tc)) {
	// TriggerSet ts = tc.getTriggerSet(trgTyp);
	// tc.c_setTrigger(ts.getTriggerSet(trgMod));
	// } else {
	// cm.pcs.firePropertyChange(PropertiesItem.CHOOSE_TRGMODECB, null, tc
	// .getCurrentTriggerSet().getTrigger().type);
	// }
	// }

	private void applyTrgMode2(int chl, int trgTyp, TrgTypeDefine trgMod) {
		// 设触发模式 TrgTypeDefine.Edge,Video;
		if (cm.singleVideoAlow(trgMod, chl, tc)) {
			TriggerSet ts = getTriggerSet(trgTyp);
			getTriggerSet(trgTyp).setTrigger(ts.getTriggerSet(trgMod));
			tc.doSubmit();
		} else {
			cm.pcs.firePropertyChange(PropertiesItem.CHOOSE_TRGMODECB, null,
					getCurrentTrigger().type);
		}
	}

	public String getTrgType() {
		return trgTypes[tc.getChannelMode()];
	}

	public String setTrgType(String args) {
		for (int i = 0; i < trgTypes.length; i++) {
			if (args.equalsIgnoreCase(trgTypes[i])) {
				tc.c_setChannelMode(i);
				updateAllTrgUI(i);
				return ScpiPool.Success;
			}
		}
		return ScpiPool.Failed;
	}

	public String getSweepMode() {
		int md = getCurrentTrigger().getSweep();
		if (tc.getSingleTriggerSet().isCurrentTrigger_Video()
				&& tc.isSingleTrg())
			return ScpiPool.Failed + ",AT VIDEO MODE";
		return trgSweepMode[md];
	}

	public String setSweepMode(String args) {
		TriggerSet ts = tc.getSingleTriggerSet();
		if (!tc.isSingleTrg())
			return ScpiPool.Failed + ",AT ALTERNATE MODE";
		if (ts.isCurrentTrigger_Video())
			return ScpiPool.Failed + ",AT VIDEO MODE";
		// 交替触发时，触发模式仅为 AUTO
		for (int i = 0; i < trgSweepMode.length; i++) {
			if (args.equalsIgnoreCase(trgSweepMode[i])) {
				ts.getTrigger().c_setSweep(i, tc);
				tc.doSubmit();
				cm.pcs.firePropertyChange(PropertiesItem.T_SweepOnce2Auto,
						null, i);
				return ScpiPool.Success;
			}
		}
		return ScpiPool.Failed;
	}

	public String getTrgTypMode(int trgTyp) {
		TriggerSet ts = getTriggerSet(trgTyp);
		return ts.getTrigger().getName();
	}

	public String setTrgTypMode(int trgTyp, String args) {
		TrgTypeDefine trgMod = null;
		for (TrgTypeDefine td : TrgTypeDefine.VALUES) {
			if (td.name().equalsIgnoreCase(args)) {
				trgMod = td;
				break;
			}
		}
		if (trgMod == null)
			return ScpiPool.Failed;
		int chl = getTriggerSetChannel(trgTyp);
		applyTrgMode2(chl, trgTyp, trgMod);
		// 更新
		updateTrgInfoPane();
		cm.pcs.firePropertyChange(PropertiesItem.APPLY_TRIGGER, null, trgTyp);
		return ScpiPool.Success;
	}

	// //trg branch order provider
	public String getSingle_Source() {
		int trgTyp = TriggerDefine.TrgModeSingleIndex;
		int chl = getTriggerSetChannel(trgTyp) + 1;
		if (chl > cm.getAllChannelsNumber())
			return "EXT";
		return CH + chl;
	}

	public String getAlt_EdgeSource() {
		StringBuilder sb = new StringBuilder();
		for (TriggerSet ts : tc.getAltTriggerSets()) {
			if (ts.isCurrentTrigger_Edge())
				sb.append(CH + (ts.getChannel() + 1) + " ");
		}
		if (sb.length() == 0)
			return "Edge Mode Not Selected";
		return sb.toString();
	}

	public String getAlt_VideoSource() {
		StringBuilder sb = new StringBuilder();
		for (TriggerSet ts : tc.getAltTriggerSets()) {
			if (ts.isCurrentTrigger_Video())
				sb.append(CH + (ts.getChannel() + 1) + " ");
		}
		if (sb.length() == 0)
			return "Video Mode Not Selected";
		return sb.toString();
	}

	public String set_Source(String ch, int trgTyp, TrgTypeDefine trgMod) {
		if (ch.toUpperCase().startsWith(CH))
			ch = ch.substring(CH.length());
		int chIdx = -1;
		try {
			chIdx = Integer.parseInt(ch) - 1;
		} catch (Exception e) {
			return ScpiPool.Failed;
		}
		if (chIdx < 0 || chIdx > cm.getAllChannelsNumber() - 1)
			return ScpiPool.ErrCh;

		// 切single、Alt
		tc.c_setChannelMode(trgTyp);
		// 设通道
		if (trgTyp == TriggerDefine.TrgModeAltIndex) {
			setCurrentAltChannel(chIdx);
		} else {
			tc.setSingleChannel(chIdx);
			cm.pcs.firePropertyChange(PropertiesItem.NEXT_SINGLE_CHANNEL, null,
					tc.singleTriggerSet.getChannel());
			tc.doSubmit();
		}
		// 设触发模式 TrgTypeDefine.Edge,Video;
		applyTrgMode2(chIdx, trgTyp, trgMod);
		updateAllTrgUI(trgTyp);

		return ScpiPool.Success;
	}

	public String get_EdgeSlope(int trgTyp) {
		return getTriggerSet(trgTyp).edge.getSlope();
	}

	public String set_EdgeSlope(int trgTyp, String risefall) {
		String respond = getTriggerSet(trgTyp).edge.setSlope(risefall);
		tc.doSubmit();
		updateTrgInfoPane();
		cm.pcs.firePropertyChange(PropertiesItem.NEXT_STATUS, 0, 1);
		return respond;
	}

	// 传入trgTyp，即单触、交替状态，获得对应的边沿触发电平
	public String get_EdgeLevel(int trgTyp) {
		int chl = getTriggerSetChannel(trgTyp);
		if (tc.isExtTrg(chl))
			return "EXT";
		ChannelInfo ci = wfm.getWaveForm(chl).wfi.ci;
		int voltbase = ci.getVoltageLabel().getValue();
		int pos0 = ci.getPos0();

		TriggerSet ts = getTriggerSet(trgTyp);
		String txt = ts.edge.getLabelText(ci.isInverse(), voltbase, pos0);
		return txt;
	}

	private TriggerSet getTriggerSet(int trgTyp) {
		return tc.getTriggerUIInfo().getTriggerSet(trgTyp);
	}

	private int getTriggerSetChannel(int trgTyp) {
		return tc.getTriggerUIInfo().getTriggerSetChannel(trgTyp);
	}

	private AbsTrigger getCurrentTrigger() {
		return tc.getTriggerUIInfo().getCurrentTriggerSet().getTrigger();
	}

	private void setCurrentAltChannel(int idx) {
		tc.getTriggerUIInfo().setCurrentAltChannel(idx);
	}

	// 传入trgTyp，即单触、交替状态，设置对应的边沿触发电平为args
	public String set_EdgeLevel(int trgTyp, String args) {
		int chl = getTriggerSetChannel(trgTyp);
		if (tc.isExtTrg(chl))
			return ScpiPool.ErrCh + ":EXT";
		TriggerSet ts = getTriggerSet(trgTyp);
		ChannelInfo ci = wfm.getWaveForm(chl).wfi.ci;
		VoltsensableTrigger vt = (VoltsensableTrigger) ts.getTrigger();

		if (!NB.isNum(args))
			return ScpiPool.ErrNum;
		int v = (int) Double.parseDouble(args);
		// v = ts.getVoltsenseHalfRange() - v;
		// System.out.println(args + "," + v);
		// 附加反相
		v = ci.getInverseValue(v);

		boolean changed = vt.c_setVoltsense(v);// 设置并限定v值
		if (changed) {
			tc.getVTPatchable().submiteVoltsense(ci.getNumber(), vt);// 同步下位机
		}
		cm.pcs.firePropertyChange(PropertiesItem.REPAINT_CHARTSCREEN, null,
				null);
		cm.pcs.firePropertyChange(PropertiesItem.UPDATE_VOLTSENSE, -1,
				ci.getNumber());
		int range = ts.getVoltsenseHalfRange();
		if (v > range || v < -range)
			return ScpiPool.Success + "," + vt.c_getVoltsense();
		return ScpiPool.Success;
	}

	public String get_VideoMod(int trgTyp) {
		return getTriggerSet(trgTyp).video.getMod();
	}

	public String set_VideoMod(int trgTyp, String args) {
		VideoTrigger vt = getTriggerSet(trgTyp).video;
		boolean respond = vt.setMod(args);
		if (!respond)
			return ScpiPool.Failed;
		tc.doSubmit();

		int trg = tc.isSingleTrg() ? TriggerDefine.TrgModeSingleIndex
				: TriggerDefine.TrgModeAltIndex;
		if (trgTyp == trg)
			cm.pcs.firePropertyChange(PropertiesItem.UPDATE_VIDEOMOD, null, vt);
		return ScpiPool.Success;
	}

	public String get_VideoSync(int trgTyp) {
		return getTriggerSet(trgTyp).video.getSync();
	}

	public String set_VideoSync(int trgTyp, String args) {
		VideoTrigger vt = getTriggerSet(trgTyp).video;
		boolean respond = vt.setSync(args);
		if (!respond)
			return ScpiPool.Failed;
		tc.doSubmit();

		int trg = tc.isSingleTrg() ? TriggerDefine.TrgModeSingleIndex
				: TriggerDefine.TrgModeAltIndex;
		// TODO 测试注意这里fire会否影响边沿斜率脉冲页面，都有NEXT_STATUS
		if (trgTyp == trg)
			cm.pcs.firePropertyChange(PropertiesItem.NEXT_STATUS, null, vt);
		updateTrgInfoPane();
		return ScpiPool.Success;
	}

	public String get_VideoLnum(int trgTyp) {
		return getTriggerSet(trgTyp).video.getLnum();
	}

	public String set_VideoLnum(int trgTyp, String args) {
		if (!NB.isNum(args))
			return ScpiPool.ErrNum;
		VideoTrigger vt = getTriggerSet(trgTyp).video;
		if (vt.isSyncValueEnable()) {
			int v;
			try {
				v = Integer.parseInt(args);
			} catch (Exception e) {
				return ScpiPool.Failed;
			}
			if (v < 0 && v >= 625)
				return ScpiPool.ErrNum;

			if (vt.syncValue != v) {
				vt.syncValue = v;
				tc.doSubmit();
			}
			updateTrgInfoPane();

			int trg = tc.isSingleTrg() ? TriggerDefine.TrgModeSingleIndex
					: TriggerDefine.TrgModeAltIndex;
			// TODO 测试注意这里fire会否影响边沿斜率脉冲页面，都有NEXT_STATUS
			if (trgTyp == trg)
				cm.pcs.firePropertyChange(PropertiesItem.NEXT_STATUS, null, vt);
			return ScpiPool.Success;
		}
		return ScpiPool.Disable;
	}

}
