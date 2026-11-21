package com.owon.uppersoft.vds.core.plug;

/**
 * This is a C program that low-pass filters a speech signal (0.3kHz~3.4kHz); the low-pass cutoff frequency is 800Hz, and the filter is a 19-tap finite impulse response (FIR) filter.
 * The speech sampling frequency is 8kHz, and each speech sample is stored as a 16-bit integer in the file insp.dat.
 */
public class FIR_Filter {
	public static int length = 180;
	/** Speech frame length = 180 points = 22.5ms@8kHz sampling */
	public static int[] x1 = new int[length + 20];
	public static double[] h = { 0.01218354, -0.009012882, -0.02881839,
			-0.04743239, -0.04584568, -0.008692503, 0.06446265, 0.1544655,
			0.2289794, 0.257883, 0.2289794, 0.1544655, 0.06446265,
			-0.008692503, -0.04584568, -0.04743239, -0.02881839, -0.009012882,
			0.01218354 };

	/** Low-pass filtering floating-point subroutine */
	public static void filter(int xin[], int xout[], int n, double h[]) {
		int i, j;
		double sum;
		for (i = 0; i < length; i++) {
			x1[n + i - 1] = xin[i];
			// System.out.println((n + i - 1) + ", " + i);
			// 18~197, 0~179
		}
		for (i = 0; i < length; i++) {
			sum = 0.0;
			for (j = 0; j < n; j++) {
				sum += h[j] * x1[i - j + n - 1];
				if (i == 1) {
					System.out.println(j + ", " + (i - j + n - 1));
					// 0~18, 18+n~n
				}
			}
			xout[i] = (int) sum;
		}
		for (i = 0; i < (n - 1); i++) {
			x1[n - i - 2] = xin[length - 1 - i];
			// System.out.println((n - i - 2) + ", " + (length - 1 - i));
			// 17~0, 179~162
		}
	}

	public static void main(String[] args) {
		int[] xin = new int[length];
		int[] xout = new int[length];
		filter(xin, xout, h.length, h);
	}

}
