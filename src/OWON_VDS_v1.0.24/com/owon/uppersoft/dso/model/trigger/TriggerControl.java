package com.owon.uppersoft.dso.model.trigger;

import com.owon.uppersoft.dso.delegate.DefaultTriggerExtendDelegate;
import com.owon.uppersoft.dso.model.trigger.common.Voltsensor;
import com.owon.uppersoft.dso.model.trigger.holdoff.HoldoffDelegate;
import com.owon.uppersoft.dso.source.comm.effect.SubmitorFactory;
import com.owon.uppersoft.dso.source.pack.VTPatchable;
import com.owon.uppersoft.vds.core.aspect.base.IOrgan;
import com.owon.uppersoft.vds.core.comm.effect.Submitable;
import com.owon.uppersoft.vds.util.Pref;

public class TriggerControl implements IOrgan, TriggerInfoForChannel {
	// static {
	// TrgTypeDefine.setResourceBundleProvider(I18nProvider
	// .getResourceBundleProvider());
	// }

	// public static final int getExtTrgDefineNumber() {
	// return TrgTypeDefine.VALUES.length;
	// }

	// private static final String[] TrggerNames;
	// static {
	// TrggerNames = new String[getExtTrgDefineNumber()];
	// int i = 0;
	// for (TrgTypeDefine etd : TrgTypeDefine.VALUES) {
	// TrggerNames[i++] = etd.key;
	// }
	// }

	private final TriggerSet[] trgchs;

	public TriggerSet[] getAltTriggerSets() {
		return trgchs;
	}

	public TriggerSet getAlternateTriggerSet(int chl) {
		return trgchs[chl];
	}

	public int getChannelsNumber() {
		return trgchs.length;
	}

	private int channelMode = 0;

	public int getChannelMode() {
		return channelMode;
	}

	/** TODO 考虑是否单一触发也每个通道都有各自的TriggerSet */
	public TriggerSet singleTriggerSet;

	// public int curTrgStatus;

	// public TriggerSet getCurrentTriggerSet() {
	// if (isSingleTrg())
	// return singleTriggerSet;
	// else {
	// return trgchs[channel];
	// }
	// }

	// public TriggerSet getTriggerSet(int trgTyp) {
	// switch (trgTyp) {
	// case TriggerDefine.TrgModeSingleIndex:
	// return getSingleTriggerSet();
	// case TriggerDefine.TrgModeAltIndex:
	// return getAlternateTriggerSet(channel);
	// }
	// return null;
	// }
	//
	// public int getTriggerSetChannel(int trgTyp) {
	// switch (trgTyp) {
	// case TriggerDefine.TrgModeSingleIndex:
	// return singleTriggerSet.getChannel();
	// case TriggerDefine.TrgModeAltIndex:
	// return channel;
	// }
	// return -1;
	// }

	// public String getEdgeSource() {
	// StringBuilder sb = new StringBuilder();
	// for (TriggerSet ts : trgchs) {
	// if (ts.isCurrentTrigger_Edge())
	// sb.append("CH" + (ts.getChannel() + 1) + " ");
	// }
	// if (sb.length() == 0)
	// return "Edge Mode Not Selected";
	// return sb.toString();
	// }
	//
	// public String getVideoSource() {
	// StringBuilder sb = new StringBuilder();
	// for (TriggerSet ts : trgchs) {
	// if (ts.isCurrentTrigger_Video())
	// sb.append("CH" + (ts.getChannel() + 1) + " ");
	// }
	// if (sb.length() == 0)
	// return "Video Mode Not Selected";
	// return sb.toString();
	// }

	public TriggerSet getTriggerSetOrNull(int idx) {
		if (isSingleTrg()) {
			if (singleTriggerSet.getChannel() != idx) {
				/** 单触时，判断保护，检测是否是同一通道 */
				// System.err.println("TriggerContro >getTriggerSet
				// >(singleTrgChannel != idx)");
				return null;
			}
			return singleTriggerSet;
		} else {
			return trgchs[idx];
		}
	}

	// public int getCurrentChannel() {
	// if (isSingleTrg())
	// return singleTriggerSet.getChannel();
	// else {
	// return channel;
	// }
	// }

	public TriggerSet getSingleTriggerSet() {
		return singleTriggerSet;
	}

	public int getSingleTrgChannel() {
		return singleTriggerSet.getChannel();
	}

	private TriggerExtendDelegate ted;

	public DefaultTriggerExtendDelegate getDelegate() {
		return (DefaultTriggerExtendDelegate) ted;
	}

	public TriggerControl(int channelsNumber, TriggerExtendDelegate ted) {
		this.ted = ted;
		/** 内部使用channelsNumber的值指代ext的序号，在控件上方便对应，指令中另行设置 */
		EXTTRG_CHANNEL = channelsNumber;

		trgchs = new TriggerSet[channelsNumber];
		oldVoltsenses = new int[channelsNumber];
	}

