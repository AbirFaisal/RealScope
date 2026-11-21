package com.owon.uppersoft.vds.core.wf.rt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ShareBuff {
	/** KNOW Used by RT to hold all received data, including multiple waveforms in mixed-screen mode, provided for RT calculations, and refreshed after the next reception */
	private int pShareBuf = 0;
	private byte[] shareBuf = null;

	protected byte[] getShareBuf() {
		return shareBuf;
	}

	public void reset() {
		pShareBuf = 0;
	}

	/**
	 * Reset the shared memory size to chlnum*chllen
	 * 
	 * @param chlnum
	 * @param chllen
	 */
	public byte[] resetRTBuffer(int chlnum, int chllen, int frmCount) {
		// If only screendatalen is saved, chllen does not need to be this large
		return resetRTBuffer(chlnum * chllen * frmCount);
	}

	/**
	 * Reset the shared memory size to chlnum*chllen
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
	 * Copy into the internal shared memory
	 * 
	 * @param arr2nd
	 * @param start
	 * @param len
	 * @return
	 */
	public ByteBuffer copy2Buffer(byte[] arr2nd, int start, int len) {
		System.arraycopy(arr2nd, start, shareBuf, pShareBuf, len);
		// KNOW Pad the one missing point
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
	 * Copy into the internal shared memory, copying only the data of length limlen starting at initPos of the provided array
	 * 
	 * @param arr2nd
	 * @param start
	 * @param len
	 * @return
	 */
	public ByteBuffer copy2Buffer(byte[] arr2nd, int start, int len,
			int initPos, int limlen) {
		System.arraycopy(arr2nd, start + initPos, shareBuf, pShareBuf, limlen);
		// KNOW Pad the one missing point
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
	 * Reset the shared memory size, currently unused
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