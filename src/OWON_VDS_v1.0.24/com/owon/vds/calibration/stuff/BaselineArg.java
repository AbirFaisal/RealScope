package com.owon.vds.calibration.stuff;

import com.owon.uppersoft.vds.core.tune.Cending;
import com.owon.uppersoft.vds.core.tune.ICal;

/**
 * Holds the content of the parameter to be tuned, and the settings for how tuning is done in detail
 * 
 */
public class BaselineArg extends AbsArg {

	public BaselineArg(int chl, int vb, ICal ic, int Unsign_Max_ADC,
			int defaultValue) {
		super(chl, vb, ic, Unsign_Max_ADC, defaultValue);
	}

	/**
	 * @param del2Standard
	 *            formerly named shouldIncrease
	 */
	public void stepCrease(double del2Standard, int vbidx) {
		int sign = 0;
		if (del2Standard < 0) {
			sign = (cdn == Cending.ascending ? 1 : -1);
		} else {
			sign = (cdn == Cending.ascending ? -1 : 1);
		}
		/** For special devices the ratio between the parameter and the value is 1:1 */
		int step = (int) Math.abs(del2Standard);
		computeNapply(sign, step, vbidx);
		tries++;
		logln("arg[" + chl + "][" + vb + "] = " + args[chl][vb]);
	}

	/**
	 * Assuming the increment by which the tuning parameter changes is known, set it this way; subclasses can inherit and implement how the increment is computed
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

		/** When the short value overflows, re-clamp it and replenish the number of allowed attempts */
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
	 * Dedicated to baseline tuning
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