package com.owon.uppersoft.vds.core.plug;

import java.nio.IntBuffer;
import java.util.ArrayDeque;

/**
 * Sine interpolation: compute 9 points, turning 1 point into 10
 * 
 * Addendum: besides linear interpolation, interpolating directly with 0 on the gaps also works with similar effect; just adjust the gain
 * 
 * @author Matt
 * 
 */
public class Plug10_old {

	public static int PlugRate = 10;
	public static double[] PlugArgs_dbl
	/**
	 * 81 parameters, arranged symmetrically outward from the array center point; middle elements may be empty
	 */
	= { -0.0000, -0.0020, -0.0038, -0.0053, -0.0063, -0.0068, -0.0066, -0.0058,
			-0.0044, -0.0024, 0, 0.0109, 0.0212, 0.0299, 0.0362, 0.0395,
			0.0391, 0.0348, 0.0266, 0.0148, 0, -0.0360, -0.0713, -0.1028,
			-0.1276, -0.1427, -0.1455, -0.1338, -0.1061, -0.0616, 0.0000,
			0.1079, 0.2279, 0.3552, 0.4844, 0.6098, 0.7260, 0.8275, 0.9096,
			0.9682, 1.0000, 0.9682, 0.9096, 0.8275, 0.7260, 0.6098, 0.4844,
			0.3552, 0.2279, 0.1079, 0.0000, -0.0616, -0.1061, -0.1338, -0.1455,
			-0.1427, -0.1276, -0.1028, -0.0713, -0.0360, 0, 0.0148, 0.0266,
			0.0348, 0.0391, 0.0395, 0.0362, 0.0299, 0.0212, 0.0109, 0, -0.0024,
			-0.0044, -0.0058, -0.0066, -0.0068, -0.0063, -0.0053, -0.0038,
			-0.0020, -0.0000 };

	/**
	 * 20 parameters case
	 * 
	 * -0.0000, -0.0176, -0.0341, -0.0485, -0.0597, -0.0666, -0.0681, -0.0632,
	 * -0.0509, -0.0301, 0, 0.1050, 0.2173, 0.3337, 0.4512, 0.5664, 0.6763,
	 * 0.7778, 0.8675, 0.9426, 1.0000, 0.9426, 0.8675, 0.7778, 0.6763, 0.5664,
	 * 0.4512, 0.3337, 0.2173, 0.1050, 0, -0.0301, -0.0509, -0.0632, -0.0681,
	 * -0.0666, -0.0597, -0.0485, -0.0341, -0.0176, -0.0000
	 */

	/**
	 * 81 parameters
	 */
	public static int[] PlugArgs_int = { 0x0000, 0xFFE0, 0xFFC2, 0xFFAA,
			0xFF99, 0xFF91, 0xFF93, 0xFFA1, 0xFFB8, 0xFFD9, 0x0000, 0x00B3,
			0x015B, 0x01EA, 0x0251, 0x0286, 0x0280, 0x023A, 0x01B4, 0x00F3,
			0x0000, 0xFDB2, 0xFB70, 0xF96B, 0xF7D6, 0xF6DF, 0xF6B1, 0xF770,
			0xF935, 0xFC0F, 0x0000, 0x06E8, 0x0E97,
			0x16BC,
			0x1F00,
			0x2708,
			0x2E77,
			0x34F7,
			0x3A38,
			0x3DF7,
			//
			0x4000,
			//
			0x3DF7, 0x3A38, 0x34F7, 0x2E77, 0x2708, 0x1F00, 0x16BC, 0x0E97,
			0x06E8, 0x0000, 0xFC0F, 0xF935, 0xF770, 0xF6B1, 0xF6DF, 0xF7D6,
			0xF96B, 0xFB70, 0xFDB2, 0x0000, 0x00F3, 0x01B4, 0x023A, 0x0280,
			0x0286, 0x0251, 0x01EA, 0x015B, 0x00B3, 0x0000, 0xFFD9, 0xFFB8,
			0xFFA1, 0xFF93, 0xFF91, 0xFF99, 0xFFAA, 0xFFC2, 0xFFE0, 0x0000 };

	public static int row = PlugRate, col = 8;
	public static ArrayDeque<Byte> ADCStack = new ArrayDeque<Byte>(col);

