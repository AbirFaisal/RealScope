package com.owon.uppersoft.vds.core.usb;

import ch.ntb.usb.Usb_Device;

/**
 * IDevice, device interface
 */
public interface IDevice {

	/**
	 * @return Usb_Device
	 */
	Usb_Device getUsb_Device();

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
