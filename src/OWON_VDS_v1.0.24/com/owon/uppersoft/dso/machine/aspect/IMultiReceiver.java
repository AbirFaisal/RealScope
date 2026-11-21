package com.owon.uppersoft.dso.machine.aspect;

import com.owon.uppersoft.dso.function.record.OfflineChannelsInfo;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.vds.core.wf.dm.DMInfo;
import com.owon.uppersoft.vds.core.wf.rt.ChannelsTransportInfo;

public interface IMultiReceiver {

	/**
	 * Load data offline
	 * 
	 * @param oci
	 */
	void receiveOfflineData(OfflineChannelsInfo oci, DataHouse dh, int status);

	/**
	 * Load data offline
	 * 
	 * @param ci
	 */
	void receiveOfflineDMData(DMInfo ci, DataHouse dh);

	/**
	 * Load data while running
	 * 
	 * @param cti
	 */
	void receiveRTData(ChannelsTransportInfo cti, DataHouse dh);

	/**
	 * Load data while running
	 * 
	 * @param ci
	 */
	void receiveRTDMData(DMInfo ci, DataHouse dh);

}