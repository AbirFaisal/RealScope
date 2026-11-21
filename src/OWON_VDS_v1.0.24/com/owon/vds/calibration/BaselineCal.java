package com.owon.vds.calibration;

import java.nio.ByteBuffer;

import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.aspect.base.Logable;
import com.owon.vds.calibration.stuff.BaselineArg;
import com.owon.vds.calibration.stuff.ArgCreator;
import com.owon.vds.calibration.stuff.CalculatUtil;
import com.owon.vds.tiny.firm.pref.model.ArgType;

public class BaselineCal implements Logable, IWFCalRoutine {

	private WaveForm wf;
	private int chl;
	private int vbnum;

	private ArgCreator ac;

	/**
	 * @param wf
	 * @param vbnum
	 * @param ac
	 *            Uses ArgCreator to create the specific context under each calibration item; involves algorithm details
	 */
	public BaselineCal(WaveForm wf, int vbnum, ArgCreator ac) {
		this.wf = wf;
		this.chl = wf.getChannelNumber();
		this.vbnum = vbnum;

		this.ac = ac;
	}

	private BaselineArg arg;

	@Override
	public void getReady() {
		initNextZeroCompensation(0);
	}

	@Override
	public int routOut() {
		ByteBuffer bb = wf.getADC_Buffer();
		double average = CalculatUtil.computeAverage(bb);

		int result = rout(average);
		logln("result[" + chl + "] = " + result);
		return result;
	}

	@Override
	public int getRoutCalType() {
		if (arg != null)
			return arg.id;
		else
			return -1;
	}

	private void initNextZeroCompensation(int vb) {
		if (vb == 0)
			wf.setZeroYLoc(0, true, false);
		arg = ac.createBaselineArg(chl, vb, ArgType.Compensation);
		wf.setVoltBaseIndex(vb, false);
	}

	private void initNextZeroAmplitude(int vb) {
		if (vb == 0)
			wf.setZeroYLoc(CalibrationRunner.ZEROSTEPCAL_BASE, true, false);
		arg = ac.createBaselineArg(chl, vb, ArgType.Step);
		wf.setVoltBaseIndex(vb, false);
	}

	private int rout(double average) {
		logln("rout[" + chl + "] avg = " + average);
		if (arg != null) {
			logln(arg);
		}
		if (arg.id == ArgType.Compensation.ordinal()) {
			int pos0 = 0;

			if (arg.isAcceptable(pos0, average)) {
				if (!arg.increaseFitTimes())
					return ROUT_ZERO;

				nextZeroCompensation();
				return ROUT_ONE;
			} else {
				if (arg.isTriesEnough()) {
					logln("too long time to cal, break");
					nextZeroCompensation();
					return ROUT_ONE;
				}

				arg.resetFitTimes();

				/** No progress in the adc calculation result of this update; a new value needs to be tried */
				argTuneAgain(average, pos0);
				return ROUT_ZERO;
			}
		} else if (arg.id == ArgType.Step.ordinal()) {
			int pos0 = CalibrationRunner.ZEROSTEPCAL_BASE;

			if (arg.isAcceptable(pos0, average)) {
				if (!arg.increaseFitTimes())
					return ROUT_ZERO;

				nextZeroAmplitude();
				return ROUT_ONE;
			} else {
				if (arg.isTriesEnough()) {
					logln("too long time to cal, break");
					nextZeroAmplitude();
					return ROUT_ONE;
				}

				arg.resetFitTimes();

				/** No progress in the adc calculation result of this update; a new value needs to be tried */
				argTuneAgain(average, pos0);
				return ROUT_ZERO;
			}
		}
		return ROUT_DONE;
	}

	private void nextZeroAmplitude() {
		/** Progress in the adc calculation result of this update */
		int vb = arg.vb + 1;
		if (vb >= vbnum) {
			/** Calibration task finished */
			arg = null;
		} else {
			initNextZeroAmplitude(vb);
		}
	}

	private void nextZeroCompensation() {
		/** Progress in the adc calculation result of this update */
		int vb = arg.vb + 1;
		if (vb >= vbnum) {
			/** The calibration type has completed all calibration; switch to the next type */
			initNextZeroAmplitude(0);
		} else {
			initNextZeroCompensation(vb);
		}
	}

	private void argTuneAgain(double average, int pos0) {
		double del = average - pos0;
		// Can be optimized
		// if (average <= -125)
		// del = -201;
		// else if (average >= 125)
		// del = 101;
		arg.stepCrease(del, arg.vb);
		wf.setZeroYLoc(pos0, true, false);
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