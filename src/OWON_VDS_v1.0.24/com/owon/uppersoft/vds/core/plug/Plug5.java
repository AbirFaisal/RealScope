package com.owon.uppersoft.vds.core.plug;

/**
 * PlugValueUtil_NEW, notes on using the interpolation algorithm:
 * 
 * Currently only usable for 5x interpolation, and the passed array needs 50 extra margin points on both left and right, which also take part in the interpolation computation, while the interpolation length passed in excludes the 50 margin points
 * 
 */
public class Plug5 {
	public static final double[][] PlugArgs_int_line = {
			{ 0, -0.0650,       0.6731, 0.4488, -0.0569 },
			{ 0, -0.0487,       0.8649, 0.2163, -0.0325 },//
			{ 0,       0,       1.000,  0,        0 }, //
			{ -0.0325, 0.2163, 0.8649, -0.0487,  0 },
			{ -0.0569, 0.4488, 0.6731, -0.0650,  0, } };

	private static final int ROWSINE = 5;
	private static final int LINESINE = 5;
	public static final int INTERPOLATENUM = 5;

	private static final int ofs = INTERPOLATENUM - 1;// Originally 4
	private static final int gbk = 2;

	private static final double compensate = 0.5;

	/**
	 * All start positions begin from 0
	 * 
	 * @param dst
	 * @param src
	 * @param src_length
	 */
	public static final void sine_interpolate(int[] dst, byte[] src,
			int src_length) {
		// int max, min;
		// max = min = 100;

		// Start interpolating from the 4th point
		for (int src_idx = ofs; src_idx < src_length; src_idx++) {
			for (int column_idx = 0; column_idx < LINESINE; column_idx++) {
				double sum = 0;
				for (int row_idx = 0; row_idx < ROWSINE; row_idx++) {
					sum += PlugArgs_int_line[column_idx][ROWSINE - 1 - row_idx]
							* (double) src[src_idx + row_idx - 2];
				}
				// Round to nearest, otherwise small bumps appear one by one when the waveform is a straight section;
				/** This handling may be model-dependent; depending on how the device's sine interpolation works, different algorithm details may need to be accommodated */
				sum += (sum > 0) ? compensate : -compensate;
				dst[src_idx * INTERPOLATENUM - gbk + column_idx] = (int) (sum);//

				//
				// if (k + j > max) {
				// max = k + j;
				// } else if (k + j < min) {
				// min = k + j;
				// }
			}
		}
		// System.out.println(max + ", " + min);
	}

	public static final int LEVEL_UPPEST = 125, LEVEL_LOWEST = -125;

	/**
	 * @param dst
	 * @param src
	 * @param src_length
	 */
	public static final void sine_interpolate(byte[] dst, byte[] src,
			int src_length) {
		// int max, min;
		// max = min = 100;
		// P5Log t = new P5Log();
		// Start interpolating from the 4th point
		for (int src_idx = ofs; src_idx < src_length; src_idx++) {
			for (int column_idx = 0; column_idx < LINESINE; column_idx++) {
				double sum = 0;
				for (int row_idx = 0; row_idx < ROWSINE; row_idx++) {
					sum += PlugArgs_int_line[column_idx][ROWSINE - 1 - row_idx]
							* (double) src[src_idx + row_idx - 2];
				}
				// Round to nearest, otherwise small bumps appear one by one when the waveform is a straight section;
				/** This handling may be model-dependent; depending on how the device's sine interpolation works, different algorithm details may need to be accommodated */
				sum += (sum > 0) ? compensate : -compensate;
				sum = (sum > LEVEL_UPPEST) ? LEVEL_UPPEST : sum;
				sum = (sum < LEVEL_LOWEST) ? LEVEL_LOWEST : sum;
				dst[src_idx * INTERPOLATENUM - gbk + column_idx] = (byte) (sum);//
				// t.log((int) sum);
				//
				// if (k + j > max) {
				// max = k + j;
				// } else if (k + j < min) {
				// min = k + j;
				// }
			}
		}
		// System.out.println(max + ", " + min);

		// t.log("\r\n");
	}
}

class P5Log {
	int i = 0;

	public void log(Object o) {
		System.out.print(o);
		i++;
		if (i % 5 == 0) {
			System.out.print(",");
		}
		System.out.print(" ");
		if (i == 100) {
			i = 0;
			System.out.println();
		}
	}
}