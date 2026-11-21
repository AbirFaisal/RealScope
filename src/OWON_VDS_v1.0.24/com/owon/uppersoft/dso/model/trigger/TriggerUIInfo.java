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
		this.channel = channel;

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
	 * channel name for alternate trigger
	 * 
	 * When setting the selected channel for single or alternate trigger, the corresponding methods can be used separately; when getting, a combined method can be used
	 */
	private int channel = 0;
	private TriggerControl tc;

	/**
	 * Used to select a channel during alternation
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
			return channel;
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