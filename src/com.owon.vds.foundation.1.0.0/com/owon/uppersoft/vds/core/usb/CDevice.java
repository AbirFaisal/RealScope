/* 
 * Java libusb wrapper
 * Copyright (c) 2005-2006 Andreas Schl鋚fer <spandi at users.sourceforge.net>
 *
 * http://libusbjava.sourceforge.net
 * This library is covered by the LGPL, read LGPL.txt for details.
 */
package com.owon.uppersoft.vds.core.usb;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.ntb.usb.LibusbJava;
import ch.ntb.usb.USBException;
import ch.ntb.usb.USBTimeoutException;
import ch.ntb.usb.Usb_Bus;
import ch.ntb.usb.Usb_Config_Descriptor;
import ch.ntb.usb.Usb_Device;
import ch.ntb.usb.Usb_Device_Descriptor;
import ch.ntb.usb.Usb_Endpoint_Descriptor;
import ch.ntb.usb.Usb_Interface;
import ch.ntb.usb.Usb_Interface_Descriptor;
import ch.ntb.usb.logger.LogUtil;

import com.owon.uppersoft.vds.util.format.SFormatter;

/**
 * This class represents an USB device.<br>
 * To get an instance of an USB device use <code>USB.getDevice(...)</code>.
 * 
 * @author schlaepfer
 */
public class CDevice {

	private static final Logger logger = LogUtil.getLogger("ch.ntb.usb");

	private int maxPacketSize;

	private int idVendor, idProduct, dev_configuration, dev_interface,
			dev_altinterface;

	private long usbDevHandle;

	private boolean resetOnFirstOpen, resetDone;

	private int resetTimeout = 2000;

	private Usb_Device dev;

	private boolean initUSBDone;

	protected CDevice(short idVendor, short idProduct) {
		initUSBDone = false;
		resetOnFirstOpen = false;
		resetDone = false;
		maxPacketSize = -1;
		this.idVendor = idVendor;
		this.idProduct = idProduct;
	}

	private void initUSB() {
		LibusbJava.usb_init();
		initUSBDone = true;
	}

	private Usb_Bus initBus() throws USBException {
		LibusbJava.usb_find_busses();
		LibusbJava.usb_find_devices();

		Usb_Bus bus = LibusbJava.usb_get_busses();
		if (bus == null) {
			throw new USBException("LibusbJava.usb_get_busses(): "
					+ LibusbJava.usb_strerror());
		}
		return bus;
	}

	private void updateMaxPacketSize(Usb_Device device) throws USBException {
		maxPacketSize = -1;
		Usb_Config_Descriptor[] confDesc = device.getConfig();
		for (int i = 0; i < confDesc.length; i++) {
			Usb_Interface[] int_ = confDesc[i].getInterface();
			for (int j = 0; j < int_.length; j++) {
				Usb_Interface_Descriptor[] intDesc = int_[j].getAltsetting();
				for (int k = 0; k < intDesc.length; k++) {
					Usb_Endpoint_Descriptor[] epDesc = intDesc[k].getEndpoint();
					for (int l = 0; l < epDesc.length; l++) {
						maxPacketSize = Math.max(epDesc[l].getWMaxPacketSize(),
								maxPacketSize);
					}
				}
			}
		}
		if (maxPacketSize <= 0) {
			throw new USBException(
					"No USB endpoints found. Check the device configuration");
		}
	}

	private Usb_Device initDevice() throws USBException {
		Usb_Bus bus = initBus();

		Usb_Device device = null;
		// search for device
		while (bus != null) {
			device = bus.getDevices();
			while (device != null) {
				Usb_Device_Descriptor devDesc = device.getDescriptor();
				if ((devDesc.getIdVendor() == idVendor)
						&& (devDesc.getIdProduct() == idProduct)) {
					logger.info("Device found: " + device.getFilename());
					updateMaxPacketSize(device);
					return device;
				}
				device = device.getNext();
			}
			bus = bus.getNext();
		}
		return null;
	}

	/**
	 * Updates the device and descriptor information from the bus.<br>
	 * The descriptors can be read with {@link #getDeviceDescriptor()} and
	 * {@link #getConfigDescriptors()}.
	 * 
	 * @throws USBException
	 */
	public void updateDescriptors() throws USBException {
		if (!initUSBDone)
			initUSB();
		dev = initDevice();
	}

