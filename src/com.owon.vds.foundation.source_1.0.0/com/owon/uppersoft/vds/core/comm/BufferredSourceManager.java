package com.owon.uppersoft.vds.core.comm;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class BufferredSourceManager implements ICommunicateManager {

	public static final int BufferSize = 20480;// 20K
	/**
	 * KNOW 给不同的源共用；所以一次只能使用一种方式连接；内部控制，特别设置起始位置为数组第0元素，每次使用从第0元素开始
	 */
	private ByteBuffer bbuf;

	public BufferredSourceManager() {
		/**
		 * 长度固定1024 * 10，每次发送这些数据的超时时间20000ms
		 */
		bbuf = ByteBuffer.allocate(BufferSize);
		bbuf.order(ByteOrder.BIG_ENDIAN);
	}

	/**
	 * KNOW 需优化，普通指令需要不大的临时缓冲区，批量指令需要一定空间的临时缓冲区，数据接收需要可反复使用的固定缓冲区
	 * 
	 * 目前的做法，前两者共用，可考虑把前两者也分离
	 * 
	 * 第三独立存放方便处理使用，但也反复创建新数组，先实现后优化
	 * 
	 * 
	 * 代理数据源操作
	 * 
	 * @return 重置后的缓冲区
	 */
	public ByteBuffer clearByteBuffer() {
		bbuf.clear();
		return bbuf;
	}

}