package com.owon.vds.tiny.circle;

import com.owon.uppersoft.vds.core.tune.Cending;
import com.owon.uppersoft.vds.core.tune.ICal;
import com.owon.vds.calibration.stuff.AbsArg;

public class GainArg extends AbsArg {

	private static final int Max = 150;

	public GainArg(int chl, int vb, ICal ic, int Unsign_Max_ADC,
			int defaultValue) {
		super(chl, vb, ic, Unsign_Max_ADC, defaultValue);
	}

	public boolean isTopBaseFit(int amp) {
		// 25*6
		return Math.abs(Max - amp) == 0;
	}

	/**
	 * @return Whether it is equal to
	 */
	public boolean isFitTimesPass() {
		return fitTimes >= 20;
	}

	/**
	 * @param del2Standard
	 *            原名shouldIncrease
	 */
	public void stepCrease(int amp, int vbidx) {
		int sign = 0;

		int del = 150 - amp;
		if (del > 0) {
			sign = (cdn == Cending.ascending ? 1 : -1);
		} else {
			sign = (cdn == Cending.ascending ? -1 : 1);
		}
		/** The ratio of special devices to parameters and values is 1:1. */
		int step = (int) Math.abs(del);
		computeNapply(sign, step, vbidx);
		tries++;
		logln("arg[" + chl + "][" + vb + "] = " + args[chl][vb]);
	}

	/**
	 * Assume that the increment of the debugging parameter change is known,
	 * and the setting is made; the subclass inherits how the implementation
	 * calculates the increment.
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

		/** When the value short overflows, the limit is imposed
		 *  and the number of attempts is replenished.
		 *  */
		if (v < 0) {
			v = Unsign_Max_ADC + v;
			tries -= 10;
		} else if (v > Unsign_Max_ADC) {
			v = v - Unsign_Max_ADC;
			tries -= 10;
		}
		args[chl][vb] = v;
	}
}