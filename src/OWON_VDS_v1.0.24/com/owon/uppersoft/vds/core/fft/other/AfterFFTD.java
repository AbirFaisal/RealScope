package com.owon.uppersoft.vds.core.fft.other;


public class AfterFFTD {
	/**
	 * 均方根，fms = (i==0)?(FFT.mod()/N) : (FFT.mod()/(N/2));
	 * 
	 * @param cs
	 * @param data
	 */
	public static final void fft_math_rms(IComplex[] cs, int[] data) {
		int length = data.length;
		IComplex tc;
		double s = 1800;
		tc = cs[0];
		data[0] = (int) (Math.hypot(tc.re(), tc.im()) / s);
		s = length * 2 / 1.4;
		for (int i = 1; i < length; i++) {
			tc = cs[i];
			data[i] = (int) (Math.hypot(tc.re(), tc.im()) / s);
		}
	}

	/**
	 * @param cs
	 * @param data
	 * @param temps
	 */
	public static final void fft_math_dB(IComplex[] cs, int[] data,
			double[] temps, double vb) {
		int length = temps.length;
		IComplex tc;
		double r = 0, t;
		for (int i = 0; i < length; i++) {
			tc = cs[i];
			t = Math.pow(tc.re(), 2) + Math.pow(tc.im(), 2);
			r += t;
			temps[i] = Math.sqrt(t) / length * vb;
		}
		r = 1000;
		double min = vb / 2000 / Math.sqrt(2) / 16;
		for (int i = 0; i < length; i++) {
			t = temps[i] / r;
			t = t >= min ? t : min;
			data[i] = (int) (Math.log10(t) * 20);
		}
	}

	/**
	 * @param cs
	 * @param data
	 * @param temps
	 */
	public static final void fft_math_dB_e(IComplex[] cs, int[] data,
			double[] temps, double vb) {
		int length = temps.length;
		IComplex tc;
		double r = 0;
		for (int i = 0; i < length; i++) {
			tc = cs[i];
			r += temps[i] = Math.pow(tc.re(), 2) + Math.pow(tc.im(), 2);
		}
		double t;
		for (int i = 0; i < length; i++) {
			t = temps[i];
			if (t != 0) {
				data[i] = (int) (Math.log10((t * length / r)) * 10);
			} else {
				data[i] = -20;
			}
		}
	}

}