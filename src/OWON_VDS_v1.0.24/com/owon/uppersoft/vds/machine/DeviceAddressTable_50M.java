package com.owon.uppersoft.vds.machine;

import com.owon.uppersoft.vds.device.interpret.DeviceAddressTable;
import com.owon.vds.firm.protocol.AddressAttachCommand;

public class DeviceAddressTable_50M extends DeviceAddressTable {
	public DeviceAddressTable_50M() {
		SLOWMOVE_ADD.setAdd(0x0a, 1);
		FORCETRG_ADD.setAdd(0x05, 1);
		TRG_D_ADD.setAdd(0x2001, 1); // @@@ trigger status address
		CHECK_STOP_ADD.setAdd(0xB1, 1);
		VIDEOTRGD_ADD.setAdd(0x2002, 1); // address for determining video trigger
		
		datafinished_ADD.setAdd(0x1004, 1); // 0x207a? 0x1004?
	}

	public AddressAttachCommand ADC_RESET = new AddressAttachCommand(
			"ADC_RESET", 0x21, 1); //

	public AddressAttachCommand READBACK_HTRG_OFFSET = new AddressAttachCommand(
			"READBACK_HTRG_OFFSET", 0x66, 2); //
	public AddressAttachCommand FLAG_5mV = new AddressAttachCommand("FLAG_5mV",
			0x20, 1); // bit flags per channel
	public AddressAttachCommand SAMPLE_50M_FLAG = new AddressAttachCommand(
			"SAMPLE_50M_FLAG", 0x17, 1); //
}