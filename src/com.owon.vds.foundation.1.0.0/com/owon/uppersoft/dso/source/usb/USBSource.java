package com.owon.uppersoft.dso.source.usb;

import java.io.IOException;
import java.nio.ByteBuffer;

import ch.ntb.usb.USBException;

import com.owon.uppersoft.vds.core.comm.ISource;
import com.owon.uppersoft.vds.core.usb.CDevice;
import com.owon.uppersoft.vds.core.usb.IDevice;

/**
 * USB数据源
 * 
 */
public class USBSource implements ISource {
	protected static final int LTimeOut = 7000;
	protected static final int DTimeOut = 200;
	protected static final int STimeOut = 3000;
	protected static final boolean reopen = false;

	protected IDevice id;
	protected CDevice dev;
	protected int WriteEp, ReadEp;

	protected int WrEpMaxPSize, RdEpMaxPSize;

	public USBSource(IDevice id, CDevice dev) {
		this.id = id;
		this.dev = dev;

		WriteEp = id.getWriteEndpoint();
		ReadEp = id.getReadEndpoint();

		WrEpMaxPSize = id.getWriteEpMaxPacketSize();
		RdEpMaxPSize = id.getReadEpMaxPacketSize();

		buf = new byte[WrEpMaxPSize];
	}

	public int getWriteEp() {
		return WriteEp;
	}

	public int getReadEp() {
		return ReadEp;
	}

	public CDevice getCDevice() {
		return dev;
	}

	public IDevice getId() {
		return id;
	}

	public int write(byte[] arr, int len) {
		return dowrite2(arr, len);
	}

	private int dowrite(byte[] arr, int len) {
		int timeout = LTimeOut;
		int wn = 0;
		try {
			// long t1 = Calendar.getInstance().getTimeInMillis();
			wn = dev.writeBulk(WriteEp, arr, len, timeout, reopen);
			// long t2 = Calendar.getInstance().getTimeInMillis();
			logln(len + " write num: " + wn);
			// dbgArray(arr, 0, len);
			// dbgln("write time: " + (t2 - t1) + "ms\n");
			return wn;
		} catch (USBException e) {
			// e.printStackTrace();
			logln(e.getMessage());
			return -1;
		} catch (Exception e) {
			// e.printStackTrace();
			logln(e.getMessage());
			return -1;
		}
	}

	private byte[] buf;

	private int dowrite2(byte[] arr, int len) {
		int timeout = DTimeOut;
		int wsum = 0;
		try {

			int packageSize = buf.length;

			int ptr = 0;
			while (ptr < len) {
				int sendSize;
				if (len - ptr > packageSize) {
					sendSize = packageSize;
				} else {
					sendSize = len - ptr;
				}
				System.arraycopy(arr, ptr, buf, 0, sendSize);
				ptr += sendSize;

				// long t1 = Calendar.getInstance().getTimeInMillis();
				int wn = dev.writeBulk(WriteEp, buf, sendSize, timeout, reopen);

				wsum += wn;
				// long t2 = Calendar.getInstance().getTimeInMillis();
				logln(len + " write num: " + wn);
				// dbgArray(arr, 0, len);
				// dbgln("write time: " + (t2 - t1) + "ms\n");
			}

			return wsum;
		} catch (USBException e) {
			// e.printStackTrace();
			logln(e.getMessage());
			return -1;
		} catch (Exception e) {
			// e.printStackTrace();
			logln(e.getMessage());
			return -1;
		}
	}

	protected void write(ByteBuffer bbuf) {
		byte[] arr = bbuf.array();
		int len = bbuf.limit();
		write(arr, len);
	}

	public int acceptResponse(byte[] arr, int len, int timeout) {
		int rn = -1;
		try {
			logln("acceptResponsing: " + len);
			rn = dev.readBulk(ReadEp, arr, len, timeout, reopen);
			logln("acceptResponsed: " + rn);
			return rn;
		} catch (USBException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rn;
	}

	public int acceptResponse(byte[] arr, int len) {
		return acceptResponse(arr, len, DTimeOut);
	}

	@Override
	public int read(byte[] arr, int start, int len) throws IOException {
		return dev.readBulk(ReadEp, arr, 11, STimeOut, reopen);
	}

	protected void logln(String string) {
	}

	protected void log(String string) {
	}

}
