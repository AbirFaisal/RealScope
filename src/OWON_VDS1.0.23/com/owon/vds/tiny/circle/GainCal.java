package com.owon.vds.tiny.circle;

import java.nio.ByteBuffer;

import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.aspect.base.Logable;
import com.owon.vds.calibration.CalibrationRunner;
import com.owon.vds.calibration.stuff.ArgCreator;
import com.owon.vds.calibration.stuff.CalculatUtil;

public class GainCal implements Logable, IWFCalRoutine2 {

	private WaveForm wf;
	private int chl;
	private int vbnum;

	private ArgCreator ac;
	private AGPControl agp;

	/**
	 * @param wf
	 * @param vbnum
	 * @param ac
	 *            使用ArgCreator来创建每个不同校正条目下的具体上下文，涉及算法细节量
	 */
	public GainCal(WaveForm wf, int vbnum, ArgCreator ac, AGPControl agp) {
		this.wf = wf;
		this.chl = wf.getChannelNumber();
		this.vbnum = vbnum;
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
		ByteBuffer bb = wf.getADC_Buffer();
		TopBaseCalResult tb = CalculatUtil.computeTopBase(bb);

		int result = rout(tb);
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

	private void initNextGain(int vb) {
		logln("initNextGain " + vb);
		if (vb == 0)
			wf.setZeroYLoc(0, true, false);

		arg = ac.createGainArg(chl, vb);
		wf.setVoltBaseIndex(vb, false);

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
				wf.setZeroYLoc(zeroPosition, true, false);
				baseCt = 0;
			} else if (tb.base < -105) {
				baseCt++;
				zeroPosition = baseCt * 20;
				wf.setZeroYLoc(zeroPosition, true, false);
				TopCt = 0;
			}
		} else {
			TopCt = 0;
			baseCt = 0;
			zeroPosition = 0;
			wf.setZeroYLoc(0, true, false);
		}
		tempVb = arg.vb;
	}

	/**
	 * <code>
	 * 1. 先验证一个电压档位下算法实现
	 * 2. 验证agp初始化工作是否就绪，并获取第一次的tb结果(TopBaseCalResult)
	 * 3. 计算幅度偏差与参数偏差直接的增量关系，研究算法实现
	 * 4. 使用方法组合在该电压档位下校正至正确
	 * 5. 根据其它电压档位下的增量关系不同适配算法的其它情况，并逐一验证
	 * </code>
	 */
	private int rout(TopBaseCalResult tb) {
		logln("rout[" + chl + "] tb = " + tb);
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

			/** 本次更新adc计算结果无进展，需要尝试新的值 */
			argTuneAgain(amp);
			return ROUT_ZERO;
		}

	}

	protected void nextGain() {
		/** 本次更新adc计算结果有进展 */
		int vb = arg.vb + 1;
		if (vb >= vbnum) {
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
		wf.setVoltBaseIndex(arg.vb, false);
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