package com.owon.vds.calibration.stuff;

import com.owon.uppersoft.vds.core.aspect.base.Logable;
import com.owon.uppersoft.vds.core.tune.Cending;
import com.owon.uppersoft.vds.core.tune.ICal;

public class AbsArg implements Logable {
	protected final Cending cdn;
	protected final int[][] args;
	protected final ICal ic;
	protected int Unsign_Max_ADC;

	public final int id;
	public final int chl, vb;

	public AbsArg(int chl, int vb, ICal ic, int Unsign_Max_ADC, int defaultValue) {
		this.chl = chl;
		this.vb = vb;
		this.ic = ic;
		this.cdn = ic.cending();
		this.id = ic.getId();
		this.args = ic.getArgs();
		this.Unsign_Max_ADC = Unsign_Max_ADC;

		for (int i = 0; i < args.length; i++) {
			for (int j = 0; j < args[i].length; j++) {
				int v = args[i][j];
				if (v <= 0)
					args[i][j] = defaultValue;
			}
		}
		tries = 0;
		fitTimes = 0;
	}

	public int getValue() {
		return args[chl][vb];
	}

	public int getChannel() {
		return chl;
	}

	public String getType() {
		return ic.getType();
	}

	@Override
	public String toString() {
		return getType() + '[' + chl + "][" + vb + "]=" + getValue();
	}

	@Override
	public void log(Object o) {
		System.err.print(o);
	}

	@Override
	public void logln(Object o) {
		System.err.println(o);
	}

	/**
	 * 尝试修改次数
	 */
	protected int tries;

	protected int fitTimes;

	/**
	 * @return 是否判定为调试通过
	 */
	public boolean increaseFitTimes() {
		++fitTimes;
		logln("equalTimes = " + fitTimes);
		return isFitTimesPass();
	}

	/**
	 * @return 是否符合相等次数
	 */
	public boolean isFitTimesPass() {
		return fitTimes >= 2;
	}

	public void resetFitTimes() {
		fitTimes = 0;
	}

	/**
	 * @return 是否超次不再调试
	 */
	public boolean isTriesEnough() {
		return tries > 25;
	}
}