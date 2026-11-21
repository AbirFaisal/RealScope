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

	/** TODO consider whether in single trigger each channel should also have its own TriggerSet */
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
				/** In single trigger, protection check, detect whether it is the same channel */
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
		/** Internally the value of channelsNumber is used to refer to the ext index, for easy correspondence in the controls; it is set separately in the commands */
		EXTTRG_CHANNEL = channelsNumber;

		trgchs = new TriggerSet[channelsNumber];
		oldVoltsenses = new int[channelsNumber];
	}

	public void load(Pref p) {
		int channelsNumber = getChannelsNumber();
		boolean video = false;

		/** Only one video is also enforced on load */
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
			// force the trigger mode setting
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

		/** When switching from the original ext, the corresponding trigger level may not be found here */
		if (!isExtTrg(oldchl)) {
			ted.handleSingleTrgChannelLevelTransport(oldchl, sel, ts, this);
		} else {
			ts.edge.c_setVoltsenseWithoutSync(oldVoltsenses[sel]);
			// switch to triggerOut
			// ted.handleSingleTrgChannelReturnFromExt();
		}
	}

	public boolean isSweepEnable() {
		return isSingleTrg();
	}

	/**
	 * Determine whether it is single trigger
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
	 * Try single-shot trigger
	 */
	public void trySweepOnce() {
		// ignore the non-single-trigger case
		if (!isSingleTrg())
			return;

		// ignore the case that is already single-shot
		// if (singleTriggerSet.isSweepOnce())
		// return;
		// set single-shot, send the command
		singleTriggerSet.trySweepOnce(this);
		doSubmit();

		ted.broadcastSweepOnce();
	}

	/**
	 * In single trigger, determine whether it is single-shot trigger
	 * 
	 * @return
	 */
	public boolean isSweepOnce() {
		boolean b = isSingleTrg() && singleTriggerSet.isSweepOnce();
		return b;
	}

	/**
	 * In single trigger, determine whether it is normal trigger
	 * 
	 * @return
	 */
	public boolean isSweepNormal() {
		boolean b = isSingleTrg() && singleTriggerSet.isSweepNormal();
		return b;
	}

	/**
	 * In single trigger, determine whether it is auto trigger
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
			// Because the current state is stopped, add the command task directly
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
			turns += 1;// + 1 can switch to external trigger
		}

		int singleTrgChannel = singleTriggerSet.getChannel();
		singleTrgChannel = (singleTrgChannel + 1) % turns;
		setSingleChannel(singleTrgChannel);
	}

	public void selfSubmit() {
	}

	/**
	 * KNOW changing to simpler send content does not seem easy, because the protocol is not designed well enough,
	 * 
	 * and it is unclear whether the device can get enough information from simplified content, and the performance impact is actually not necessarily large
	 */

	/**
	 * Do not send everything
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
		// Because the current state is stopped, add the command task directly
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
