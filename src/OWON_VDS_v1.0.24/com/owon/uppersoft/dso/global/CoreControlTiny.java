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
		/** Peak detect processing is implemented entirely by the host, and only supports the case where the sampling rate can be raised */
		return isSamplePromtion();
	}

	private TinyTrgSubmitHandler ttsh = new TinyTrgSubmitHandler();

	@Override
	public double getTimePerPoint(int points) {
		int Vnum = points;
		// final double samp = 1000 / (double) Vnum;// 1000what is 1000?
		double timebase = getTimeControl().getBDTimebase().doubleValue();
		double timerange = timebase * GDefine.BlockNum;
		// System.err.println("timebase: ");
		// System.err.println(timerange);

		double timePerPoint;
		/**
		 * Used only by the current fft implementation
		 * 
		 * Frequency calculation: keep the normal approach, and distinguish when fft is enabled; when full at 2048 points, derive from the actual sampling rate (no compression and no interpolation, can be regarded as 1/point interval time),
		 * 
		 * With fewer than 2048 points, still use the original algorithm, and note to add *2048 / 2000
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