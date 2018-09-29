package com.owon.uppersoft.vds.core.fft.other;

public class FFTFunctionD {
	public static final int powers(int size) {
		int i = 0;
		while (size > 1) {
			size = size >>> 1;
			i++;
		}
		return i;
	}

	public static final ComplexD[] fft_adc(int[] wnd_adc, int start, int end,
			ComplexD[] TD) {
		for (int i = start, j = 0; i < end; i++, j++) {
			TD[j].re = wnd_adc[i];
			TD[j].im = 0;
		}

		InplaceFFT.fft(TD);
		return TD;
	}
}
