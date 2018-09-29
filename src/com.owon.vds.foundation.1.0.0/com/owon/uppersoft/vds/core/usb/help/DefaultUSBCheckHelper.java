package com.owon.uppersoft.vds.core.usb.help;

import java.util.ArrayList;
import java.util.List;

import com.owon.uppersoft.vds.core.usb.IDevice;

public class DefaultUSBCheckHelper implements IUSBCheckHelper {
	@Override
	public void askConnectUSB(IDevice id) {
	}

	@Override
	public List<IDevice> getDeviceList() {
		return new ArrayList<IDevice>();
	}

	@Override
	public void runInstallBat() {
	}

//	@Override
//	public void initLink(IDevice id) {
//	}

	@Override
	public void askDisconnect() {
	}

	@Override
	public boolean isNoCheck() {
		return false;
	}

	@Override
	public void setNoCheck(boolean b) {
	}
}