	/**
	 * Returns the device descriptor associated with this device.<br>
	 * The descriptor is updated by calling {@link #updateDescriptors()} or
	 * {@link #open(int, int, int)}.
	 * 
	 * @return the device descriptor associated with this device or
	 *         <code>null</code>
	 */
	public Usb_Device_Descriptor getDeviceDescriptor() {
		if (dev == null) {
			return null;
		}
		return dev.getDescriptor();
	}

	/**
	 * Returns the configuration descriptors associated with this device.<br>
	 * The descriptors are updated by calling {@link #updateDescriptors()} or
	 * {@link #open(int, int, int)}.
	 * 
	 * @return the configuration descriptors associated with this device or
	 *         <code>null</code>
	 */
	public Usb_Config_Descriptor[] getConfigDescriptors() {
		if (dev == null) {
			return null;
		}
		return dev.getConfig();
	}

	/**
	 * Opens the device and claims the specified configuration, interface and
	 * altinterface.<br>
	 * First the bus is enumerated. If the device is found its descriptors are
	 * read and the <code>maxPacketSize</code> value is updated. If no
	 * endpoints are found in the descriptors an exception is thrown.
	 * 
	 * @param configuration
	 *            the configuration
	 * @param interface_
	 *            the interface
	 * @param altinterface
	 *            the alternate interface. If no alternate interface must be set
	 *            <i>-1</i> can be used.
	 * @throws USBException
	 */
	public void open(int configuration, int interface_, int altinterface)
			throws USBException {
		this.dev_configuration = configuration;
		this.dev_interface = interface_;
		this.dev_altinterface = altinterface;

		if (usbDevHandle != 0) {
			throw new USBException("device opened, close or reset first");
		}

		initUSB();

		dev = initDevice();

		if (dev != null) {
			long res = LibusbJava.usb_open(dev);
			if (res == 0) {
				throw new USBException("LibusbJava.usb_open: "
						+ LibusbJava.usb_strerror());
			}
			usbDevHandle = res;
		}

		if (dev == null || usbDevHandle == 0) {
			throw new USBException("USB device with idVendor 0x"
					+ Integer.toHexString(idVendor & 0xFFFF)
					+ " and idProduct 0x"
					+ Integer.toHexString(idProduct & 0xFFFF) + " not found");
		}
		claim_interface(usbDevHandle, configuration, interface_, altinterface);
		if (resetOnFirstOpen & !resetDone) {
			logger.info("reset on first open");
			resetDone = true;
			reset();
			try {
				Thread.sleep(resetTimeout);
			} catch (InterruptedException e) {
				//
			}
			open(configuration, interface_, altinterface);
		}
	}

	/**
	 * Release the claimed interface and close the opened device.<br>
	 * 
	 * @throws USBException
	 */
	public void close() throws USBException {
		if (usbDevHandle == 0) {
			throw new USBException("invalid device handle");
		}
		release_interface(usbDevHandle, dev_interface);
		if (LibusbJava.usb_close(usbDevHandle) < 0) {
			usbDevHandle = 0;
			throw new USBException("LibusbJava.usb_close: "
					+ LibusbJava.usb_strerror());
		}
		usbDevHandle = 0;
		maxPacketSize = -1;
		logger.info("device closed");
	}

