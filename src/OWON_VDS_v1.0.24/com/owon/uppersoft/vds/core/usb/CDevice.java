/*
 * Java libusb wrapper
 * Copyright (c) 2005-2006 Andreas Schläfer <spandi at users.sourceforge.net>
 *
 * http://libusbjava.sourceforge.net
 * This library is covered by the LGPL, read LGPL.txt for details.
 */
package com.owon.uppersoft.vds.core.usb;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.codecrete.usb.Usb;
import net.codecrete.usb.UsbDevice;
import net.codecrete.usb.UsbDirection;
import net.codecrete.usb.UsbException;
import net.codecrete.usb.UsbTimeoutException;

import com.owon.uppersoft.vds.util.format.SFormatter;

/**
 * This class represents an USB device, accessed through Java Does USB.<br>
 * To get an instance of an USB device use <code>CDevice.getDevice(...)</code>
 * and open it with <code>CDevice.simulateOpen(...)</code>.
 *
 * @author schlaepfer
 */
public class CDevice {

	private static final Logger logger = Logger.getLogger(CDevice.class
			.getName());

	static {
		/** silent by default like the libusb wrapper; lower it to trace transfers */
		logger.setLevel(Level.OFF);
	}

	private int idVendor, idProduct, dev_configuration, dev_interface,
			dev_altinterface;

	private UsbDevice dev;

	protected CDevice(short idVendor, short idProduct) {
		this.idVendor = idVendor;
		this.idProduct = idProduct;
	}

	/**
	 * Opens the device and claims the specified interface and altinterface.<br>
	 * The configuration is selected when the device is opened, it is only
	 * recorded here.
	 *
	 * @param usbDevice
	 *            the device to open
	 * @param configuration
	 *            the configuration
	 * @param interface_
	 *            the interface
	 * @param altinterface
	 *            the alternate interface. If no alternate interface must be set
	 *            <i>-1</i> can be used.
	 * @throws USBException
	 */
	private void open(UsbDevice usbDevice, int configuration, int interface_,
			int altinterface) throws USBException {
		if (isOpen()) {
			throw new USBException("device opened, close or reset first");
		}
		if (usbDevice == null || !usbDevice.isConnected()) {
			throw new USBException("USB device with idVendor 0x"
					+ Integer.toHexString(idVendor & 0xFFFF)
					+ " and idProduct 0x"
					+ Integer.toHexString(idProduct & 0xFFFF) + " not found");
		}
		this.dev = usbDevice;
		this.dev_configuration = configuration;
		this.dev_interface = interface_;
		this.dev_altinterface = altinterface;

		try {
			usbDevice.open();
			usbDevice.claimInterface(interface_);
			if (altinterface >= 0) {
				usbDevice.selectAlternateSetting(interface_, altinterface);
			}
		} catch (UsbException e) {
			try {
				if (usbDevice.isOpened())
					usbDevice.close();
			} catch (UsbException e2) {
				// ignore
			}
			throw new USBException("open: " + e.getMessage(), e);
		}
		logger.info("interface claimed");
	}

	/**
	 * Release the claimed interface and close the opened device.<br>
	 *
	 * @throws USBException
	 */
	public void close() throws USBException {
		if (!isOpen()) {
			throw new USBException("invalid device handle");
		}
		try {
			dev.releaseInterface(dev_interface);
		} catch (UsbException e) {
			// the interface is released when the device is closed anyway
		}
		try {
			dev.close();
		} catch (UsbException e) {
			throw new USBException("close: " + e.getMessage(), e);
		}
		logger.info("device closed");
	}

	/**
	 * Closes and opens the device again, as there is no USB reset.
	 *
	 * @throws USBException
	 */
	private void reopen() throws USBException {
		UsbDevice usbDevice = dev;
		close();
		open(usbDevice, dev_configuration, dev_interface, dev_altinterface);
	}

	private void checkTransfer(byte[] data, int size) throws USBException {
		if (!isOpen()) {
			throw new USBException("invalid device handle");
		}
		if (data == null) {
			throw new USBException("data must not be null");
		}
		if (size <= 0 || size > data.length) {
			throw new ArrayIndexOutOfBoundsException("invalid size: " + size);
		}
	}

	/**
	 * Write data to the device using a bulk transfer.<br>
	 *
	 * @param out_ep_address
	 *            endpoint address to write to
	 * @param data
	 *            data to write to this endpoint
	 * @param size
	 *            size of the data
	 * @param timeout
	 *            amount of time in ms the device will try to send the data
	 *            until a timeout exception is thrown
	 * @param reopenOnTimeout
	 *            if set to true, the device will try to open the connection and
	 *            send the data again before a timeout exception is thrown
	 * @return the actual number of bytes written
	 * @throws USBException
	 */
	public int writeBulk(int out_ep_address, byte[] data, int size,
			int timeout, boolean reopenOnTimeout) throws USBException {
		checkTransfer(data, size);
		try {
			dev.transferOut(out_ep_address & 0x7f, data, 0, size, timeout);
		} catch (UsbTimeoutException e) {
			// try to reopen the device and send the data again
			if (reopenOnTimeout) {
				logger.info("try to reopen");
				reopen();
				return writeBulk(out_ep_address, data, size, timeout, false);
			}
			throw new USBTimeoutException("writeBulk: " + e.getMessage(), e);
		} catch (UsbException e) {
			throw new USBException("writeBulk: " + e.getMessage(), e);
		}

		logger.info("length written: " + size);
		logData("bulkwrite, ep 0x" + Integer.toHexString(out_ep_address)
				+ ": " + size + " Bytes sent: ", data, size);
		return size;
	}

