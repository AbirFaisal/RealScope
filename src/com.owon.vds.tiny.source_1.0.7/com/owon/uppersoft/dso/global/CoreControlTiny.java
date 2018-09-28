package com.owon.uppersoft.dso.global;

import java.nio.ByteBuffer;

import com.owon.uppersoft.dso.model.WaveFormInfoControl;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.model.trigger.TriggerExtendDelegate;
import com.owon.uppersoft.dso.source.comm.effect.SubmitorFactory;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.aspect.control.Pos0_VBChangeInfluence;
import com.owon.uppersoft.vds.core.aspect.control.VoltageProvider;
import com.owon.uppersoft.vds.core.pref.Config;
import com.owon.uppersoft.vds.core.wf.WaveFormInfo;
import com.owon.uppersoft.vds.core.wf.rt.ChannelDataInfo;
import com.owon.uppersoft.vds.core.wf.rt.LoadMedia;
import com.owon.uppersoft.vds.source.comm.Submitor2;
import com.owon.uppersoft.vds.source.comm.data.ChannelDataInfo_Tiny;
import com.owon.uppersoft.vds.source.comm.ext.TinyTrgSubmitHandler;
import com.owon.uppersoft.vds.wf.WFI_tiny;

public class CoreControlTiny extends CoreControl {
	public CoreControlTiny(Config conf, Principle principle) {
		super(conf, principle);
	}

	@Override
	public boolean checkPeakDetectWork() {
		/** 峰值检测的处理完全由上位机实现，只支持采样率可以提升的情况 */
		return isSamplePromtion();
	}

	private TinyTrgSubmitHandler ttsh = new TinyTrgSubmitHandler();

	@Override
	public double getTimePerPoint(int points) {
		int Vnum = points;
		// final double samp = 1000 / (double) Vnum;// 1000是什么？
		double timebase = getTimeControl().getBDTimebase().doubleValue();
		double timerange = timebase * GDefine.BlockNum;
		// System.err.println("timebase: ");
		// System.err.println(timerange);

		double timePerPoint;
		/**
		 * 仅使用于当前的fft实现方式
		 * 
		 * 频率计算：保留普通时的做法，在fft开启时区分，当满2048点时使用实际采样率倒推(无压缩无插值，可视为1/点间隔时间)，
		 * 
		 * 不满2048点仍用原算法，注意加上*2048 / 2000
		 */
		timePerPoint = timerange / (double) Vnum;
		// System.err.println(timerange);
		// System.err.println(Hconst);

		return timePerPoint;
	}

	@Override
	protected TriggerControl createTriggerControl(int channelsNumber,
			TriggerExtendDelegate ted) {
		return new TriggerControl(channelsNumber, ted) {
			@Override
			public void selfSubmit() {
				ttsh.selfSubmit_Tiny(
						(Submitor2) SubmitorFactory.getSubmitable(), this);
			}
		};
	}

	@Override
	protected WaveFormInfoControl createWaveFormInfoControl(VoltageProvider vp,
			int channelsNumber, Pos0_VBChangeInfluence pvi) {
		return new WaveFormInfoControl(vp, channelsNumber, pvi) {
			@Override
			protected WaveForm createWaveForm(DataHouse dh,
					WaveFormInfo waveFormInfo) {
				return new WaveForm(dh, waveFormInfo) {
					@Override
					public ByteBuffer getFFT_Buffer() {
						return alldmbuf;
					}

					private ByteBuffer alldmbuf = null;

					@Override
					public void prepareRTNormalPaint(ChannelDataInfo cdi,
							LoadMedia cti, int yb) {
						super.prepareRTNormalPaint(cdi, cti, yb);

						if (cdi instanceof ChannelDataInfo_Tiny) {
							ChannelDataInfo_Tiny cdit = (ChannelDataInfo_Tiny) cdi;
							alldmbuf = cdit.getALLDMBuffer();
						} else {
							alldmbuf = null;
						}
					}
				};
			}

			@Override
			protected WaveFormInfo createWaveFormInfo(int i,
					VoltageProvider vp, Pos0_VBChangeInfluence pvi) {
				return new WFI_tiny(i, vp, pvi);
			}
		};
	}
}