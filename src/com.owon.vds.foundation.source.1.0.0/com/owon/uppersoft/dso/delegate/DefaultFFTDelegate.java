package com.owon.uppersoft.dso.delegate;

import java.math.BigDecimal;

import com.owon.uppersoft.dso.global.CoreControl;
import com.owon.uppersoft.vds.core.aspect.control.FFTDelegate;

public class DefaultFFTDelegate implements FFTDelegate {
	private CoreControl cc;

	public DefaultFFTDelegate(CoreControl cc) {
		this.cc = cc;
	}

	@Override
	public BigDecimal getBDFFTTimeBases() {
		return cc.getMachineInfo().BDFFTTimeBases[getFFTTimebaseIdx()];
	}

	@Override
	public int getFFTTimebaseIdx() {
		return cc.getTimeControl().getTimebaseIdx();
	}

	@Override
	public void ontFFTSwitch(boolean ffton, int fftchl) {
		cc.ontFFTSwitch(ffton, fftchl);
	}

	@Override
	public void preSetChannelsForFFT(int fftchl) {
		cc.getWaveFormInfoControl().preSetChannelsForFFT(fftchl);
	}
}