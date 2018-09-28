package com.owon.uppersoft.vds.core.usb;

import ch.ntb.usb.Usb_Config_Descriptor;
import ch.ntb.usb.Usb_Device;
import ch.ntb.usb.Usb_Endpoint_Descriptor;
import ch.ntb.usb.Usb_Interface;
import ch.ntb.usb.Usb_Interface_Descriptor;

import com.owon.uppersoft.vds.util.format.SFormatter;

/**
 * ComparableDevice，可比较的设备
 */
public class ComparableDevice implements IDevice, Comparable<ComparableDevice> {

	private Usb_Device usb_Device;
	private String serialNumber;

	private int bConfigurationValue, bInterfaceNumber, bAlternateSetting;

	public int getBAlternateSetting() {
		return bAlternateSetting;
	}

	public int getBConfigurationValue() {
		return bConfigurationValue;
	}

	public int getBInterfaceNumber() {
		return bInterfaceNumber;
	}

	public int getReadEpMaxPacketSize() {
		return RdEpMaxPacketSize;
	}

	public int getWriteEpMaxPacketSize() {
		return WrEpMaxPacketSize;
	}

	private int WrEpMaxPacketSize, RdEpMaxPacketSize;
	private int WriteEndpoint, ReadEndpoint;

	public int getReadEndpoint() {
		return ReadEndpoint;
	}

	public int getWriteEndpoint() {
		return WriteEndpoint;
	}

	private ComparableDevice(Usb_Device usb_Device, String serialNumber) {
		this.usb_Device = usb_Device;
		this.serialNumber = serialNumber;
	}

	public String info() {
		String msg = SFormatter.UIformat(
				"cfg: %s, itf: %s, altitf: %s, in: %s, out: %s\r\n",
				bConfigurationValue, bInterfaceNumber, bAlternateSetting,
				Integer.toHexString(ReadEndpoint),
				Integer.toHexString(WriteEndpoint));
		return msg;
	}

	public static final ComparableDevice getInstance(Usb_Device usb_Device,
			String serialNumber) {
		Usb_Config_Descriptor[] ucds = usb_Device.getConfig();
		// System.out.println(ucds.length);
		if (ucds == null || ucds.length == 0)
			return null;

		Usb_Config_Descriptor ucd = ucds[0];
		Usb_Interface[] uis = ucd.getInterface();
		// System.out.println(uis.length);
		if (uis == null || uis.length == 0)
			return null;

		Usb_Interface ui = uis[0];
		Usb_Interface_Descriptor[] uids = ui.getAltsetting();
		// System.out.println(uids.length);
		if (uids == null || uids.length == 0)
			return null;

		Usb_Interface_Descriptor uid = uids[0];
		Usb_Endpoint_Descriptor[] ueds = uid.getEndpoint();
		// System.out.println(ueds.length);
		if (ueds == null || ueds.length < 2)
			return null;

		ComparableDevice cd = new ComparableDevice(usb_Device, serialNumber);
		cd.bConfigurationValue = ucd.getBConfigurationValue();
		cd.bInterfaceNumber = uid.getBInterfaceNumber();
		cd.bAlternateSetting = IUSBProtocol.DevAltinterface;// uid.getBAlternateSetting();

		for (Usb_Endpoint_Descriptor ued : ueds) {
			int ep = ued.getBEndpointAddress();
			int adr = ued.getBEndpointAddress() & 0xff;
			int MaxPacketSize = ued.getWMaxPacketSize();
			if ((ep & 0x80) > 0) {
				// IN
				cd.ReadEndpoint = adr;
				cd.RdEpMaxPacketSize = MaxPacketSize;
			} else {
				// OUT
				cd.WriteEndpoint = adr;
				cd.WrEpMaxPacketSize = MaxPacketSize;
			}
			// System.err.println(adr+", "+MaxPacketSize);
		}
		// DBG.errprintln(cd);
		return cd;
	}

	public Usb_Device getUsb_Device() {
		return usb_Device;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public void setUsb_Device(Usb_Device usb_Device) {
		this.usb_Device = usb_Device;
	}

	public int compareTo(ComparableDevice o) {
		return serialNumber.compareTo(o.serialNumber);
	}

	private String text;

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text == null ? serialNumber : text;
	}
}
