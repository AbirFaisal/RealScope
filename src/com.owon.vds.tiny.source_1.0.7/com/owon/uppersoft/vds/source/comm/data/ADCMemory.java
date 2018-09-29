package com.owon.uppersoft.vds.source.comm.data;

public class ADCMemory {
	public ADCMemory(int allChannlesNumber, int ReceiveSize) {
		usbbufs = new byte[allChannlesNumber][ReceiveSize];
		firstReceiveBuf = new byte[ReceiveSize];
	}

	public final byte[][] usbbufs;
	public byte[] firstReceiveBuf;
}