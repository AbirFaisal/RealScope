package com.owon.uppersoft.vds.core.usb;

import java.util.List;

import net.codecrete.usb.UsbAlternateInterface;
import net.codecrete.usb.UsbDevice;
import net.codecrete.usb.UsbDirection;
import net.codecrete.usb.UsbEndpoint;
import net.codecrete.usb.UsbInterface;

import com.owon.uppersoft.vds.util.format.SFormatter;

/**
 * ComparableDevice, a comparable device
 */
public class ComparableDevice implements IDevice, Comparable<ComparableDevice> {

	private UsbDevice usbDevice;
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

	private ComparableDevice(UsbDevice usbDevice, String serialNumber) {
		this.usbDevice = usbDevice;
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

	public static final ComparableDevice getInstance(UsbDevice usbDevice,
			String serialNumber) {
		List<UsbInterface> uis = usbDevice.getInterfaces();
		// System.out.println(uis.size());
		if (uis.isEmpty())
			return null;

		UsbInterface ui = uis.get(0);
		List<UsbAlternateInterface> uas = ui.getAlternates();
		// System.out.println(uas.size());
		if (uas.isEmpty())
			return null;

		UsbAlternateInterface ua = uas.get(0);
		List<UsbEndpoint> ueps = ua.getEndpoints();
		// System.out.println(ueps.size());
		if (ueps.size() < 2)
			return null;

		ComparableDevice cd = new ComparableDevice(usbDevice, serialNumber);
		/** bConfigurationValue is byte 5 of the configuration descriptor */
		cd.bConfigurationValue = usbDevice.getConfigurationDescriptor()[5] & 0xff;
		cd.bInterfaceNumber = ui.getNumber();
		cd.bAlternateSetting = IUSBProtocol.DevAltinterface;// ua.getNumber();

		for (UsbEndpoint uep : ueps) {
			int MaxPacketSize = uep.getPacketSize();
			if (uep.getDirection() == UsbDirection.IN) {
				// IN, keep the endpoint address with its direction bit
				cd.ReadEndpoint = uep.getNumber() | 0x80;
				cd.RdEpMaxPacketSize = MaxPacketSize;
			} else {
				// OUT
				cd.WriteEndpoint = uep.getNumber();
				cd.WrEpMaxPacketSize = MaxPacketSize;
			}
			// System.err.println(uep.getNumber()+", "+MaxPacketSize);
		}
		// DBG.errprintln(cd);
		return cd;
	}

	public UsbDevice getUsbDevice() {
		return usbDevice;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public void setUsbDevice(UsbDevice usbDevice) {
		this.usbDevice = usbDevice;
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
