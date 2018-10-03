package com.owon.uppersoft.dso.machine.aspect;

import com.owon.uppersoft.dso.function.record.OfflineChannelsInfo;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.vds.core.wf.dm.DMInfo;
import com.owon.uppersoft.vds.core.wf.rt.ChannelsTransportInfo;

public interface IMultiReceiver {

	/**
	 * 离线载入数据
	 * 
	 * @param oci
	 */
	void receiveOfflineData(OfflineChannelsInfo oci, DataHouse dh, int status);

	/**
	 * 离线载入数据
	 * 
	 * @param ci
	 */
	void receiveOfflineDMData(DMInfo ci, DataHouse dh);

	/**
	 * 运行时载入数据
	 * 
	 * @param cti
	 */
	void receiveRTData(ChannelsTransportInfo cti, DataHouse dh);

	/**
	 * 运行时载入数据
	 * 
	 * @param ci
	 */
	void receiveRTDMData(DMInfo ci, DataHouse dh);

}