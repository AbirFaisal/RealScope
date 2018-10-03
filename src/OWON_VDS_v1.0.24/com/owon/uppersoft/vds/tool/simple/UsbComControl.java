package com.owon.uppersoft.vds.tool.simple;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTextField;

import ch.ntb.usb.LibusbJava;
import ch.ntb.usb.Usb_Device;
import ch.ntb.usb.Usb_Device_Descriptor;

import com.owon.uppersoft.dso.source.usb.USBSource;
import com.owon.uppersoft.dso.source.usb.USBSourceManager;
import com.owon.uppersoft.dso.util.DBG;
import com.owon.uppersoft.vds.core.usb.CDevice;
import com.owon.uppersoft.vds.core.usb.ComparableDevice;
import com.owon.uppersoft.vds.core.usb.IDevice;
import com.owon.uppersoft.vds.core.usb.IDeviceCreator;
import com.owon.uppersoft.vds.core.usb.IUSBProtocol;
import com.owon.uppersoft.vds.util.format.EndianUtil;

public class UsbComControl implements IDeviceCreator {

	public static final int hcenter = 125;

	private UsbCommunicator uct;
	private USBSourceManager usm;
	// private ByteBuffer sendCMDbuf;
	private ByteBuffer sendCMDbuf = ByteBuffer.allocate(255);
	private byte[] acpCMDbuf = new byte[5];

	public ByteBuffer getSendCMDbuf() {
		return sendCMDbuf;
	}

	UCModel um;

	public UsbComControl(UsbCommunicator uct) {
		this.uct = uct;
		um = uct.getUCModel();
		usm = new USBSourceManager(null);

	}

	public void addTextArea(String txt) {
		uct.addTextArea(txt);
	}

	public int insertdata(byte[] acpbuf) {
		int p = 0;
		acpbuf[p++] = 0;// 0x45;// 'E';
		acpbuf[p++] = 0x42;// 'B'
		acpbuf[p++] = 0x55;// 'U'
		acpbuf[p++] = 0x53;// 'S'
		acpbuf[p++] = 0x59;// 'Y'
		byte k = -128;
		boolean sw = true;

		for (; p < acpbuf.length; p++) {
			if (p < 1000)
				acpbuf[p] = 0;
			else
				acpbuf[p] = k;

			if (sw)
				k++;
			else
				k--;
			if (k == -128 || k == 127)
				sw = !sw;

			// System.out.println(acpbuf[i]);
		}
		return p;
	}

	@Deprecated
	public void sendCommandtogetVDS_Version() {
		String slver_d = ":SDSLVER";
		byte[] slver = ":SDSLVER#".getBytes();

		write(slver, slver.length);
		int n = 33;
		byte[] arr = new byte[n];
		// System.out.println("sm.acceptResponse(");
		int rn = acceptResponse(arr, n);
		// DBG.dbgArray(arr, 0, rn);
		if (rn != n) {
			DBG.outprintln("rn != " + n);
			return;
		}

		int i = 8;
		String h = new String(arr, 0, i);
		if (!h.equals(slver_d)) {
			DBG.outprintln("recieve arr!=" + slver_d);
			return;
		}
		uct.addTextArea("version:" + new String(arr, 0, rn));
	}

	/** USB methods { */
	public List<IDevice> refreshUSBPort() {
		try {
			final List<IDevice> udl = CDevice.scanMatchedDevices(
					IUSBProtocol.IdVender, IUSBProtocol.IdProduct, this);
			return collectPorts(udl);
		} catch (Throwable e) {
			e.printStackTrace();
			return new LinkedList<IDevice>();
		}
	}

	@Override
	public IDevice createIDevice(Usb_Device usb_Device) {
		return ComparableDevice.getInstance(usb_Device, "");
	}

