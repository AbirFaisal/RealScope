/**
 * 
 */
package com.owon.uppersoft.dso.model.trigger.condition;

import static com.owon.uppersoft.dso.model.trigger.condition.TrgConditionDelegate.common_max;
import static com.owon.uppersoft.dso.model.trigger.condition.TrgConditionDelegate.common_min;
import static com.owon.uppersoft.dso.model.trigger.condition.TrgConditionDelegate.common_ministep;
import static com.owon.uppersoft.dso.model.trigger.condition.TrgConditionDelegate.stg_x_xxus_max;
import static com.owon.uppersoft.dso.model.trigger.condition.TrgConditionDelegate.stg_x_xxus_ministep;
import static com.owon.uppersoft.dso.model.trigger.condition.TrgConditionDelegate.stg_xx0ns_max;
import static com.owon.uppersoft.dso.model.trigger.condition.TrgConditionDelegate.stg_xx0ns_min;
import static com.owon.uppersoft.dso.model.trigger.condition.TrgConditionDelegate.stg_xx0ns_ministep;

import com.owon.uppersoft.dso.model.trigger.helper.ETV_TrgConditon;
import com.owon.uppersoft.dso.model.trigger.helper.EnumLabelProvider;
import com.owon.uppersoft.vds.core.trigger.help.EnumNValue;
import com.owon.uppersoft.vds.util.format.SFormatter;

/**
 * TrgCondition, it only handles the order of magnitude, or unit part; the concrete value is not stored internally, and concrete values are of the same order of magnitude only within the same unit
 * 
 */
public enum TrgCondition implements EnumLabelProvider {
	stg_xx0ns(0.01, "us", 2, stg_xx0ns_min, stg_xx0ns_max, stg_xx0ns_ministep),
	// 30ns~990ns, 0.01us, 3~99
	stg_x_xxus(0.01, "us", 2, common_min, stg_x_xxus_max, stg_x_xxus_ministep),
	// 1.00us~9.98us, 0.02us, 100~998
	stg_xx_xus(0.1, "us", 1), // 10.0us~99.9us, 0.1us, 100~999
	stg_xxxus(1, "us", 0), // 100us~999us, 1us, 100~999
	stg_x_xxms(0.01, "ms", 2), // 1.00ms~9.99ms, 0.01ms, 100~999
	stg_xx_xms(0.1, "ms", 1), // 10.0ms~99.9ms, 0.1ms, 100~999
	stg_xxxms(1, "ms", 0), // 100ms~999ms, 1ms, 100~999
	stg_x_xxs(0.01, "s", 2), // 1.00s~9.99s, 0.01s, 100~999
	stg_xx_xs(0.1, "s", 1); // 10.0s

	private double rate;
	private String unit;
	private int dotbacknum;
	private int cmp, div_10ns;

	private int max, min;
	private int ministep;

	private TrgCondition(double rate, String unit, int dotbacknum, int min,
			int max, int ministep) {
		this.rate = rate;
		this.unit = unit;
		this.dotbacknum = dotbacknum;

		int rank = ordinal();
		cmp = (int) Math.pow(10, rank + 2);

		if (rank == 0 || rank == 1)
			div_10ns = 1;
		else
			div_10ns = (int) Math.pow(10, rank - 1);

		this.max = max;
		this.min = min;

		this.ministep = ministep;
	}

	private TrgCondition(double rate, String unit, int dotbacknum) {
		this(rate, unit, dotbacknum, common_min, common_max, common_ministep);
	}

	public int getMin() {
		return min;
	}

	/** The internal settings may change with the model */
	private void setMinistep(int ministep) {
		this.ministep = ministep;
	}

	private void setMin(int min) {
		this.min = min;
	}

	private void setMax(int max) {
		this.max = max;
	}

	/***/

	public static void resumeDefaultSet() {
		stg_xx0ns.ministep = stg_xx0ns_ministep;
		stg_xx0ns.min = stg_xx0ns_min;
		stg_xx0ns.max = stg_xx0ns_max;
	}

	private static void resumeSet1() {
		stg_xx0ns.ministep = 2;
		stg_xx0ns.min = 6;
		stg_xx0ns.max = 98;
	}

	public static void resumeSet2() {
		stg_x_xxus.ministep = 1;
		stg_x_xxus.min = 1;
		stg_x_xxus.max = 999;
	}

	public static final TrgCondition[] VALUES = values();

	public TrgCondition previous() {
		int idx = ordinal() - 1;
		if (idx < 0)
			return this;
		return VALUES[idx];
	}

	public TrgCondition next() {
		int idx = ordinal() + 1;
		if (idx >= VALUES.length)
			return this;
		return VALUES[idx];
	}

	// adding step may move up a level
	private static final int[] steps = { 1, 10, 100 };
	public static final int STEP_LENGTH = steps.length;

	private int getStep(int stepidx) {
		if (stepidx == 0)
			return ministep;
		return steps[stepidx];
	}

	public int getMinStep() {
		return getStep(0);
	}

