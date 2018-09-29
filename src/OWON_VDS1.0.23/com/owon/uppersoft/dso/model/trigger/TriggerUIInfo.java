package com.owon.uppersoft.dso.model.trigger;

import com.owon.uppersoft.vds.util.Pref;

public class TriggerUIInfo {

	public TriggerUIInfo(TriggerControl tc) {
		this.tc = tc;
	}

	public void load(Pref p, int channelsNumber) {
		int channel = p.loadInt("triggerChannelIndex");
		if (channel >= channelsNumber)
			channel = 0;
		setCurrentAltChannel(channel);

		auto_trglevel_middle = p.loadBoolean("auto_trglevel_middle");
	}

	public void persist(Pref p) {
		p.persistInt("triggerChannelIndex", channel);
		p.persistBoolean("auto_trglevel_middle", auto_trglevel_middle);
	}

	private boolean auto_trglevel_middle;

	public boolean isAuto_trglevel_middle() {
		return auto_trglevel_middle;
	}

	public void setAuto_trglevel_middle(boolean auto_trglevel_middle) {
		this.auto_trglevel_middle = auto_trglevel_middle;
	}

	/**
	 * 交替触发的通道名
	 * 
	 * 对单一或交替的选择通道进行设置时，可以分别使用对应的方法；进行获取时，可合并使用方法
	 */
	private int channel = 0;
	private TriggerControl tc;

	/**
	 * 用于交替时选定通道
	 * 
	 * @param idx
	 */
	public void setCurrentAltChannel(int idx) {
		channel = idx;
	}

	public int getChannel() {
		return channel;
	}

	public int getCurrentChannel() {
		if (tc.isSingleTrg())
			return tc.singleTriggerSet.getChannel();
		else {
			return getChannel();
		}
	}

	public TriggerSet getCurrentTriggerSet() {
		if (tc.isSingleTrg())
			return tc.singleTriggerSet;
		else {
			return tc.getAlternateTriggerSet(channel);
		}
	}

	public TriggerSet getTriggerSet(int trgStus) {
		switch (trgStus) {
		case TriggerDefine.TrgModeSingleIndex:
			return tc.getSingleTriggerSet();
		case TriggerDefine.TrgModeAltIndex:
			return tc.getAlternateTriggerSet(channel);
		}
		return null;
	}

	public void c_setTrigger(AbsTrigger trg) {
		getCurrentTriggerSet().setTrigger(trg);
		tc.doSubmit();
	}

	public int getTriggerSetChannel(int trgStus) {
		switch (trgStus) {
		case TriggerDefine.TrgModeSingleIndex:
			return tc.singleTriggerSet.getChannel();
		case TriggerDefine.TrgModeAltIndex:
			return channel;
		}
		return -1;
	}
}