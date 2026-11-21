package com.owon.uppersoft.vds.util;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class BufferHandleUtil {
	/**
	 * Adjust for inversion
	 * 
	 * @param pos
	 * @param limit
	 */
	public static final void restrictForReverse(IntBuffer i_adcbuf,
			boolean reverse, int loadPos0) {
		/** Hand over to the drawing screen, handling the inversion issue */
		reverse = false;

		/** Perform the necessary inversion on the adc data before it is taken out for drawing */
		/** Internally the intArray is also converted to byteArray here, so both are handled at once */
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
	 * Adjust for inversion
	 * 
	 * @param pos
	 * @param limit
	 */
	public static final void restrictForReverse(ByteBuffer i_adcbuf,
			boolean reverse, int loadPos0) {
		/** Hand over to the drawing screen, handling the inversion issue */
		reverse = false;

		/** Perform the necessary inversion on the adc data before it is taken out for drawing */
		/** Internally the intArray is also converted to byteArray here, so both are handled at once */
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