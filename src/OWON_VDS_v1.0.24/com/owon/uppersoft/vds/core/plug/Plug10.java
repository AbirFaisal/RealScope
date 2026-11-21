package com.owon.uppersoft.vds.core.plug;

/**
 * Plug10, notes on using the interpolation algorithm:
 * 
 * Parameter array [9][10]: the column count is the interpolation factor, the row count is the number of parameters used per multiply-accumulate
 * 
 */
public class Plug10 {

	public static double[][] PlugArgs_dbl = {
			{ 0, -0.0020, -0.0038, -0.0053, -0.0063, -0.0068, -0.0066, -0.0058,
					-0.0044, -0.0024 },
			{ 0, 0.0109, 0.0212, 0.0299, 0.0362, 0.0395, 0.0391, 0.0348,
					0.0266, 0.0148 },
			{ 0, -0.0360, -0.0713, -0.1028, -0.1276, -0.1427, -0.1455, -0.1338,
					-0.1061, -0.0616 },
			{ 0, 0.1079, 0.2279, 0.3552, 0.4844, 0.6098, 0.7260, 0.8275,
					0.9096, 0.9682 },
			{ 1, 0.9682, 0.9096, 0.8275, 0.7260, 0.6098, 0.4844, 0.3552,
					0.2279, 0.1079 },
			{ 0, -0.0616, -0.1061, -0.1338, -0.1455, -0.1427, -0.1276, -0.1028,
					-0.0713, -0.0360 },
			{ 0, 0.0148, 0.0266, 0.0348, 0.0391, 0.0395, 0.0362, 0.0299,
					0.0212, 0.0109 },
			{ 0, -0.0024, -0.0044, -0.0058, -0.0066, -0.0068, -0.0063, -0.0053,
					-0.0038, -0.0020 }, //
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } };

	public static final int INTERPOLATENUM = 10;

	private static final int ROWSINE = INTERPOLATENUM;
	private static final int LINESINE = 9;

	public static final int SOURCE_DATA_OFFSET = INTERPOLATENUM - 1;

	/**
	 * H0, H0H1, ... H0~H8 take part in the multiply-accumulate, then H1~H9 next; if fewer than 9 adc data points are at the front, pad with 0 at the front; actual interpolation takes extra data at the front
	 * 
	 * @param dst
	 * @param src
	 * @param src_length
	 * @param dest_beg
	 *            Start filling data from here
	 * @param src_beg
	 *            From here on, SOURCE_DATA_OFFSET zeros are needed first, then the adc data; these data take part in the calculation
	 */
	public static final void sine_interpolate(byte[] dst, byte[] src,
			int src_length, int dest_beg, int src_beg) {

		for (int src_idx = src_beg, dest_idx = dest_beg; src_idx < src_length; src_idx++) {
			for (int column_idx = 0; column_idx < ROWSINE; column_idx++) {// 10
				double sum = 0;
				for (int row_idx = 0; row_idx < LINESINE; row_idx++) {// 9

					sum += PlugArgs_dbl[LINESINE - 1 - row_idx][column_idx]
							* src[src_idx + row_idx];
				}
				// Round to nearest, otherwise small bumps appear one by one when the waveform is a straight section;
				/** This handling may be model-dependent; depending on how the device's sine interpolation works, different algorithm details may need to be accommodated */

				sum += (sum > 0) ? compensate : -compensate;
				sum = (sum > LEVEL_UPPEST) ? LEVEL_UPPEST : sum;
				sum = (sum < LEVEL_LOWEST) ? LEVEL_LOWEST : sum;
				dst[dest_idx++] = (byte) (sum);
			}
		}

	}

	public static final void sine_interpolate(int[] dst, byte[] src,
			int src_length, int dest_beg, int src_beg) {

		for (int src_idx = src_beg, dest_idx = dest_beg; src_idx < src_length; src_idx++) {
			for (int column_idx = 0; column_idx < ROWSINE; column_idx++) {// 10
				double sum = 0;
				for (int row_idx = 0; row_idx < LINESINE; row_idx++) {// 9
					sum += PlugArgs_dbl[LINESINE - 1 - row_idx][column_idx]
							* src[src_idx + row_idx];
				}
				// Round to nearest, otherwise small bumps appear one by one when the waveform is a straight section;
				/** This handling may be model-dependent; depending on how the device's sine interpolation works, different algorithm details may need to be accommodated */

				sum += (sum > 0) ? compensate : -compensate;
				sum = (sum > LEVEL_UPPEST) ? LEVEL_UPPEST : sum;
				sum = (sum < LEVEL_LOWEST) ? LEVEL_LOWEST : sum;
				dst[dest_idx++] = (byte) (sum);
			}
		}

	}

	private static final double compensate = 0.5;

	public static final int LEVEL_UPPEST = 125, LEVEL_LOWEST = -125;

}