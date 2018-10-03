package com.owon.vds.tiny.circle;

import java.nio.ByteBuffer;

import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.aspect.base.Logable;
import com.owon.vds.calibration.stuff.ArgCreator;
import com.owon.vds.calibration.stuff.CalculatUtil;

public class GainCal implements Logable, IWFCalRoutine2 {

	private WaveForm waveForm;
	private int channel;
	private int voltageBaseValue;

	private ArgCreator ac;
	private AGPControl agp;

	/**
	 * @param waveForm
	 * @param voltageBaseValue
	 * @param ac
	 *            Use ArgCreator to create a specific context under each different
	 *            correction entry, involving the amount of algorithmic detail.
	 */
	public GainCal(WaveForm waveForm, int voltageBaseValue, ArgCreator ac, AGPControl agp) {
		this.waveForm = waveForm;
		this.channel = waveForm.getChannelNumber();
		this.voltageBaseValue = voltageBaseValue;
		this.agp = agp;

		this.ac = ac;
	}

	private GainArg arg;

	@Override
	public void getReady() {
	}

	@Override
	public void forVB(int vb) {
		initNextGain(vb);
	}

	@Override
	public int routOut() {
		ByteBuffer bb = waveForm.getADC_Buffer();
		TopBaseCalResult tb = CalculatUtil.computeTopBase(bb);

		int result = rout(tb);
		logln("result[" + channel + "] = " + result);
		return result;
	}

	@Override
	public int getRoutCalType() {
		if (arg != null)
			return arg.id;
		else
			return -1;
	}

	private void initNextGain(int vb) {
		logln("initNextGain " + vb);
		if (vb == 0)
			waveForm.setZeroYLoc(0, true, false);

		arg = ac.createGainArg(channel, vb);
		waveForm.setVoltBaseIndex(vb, false);

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// agp.genWFwithVB(vb);
	}

	private int TopCt = 0, baseCt = 0, tempVb, zeroPosition;

	public void setZero(TopBaseCalResult tb) {
		if (tempVb == arg.vb && arg.vb <= 1) {
			if (tb.top > 105) {
				TopCt++;
				zeroPosition = TopCt * -20;
				waveForm.setZeroYLoc(zeroPosition, true, false);
				baseCt = 0;
			} else if (tb.base < -105) {
				baseCt++;
				zeroPosition = baseCt * 20;
				waveForm.setZeroYLoc(zeroPosition, true, false);
				TopCt = 0;
			}
		} else {
			TopCt = 0;
			baseCt = 0;
			zeroPosition = 0;
			waveForm.setZeroYLoc(0, true, false);
		}
		tempVb = arg.vb;
	}

	/**
	 * <code>
	 * 1. First verify the algorithm implementation under a voltage gear
	 * 2. Verify that the agp initialization is ready and get the first tb result (TopBaseCalResult)
	 * 3. Calculate the direct incremental relationship between the amplitude deviation and the parameter
	 * deviation, and study the algorithm implementation.
	 * 4. Use method combination to correct to correct under this voltage position
	 * 5. According to the incremental relationship under other voltage positions, the other conditions
	 * of the algorithm are adapted and verified one by one.
	 * </code>
	 */
	private int rout(TopBaseCalResult tb) {
		logln("rout[" + channel + "] tb = " + tb);
		if (arg != null) {
			logln(arg);
		}

		// if (1 == 1) {
		// nextGain();
		// return ROUT_ONE;
		// }
		setZero(tb);
		int amp = tb.amp;

		if (arg.isTopBaseFit(amp)) {
			if (!arg.increaseFitTimes())
				return ROUT_ZERO;

			// nextGain();
			arg = null;
			return ROUT_ONE;
		} else {
			if (arg.isTriesEnough()) {
				logln("too long time to cal, break");
				// nextGain();
				arg = null;
				return ROUT_ONE;
			}

			arg.resetFitTimes();

			/** There is no progress in updating the adc calculation results, you need to try new values. */
			argTuneAgain(amp);
			return ROUT_ZERO;
		}

	}

	protected void nextGain() {
		/** 本次更新adc计算结果有进展 */
		int vb = arg.vb + 1;
		if (vb >= voltageBaseValue) {
			/** 校正任务终结 */
			arg = null;
		} else {
			initNextGain(vb);
		}
	}

	private void argTuneAgain(int amp) {
		// 可优化
		// if (average <= -125)
		// del = -201;
		// else if (average >= 125)
		// del = 101;
		arg.stepCrease(amp, arg.vb);
		waveForm.setVoltBaseIndex(arg.vb, false);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void log(Object o) {
		System.err.print(o);
	}

	@Override
	public void logln(Object o) {
		System.err.println(o);
	}
}