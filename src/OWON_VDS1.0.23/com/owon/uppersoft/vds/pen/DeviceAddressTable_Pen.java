package com.owon.uppersoft.vds.pen;

import com.owon.uppersoft.vds.device.interpret.DeviceAddressTable;
import com.owon.vds.firm.protocol.AddressAttachCommand;

public class DeviceAddressTable_Pen extends DeviceAddressTable {
	public DeviceAddressTable_Pen() {
		/** 以下为VDS1021改动 */
		FORCETRG_ADD.setAdd(0x0a, 1);

		CHECK_STOP_ADD.setAdd(0x2062, 1); // @@@
		RUNSTOP_ADD.setAdd(0x62, 1); // @@@
		SLOWMOVE_ADD.setAdd(0x14, 1); // @@@
		SAMPLE_ADD.setAdd(0x12, 1); // @@@

		datafinished_ADD.setAdd(0x1004, 1); // 0x207a? 0x1004?
		// 笔式无此功能，只是把地址先分配为此，但实际不支持，该地址可能被用到其它指令去
		// SYNCOUTPUT_ADD.setAdd(0xc, 1); // @@@

		CHL_ON_ADD.setAdd(0x16, 1); // @@@
		slope_thredshold[0].setAdd(0x1a, 2); // @@@ slope_thredshold
		TRG_D_ADD.setAdd(0x2002, 1); // @@@ 触发状态地址
		VIDEOTRGD_ADD.setAdd(0x2004, 1);
		channel_set[0].setAdd(0x110, 1);

		trg_holdoff[0][0].setAdd(0x26, 2);// 偶地址写双字
	}

	public AddressAttachCommand LED_CONTROL = new AddressAttachCommand(
			"LED_CONTROL", 0x1006, 1); //
}