	public void load(Pref p) {
		int channelsNumber = getChannelsNumber();
		boolean video = false;

		/** 载入的时候也限制了只有一个视频 */
		for (int i = 0; i < channelsNumber; i++) {
			String pre = "CH" + (i + 1) + ".";
			int mode = p.loadInt(pre + "trgMode");
			TrgTypeDefine etd = TrgTypeDefine.getExtTrgDefine(mode);
			if (TrgTypeDefine.Video == etd) {
				if (video)
					etd = TrgTypeDefine.Edge;
				else
					video = true;
			}
			if (trgchs[i] == null) {
				trgchs[i] = new TriggerSet(TriggerDefine.TrgModeAltIndex);
			}

			TriggerSet ts = trgchs[i];

			ts.initTrigger(etd, i + 1);
			ts.setChannel(i);
			ts.load(pre, p);
		}
		channelMode = p.loadInt("triggerChannelMode");

		if (!isAlternativeSupport()) {
			// 触发模式强制设置
			channelMode = TriggerDefine.TrgModeSingleIndex;
		}

		tui.load(p, channelsNumber);

		int singleTrgChannel = p.loadInt("SingleTrg.selectIndex");
		if (singleTriggerSet == null)
			singleTriggerSet = new TriggerSet(TriggerDefine.TrgModeSingleIndex);

		int mode = p.loadInt("SingleTrg.trgMode");
		TrgTypeDefine etd = TrgTypeDefine.values()[mode];
		singleTriggerSet.initTrigger(etd, 0);
		singleTriggerSet.setChannel(singleTrgChannel);
		singleTriggerSet.load("SingleTrg.", p);
	}

	private TriggerUIInfo tui = new TriggerUIInfo(this);

	public TriggerUIInfo getTriggerUIInfo() {
		return tui;
	}

	public void persist(Pref p) {
		int size = getChannelsNumber();
		for (int i = 0; i < size; i++) {
			String pre = "CH" + (i + 1) + ".";
			TriggerSet ts = trgchs[i];
			p.persistInt(pre + "trgMode", ts.getTrigger().type.ordinal());
			ts.slope.persist(pre, p);
			ts.edge.persist(pre, p);
			ts.video.persist(pre, p);
			ts.pulse.persist(pre, p);
		}
		p.persistInt("SingleTrg.selectIndex", singleTriggerSet.getChannel());
		p.persistInt("SingleTrg.trgMode",
				singleTriggerSet.getTrigger().type.ordinal());

		tui.persist(p);

		p.persistInt("triggerChannelMode", channelMode);

		singleTriggerSet.slope.persist("SingleTrg.", p);
		singleTriggerSet.edge.persist("SingleTrg.", p);
		singleTriggerSet.video.persist("SingleTrg.", p);
		singleTriggerSet.pulse.persist("SingleTrg.", p);
	}

	public void c_setChannelMode(int channelMode) {
		this.channelMode = channelMode;

		doSubmit();
	}

	public void resetAltTrgSet() {
		int len = trgchs.length;
		for (int i = 0; i < len; i++) {
			TriggerSet ts = trgchs[i];
			EdgeTrigger et = ts.edge;
			ts.setTrigger(et);
			et.c_setVoltsense(0);
			et.setSweep(TriggerDefine.TrigSweepAutoIndex);
			et.etvho = HoldoffDelegate.createDefaultETV();
		}
		selfSubmit();
	}

	public void resetSingleTrgset() {
		EdgeTrigger et = singleTriggerSet.edge;
		singleTriggerSet.setTrigger(et);
		et.c_setVoltsense(0);
		et.setSweep(TriggerDefine.TrigSweepAutoIndex);
		et.etvho = HoldoffDelegate.createDefaultETV();
	}

	public void setChannelMode(int channelMode) {
		this.channelMode = channelMode;
	}

	public boolean isOnExtTrgMode() {
		return isSingleTrg() && isExtTrg(getSingleTrgChannel());
	}

	private final int EXTTRG_CHANNEL;

	public void setSingleExtTrg() {
		TriggerSet ts = singleTriggerSet;
		ts.setChannel(EXTTRG_CHANNEL);
		ts.setTrigger(ts.edge);
		// ts.edge.c_setVoltsenseWithoutSync(0);

		ted.handleSingleTrgChannel2Ext();
	}

	public boolean isExtTrg(int chl) {
		return chl == EXTTRG_CHANNEL;
	}

	private int oldVoltsenses[];

	public void saveOldVoltsenses(int chidx, long v) {
		if (isExtTrg(chidx)) {
			return;
		}
		this.oldVoltsenses[chidx] = (int) v;
	}

