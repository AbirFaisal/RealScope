package com.owon.uppersoft.vds.pen;

import com.owon.uppersoft.vds.device.interpret.DeviceAddressTable;
import com.owon.vds.firm.protocol.AddressAttachCommand;

public class DeviceAddressTable_Pen extends DeviceAddressTable {
	public DeviceAddressTable_Pen() {
		/** the following are changes for VDS1021 */
		FORCETRG_ADD.setAdd(0x0a, 1);

		CHECK_STOP_ADD.setAdd(0x2062, 1); // @@@
		RUNSTOP_ADD.setAdd(0x62, 1); // @@@
		SLOWMOVE_ADD.setAdd(0x14, 1); // @@@
		SAMPLE_ADD.setAdd(0x12, 1); // @@@

		datafinished_ADD.setAdd(0x1004, 1); // 0x207a? 0x1004?
		// the pen model has no such function; the address is just assigned to this for now but is not actually supported, and the address may be used by other commands
		// SYNCOUTPUT_ADD.setAdd(0xc, 1); // @@@

		CHL_ON_ADD.setAdd(0x16, 1); // @@@
		slope_thredshold[0].setAdd(0x1a, 2); // @@@ slope_thredshold
		TRG_D_ADD.setAdd(0x2002, 1); // @@@ trigger status address
		VIDEOTRGD_ADD.setAdd(0x2004, 1);
		channel_set[0].setAdd(0x110, 1);

		trg_holdoff[0][0].setAdd(0x26, 2);// even address writes a double word
	}

	public AddressAttachCommand LED_CONTROL = new AddressAttachCommand(
			"LED_CONTROL", 0x1006, 1); //
}