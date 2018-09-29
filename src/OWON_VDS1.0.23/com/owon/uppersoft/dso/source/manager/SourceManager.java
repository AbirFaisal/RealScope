package com.owon.uppersoft.dso.source.manager;

import java.util.List;

import com.owon.uppersoft.dso.source.net.NetSourceManager;
import com.owon.uppersoft.dso.source.usb.USBSource;
import com.owon.uppersoft.dso.source.usb.USBSourceManager;
import com.owon.uppersoft.vds.core.comm.BufferredSourceManager;
import com.owon.uppersoft.vds.core.comm.ICommunicateManager;
import com.owon.uppersoft.vds.core.comm.IRuntime;
import com.owon.uppersoft.vds.core.usb.IDevice;

/**
 * SourceManager，管理同数据源的交互
 * 
 */
public abstract class SourceManager extends BufferredSourceManager implements
		ICommunicateManager {

	private USBSourceManager usbsm;
	private ICommunicateManager choice;
	private NetSourceManager ntsm;
	private IRuntime ir;

	public SourceManager(IRuntime ir) {
		this.ir = ir;
	}

	public void setup() {
		choice = usbsm = createUSBSourceManager();
		ntsm = createNetSourceManager();

		ir.setRecentStop(false);
	}

	public ICommunicateManager getChoice() {
		return choice;
	}

	protected abstract USBSourceManager createUSBSourceManager();

	protected abstract NetSourceManager createNetSourceManager();

	public USBSourceManager getUSBSourceManager() {
		return usbsm;
	}

	/** 连接源状态 */
	public static final int INT_NoSource = -1;
	public static final int INT_USBSource = 0;
	public static final int INT_NETSource = 1;

	public int connectContext = INT_NoSource;

	public boolean isUSBConnect() {
		return connectContext == INT_USBSource;
	}

	public boolean isNETConnect() {// isNetConnected
		return connectContext == INT_NETSource;
	}

	public boolean isConnected() {
		return connectContext != INT_NoSource;
	}

	public boolean connectUSB(IDevice id) {
		ir.setRecentStop(false);
		USBSource us = usbsm._getUSBSource(id);
		if (us != null) {
			chooseManager(INT_USBSource);
			return true;
		}
		return false;
	}

	public void disconnectSource() {
		if (connectContext == INT_USBSource) {
			usbsm.releaseUSBSource();
		}
		if (connectContext == INT_NETSource) {
			ntsm.disconnect();
		}
		choice = null;
		connectContext = INT_NoSource;
		ir.setRecentStop(false);
	}

	public boolean connectNet(byte[] ip, int port) {
		boolean b = ntsm.connect(ip, port);
		chooseManager(INT_NETSource);
		return b;
	}

	private void chooseManager(int idx) {
		if (idx == INT_USBSource) {
			choice = usbsm;
			connectContext = INT_USBSource;
		} else if (idx == INT_NETSource) {
			choice = ntsm;
			connectContext = INT_NETSource;
		}
	}

	@Override
	public int write(byte[] arr, int len) {
		return choice.write(arr, len);
	}

	@Override
	public int retryTimes() {
		if (choice == null)
			return 3;
		return choice.retryTimes();
	}

	@Override
	public boolean tryRescue() {
		return choice.tryRescue();
	}

	@Override
	public int acceptResponse(byte[] arr, int len) {
		return choice.acceptResponse(arr, len);
	}

	public int getRecommendPeriod() {
		if (connectContext == INT_USBSource) {
			return 0;
		} else {// if (connectContext == INT_NETSource)
			return 0;
		}
	}

	public void connectAfterReboot() {
		String sn = usbsm.getUsbSN();

		List<IDevice> ids;
		IDevice sel;
		int len = 0;
		int count = 0;
		/** 循环检测sn相同的usb端口，连接；超次返回 */
		OUT: while (true) {
			ids = usbsm.refreshUSBPort();
			len = ids.size();
			// System.out.println(len);
			if (len > 0) {
				for (IDevice id : ids) {
					if (id.getSerialNumber().equalsIgnoreCase(sn)) {
						sel = id;
						break OUT;
					}
				}
			}

			count++;

			if (count > 10)
				return;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		connectUSB(sel);
	}
}