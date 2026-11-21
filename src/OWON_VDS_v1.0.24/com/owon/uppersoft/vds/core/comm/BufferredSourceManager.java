package com.owon.uppersoft.vds.core.comm;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class BufferredSourceManager implements ICommunicateManager {

	public static final int BufferSize = 20480;// 20K
	/**
	 * KNOW Shared by different sources; so only one connection method can be used at a time; internally controlled, the start position is specially set to element 0 of the array, and each use starts from element 0
	 */
	private ByteBuffer bbuf;

	public BufferredSourceManager() {
		/**
		 * Fixed length 1024 * 10, timeout for sending this data each time: 20000ms
		 */
		bbuf = ByteBuffer.allocate(BufferSize);
		bbuf.order(ByteOrder.BIG_ENDIAN);
	}

	/**
	 * KNOW Needs optimization: ordinary commands need a small temporary buffer, batch commands need a temporary buffer of some size, and data reception needs a fixed buffer that can be reused repeatedly
	 * 
	 * Current approach: the first two share one; could consider separating the first two as well
	 * 
	 * The third is stored independently for easier handling, but it also creates new arrays repeatedly; implement first, optimize later
	 * 
	 * 
	 * Proxy data source operations
	 * 
	 * @return the buffer after reset
	 */
	public ByteBuffer clearByteBuffer() {
		bbuf.clear();
		return bbuf;
	}

}