	/**
	 * @param bi
	 *            in units of 10ns
	 * @param etv
	 */
	public static void fromInt(int v, EnumNValue etv) {
		TrgCondition hs = stg_xx0ns, tmp;
		int hov = 1;

		int cmp, div;
		while (true) {
			cmp = hs.cmp;
			div = hs.div_10ns;

			if (v >= 1000000000) {// 1000000000~9990000000
				hs = stg_xx_xs;
				hov = v / 100000000;
				break;
			}

			if (v < cmp) {
				hov = v / div;
				break;
			}
			tmp = hs;
			hs = hs.next();
			if (hs == tmp)
				return;
		}

		/** Another way of writing it, for debugging */
		// if (v < 100) {// 3~99
		// hs = stg_xx0ns;
		// hov = v;
		// } else if (v <= stg_x_xxus_max) {// 100~998
		// hs = stg_x_xxus;
		// hov = v;
		// } else if (v < 10000) {// 1000~9990
		// hs = stg_xx_xus;
		// hov = v / 10;
		// } else if (v < 100000) {// 10000~99900
		// hs = stg_xxxus;
		// hov = v / 100;
		// } else if (v < 1000000) {// 100000~999000
		// hs = stg_x_xxms;
		// hov = v / 1000;
		// } else if (v < 10000000) {// 1000000~9990000
		// hs = stg_xx_xms;
		// hov = v / 10000;
		// } else if (v < 100000000) {// 10000000~99900000
		// hs = stg_xxxms;
		// hov = v / 100000;
		// } else if (v < 1000000000) {// 100000000~999000000
		// hs = stg_x_xxs;
		// hov = v / 1000000;
		// } else if (v >= 1000000000) {// 1000000000~9990000000
		// hs = stg_xx_xs;
		// hov = v / 100000000;
		// }

		/** Limit by the minimum value when getting the value */
		if (hov < hs.min)
			hov = hs.min;
		etv.set(hs.ordinal(), hov);
	}

	/**
	 * Converted to a value in units of 10ns
	 * 
	 * @param hs
	 * @param vpart
	 * @return
	 */
	public static int ToInt(TrgCondition hs, int vpart) {
		int r = hs.div_10ns * vpart;
		// System.err.println("static: "+r);
		/** Another way of writing it, for debugging */
		// switch (hs) {
		// default:
		// case stg_xx0ns:
		// r = v;
		// break;
		// case stg_x_xxus:
		// r = v;
		// break;
		// case stg_xx_xus:
		// r = 10 * v;
		// break;
		// case stg_xxxus:
		// r = 100 * v;
		// break;
		// case stg_x_xxms:
		// r = 1000 * v;
		// break;
		// case stg_xx_xms:
		// r = 10000 * v;
		// break;
		// case stg_xxxms:
		// r = 100000 * v;
		// break;
		// case stg_x_xxs:
		// r = 1000000 * v;
		// break;
		// case stg_xx_xs:
		// r = 10000000 * v;
		// break;
		// }
		return r;
	}

	@Override
	public ETV_TrgConditon getNext(int hov, int stepidx) {
		TrgCondition stage = this;

		if (stage == stg_xx_xs)
			return null;
		hov = hov + getStep(stepidx);

		if (hov > stage.max) {
			stage = stage.next();
			hov = stage.min;
		}

		// if (stage == stg_xx0ns) {
		// if (hov > stg_xx0ns_max) {
		// hov = common_min;
		// stage = stage.next();
		// }
		// } else if (stage == stg_x_xxus) {
		// if (hov > stg_x_xxus_max) {
		// hov = common_min;
		// stage = stage.next();
		// }
		// } else if (hov > common_max) {
		// hov = common_min;
		// stage = stage.next();
		// }
		return TrgConditionDelegate.createETV(stage, hov, stepidx);
	}

	@Override
	public ETV_TrgConditon getPrevious(int hov, int stepidx) {
		TrgCondition stage = this;
		TrgCondition stage2 = stage;

		hov = hov - getStep(stepidx);
		if (hov < stage.min) {
			stage = stage.previous();
			if (stage == stage2)
				return null;
			hov = stage.max;
		}
		// if (stage == stg_xx0ns) {
		// if (hov == stg_xx0ns_min) {
		// return null;
		// }
		// hov = hov - getStep(stepidx);
		// if (hov < stg_xx0ns_min)
		// hov = stg_xx0ns_min;
		// return TrgConditionDelegate.createETV(stage, hov, stepidx);
		// }
		//
		// hov = hov - getStep(stepidx);
		// if (hov < common_min) {
		// stage = stage.previous();
		// if (stage == stg_x_xxus) {
		// hov = stg_x_xxus_max;
		// } else if (stage == stg_xx0ns) {
		// hov = stg_xx0ns_max;
		// } else {
		// hov = common_max;
		// }
		// }
		return TrgConditionDelegate.createETV(stage, hov, stepidx);
	}

	@Override
	public int toInt(int vpart) {
		return ToInt(this, vpart);
	}

	@Override
	public String toString(int hov) {
		if (stg_xx0ns == this) {
			return SFormatter.UIformat("%d ns", hov * 10);
		}
		double result = rate * hov;
		return SFormatter.UIformat("%." + dotbacknum + "f " + unit, result);
	}

}