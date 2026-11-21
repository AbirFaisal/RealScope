package com.owon.uppersoft.vds.core.usb;

import net.codecrete.usb.UsbDevice;

public interface IDeviceCreator {
	IDevice createIDevice(UsbDevice usbDevice);
}
