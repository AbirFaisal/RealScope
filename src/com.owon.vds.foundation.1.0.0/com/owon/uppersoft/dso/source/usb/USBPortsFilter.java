package com.owon.uppersoft.dso.source.usb;

import java.util.List;

import com.owon.uppersoft.vds.core.usb.IDevice;

public interface USBPortsFilter {
	List<IDevice> collectPorts(List<IDevice> udl, USBDelegate usbsm);
}