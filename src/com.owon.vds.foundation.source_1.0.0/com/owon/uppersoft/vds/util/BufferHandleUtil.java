package com.owon.uppersoft.vds.util;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class BufferHandleUtil {
	/**
	 * 为反相作调整
	 * 
	 * @param pos
	 * @param limit
	 */
	public static final void restrictForReverse(IntBuffer i_adcbuf,
			boolean reverse, int loadPos0) {
		/** 交给画图屏幕，处理反相问题 */
		reverse = false;

		/** 在adc数据取出绘图前进行必要的反相处理 */
		/** 内部也在这里把intArray转成byteArray，可同时进行两项处理 */
		if (reverse) {
			int pos = i_adcbuf.position();
			int limit = i_adcbuf.limit();

			int[] array = i_adcbuf.array();
			final int dpos0 = loadPos0 << 1;
			for (int i = pos; i < limit; i++) {
				array[i] = dpos0 - array[i];
			}
		}
	}

	/**
	 * 为反相作调整
	 * 
	 * @param pos
	 * @param limit
	 */
	public static final void restrictForReverse(ByteBuffer i_adcbuf,
			boolean reverse, int loadPos0) {
		/** 交给画图屏幕，处理反相问题 */
		reverse = false;

		/** 在adc数据取出绘图前进行必要的反相处理 */
		/** 内部也在这里把intArray转成byteArray，可同时进行两项处理 */
		if (reverse) {
			int pos = i_adcbuf.position();
			int limit = i_adcbuf.limit();

			byte[] array = i_adcbuf.array();
			final int dpos0 = loadPos0 << 1;
			for (int i = pos; i < limit; i++) {
				array[i] = (byte) (dpos0 - array[i]);
			}
		}
	}

	public static final void skipByteBuffer(Buffer bb, int skip) {
		bb.position(bb.position() + skip);
	}
}