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

	/** 持续获取统计得到的帧总数 */
	public int frameAllNum = 0;
	/** 有效数据示数，普通情况下为满屏数，fft时为有效数据个数 */
	public int points = -1;

	// KNOW 深存储，是一种停止状态下获取数据的方式，所以在深存储的情况下，是停止的
	private byte DMem;

	public byte getDMem() {
		return DMem;
	}

	/**
	 * 通道开关的位标志
	 */
	public byte chlflag;

	/** 判断存储深度在网络中选择不同的发送方式 */
	public int dmidx;

	public int screendatalen, slowMove;

	/** 接收通用 */
	public int status;
	public int channelStatus = 0;

	public int triggerStatus = 0;

	/**
	 * 包含情况当前channelsList列表
	 * 
	 * @param t
	 */
	public void reset(byte t) {
		DMem = t;
		points = -1;
		clearChannels();
	}

	/**
	 * 包含情况当前channelsList列表
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