	/**
	 * Sends an USB reset to the device. The device handle will no longer be
	 * valid. To use the device again, {@link #open(int, int, int)} must be
	 * called.
	 * 
	 * @throws USBException
	 */
	public void reset() throws USBException {
		if (usbDevHandle == 0) {
			throw new USBException("invalid device handle");
		}
		release_interface(usbDevHandle, dev_interface);
		if (LibusbJava.usb_reset(usbDevHandle) < 0) {
			usbDevHandle = 0;
			throw new USBException("LibusbJava.usb_reset: "
					+ LibusbJava.usb_strerror());
		}
		usbDevHandle = 0;
		logger.info("device reset");
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
		if (usbDevHandle == 0) {
			throw new USBException("invalid device handle");
		}
		if (data == null) {
			throw new USBException("data must not be null");
		}
		if (size <= 0 || size > data.length) {
			throw new ArrayIndexOutOfBoundsException("invalid size: " + size);
		}
		int lenWritten = LibusbJava.usb_bulk_write(usbDevHandle,
				out_ep_address, data, size, timeout);
		if (lenWritten < 0) {
			if (lenWritten == LibusbJava.ERROR_TIMEDOUT) {
				// try to reopen the device and send the data again
				if (reopenOnTimeout) {
					logger.info("try to reopen");
					reset();
					open(dev_configuration, dev_interface, dev_altinterface);
					return writeBulk(out_ep_address, data, size, timeout, false);
				}
				throw new USBTimeoutException("LibusbJava.usb_bulk_write: "
						+ LibusbJava.usb_strerror());
			}
			throw new USBException("LibusbJava.usb_bulk_write: "
					+ LibusbJava.usb_strerror());
		}

		logger.info("length written: " + lenWritten);
		if (logger.isLoggable(Level.FINEST)) {
			StringBuffer sb = new StringBuffer("bulkwrite, ep 0x"
					+ Integer.toHexString(out_ep_address) + ": " + lenWritten
					+ " Bytes sent: ");
			for (int i = 0; i < lenWritten; i++) {
				sb.append("0x" + SFormatter.UIformat("%1$02X", data[i]) + " ");
			}
			logger.info(sb.toString());
		}
		return lenWritten;
	}

