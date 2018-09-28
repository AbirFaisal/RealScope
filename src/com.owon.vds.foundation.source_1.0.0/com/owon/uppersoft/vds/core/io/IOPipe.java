package com.owon.uppersoft.vds.core.io;

public class IOPipe {
	// private int WrEpMaxPSize;
	// private int RdEpMaxPSize;
	private byte[] wrbuf, rdbuf;
	private IOPacket iop;

	public IOPipe(IOPacket iop, int WrEpMaxPSize, int RdEpMaxPSize) {
		this.iop = iop;
		wrbuf = new byte[WrEpMaxPSize];
		rdbuf = new byte[RdEpMaxPSize];
	}

	public int read(byte[] arr, final int start, final int len) {
		int buflen = rdbuf.length;

		int beg = start;
		int left = len;
		int readlen;
		int rn;
		while (left > 0) {
			readlen = ((left >= buflen) ? buflen : left);
			rn = readPacket(rdbuf, readlen);

			if (rn < 0)
				return (len - left);

			System.arraycopy(rdbuf, 0, arr, beg, rn);
			left -= rn;
			beg += rn;
		}
		return len;
	}

	public int write(byte[] arr, final int start, final int len) {
		int buflen = wrbuf.length;

		int beg = start;
		int left = len;
		int sendlen;
		int wn;
		while (left > 0) {
			sendlen = ((left >= buflen) ? buflen : left);
			System.arraycopy(arr, beg, wrbuf, 0, sendlen);
			wn = writePacket(wrbuf, sendlen);
			if (wn < 0)
				return (len - left);
			left -= wn;
			beg += wn;
		}
		return len;
	}

	private int readPacket(byte[] rdbuf, int readlen) {
		return iop.readPacket(rdbuf, readlen);
	}

	private int writePacket(byte[] wrbuf, int sendlen) {
		return iop.writePacket(wrbuf, sendlen);
	}
}