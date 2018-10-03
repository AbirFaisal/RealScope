package com.owon.uppersoft.dso.source.usb;

import com.owon.uppersoft.vds.core.usb.IDevice;

public interface USBDelegate {
	USBSource _getUSBSource(IDevice id);

	void releaseUSBSource(USBSource us);
}