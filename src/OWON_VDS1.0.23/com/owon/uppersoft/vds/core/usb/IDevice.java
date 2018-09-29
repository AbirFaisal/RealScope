package com.owon.uppersoft.vds.core.usb;

import ch.ntb.usb.Usb_Device;

/**
 * IDevice，设备接口
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

	/** 可用于对本类的toString方法追加覆盖的文本 */
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