	private List<IDevice> collectPorts(List<IDevice> udl) {
		Iterator<IDevice> it = udl.iterator();
		int i = 0;
		while (it.hasNext()) {
			IDevice id = it.next();
			Usb_Device dev = id.getUsb_Device();
			long handle = LibusbJava.usb_open(dev);
			Usb_Device_Descriptor devDesc = dev.getDescriptor();
			String iSerialNumber = LibusbJava.usb_get_string_simple(handle,
					devDesc.getISerialNumber());
			LibusbJava.usb_close(handle);
			if (iSerialNumber == null || iSerialNumber.isEmpty()) {
				iSerialNumber = "" + i;
			}
			i++;
			id.setSerialNumber(iSerialNumber);
		}
		return udl;
	}

	protected boolean isConnect() {
		return usm.isConnected();
	}

	protected USBSource _getUSBSource(IDevice id) {
		return usm._getUSBSource(id);
	}

	public USBSourceManager getUSBSourceManager() {
		return usm;
	}

	protected void releaseUSBSource() {
		usm.releaseUSBSource();
	}

	public int acceptResponse(byte[] arr, int len) {
		return usm.acceptResponse(arr, len);
	}

	public int write(byte[] arr, int len) {
		return usm.write(arr, len);
	}

	public void addCMD(int add, int bytes, int value, ByteBuffer sendbuf) {
		byte[] buf = sendbuf.array();
		int i = sendbuf.position();
		int p = i;
		EndianUtil.writeIntL(buf, i, add);
		i += 4;
		// EndianUtil.writeIntL(buf, i, bytes);
		// i += 4;
		bytes = multBytesWriteL(bytes, value, buf, i);
		// EndianUtil.writeIntL(buf, i, value);
		// i += 4;

		// add = bytes = value = 0;
		// i = 0;
		// add = EndianUtil.nextIntL(buf, i);
		// i += 4;
		// bytes = EndianUtil.nextIntL(buf, i);
		// i += 4;
		// value = EndianUtil.nextIntL(buf, i);
		// i += 4;

		int sendLen = 4 + 1 + bytes;
		if (!uct.isKeepGet()) {
			addTextArea("[ADD (" + sendLen + ") >>] 0x"
					+ Integer.toHexString(add) + ", 0x"
					+ Integer.toHexString(bytes) + ", 0x"
					+ Integer.toHexString(value));
			int start = 0;
			int end = start + sendLen;
			for (int j = start; j < end; j++) {
				uct.addwordinLine(buf[j]);
			}
			addTextArea("");
		}

		int l = p + sendLen;
		sendbuf.position(l);
		// sendbuf.limit(l);
		// sendbuf.put(buf, 0, sendLen);
		// bbl.add(ByteBuffer.wrap(buf, 0, sendLen));
	}

	private int multBytesWriteL(int bytes, int value, byte[] buf, int i) {
		buf[i++] = (byte) bytes;

		switch (bytes) {
		case 1:
			buf[i++] = (byte) value;
			break;
		case 2:
			buf[i++] = (byte) value;
			buf[i++] = (byte) (value >> 8);
			break;
		case 3:
			buf[i++] = (byte) value;
			buf[i++] = (byte) (value >> 8);
			buf[i++] = (byte) (value >> 16);
			break;
		case 4:
			buf[i++] = (byte) value;
			buf[i++] = (byte) (value >> 8);
			buf[i++] = (byte) (value >> 16);
			buf[i++] = (byte) (value >> 24);
			break;
		default:
			bytes = 0;
		}
		return bytes;
	}

	protected void sendByte_Int(JTextField tf_byte, JTextField tf_Int) {
		int i = 0;
		byte[] buf = new byte[5];
		byte fo = (byte) Integer.parseInt(tf_byte.getText(), 16);
		int value = Integer.parseInt(tf_Int.getText(), 16);

		buf[i++] = fo;
		/** value 按小尾序放入buf数组后4位 */
		buf[i++] = (byte) value;
		buf[i++] = (byte) (value >> 8);
		buf[i++] = (byte) (value >> 16);
		buf[i] = (byte) (value >> 24);

		int w = write(buf, buf.length);
		// int req = EndianUtil.nextIntL(buf, 1);
		// String s = Integer.toHexString(req);
		addTextArea("[send " + w + ":]0x" + tf_byte.getText() + ", 0x"
				+ tf_Int.getText());

		Arrays.fill(buf, (byte) 0);
		int r = acceptResponse(buf, 5);
		int res = EndianUtil.nextIntL(buf, 1);
		addTextArea("[GET (" + r + ") >>] " + ((char) buf[0]) + " 0x"
				+ Integer.toHexString(res));

	}