	public static void main(String[] args) {
		System.out.println(PlugArgs_dbl.length);

		int len = PlugArgs_dbl.length;
		for (int i = 0; i < len; i++) {
			System.out.println(PlugArgs_int[i] / (double) Plug0_AMP_int);
		}
	}

	/**
	 * out[len]; in[PlugArgs_int.length + (len - 1)]<-len/10 adc points linearly interpolated, then filled starting from element number PlugArgs_int.length -
	 * 1
	 * 
	 * @param out
	 * @param in
	 *            in.length = pa.length + (len - 1)
	 * @param len
	 */
	public static void fill_dbl(byte[] out, byte[] in, int len, double[] pa) {
		int palen_1 = pa.length - 1;
		int i = 0;
		double v;
		for (i = 0; i < len; i++) {
			v = 0;
			for (int k = i, j = palen_1; j >= 0; j--, k++) {
				v += in[k] * pa[j];
			}
			v /= AMP_dbl;

			if (v > 127) {
				out[i] = 127;
			} else if (v < -128) {
				out[i] = -128;
			} else {
				out[i] = (byte) v;
			}
		}
	}

	/**
	 * KNOW Since it relies on earlier data for computation, only the later part of the interpolation result is usable
	 * 
	 * out[len]; in[PlugArgs_int.length + (len - 1)]<-len/10 adc points linearly interpolated, then filled starting from element number PlugArgs_int.length -
	 * 1
	 * 
	 * @param out
	 * @param in
	 *            in.length = pa.length + (len - 1)
	 * @param len
	 */
	private static void fill_int(int[] out, byte[] in, int len, int[] pa) {
		int palen_1 = pa.length - 1;
		int i = 0;
		int v;
		for (i = 0; i < len; i++) {
			v = 0;
			for (int k = i, j = palen_1; j >= 0; j--, k++) {
				v += in[k] * (short) pa[j];
			}
			v /= Plug0_AMP_int;

			if (v > 127) {
				out[i] = 127;
			} else if (v < -128) {
				out[i] = -128;
			} else {
				out[i] = (byte) v;
			}
			// PlugValueUtil.compute(v);// v >>> 24;//
		}
	}

	// static byte[] plugbufarr = new byte[(Define.def.AREA_WIDTH << 1) + 1000];

	/**
	 * @param iPos
	 * @param len_
	 * @param plugbufarr
	 * @Deprecated
	 */
	private static final void plug_old(int iPos, int len_, byte[] plugbufarr,
			IntBuffer i_adcbuf, byte[] bufarr) {// DiluteInfoUnit diu,
		int len = (int) (len_ * (double) 21 / 20);
		int[] array = i_adcbuf.array();
		/**
		 * KNOW
		 * 
		 * How are the obtained points arranged on screen based on the original points? Starting from the first point, fill the gaps
		 * 
		 * Because the interpolation algorithm changed, consider changing the interpolation factor so that 1k points are always obtained for the sine transform
		 */
		// diu.retrieveBuf(bufarr, iPos, 0, len);
		/** KNOW Alternative: use the stretch ratio as the dynamic interpolation factor, always getting a number of points equal to the screen range */
		int pr = PlugRate;// rate;

		int palen = len * pr;
		int aglen = PlugArgs_int.length;

		/**
		 * KNOW Note that adc data must be filled starting from one point fewer than the number of coefficients; after interpolation the data is used from point 45, interpolated data / 159000,
		 * these are only estimated values that temporarily give good results for sine waves
		 */
		// Changed to interpolating with 0 values
		VDSource.genSimulatePlugZero(plugbufarr, aglen - 1, bufarr, 0, len, pr);
		// System.arraycopy(plugbufarr, aglen, array, 0, j);
		fill_int(array, plugbufarr, palen, PlugArgs_int);

		// System.out.println("palen: " + palen + ", aglen: " + aglen);
		// System.out.println(plugbufarr.length + "()" + array.length);

		i_adcbuf.position(45);
		i_adcbuf.limit(palen + 45);
	}

	public static final int Plug0_AMP_int = 18000;
	private static final int AMP_int = 159000;
	public static final double AMP_dbl = 1.01;
}