	/**
	 * Read data from the device using a bulk transfer.<br>
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
		if (usbDevHandle == 0) {
			throw new USBException("invalid device handle");
		}
		if (data == null) {
			throw new USBException("data must not be null");
		}
		if (size <= 0 || size > data.length) {
			throw new ArrayIndexOutOfBoundsException("invalid size: " + size);
		}
		int lenRead = LibusbJava.usb_bulk_read(usbDevHandle, in_ep_address,
				data, size, timeout);
		if (lenRead < 0) {
			String t = "";
			try {
				t = new String(LibusbJava.usb_strerror().getBytes(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			if (lenRead == LibusbJava.ERROR_TIMEDOUT) {
				// try to reopen the device and send the data again
				if (reopenOnTimeout) {
					logger.info("try to reopen");
					reset();
					open(dev_configuration, dev_interface, dev_altinterface);
					return readBulk(in_ep_address, data, size, timeout, false);
				}
				throw new USBTimeoutException("LibusbJava.usb_bulk_read: " + t);
			}
			throw new USBException("LibusbJava.usb_bulk_read: " + t);
		}

		logger.info("length read: " + lenRead);
		if (logger.isLoggable(Level.FINEST)) {
			StringBuffer sb = new StringBuffer("bulkread, ep 0x"
					+ Integer.toHexString(in_ep_address) + ": " + lenRead
					+ " Bytes received: ");
			for (int i = 0; i < lenRead; i++) {
				sb.append("0x" + SFormatter.UIformat("%1$02X", data[i]) + " ");
			}
			logger.info(sb.toString());
		}
		return lenRead;
	}

	/**
	 * Write data to the device using a interrupt transfer.<br>
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
	public int writeInterrupt(int out_ep_address, byte[] data, int size,
			int timeout, boolean reopenOnTimeout) throws USBException {
		if (usbDevHandle == 0) {
			throw new USBException("invalid device handle");
		}
		if (data == null) {
			throw new USBException("data must not be null");
		}
		if (size <= 0 || size > data.length) {
			throw new ArrayIndexOutOfBoundsException("invalid size: " + size);
		}
		int lenWritten = LibusbJava.usb_interrupt_write(usbDevHandle,
				out_ep_address, data, size, timeout);
		if (lenWritten < 0) {
			if (lenWritten == LibusbJava.ERROR_TIMEDOUT) {
				// try to reopen the device and send the data again
				if (reopenOnTimeout) {
					logger.info("try to reopen");
					reset();
					open(dev_configuration, dev_interface, dev_altinterface);
					return writeInterrupt(out_ep_address, data, size, timeout,
							false);
				}
				throw new USBTimeoutException(
						"LibusbJava.usb_interrupt_write: "
								+ LibusbJava.usb_strerror());
			}
			throw new USBException("LibusbJava.usb_interrupt_write: "
					+ LibusbJava.usb_strerror());
		}

		logger.info("length written: " + lenWritten);
		if (logger.isLoggable(Level.FINEST)) {
			StringBuffer sb = new StringBuffer("interruptwrite, ep 0x"
					+ Integer.toHexString(out_ep_address) + ": " + lenWritten
					+ " Bytes sent: ");
			for (int i = 0; i < lenWritten; i++) {
				sb.append("0x" + SFormatter.UIformat("%1$02X", data[i]) + " ");
			}
			logger.info(sb.toString());
		}
		return lenWritten;
	}

	/**
	 * Read data from the device using a interrupt transfer.<br>
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
	public int readInterrupt(int in_ep_address, byte[] data, int size,
			int timeout, boolean reopenOnTimeout) throws USBException {
		if (usbDevHandle == 0) {
			throw new USBException("invalid device handle");
		}
		if (data == null) {
			throw new USBException("data must not be null");
		}
		if (size <= 0 || size > data.length) {
			throw new ArrayIndexOutOfBoundsException("invalid size: " + size);
		}
		int lenRead = LibusbJava.usb_interrupt_read(usbDevHandle,
				in_ep_address, data, size, timeout);
		if (lenRead < 0) {
			if (lenRead == LibusbJava.ERROR_TIMEDOUT) {
				// try to reopen the device and send the data again
				if (reopenOnTimeout) {
					logger.info("try to reopen");
					reset();
					open(dev_configuration, dev_interface, dev_altinterface);
					return readInterrupt(in_ep_address, data, size, timeout,
							false);
				}
				throw new USBTimeoutException("LibusbJava.usb_interrupt_read: "
						+ LibusbJava.usb_strerror());
			}
			throw new USBException("LibusbJava.usb_interrupt_read: "
					+ LibusbJava.usb_strerror());
		}

		logger.info("length read: " + lenRead);
		if (logger.isLoggable(Level.FINEST)) {
			StringBuffer sb = new StringBuffer("interrupt, ep 0x"
					+ Integer.toHexString(in_ep_address) + ": " + lenRead
					+ " Bytes received: ");
			for (int i = 0; i < lenRead; i++) {
				sb.append("0x" + SFormatter.UIformat("%1$02X", data[i]) + " ");
			}
			logger.info(sb.toString());
		}
		return lenRead;
	}

	/**
	 * Performs a control request to the default control pipe on a device.<br>
	 * The parameters mirror the types of the same name in the USB
	 * specification.
	 * 
	 * @param requestType
	 *            USB device request type (USB specification 9.3,
	 *            bmRequestType). Use constants from {@link ch.ntb.usb.USB}
	 *            (REQ_TYPE_xxx).
	 * @param request
	 *            specific request (USB specification 9.4, bRequest). Use
	 *            constants from {@link ch.ntb.usb.USB} (REQ_xxx).
	 * @param value
	 *            field that varies according to request (USB specification 9.4,
	 *            wValue)
	 * @param index
	 *            field that varies according to request (USB specification 9.4,
	 *            wIndex)
	 * @param data
	 *            the send/receive buffer
	 * @param size
	 *            the buffer size
	 * @param timeout
	 *            amount of time in ms the device will try to send/receive data
	 *            until a timeout exception is thrown
	 * @param reopenOnTimeout
	 *            if set to true, the device will try to open the connection and
	 *            send/receive the data again before a timeout exception is
	 *            thrown
	 * @return the number of bytes written/read
	 * @throws USBException
	 */
	public int controlMsg(int requestType, int request, int value, int index,
			byte[] data, int size, int timeout, boolean reopenOnTimeout)
			throws USBException {
		if (usbDevHandle == 0) {
			throw new USBException("invalid device handle");
		}
		if (data == null) {
			throw new USBException("data must not be null");
		}
		if (size <= 0 || size > data.length) {
			throw new ArrayIndexOutOfBoundsException("invalid size: " + size);
		}
		int len = LibusbJava.usb_control_msg(usbDevHandle, requestType,
				request, value, index, data, size, timeout);
		if (len < 0) {
			if (len == LibusbJava.ERROR_TIMEDOUT) {
				// try to reopen the device and send the data again
				if (reopenOnTimeout) {
					logger.info("try to reopen");
					reset();
					open(dev_configuration, dev_interface, dev_altinterface);
					return controlMsg(requestType, request, value, index, data,
							size, timeout, false);
				}
				throw new USBTimeoutException("LibusbJava.controlMsg: "
						+ LibusbJava.usb_strerror());
			}
			throw new USBException("LibusbJava.controlMsg: "
					+ LibusbJava.usb_strerror());
		}

		logger.info("length read/written: " + len);
		if (logger.isLoggable(Level.FINEST)) {
			StringBuffer sb = new StringBuffer("controlMsg: " + len
					+ " Bytes received(written: ");
			for (int i = 0; i < len; i++) {
				sb.append("0x" + SFormatter.UIformat("%1$02X", data[i]) + " ");
			}
			logger.info(sb.toString());
		}
		return len;
	}

