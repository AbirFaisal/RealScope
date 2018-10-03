package com.owon.uppersoft.vds.core.plug;

/**
 * PlugValueUtil_NEW, 插值算法使用须知:
 * 
 * 目前只可用于5倍插值，且传入的数组需要左右有多50个点的余量数据，这些数据也参与了插值的运算,而传入的插值长度为不含50个余点的
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

	private static final int ofs = INTERPOLATENUM - 1;// 原来为4
	private static final int gbk = 2;

	private static final double compensate = 0.5;

	/**
	 * 起始位置都从0开始
	 * 
	 * @param dst
	 * @param src
	 * @param src_length
	 */
	public static final void sine_interpolate(int[] dst, byte[] src,
			int src_length) {
		// int max, min;
		// max = min = 100;

		// 从第4点开始插值
		for (int src_idx = ofs; src_idx < src_length; src_idx++) {
			for (int column_idx = 0; column_idx < LINESINE; column_idx++) {
				double sum = 0;
				for (int row_idx = 0; row_idx < ROWSINE; row_idx++) {
					sum += PlugArgs_int_line[column_idx][ROWSINE - 1 - row_idx]
							* (double) src[src_idx + row_idx - 2];
				}
				// 做四舍五入处理，否则当波形是直线部分时会出现一个一个的小突起;
				/** 这一处理可能是机型相关的，根据下位机的正弦插值方式不同，可能需要去适应不同的算法细节 */
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
		// 从第4点开始插值
		for (int src_idx = ofs; src_idx < src_length; src_idx++) {
			for (int column_idx = 0; column_idx < LINESINE; column_idx++) {
				double sum = 0;
				for (int row_idx = 0; row_idx < ROWSINE; row_idx++) {
					sum += PlugArgs_int_line[column_idx][ROWSINE - 1 - row_idx]
							* (double) src[src_idx + row_idx - 2];
				}
				// 做四舍五入处理，否则当波形是直线部分时会出现一个一个的小突起;
				/** 这一处理可能是机型相关的，根据下位机的正弦插值方式不同，可能需要去适应不同的算法细节 */
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