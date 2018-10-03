package com.owon.uppersoft.dso.mode.control;

import java.math.BigDecimal;

import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.source.comm.effect.SubmitorFactory;
import com.owon.uppersoft.vds.core.aspect.base.IOrgan;
import com.owon.uppersoft.vds.core.aspect.control.FFTDelegate;
import com.owon.uppersoft.vds.core.aspect.control.VoltageProvider;
import com.owon.uppersoft.vds.core.comm.effect.IPatchable;
import com.owon.uppersoft.vds.core.comm.effect.Submitable;
import com.owon.uppersoft.vds.util.Pref;
import com.owon.uppersoft.vds.util.format.UnitConversionUtil;

public class FFTControl implements IOrgan, IPatchable {

	public class Volt {
		public int idx;

		public Volt(int idx) {
			this.idx = idx;
		}

		public String getLabel(int pbrate) {
			return UnitConversionUtil.getIntVoltageLabel_mV(vp.getVoltage(
					pbrate, idx));
		}

		public int getVolt() {
			return vp.getVoltage(0, idx);
		}

		@Override
		public String toString() {
			return getLabel(0);
		}
	}

	public Volt[] VOLTAGEs;

	public String[] dBPerDiv;
	private WaveFormManager wfm;
	private FFTDelegate cc;
	private VoltageProvider vp;

	public FFTControl(FFTDelegate cc, VoltageProvider vp) {
		this.cc = cc;

		int len = dBValuePerDiv.length;
		dBPerDiv = new String[len];
		for (int i = 0; i < len; i++) {
			dBPerDiv[i] = dBValuePerDiv[i] + "dB";
		}
		this.vp = vp;
		len = vp.getVoltageNumber();
		VOLTAGEs = new Volt[len];
		for (int m = 0; m < len; m++) {
			VOLTAGEs[m] = new Volt(m);
		}
	}

	public void load(Pref p, int channelsNumber) {
		fftchl = p.loadInt("Math.fftchl");
		if (fftchl >= channelsNumber)
			fftchl = channelsNumber - 1;
		fftvaluetype = p.loadInt("Math.fftvaluetype");
		this.fftwnd = p.loadInt("Math.fftwnd");
		fftscale = p.loadInt("Math.fftscale");

		// 实现对缩放档位的保存不太容易载入，而且意义也不大
		fftscale = 0;

		ffton = p.loadBoolean("Math.ffton");

		ffton = false;

		if (ffton)
			cc.preSetChannelsForFFT(fftchl);
	}

	public void persist(Pref p) {
		p.persistInt("Math.fftchl", fftchl);
		p.persistInt("Math.fftvaluetype", fftvaluetype);
		p.persistInt("Math.fftwnd", fftwnd);
		p.persistInt("Math.fftscale", fftscale);

		p.persistBoolean("Math.ffton", ffton);
	}

	public static final String[] format = { "Vrms", "dB" };
	public static final String[] scale = { "X1", "X2", "X5", "X10" };
	public static final int[] SCALES_VALUE = { 1, 2, 5, 10 };
	// public static final String[] dBPerDiv = { "1dB", "2dB", "5dB",
	// "10dB","20dB" };
	public static final int[] dBValuePerDiv = { 20, 10, 5, 2, 1 };

	public int fftvaluetype, fftscale;
	private int fftchl, fftwnd;

	public void c_setFFTchl(int fftchl) {
		this.fftchl = fftchl;
		cc.preSetChannelsForFFT(fftchl);
		wfm.getFFTView().updateYScaleRate();

		Submitable sbm = SubmitorFactory.reInit();
		sbm.c_fft(ffton, fftchl);
		sbm.apply();
	}

	public void setWaveFormManager(WaveFormManager wfm) {
		this.wfm = wfm;
	}

	public int getFFTchl() {
		return fftchl;
	}

	public int getFFTwnd() {
		return fftwnd;
	}

	public void setFFTwnd(int fftwnd) {
		this.fftwnd = fftwnd;
	}

	public String getFORMat() {
		return format[fftvaluetype];
	}

	private boolean ffton;

	public void c_setFFTon(boolean ffton) {
		if (this.ffton == ffton)
			return;

		this.ffton = ffton;
		/** 要更新采样率 */

		wfm.getFFTView().updateYScaleRate();
		// System.err.println("FFT "+ ffton);
		// if (FFTView.FFTNew) {
		cc.ontFFTSwitch(ffton, fftchl);
	}

	public void setFFTTimebaseIndex(int idx) {
		Platform.getMainWindow().getToolPane().getDetailPane()
				.changeFFTTimeBase(idx);
	}

	public int getFFTTimebaseIndex() {
		return cc.getFFTTimebaseIdx();
	}

	public BigDecimal getFFTTimebaseBD() {
		return cc.getBDFFTTimeBases();
	}

	public BigDecimal getFFTSampleRateBD() {
		BigDecimal bd = getFFTTimebaseBD();
		// System.out.println(bd);
		// fft频谱档位*10*2得到采样率
		return bd.multiply(VALUE_OF_20);
	}

	private static final BigDecimal VALUE_OF_20 = BigDecimal.valueOf(20);

	public boolean isFFTon() {
		return ffton;
	}

	public void selfSubmit(Submitable sbm) {
		sbm.c_fft(ffton, fftchl);

		// System.err.println(String.format("MFT: on%d chl_%d\n", (b ? 1 : 0),
		// fftchl));
	}

}