	/**
	 * Claim an interface to send and receive USB data.<br>
	 * 
	 * @param usb_dev_handle
	 *            the handle of the device <b>(MUST BE VALID)</b>
	 * @param configuration
	 *            the configuration to use
	 * @param interface_
	 *            the interface to claim
	 * @param altinterface
	 *            the alternate interface to use. If no alternate interface must
	 *            be set <i>-1</i> can be used.
	 * @throws USBException
	 *             throws an USBException if the action fails
	 */
	private void claim_interface(long usb_dev_handle, int configuration,
			int interface_, int altinterface) throws USBException {
		if (LibusbJava.usb_set_configuration(usb_dev_handle, configuration) < 0) {
			usbDevHandle = 0;
			throw new USBException("LibusbJava.usb_set_configuration: "
					+ LibusbJava.usb_strerror());
		}
		if (LibusbJava.usb_claim_interface(usb_dev_handle, interface_) < 0) {
			usbDevHandle = 0;
			throw new USBException("LibusbJava.usb_claim_interface: "
					+ LibusbJava.usb_strerror());
		}
		if (altinterface >= 0) {
			if (LibusbJava.usb_set_altinterface(usb_dev_handle, altinterface) < 0) {
				try {
					release_interface(usb_dev_handle, interface_);
				} catch (USBException e) {
					// ignore
				}
				usbDevHandle = 0;
				throw new USBException("LibusbJava.usb_set_altinterface: "
						+ LibusbJava.usb_strerror());
			}
		}
		logger.info("interface claimed");
	}

