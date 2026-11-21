package com.owon.uppersoft.vds.core.rt;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class AverageContainer {
	private FloatBuffer avgbuf;

	public void updateAverage(ByteBuffer adcbuf, int avgtimes, boolean avgon) {
		if (!avgon || avgtimes == 1 || adcbuf == null) {
			avgbuf = null;
			return;
		}

		// int chl = cdi.chl;
		final int mat = avgtimes - 1;

		final int p_pix = 0;

		final int p_adc = adcbuf.position();
		final int l_adc = adcbuf.limit();// p_adc + 100;//

		byte[] adcs = adcbuf.array();
		final int datalen = l_adc - p_adc;
		//System.out.println("[]chl: " + chl);
		//System.out.println("[] " + p_adc + ", " + l_adc);
		//DBG.dbgArray(adcs, p_adc, 10);
		if (avgbuf == null || avgbuf.remaining() < datalen) {
			// For the first average, assign the adc data directly
			avgbuf = FloatBuffer.allocate(datalen);
			float[] avgs = avgbuf.array();
			//System.out.println(": " + (l_adc - p_adc));
			for (int i = p_adc, j = p_pix; i < l_adc; i++, j++) {
				avgs[j] = (float) adcs[i];

				//if (j < 10)
				//	System.out.println(avgs[j]);
			}
			//System.out.println();
			return;
		}

		float[] avgs = avgbuf.array();
		float v;
		/**
		 * KNOW After this, use the computed result, formula:
		 * 
		 * AVG(this)=(AVG(prev)*(T-1)+adc(this))/T
		 */
		for (int i = p_adc, j = p_pix; i < l_adc; i++, j++) {
			//if (j < 10)
			//	System.out.println(avgs[j] + ", " + adcs[i]);
			v = (avgs[j] * mat + adcs[i]) / avgtimes;
			// v = adcs[i];
			avgs[j] = v;

			adcs[i] = (byte) v;
		}
		//DBG.dbgArray(adcs, p_adc, 10);
	}

	/**
	 * KNOW When averaging samples, the current sample point is still kept and used; only the drawing uses the effect of the averaged point
	 * 
	 * After stopping, just set
	 * 
	 * @param pc
	 * @return true means the coordinate calculation of the pixel point array is done and the following will be skipped; false means the regular pixel point array coordinate calculation needs to run, and memory will be cleared
	 */
	private boolean checkAverage(boolean ScreenMode_3, int yb, boolean avgon,
			int avgtimes, IntBuffer pixbuf, ByteBuffer adcbuf) {
		int at = avgtimes;
		boolean b = at > 1 && avgon;
		if (!b) {
			// System.err.println("check no");
			avgbuf = null;
			return false;
		}

		int mat = at - 1;
		int i, j;

		// avgbuf and pixbuf indices both start from 0
		int p_pix = 0;
		int[] pixs = pixbuf.array();

		int p_adc = adcbuf.position();
		int l_adc = adcbuf.limit();
		byte[] adcs = adcbuf.array();
		int datalen = l_adc - p_adc;
		if (avgbuf == null || avgbuf.remaining() < datalen) {
			// For the first average, assign the adc data directly
			avgbuf = FloatBuffer.allocate(datalen);
			float[] avgs = avgbuf.array();
			if (ScreenMode_3) {
				for (i = p_adc, j = p_pix; i < l_adc; i++, j++) {
					pixs[j] = (int) (yb - (avgs[j] = (float) adcs[i]));
				}
			} else {
				for (i = p_adc, j = p_pix; i < l_adc; i++, j++) {
					pixs[j] = (int) (yb - ((avgs[j] = (float) adcs[i]) * 2));
				}
			}
			pixbuf.position(0);
			pixbuf.limit(j);
			// System.err.println("check first");
			return true;
		}

		float[] avgs = avgbuf.array();
		float v;// TODO Replacing v directly with the assignment expression may improve efficiency
		/**
		 * KNOW After this, use the computed result, formula:
		 * 
		 * AVG(this)=(AVG(prev)*(T-1)+adc(this))/T
		 */
		if (ScreenMode_3) {
			for (i = p_adc, j = p_pix; i < l_adc; i++, j++) {
				v = (avgs[j] * mat + adcs[i]) / at;
				pixs[j] = (int) (yb - (avgs[j] = v));
			}
		} else {
			for (i = p_adc, j = p_pix; i < l_adc; i++, j++) {
				v = (avgs[j] * mat + adcs[i]) / at;
				pixs[j] = (int) (yb - ((avgs[j] = v) * 2));
			}
		}
		pixbuf.position(0);
		pixbuf.limit(j);
		// System.err.println("check keep");
		return true;
	}
}