	/**
	 * Read data from the device using a bulk transfer.<br>
	 * Packets are read until <code>size</code> bytes are received or the
	 * device ends the transfer with a short packet.
	 *
	 * @param in_ep_address
	 *            endpoint address to read from
	 * @param data
	 *            data buffer for the data to be read
	 * @param size
	 *            the maximum requested data size
	 * @param timeout
	 *            amount of time in ms the device will try to receive data until
	 *            a timeout exception is thrown
	 * @param reopenOnTimeout
	 *            if set to true, the device will try to open the connection and
	 *            receive the data again before a timeout exception is thrown
	 * @return the actual number of bytes read
	 * @throws USBException
	 */
	public int readBulk(int in_ep_address, byte[] data, int size, int timeout,
			boolean reopenOnTimeout) throws USBException {
		checkTransfer(data, size);
		int ep = in_ep_address & 0x7f;
		int lenRead = 0;
		try {
			int packetSize = dev.getEndpoint(UsbDirection.IN, ep)
					.getPacketSize();
			while (lenRead < size) {
				/** transferIn returns a single packet */
				byte[] packet = dev.transferIn(ep, timeout);
				int n = Math.min(packet.length, size - lenRead);
				if (n < packet.length) {
					logger.warning("bulkread, ep 0x"
							+ Integer.toHexString(in_ep_address) + ": "
							+ (packet.length - n) + " Bytes dropped");
				}
				System.arraycopy(packet, 0, data, lenRead, n);
				lenRead += n;
				if (packet.length < packetSize)
					break;
			}
		} catch (UsbTimeoutException e) {
			/** data already received is returned, the caller checks the length */
			if (lenRead == 0) {
				// try to reopen the device and receive the data again
				if (reopenOnTimeout) {
					logger.info("try to reopen");
					reopen();
					return readBulk(in_ep_address, data, size, timeout, false);
				}
				throw new USBTimeoutException("readBulk: " + e.getMessage(), e);
			}
		} catch (UsbException e) {
			throw new USBException("readBulk: " + e.getMessage(), e);
		}

		logger.info("length read: " + lenRead);
		logData("bulkread, ep 0x" + Integer.toHexString(in_ep_address) + ": "
				+ lenRead + " Bytes received: ", data, lenRead);
		return lenRead;
	}

	private void logData(String head, byte[] data, int len) {
		if (logger.isLoggable(Level.FINEST)) {
			StringBuffer sb = new StringBuffer(head);
			for (int i = 0; i < len; i++) {
				sb.append("0x" + SFormatter.UIformat("%1$02X", data[i]) + " ");
			}
			logger.info(sb.toString());
		}
	}

	/**
	 * Returns the product ID of the device.<br>
	 *
	 * @return the product ID of the device.
	 */
	public int getIdProduct() {
		return idProduct;
	}

	/**
	 * Returns the vendor ID of the device.<br>
	 *
	 * @return the vendor ID of the device.
	 */
	public int getIdVendor() {
		return idVendor;
	}

	/**
	 * Returns the alternative interface.<br>
	 * This value is only valid after opening the device.
	 *
	 * @return the alternative interface. This value is only valid after opening
	 *         the device.
	 */
	public int getAltinterface() {
		return dev_altinterface;
	}

	/**
	 * Returns the current configuration used.<br>
	 * This value is only valid after opening the device.
	 *
	 * @return the current configuration used. This value is only valid after
	 *         opening the device.
	 */
	public int getConfiguration() {
		return dev_configuration;
	}

	/**
	 * Returns the current interface.<br>
	 * This value is only valid after opening the device.
	 *
	 * @return the current interface. This value is only valid after opening the
	 *         device.
	 */
	public int getInterface() {
		return dev_interface;
	}

	/**
	 * Check if the device is open.<br>
	 * This doesn't check if the device is still attached or working.
	 *
	 * @return true if the device is open
	 */
	public boolean isOpen() {
		return dev != null && dev.isOpened();
	}

	/** below is customize methods */
	/** scan Matched Devices and return as a LinkedList */
	public static List<IDevice> scanMatchedDevices(short idVendor,
			short idProduct, IDeviceCreator creator) throws USBException {
		List<IDevice> devices = new LinkedList<IDevice>();

		try {
			// search for device
			for (UsbDevice device : Usb.getDevices()) {
				if ((device.getVendorId() == (idVendor & 0xFFFF))
						&& (device.getProductId() == (idProduct & 0xFFFF))) {
					IDevice cd = creator.createIDevice(device);
					if (cd != null)
						devices.add(cd);
				}
			}
		} catch (UsbException e) {
			throw new USBException("Usb.getDevices(): " + e.getMessage(), e);
		}
		return devices;
	}

	/**
	 * Java Does USB keeps the device list up to date, so this is the same as
	 * scanMatchedDevices
	 */
	public static List<IDevice> scanMatchedDevices_Direct(short idVendor,
			short idProduct, IDeviceCreator creator) throws USBException {
		return scanMatchedDevices(idVendor, idProduct, creator);
	}

	/** create a raw Device */
	public static CDevice getDevice(short idVendor, short idProduct) {
		CDevice dev = new CDevice(idVendor, idProduct);
		return dev;
	}

	/**
	 * open the certain device found by scanMatchedDevices, so that it could
	 * work for multiDevices own the same idVendor and idProduct
	 */
	public static void simulateOpen(CDevice device, IDevice id)
			throws USBException {
		device.open(id.getUsbDevice(), id.getBConfigurationValue(), id
				.getBInterfaceNumber(), id.getBAlternateSetting());
	}
}
