package com.owon.vds.calibration.stuff;

import com.owon.uppersoft.vds.core.tune.Cending;
import com.owon.uppersoft.vds.core.tune.ICal;

/**
 * Save the contents of the parameters that need to be debugged,
 * as well as the details of the debugging settings
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
		/** The ratio of special devices to parameters and values is 1:1. */
		int step = (int) Math.abs(del2Standard);
		computeNapply(sign, step, vbidx);
		tries++;
		logln("arg[" + chl + "][" + vb + "] = " + args[chl][vb]);
	}

	/**
	 * Assume that the increment of the debugging parameter change is known,
	 * and the setting is made; the subclass inherits how the implementation calculates the increment.
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

		/** When the value short overflows, the limit is imposed and the number of attempts is replenished. */
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
	 * Dedicated to adjusting the baseline
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