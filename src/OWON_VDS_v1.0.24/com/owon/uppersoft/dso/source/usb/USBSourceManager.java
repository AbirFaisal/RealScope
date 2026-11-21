package com.owon.uppersoft.dso.source.usb;

import static com.owon.uppersoft.vds.core.usb.IUSBProtocol.IdProduct;
import static com.owon.uppersoft.vds.core.usb.IUSBProtocol.IdVender;

import java.util.LinkedList;
import java.util.List;

import net.codecrete.usb.Usb;
import net.codecrete.usb.UsbDevice;

import com.owon.uppersoft.vds.core.comm.ICommunicateManager;
import com.owon.uppersoft.vds.core.usb.CDevice;
import com.owon.uppersoft.vds.core.usb.ComparableDevice;
import com.owon.uppersoft.vds.core.usb.IDevice;
import com.owon.uppersoft.vds.core.usb.IDeviceCreator;
import com.owon.uppersoft.vds.core.usb.IUSBProtocol;
import com.owon.uppersoft.vds.core.usb.USBException;

/**
 * Manages the state and opening of the USB data source, provides the USB data source
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

	/** The methods below are only valid when the usb connection is open */
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
	 * Separates the initialization action
	 */
	public boolean init_check_usbdrv() {
		try {
			Usb.getDevices();
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public IDevice createIDevice(UsbDevice usbDevice) {
		return ComparableDevice.getInstance(usbDevice, "");
	}

	/**
	 * Scan and refresh directly, assuming initialization is already done
	 * 
	 * @return USBList of usable ports in USB; length 0 means none are available
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
	 * @return USBList of usable ports in USB; length 0 means none are available, null means the driver call failed
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
	 * KNOW Try to replace with dh.controlManager.sourceManager.connectUSB(IDevice id);
	 * 
	 * @param id
	 *            Device object
	 * @return USBUSB data source; empty means a configuration error
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
				String iSerialNumber = id.getUsbDevice().getSerialNumber();
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
	 * Not finished
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
	 * Release the USB data source
	 * 
	 * @param us
	 *            USBUSB data source
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
