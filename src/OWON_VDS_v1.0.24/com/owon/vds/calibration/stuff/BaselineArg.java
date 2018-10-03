package com.owon.vds.calibration.stuff;

import com.owon.uppersoft.vds.core.tune.Cending;
import com.owon.uppersoft.vds.core.tune.ICal;

/**
 * 保存需要进行调试的参数的内容，以及调试的细节方式设置
 * 
 */
public class BaselineArg extends AbsArg {

	public BaselineArg(int chl, int vb, ICal ic, int Unsign_Max_ADC,
			int defaultValue) {
		super(chl, vb, ic, Unsign_Max_ADC, defaultValue);
	}

	/**
	 * @param del2Standard
	 *            原名shouldIncrease
	 */
	public void stepCrease(double del2Standard, int vbidx) {
		int sign = 0;
		if (del2Standard < 0) {
			sign = (cdn == Cending.ascending ? 1 : -1);
		} else {
			sign = (cdn == Cending.ascending ? -1 : 1);
		}
		/** 特殊器件在参数和数值上的比例是1:1 */
		int step = (int) Math.abs(del2Standard);
		computeNapply(sign, step, vbidx);
		tries++;
		logln("arg[" + chl + "][" + vb + "] = " + args[chl][vb]);
	}

	/**
	 * 假设已知调试参数改变的增量，进行如此设置；子类可继承实现如何计算得到增量
	 * 
	 * @param sign
	 * @param step
	 * @param vbidx
	 */
	protected void computeNapply(int sign, int step, int vbidx) {
		int v = args[chl][vb];

		if (step < 1)
			step = 1;

		if (sign > 0)
			v += step;
		else
			v -= step;

		/** 当数值short溢出时进行重限位，并补充可尝试次数 */
		if (v < 0) {
			v = Unsign_Max_ADC + v;
			tries -= 10;
		} else if (v > Unsign_Max_ADC) {
			v = v - Unsign_Max_ADC;
			tries -= 10;
		}
		args[chl][vb] = v;
	}

	/**
	 * 专用于调基线
	 * 
	 * @param standard
	 * @param result
	 * @return
	 */
	public boolean isAcceptable(double standard, double result) {
		System.out.println("result=" + Math.abs(result - standard));
		return Math.abs(result - standard) < 0.3;// VDS1022 <0.7 ,VDS2052<0.3
	}

}