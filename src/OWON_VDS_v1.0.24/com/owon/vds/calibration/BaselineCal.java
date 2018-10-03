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
	 *            使用ArgCreator来创建每个不同校正条目下的具体上下文，涉及算法细节量
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

				/** 本次更新adc计算结果无进展，需要尝试新的值 */
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

				/** 本次更新adc计算结果无进展，需要尝试新的值 */
				argTuneAgain(average, pos0);
				return ROUT_ZERO;
			}
		}
		return ROUT_DONE;
	}

	private void nextZeroAmplitude() {
		/** 本次更新adc计算结果有进展 */
		int vb = arg.vb + 1;
		if (vb >= vbnum) {
			/** 校正任务终结 */
			arg = null;
		} else {
			initNextZeroAmplitude(vb);
		}
	}

	private void nextZeroCompensation() {
		/** 本次更新adc计算结果有进展 */
		int vb = arg.vb + 1;
		if (vb >= vbnum) {
			/** 校正类型完成全部校正，切换到下一类型 */
			initNextZeroAmplitude(0);
		} else {
			initNextZeroCompensation(vb);
		}
	}

	private void argTuneAgain(double average, int pos0) {
		double del = average - pos0;
		// 可优化
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