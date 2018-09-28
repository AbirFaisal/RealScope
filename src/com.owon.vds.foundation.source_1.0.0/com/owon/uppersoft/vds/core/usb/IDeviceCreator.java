package com.owon.uppersoft.vds.core.usb;

import ch.ntb.usb.Usb_Device;

public interface IDeviceCreator {
	IDevice createIDevice(Usb_Device usb_Device);
}