	public void setSingleChannel(int sel) {
		TriggerSet ts = singleTriggerSet;
		int oldchl = ts.getChannel();

		if (ts.isVoltsenseSupport()) {
			Voltsensor vsr = ts.getVoltsense();
			if (vsr != null) {
				long v = vsr.getVoltsense();
				saveOldVoltsenses(oldchl, v);
			}
		}

		if (sel >= getChannelsNumber()) {
			sel = EXTTRG_CHANNEL;
		}

		if (isExtTrg(sel)) {
			setSingleExtTrg();
			return;
		}

		if (oldchl == sel)
			return;

		ts.setChannel(sel);

		/** 这里可能出现由原来的ext切换过来的时候找不到对应的触发电平 */
		if (!isExtTrg(oldchl)) {
			ted.handleSingleTrgChannelLevelTransport(oldchl, sel, ts, this);
		} else {
			ts.edge.c_setVoltsenseWithoutSync(oldVoltsenses[sel]);
			// 切triggerOut
			// ted.handleSingleTrgChannelReturnFromExt();
		}
	}

	public boolean isSweepEnable() {
		return isSingleTrg();
	}

	/**
	 * 判断是否单一触发
	 * 
	 * @return
	 */
	public boolean isSingleTrg() {
		return channelMode == TriggerDefine.TrgModeSingleIndex;
	}

	public boolean isChannelVideoTrg(int channel) {
		if (isSingleTrg()) {
			if (singleTriggerSet.getChannel() == channel) {
				return singleTriggerSet.isCurrentTrigger_Video();
			}
			return false;
		} else {
			TriggerSet ts = getAlternateTriggerSet(channel);
			if (ts != null)
				return ts.isCurrentTrigger_Video();

			return false;
		}
	}

	/**
	 * 尝试单次触发
	 */
	public void trySweepOnce() {
		// 忽略非单触的情况
		if (!isSingleTrg())
			return;

		// 忽略已经单次的情况
		// if (singleTriggerSet.isSweepOnce())
		// return;
		// 设置单次，发送指令
		singleTriggerSet.trySweepOnce(this);
		doSubmit();

		ted.broadcastSweepOnce();
	}

	/**
	 * 在单一触发情况下，判断是否单次触发
	 * 
	 * @return
	 */
	public boolean isSweepOnce() {
		boolean b = isSingleTrg() && singleTriggerSet.isSweepOnce();
		return b;
	}

	/**
	 * 在单一触发情况下，判断是否正常触发
	 * 
	 * @return
	 */
	public boolean isSweepNormal() {
		boolean b = isSingleTrg() && singleTriggerSet.isSweepNormal();
		return b;
	}

	/**
	 * 在单一触发情况下，判断是否自动触发
	 * 
	 * @return
	 */
	public boolean isSweepAuto() {
		boolean b = isSingleTrg() && singleTriggerSet.isSweepAuto();
		return b;
	}

	public boolean resumeAuto() {
		if (isSweepOnce()) {
			singleTriggerSet.resumeAuto(this);
			// 因为当前是在停止的情况，所以直接添加指令任务
			doSubmit();
			return true;
		}
		return false;
	}

	public byte getChannelModeAlpha() {
		return (byte) (isSingleTrg() ? 's' : 'a');
	}

	public void nextSingleChannel() {
		int turns = getChannelsNumber();
		if (singleTriggerSet.isCurrentTrigger_Edge() && ted.isExtTrgSupport()) {
			turns += 1;// + 1可切换到外边触发
		}

		int singleTrgChannel = singleTriggerSet.getChannel();
		singleTrgChannel = (singleTrgChannel + 1) % turns;
		setSingleChannel(singleTrgChannel);
	}

	public void selfSubmit() {
	}

	/**
	 * KNOW 改为更简单的发送内容似乎不容易，因为协议设计得不够好，
	 * 
	 * 也不清楚下位机是否能够通过简化的内容得到足够的信息了，而且性能的影响其实不见得很大
	 */

	/**
	 * 不全发送
	 * 
	 * @return
	 */
	public VTPatchable getVTPatchable() {
		return new TrgVTPatch(this);
	}

	private boolean trgEnable;

	public void setTrgEnable(boolean b) {
		trgEnable = b;
	}

	public void behaveWhenSwitch2SlowMove() {
		if (isSingleTrg())
			singleTriggerSet.resumeAuto(this);
		// 因为当前是在停止的情况，所以直接添加指令任务
		doSubmit();
	}

	public boolean isTrgEnable() {
		return trgEnable;
	}

	public boolean isAlternativeSupport() {
		return getChannelsNumber() > 1;
	}

	public void doSubmit() {
		Submitable sbm = SubmitorFactory.reInit();
		selfSubmit();
		sbm.apply_trg();
	}

	public void doSumbitTrgSweep(int sweepIndex) {
		Submitable sbm = SubmitorFactory.reInit();
		selfSubmit();
		sbm.apply_trgSweep(sweepIndex);
	}

}
