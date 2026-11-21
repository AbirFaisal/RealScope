package com.owon.uppersoft.vds.core.fft;


public enum WndType {

	Hamming(IFFTRef.hamming), Rectangle(IFFTRef.rectangle), Blackman(
			IFFTRef.blackman), Hanning(IFFTRef.hanning);

	private int[] mults;

	private WndType(int[] mults) {
		this.mults = mults;
	}

	@Override
	public String toString() {
		return name();
	}

	/**
	 * adc data is processed here; point fetching and windowing are done together for efficiency
	 * 
	 * @param adc
	 * @param start
	 * @param end
	 * @param len
	 * @param wnd_adc
	 */
	public final void wnding(int[] adc, int start, int end, int[] wnd_adc,
			int start2, int end2) {
		int half = (end2 - start2) >> 1;
		int center = (start + end) >> 1;
		start = center - half;
		end = center + half;
		for (int i = start, j = start2; i < end; i++, j++) {
			wnd_adc[j] = ((adc[i] * mults[j]) >> 16);
		}
	}
}