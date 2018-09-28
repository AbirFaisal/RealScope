package com.owon.uppersoft.dso.source.usb;

import static com.owon.uppersoft.vds.core.usb.IUSBProtocol.IdProduct;
import static com.owon.uppersoft.vds.core.usb.IUSBProtocol.IdVender;

import java.util.LinkedList;
import java.util.List;

import ch.ntb.usb.LibusbJava;
import ch.ntb.usb.USBException;
import ch.ntb.usb.Usb_Device;
import ch.ntb.usb.Usb_Device_Descriptor;

import com.owon.uppersoft.vds.core.comm.ICommunicateManager;
import com.owon.uppersoft.vds.core.usb.CDevice;
import com.owon.uppersoft.vds.core.usb.ComparableDevice;
import com.owon.uppersoft.vds.core.usb.IDevice;
import com.owon.uppersoft.vds.core.usb.IDeviceCreator;
import com.owon.uppersoft.vds.core.usb.IUSBProtocol;

/**
 * 管理USB数据源的状态和开启，提供USB数据源
 * 
 */
public class USBSourceManager implements ICommunicateManager, IDeviceCreator,
		USBDelegate {
	public static final String USBDrvDir = "USBDRV";
	private USBPortsFilter pf;

	public USBSourceManager(USBPortsFilter pf) {
		this.pf = pf;
	}

	public static final int FailureRetryTimes = 20;

	@Override
	public int retryTimes() {
		return FailureRetryTimes;
	}

	/** 以下方法在usb打开连接的时候才有效 */
	@Override
	public int acceptResponse(byte[] arr, int len) {
		if (isConnected())
			return usbs.acceptResponse(arr, len);
		else
			return -1;
	}

	public int acceptResponse(byte[] arr, int len, int timeout) {
		if (isConnected())
			return usbs.acceptResponse(arr, len, timeout);
		else
			return -1;
	}

	@Override
	public int write(byte[] arr, int len) {
		if (isConnected()) {
			return usbs.write(arr, len);
		} else
			return -1;
	}

	/**
	 * 分离初始化的动作
	 */
	public boolean init_check_usbdrv() {
		try {
			LibusbJava.usb_init();
			LibusbJava.usb_find_busses();
			LibusbJava.usb_find_devices();
			LibusbJava.usb_get_busses();
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public IDevice createIDevice(Usb_Device usb_Device) {
		return ComparableDevice.getInstance(usb_Device, "");
	}

	/**
	 * 在默认已经初始化的前提下，进行直接扫描刷新
	 * 
	 * @return USB中可用的端口的列表，长度为0说明无可用
	 */
	public List<IDevice> refreshUSBPort_Direct() {
		try {
			final List<IDevice> udl = CDevice.scanMatchedDevices_Direct(
					IUSBProtocol.IdVender, IUSBProtocol.IdProduct, this);
			return pf.collectPorts(udl, this);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return new LinkedList<IDevice>();
	}

	/**
	 * 
	 * @return USB中可用的端口的列表，长度为0说明无可用，为null说明驱动调用失败
	 */
	public List<IDevice> refreshUSBPort() {
		try {
			final List<IDevice> udl = CDevice.scanMatchedDevices(
					IUSBProtocol.IdVender, IUSBProtocol.IdProduct, this);
			return pf.collectPorts(udl, this);
		} catch (Throwable e) {
			e.printStackTrace();
			return new LinkedList<IDevice>();
		}
	}

	/**
	 * KNOW 尽量替换为dh.controlManager.sourceManager.connectUSB(IDevice id);
	 * 
	 * @param id
	 *            设备对象
	 * @return USB数据源，为空说明配置错误
	 */
	public USBSource _getUSBSource(IDevice id) {
		if (id == null)
			return null;
		CDevice dev = CDevice.getDevice(IdVender, IdProduct);
		try {
			CDevice.simulateOpen(dev, id);
			return usbs = createUSBSource(id, dev);
		} catch (USBException e) {
			e.printStackTrace();
		}
		return null;
	}

	private IDevice rescanDeviceBySerialNumber(String sn) {
		List<IDevice> udl;
		try {
			udl = CDevice.scanMatchedDevices(IUSBProtocol.IdVender,
					IUSBProtocol.IdProduct, this);

			for (IDevice id : udl) {
				Usb_Device dev = id.getUsb_Device();
				long handle = LibusbJava.usb_open(dev);
				Usb_Device_Descriptor devDesc = dev.getDescriptor();
				String iSerialNumber = LibusbJava.usb_get_string_simple(handle,
						devDesc.getISerialNumber());
				LibusbJava.usb_close(handle);
				// System.err.println("find: " + iSerialNumber);
				if (sn.equalsIgnoreCase(iSerialNumber)) {
					id.setSerialNumber(iSerialNumber);
					return id;
				}
			}
		} catch (USBException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected USBSource createUSBSource(IDevice id, CDevice dev) {
		return new USBSource(id, dev);
	}

	/**
	 * 未完成
	 * 
	 * @return
	 */
	public boolean reConnectTheSameDevice() {
		String sn = usbs.getId().getSerialNumber();
		releaseUSBSource();
		logln("find again");
		IDevice id = rescanDeviceBySerialNumber(sn);

		if (id == null)
			return false;
		CDevice dev = CDevice.getDevice(IdVender, IdProduct);
		try {
			CDevice.simulateOpen(dev, id);
			usbs = createUSBSource(id, dev);
			return true;
		} catch (USBException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean tryRescue() {
		return reConnectTheSameDevice();
	}

	public boolean isConnected() {
		return usbs != null && usbs.getCDevice().isOpen();
	}

	private USBSource usbs = null;

	public String getUsbSN() {
		if (usbs == null)
			return "usbs null";
		return usbs.getId().getSerialNumber();
	}

	public void releaseUSBSource() {
		if (usbs == null)
			return;
		releaseUSBSource(usbs);
		usbs = null;
	}

	public USBSource getUsbs() {
		return usbs;
	}

	/**
	 * 释放USB数据源
	 * 
	 * @param us
	 *            USB数据源
	 */
	public void releaseUSBSource(USBSource us) {
		CDevice dev = us.getCDevice();
		if (dev != null && dev.isOpen()) {
			try {
				dev.close();
				logln("releaseUSBSource");
			} catch (USBException e) {
				// e.printStackTrace();
				logln(e.getMessage());
			}
		}

	}

	private void logln(String message) {
	}
}
