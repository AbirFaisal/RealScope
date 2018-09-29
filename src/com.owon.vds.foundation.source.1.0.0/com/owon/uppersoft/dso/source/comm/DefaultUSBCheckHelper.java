package com.owon.uppersoft.dso.source.comm;

import java.util.List;

import com.owon.uppersoft.dso.source.comm.detect.USBLoopChecker;
import com.owon.uppersoft.vds.core.usb.IDevice;
import com.owon.uppersoft.vds.core.usb.help.IUSBCheckHelper;

public class DefaultUSBCheckHelper implements IUSBCheckHelper {
	private InfiniteDaemon dmn;
	private USBLoopChecker uc;

	public DefaultUSBCheckHelper(InfiniteDaemon dmn, USBLoopChecker uc) {
		this.dmn = dmn;
		this.uc = uc;
	}

	@Override
	public void askConnectUSB(IDevice id) {
		dmn.askConnectUSB(id);
	}

	@Override
	public List<IDevice> getDeviceList() {
		return uc.getDeviceList();
	}

	@Override
	public void runInstallBat() {
		uc.runInstallBat();
	}

//	@Override
//	public void initLink(IDevice id) {
//		dmn.initLink(id);
//	}

	@Override
	public void askDisconnect() {
		dmn.askDisconnect();
	}

	@Override
	public boolean isNoCheck() {
		return uc.isNoCheck();
	}

	@Override
	public void setNoCheck(boolean b) {
		uc.setNoCheck(b);

	}
}