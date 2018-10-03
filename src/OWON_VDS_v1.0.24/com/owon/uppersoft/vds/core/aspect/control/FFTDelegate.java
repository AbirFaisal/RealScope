package com.owon.uppersoft.vds.core.aspect.control;

import java.math.BigDecimal;

public interface FFTDelegate {

	void preSetChannelsForFFT(int fftchl);

	void ontFFTSwitch(boolean ffton, int fftchl);

	int getFFTTimebaseIdx();

	BigDecimal getBDFFTTimeBases();
}