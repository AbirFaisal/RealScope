package com.owon.uppersoft.vds.core.fft;

import static com.owon.uppersoft.vds.core.fft.IFFTRef.LENGTH;

import java.util.Arrays;

import com.owon.uppersoft.vds.core.fft.other.AfterFFTD;
import com.owon.uppersoft.vds.core.fft.other.ComplexD;
import com.owon.uppersoft.vds.core.fft.other.FFTFunctionD;

public class FFTUtil {
	// private static final int[] wnd_adc = new int[LENGTH];
	// private static final int[] temps = new int[LENGTH];
	private static final double[] tempsD = new double[LENGTH >> 1];
	// private static final ComplexD[] TD = new ComplexD[LENGTH];
	// private static final ComplexD[] FD = new ComplexD[LENGTH];
	private static final ComplexD[] TDD = new ComplexD[LENGTH];
	// private static final ComplexD[] X2 = new ComplexD[LENGTH];

	static {
		// Arrays.fill(wnd_adc, 0);
		// Arrays.fill(temps, 0);
		Arrays.fill(tempsD, 0);

		for (int i = 0; i < LENGTH; i++) {
			// TD[i] = new ComplexD();
			// FD[i] = new ComplexD();
			// X2[i] = new ComplexD();
			TDD[i] = new ComplexD();
		}
	}

	public static final void compute_db(int[] data, WndType wt, double vb) {
		wt.wnding(data, 0, data.length, data, 0, data.length);
		/*
		 * FFTFunction.fft_adc(wnd_adc, 0, wnd_adc.length, TD, FD, X2);
		 * AfterFFTD.fft_math_dB(FD, data, tempsD, vb);
		 */

		FFTFunctionD.fft_adc(data, 0, data.length, TDD);
		AfterFFTD.fft_math_dB(TDD, data, tempsD, vb);
	}

	public static final void compute_rms(int[] data, WndType wt) {
		wt.wnding(data, 0, data.length, data, 0, data.length);

		/*
		 * FFTFunction.fft_adc(wnd_adc, 0, wnd_adc.length, TD, FD, X2);
		 * AfterFFTD.fft_math_rms(FD, data);
		 */

		FFTFunctionD.fft_adc(data, 0, data.length, TDD);
		AfterFFTD.fft_math_rms(TDD, data);
	}

	/**
	 * 插值算法，通常只在原始数据上计算一次
	 * 
	 * @param src
	 * @param dest
	 * @param length
	 */
	public static final void plugValues(byte[] src, int[] dest, int ds, int de,
			int length, int pos0) {
		int slen = de - ds;
		int srclen = Integer.highestOneBit(slen);
		// System.out.println("srclen: " + srclen);

		int step = length / srclen;
		// System.out.println("step: " + step);

		int half = srclen >> 1;
		int start = ds, end = de;
		int center = (start + end) >> 1;
		start = center - half;
		end = center + half;

		// System.out.println("start: " + start + "; end: " + end);

		/*
		 * src起码4个点才适用，下面将scrlen个点进行1->step的扩充
		 */
		dest[0] = src[start] - pos0;
		int i, k, lasti = 0, f, s;
		for (i = 0 + step, k = start + 1; i < length; i += step, k++) {
			f = dest[lasti];
			s = dest[i] = src[k] - pos0;
			int nv = (int) ((s - f) / (double) step);
			for (int m = lasti + 1; m < i; m++) {
				dest[m] = (f += nv);
			}
			lasti = i;
		}

		// System.out.println("lasti: " + lasti);
		// System.out.println("k: " + k);
		// System.out.println("i: " + i);

		int last = dest[lasti];
		for (int j = lasti + 1; j < length; j++) {
			dest[j] = last;
		}
	}

	/**
	 * 插值算法，通常只在原始数据上计算一次
	 * 
	 * @param src
	 * @param dest
	 * @param length
	 */
	public static final void plugValues(int[] src, int[] dest, int ds, int de,
			int length, int pos0) {
		int slen = de - ds;
		int srclen = Integer.highestOneBit(slen);
		// System.out.println("srclen: " + srclen);

		int step = length / srclen;
		// System.out.println("step: " + step);

		int half = srclen >> 1;
		int start = ds, end = de;
		int center = (start + end) >> 1;
		start = center - half;
		end = center + half;

		// System.out.println("start: " + start + "; end: " + end);

		/*
		 * src起码4个点才适用，下面将scrlen个点进行1->step的扩充
		 */
		dest[0] = src[start] - pos0;
		int i, k, lasti = 0, f, s;
		for (i = 0 + step, k = start + 1; i < length; i += step, k++) {
			f = dest[lasti];
			s = dest[i] = src[k] - pos0;
			int nv = (int) ((s - f) / (double) step);
			for (int m = lasti + 1; m < i; m++) {
				dest[m] = (f += nv);
			}
			lasti = i;
		}

		// System.out.println("lasti: " + lasti);
		// System.out.println("k: " + k);
		// System.out.println("i: " + i);

		int last = dest[lasti];
		for (int j = lasti + 1; j < length; j++) {
			dest[j] = last;
		}
	}
}