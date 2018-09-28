package com.owon.uppersoft.vds.core.wf.rt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ShareBuff {
	/** KNOW 用于RT存放接收的所有数据，包括混屏情况下的多次波形，提供给RT的相关运算，在下次接收后被刷新 */
	private int pShareBuf = 0;
	private byte[] shareBuf = null;

	protected byte[] getShareBuf() {
		return shareBuf;
	}

	public void reset() {
		pShareBuf = 0;
	}

	/**
	 * 重设共享内存大小为chlnum*chllen
	 * 
	 * @param chlnum
	 * @param chllen
	 */
	public byte[] resetRTBuffer(int chlnum, int chllen, int frmCount) {
		// 如果只保存screendatalen，则不需要chllen这么大
		return resetRTBuffer(chlnum * chllen * frmCount);
	}

	/**
	 * 重设共享内存大小为chlnum*chllen
	 * 
	 * @param chlnum
	 * @param chllen
	 */
	public byte[] resetRTBuffer(int bufsize) {
		resetBuffer(bufsize);
		return shareBuf;
	}

	public ByteBuffer allocBuffer(int len) {
		// DBG.dbgln("cti.allocBuffer Bufpos0: " + pShareBuf + " <<+len: " +
		// len);
		ByteBuffer bb = ByteBuffer.wrap(shareBuf, pShareBuf, len);
		bb.order(ByteOrder.BIG_ENDIAN);
		pShareBuf += len;
		return bb;
	}

	/**
	 * 拷贝到内部共享内存
	 * 
	 * @param arr2nd
	 * @param start
	 * @param len
	 * @return
	 */
	public ByteBuffer copy2Buffer(byte[] arr2nd, int start, int len) {
		System.arraycopy(arr2nd, start, shareBuf, pShareBuf, len);
		// KNOW 补齐缺的一点
		// if (false && (len == 1999 || len == 999)) {
		// shareBuf[pShareBuf + len] = shareBuf[pShareBuf + len - 1];
		// len++;
		// }
		ByteBuffer bb = ByteBuffer.wrap(shareBuf, pShareBuf, len);
		bb.order(ByteOrder.BIG_ENDIAN);
		pShareBuf += len;
		return bb;
	}

	/**
	 * 拷贝到内部共享内存，只复制提供数组的initPos开始，limlen长的数据
	 * 
	 * @param arr2nd
	 * @param start
	 * @param len
	 * @return
	 */
	public ByteBuffer copy2Buffer(byte[] arr2nd, int start, int len,
			int initPos, int limlen) {
		System.arraycopy(arr2nd, start + initPos, shareBuf, pShareBuf, limlen);
		// KNOW 补齐缺的一点
		// if (false && (len == 1999 || len == 999)) {
		// shareBuf[pShareBuf + limlen] = shareBuf[pShareBuf + limlen - 1];
		// limlen++;
		// }
		ByteBuffer bb = ByteBuffer.wrap(shareBuf, pShareBuf, limlen);
		bb.order(ByteOrder.BIG_ENDIAN);
		pShareBuf += limlen;
		return bb;
	}

	public void release() {
		shareBuf = null;
	}

	public boolean isBufNull() {
		return shareBuf == null;
	}

	/**
	 * 重设共享内存大小，暂无使用
	 * 
	 * @param len
	 */
	private void resetBuffer(int len) {
		if (shareBuf == null || shareBuf.length < len) {
			// if (shareBuf == null)
			// DBG.configln("cti.ResetBuffer null ");
			// else {
			// DBG.configln("cti.ResetBuffer: " + shareBuf.length + " < "
			// + len);
			// }
			shareBuf = new byte[len];
		}
	}
}