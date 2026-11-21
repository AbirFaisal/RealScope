package com.owon.uppersoft.vds.core.wf.rt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class ChannelsTransportInfo extends LoadMedia {

	public static final int Status_RT_OK = 0;

	public static final int Status_RT_UnknownErr = 1;
	public static final int Status_RT_ReadContentErr = 2;
	public static final int Status_RT_ConnectErr = 3;
	public static final int Status_RT_WriteContentErr = 4;

	public static final int Status_RT_Count = 5;

	private int[] statusArr = new int[Status_RT_Count];

	public ChannelsTransportInfo() {
	}

	public void resetStatusArr() {
		int len = statusArr.length;
		for (int i = 0; i < len; i++) {
			statusArr[i] = 0;
		}
	}

	public void updateStatus(int i) {
		if (i >= 0 && i < statusArr.length) {
			statusArr[i]++;
		}
	}

	public int getStatistics() {
		int len = statusArr.length;
		int v = 0, j = Status_RT_UnknownErr;
		for (int i = 0; i < len; i++) {
			int v1 = statusArr[i];
			// System.err.println(i + ", " + v1);
			if (v < v1) {
				j = i;
				v = v1;
			}
		}
		return j;
	}

	/** Total number of frames obtained by continuous acquisition statistics */
	public int frameAllNum = 0;
	/** Valid data count; normally the full-screen count, for fft the number of valid data points */
	public int points = -1;

	// KNOW Deep memory is a way of getting data in the stopped state, so with deep memory the scope is stopped
	private byte DMem;

	public byte getDMem() {
		return DMem;
	}

	/**
	 * Bit flags for channel on/off
	 */
	public byte chlflag;

	/** Choose different sending methods over the network based on the memory depth */
	public int dmidx;

	public int screendatalen, slowMove;

	/** Common for receiving */
	public int status;
	public int channelStatus = 0;

	public int triggerStatus = 0;

	/**
	 * Current channelsList list, depending on what is included
	 * 
	 * @param t
	 */
	public void reset(byte t) {
		DMem = t;
		points = -1;
		clearChannels();
	}

	/**
	 * Current channelsList list, depending on what is included
	 * 
	 * @param t
	 */
	public void reset(char t) {
		reset((byte) t);
	}

	public int setChannelStatus(int channelStatus) {
		this.channelStatus = channelStatus;
		return Integer.bitCount(channelStatus);// setChannelNum
	}

	public int getChannelStatus() {
		return channelStatus;
	}

	public void loadFinish() {
	}

	private boolean mayReady;

	public void checkReady() {
		mayReady = (getFrameCount() == 0);
	}

	public boolean mayReady() {
		return mayReady;
	}

	public void release() {
	}

}