	// /** @deprecated 发指令要一帧数据 */
	// private void sendNAcceptOneFrame(JTextField tf_add, JTextField tf_bytes,
	// JTextField tf_value) {
	//
	// if (sendCMDbuf.position() == 0) {
	// add2CMDList(tf_add, tf_bytes, tf_value);
	// }
	// write(sendCMDbuf.array(), sendCMDbuf.position());
	// sendCMDbuf.position(0);
	// acceptOneFrame(null);// ch0
	// acceptOneFrame(null);// ch1
	// }
	private static final int ReceiveSize = 5100;
	private byte[] adcbuf = new byte[10000];// 接收adc数据
	private byte[] usbbuf = new byte[ReceiveSize];

	/** 接收一帧数据 */
	protected void acceptOneFrame(int chlnum) {
		int p = 0;
		int rn;

		rn = acceptResponse(usbbuf, ReceiveSize);
		addTextArea("[GET (" + rn + ") >>] " + p + ", " + rn);
		System.arraycopy(usbbuf, 0, adcbuf, p, rn);

		p += rn;
		// rn = acceptResponse(usbbuf, _5k);
		// System.arraycopy(usbbuf, 0, adcbuf, p, rn);

		// addTextArea("[GET (" + rn + ") adcData] ");
		// int rn = insertdata(adcbuf);

		if (adcbuf[0] == 'E') {
			String ebusy = (char) adcbuf[0] + "";
			for (int i = 1; i <= 4; i++) {
				ebusy = ebusy + (char) adcbuf[i];
			}
			addTextArea(ebusy);
		} else if (rn < 5000) {
			addTextArea("failed");
			uct.viewchartClear();
		} else {
			addTextArea("[GET adcData] ");
			int cnt = chlnum - 1;
			while (cnt > 0) {
				rn = acceptResponse(usbbuf, ReceiveSize);
				addTextArea("[GET (" + rn + ") adcData] " + p + ", " + rn);
				System.arraycopy(usbbuf, 0, adcbuf, p, rn);
				p += rn;
				cnt--;
			}

			ByteBuffer bb = ByteBuffer.wrap(adcbuf, 0, p);
			bb.position(0);
			um.resetNtailorBuf(bb, hcenter);

			uct.re_paint();
		}
	}

	protected void add2CMDList(JTextField tf_add, JTextField tf_bytes,
			JTextField tf_value) {
		int add = Integer.parseInt(tf_add.getText(), 16);
		int bytes = Integer.parseInt(tf_bytes.getText(), 16);
		int value = Integer.parseInt(tf_value.getText(), 16);
		addCMD(add, bytes, value, sendCMDbuf);
	}

	protected void sendNAcceptOneCMD(JTextField tf_add, JTextField tf_bytes,
			JTextField tf_value) {
		// int size = bbl.size();
		// if (size <= 0) {
		// add2CMDList();
		// }
		// addTextArea("[SEND ( " + size + " commands ) ] ");
		// for (ByteBuffer buf : bbl) {
		// write(buf.array(), buf.remaining());
		// }
		// bbl.clear();
		if (sendCMDbuf.position() == 0) {
			add2CMDList(tf_add, tf_bytes, tf_value);
		}

		dealCMDInteractive();
	}

	protected void dealCMDInteractive() {
		int p = sendCMDbuf.position();
		System.out.println("position:" + p);
		// if (p == 0)
		// return;
		write(sendCMDbuf.array(), p);
		sendCMDbuf.position(0);

		acceptResponse(acpCMDbuf, 5);
		int res = EndianUtil.nextIntL(acpCMDbuf, 1);
		addTextArea("[GET (" + 5 + ") >>] " + ((char) acpCMDbuf[0]) + " 0x"
				+ Integer.toHexString(res));
	}
}
