package com.owon.uppersoft.vds.core.usb.help;

import java.util.List;

import com.owon.uppersoft.vds.core.usb.IDevice;

public interface IUSBCheckHelper {
	List<IDevice> getDeviceList();

	void askConnectUSB(IDevice id);

	void runInstallBat();

//	void initLink(IDevice id);

	void askDisconnect();

	void setNoCheck(boolean b);

	boolean isNoCheck();
}