	/**
	 * Release a previously claimed interface.<br>
	 * 
	 * @param dev_handle
	 *            the handle of the device <b>(MUST BE VALID)</b>
	 * @param interface_
	 *            the interface to claim
	 * @throws USBException
	 *             throws an USBException if the action fails
	 */
	private void release_interface(long dev_handle, int interface_)
			throws USBException {
		if (LibusbJava.usb_release_interface(dev_handle, interface_) < 0) {
			usbDevHandle = 0;
			throw new USBException("LibusbJava.usb_release_interface: "
					+ LibusbJava.usb_strerror());
		}
		logger.info("interface released");
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
	 * Returns the maximum packet size in bytes which is allowed to be
	 * transmitted at once.<br>
	 * The value is determined by reading the endpoint descriptor(s) when
	 * opening the device. It is invalid before the device is opened! Note that
	 * if some endpoints use different packet sizes the maximum packet size is
	 * return. This value may be used to determine if a device is opened in
	 * fullspeed or highspeed mode.
	 * 
	 * @return the maximum packet size
	 */
	public int getMaxPacketSize() {
		return maxPacketSize;
	}

	/**
	 * Check if the device is open.<br>
	 * This checks only for a valid device handle. It doesn't check if the
	 * device is still attached or working.
	 * 
	 * @return true if the device is open
	 */
	public boolean isOpen() {
		return usbDevHandle != 0;
	}

	/**
	 * If enabled, the device is reset when first opened. <br>
	 * This will only happen once. When the application is started, the device
	 * state is unknown. If the device is not reset, read or write may result in
	 * a {@link USBTimeoutException}.<br>
	 * <br>
	 * This feature is disabled by default.
	 * 
	 * @param enable
	 *            true if the device should be reset when first opened
	 * @param timeout
	 *            the timeout between the reset and the reopening
	 */
	public void setResetOnFirstOpen(boolean enable, int timeout) {
		resetOnFirstOpen = enable;
		resetTimeout = timeout;
	}

	/** below is customize methods */
	/** scan Matched Devices and return as a LinkedList */
	public static List<IDevice> scanMatchedDevices(short idVendor,
			short idProduct, IDeviceCreator creator) throws USBException {
		LibusbJava.usb_init();

		LibusbJava.usb_find_busses();
		LibusbJava.usb_find_devices();

		Usb_Bus bus = LibusbJava.usb_get_busses();
		if (bus == null) {
			throw new USBException("LibusbJava.usb_get_busses(): "
					+ LibusbJava.usb_strerror());
		}

		Usb_Device device = null;

		List<IDevice> devices = new LinkedList<IDevice>();

		// search for device
		while (bus != null) {
			device = bus.getDevices();
			while (device != null) {
				Usb_Device_Descriptor devDesc = device.getDescriptor();
				if ((devDesc.getIdVendor() == idVendor)
						&& (devDesc.getIdProduct() == idProduct)) {
					IDevice cd = creator.createIDevice(device);
					if (cd != null)
						devices.add(cd);
				}
				device = device.getNext();
			}
			bus = bus.getNext();
		}
		return devices;
	}

	/** below is customize methods */
	/** scan Matched Devices and return as a LinkedList */
	public static List<IDevice> scanMatchedDevices_Direct(short idVendor,
			short idProduct, IDeviceCreator creator) throws USBException {
		LibusbJava.usb_find_busses();
		LibusbJava.usb_find_devices();

		Usb_Bus bus = LibusbJava.usb_get_busses();
		if (bus == null) {
			throw new USBException("LibusbJava.usb_get_busses(): "
					+ LibusbJava.usb_strerror());
		}

		Usb_Device device = null;

		List<IDevice> devices = new LinkedList<IDevice>();

		// search for device
		while (bus != null) {
			device = bus.getDevices();
			while (device != null) {
				Usb_Device_Descriptor devDesc = device.getDescriptor();
				if ((devDesc.getIdVendor() == idVendor)
						&& (devDesc.getIdProduct() == idProduct)) {
					IDevice cd = creator.createIDevice(device);
					if (cd != null)
						devices.add(cd);
				}
				device = device.getNext();
			}
			bus = bus.getNext();
		}
		return devices;
	}

	/** create a raw Device */
	public static CDevice getDevice(short idVendor, short idProduct) {
		CDevice dev = new CDevice(idVendor, idProduct);
		return dev;
	}

	public static void simulateOpen(CDevice device, IDevice id)
			throws USBException {
		Usb_Device dev = id.getUsb_Device();
		simulateOpen(device, dev, id.getBConfigurationValue(), id
				.getBInterfaceNumber(), id.getBAlternateSetting());
	}

	/**
	 * for the real Device.open() enumerate Device_Descriptor only once, this
	 * method simulate the course and indicate the certain Device_Descriptor so
	 * that it could work for multiDevices own the same idVendor and idProduct
	 */
	public static void simulateOpen(CDevice device, Usb_Device dev,
			int configuration, int interface_, int altinterface)
			throws USBException {
		device.dev_configuration = configuration;
		device.dev_interface = interface_;
		device.dev_altinterface = altinterface;

		if (device.usbDevHandle != 0) {
			throw new USBException("device opened, close or reset first");
		}

		device.initUSB();

		device.initBus();

		if (dev != null) {
			long res = LibusbJava.usb_open(dev);
			if (res == 0) {
				throw new USBException("LibusbJava.usb_open: "
						+ LibusbJava.usb_strerror());
			}
			device.usbDevHandle = res;
		}

		if (dev == null || device.usbDevHandle == 0) {
			throw new USBException("USB device with idVendor 0x"
					+ Integer.toHexString(device.idVendor & 0xFFFF)
					+ " and idProduct 0x"
					+ Integer.toHexString(device.idProduct & 0xFFFF)
					+ " not found");
		}
		device.claim_interface(device.usbDevHandle, configuration, interface_,
				altinterface);
		if (device.resetOnFirstOpen & !device.resetDone) {
			logger.info("reset on first open");
			device.resetDone = true;
			device.reset();
			try {
				Thread.sleep(device.resetTimeout);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			simulateOpen(device, dev, configuration, interface_, altinterface);
		}
	}
}
