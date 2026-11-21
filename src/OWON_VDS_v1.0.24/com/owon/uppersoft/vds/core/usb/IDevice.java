package com.owon.uppersoft.vds.core.usb;

import net.codecrete.usb.UsbDevice;

/**
 * IDevice, device interface
 */
public interface IDevice {

	/**
	 * @return UsbDevice
	 */
	UsbDevice getUsbDevice();

	/**
	 * @return SerialNumber
	 */
	String getSerialNumber();

	/** Text that can be used to append to or override the toString method of this class */
	void setText(String txt);
	
	/**
	 * @param sn
	 *            SerialNumber
	 */
	void setSerialNumber(String sn);

	int getBAlternateSetting();

	int getBConfigurationValue();

	int getBInterfaceNumber();

	int getReadEndpoint();

	int getWriteEndpoint();

	int getReadEpMaxPacketSize();

	int getWriteEpMaxPacketSize();
}
