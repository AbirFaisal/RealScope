package com.owon.uppersoft.dso.model.trigger;

public interface TriggerExtendDelegate {
	void handleSingleTrgChannelLevelTransport(int oldchl, int newchl,
			TriggerSet singlets, TriggerControl tc);

	void handleSingleTrgChannel2Ext();
	
	void broadcastSweepOnce();
	
	void handleSingleTrgChannelReturnFromExt();
	
	boolean isExtTrgSupport();
}