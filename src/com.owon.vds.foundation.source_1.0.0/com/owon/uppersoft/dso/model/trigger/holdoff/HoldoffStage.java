package com.owon.uppersoft.dso.model.trigger.holdoff;

import com.owon.uppersoft.dso.model.trigger.helper.ETV_Holdoff;
import com.owon.uppersoft.dso.model.trigger.helper.EnumLabelProvider;
import com.owon.uppersoft.vds.core.trigger.help.EnumNValue;
import com.owon.uppersoft.vds.util.format.SFormatter;

/**
 * 释抑不同的步进效果
 * 
 */
public enum HoldoffStage implements EnumLabelProvider {
	stg_xx_xus(0.1, "us", 1), // 0.1us~99.9us; 0.1us; 1~999
	stg_xxxus(1, "us", 0), // 100us~999us; 1us; 100~999
	stg_x_xxms(0.01, "ms", 2), // 1.00ms~9.99ms; 0.01ms; 100~999
	stg_xx_xms(0.1, "ms", 1), // 10.0ms~99.9ms; 0.1ms; 100~999
	stg_xxxms(1, "ms", 0), // 100ms~999ms; 1ms; 100~999
	stg_x_xxs(0.01, "s", 2), // 1.00s~9.99s; 0.01s; 100~999
	stg_xx_xs(0.1, "s", 1);// 10.0s; 0.1s; 100

	public static final int stg_xx_xus_min = 1, stg_xx_xus_asnS_max = 9,
			common_min = 100, common_max = 999;

	private double rate;
	private String unit;
	private int dotbacknum;
	private int cmp, div_100ns;

	private HoldoffStage(double rate, String unit, int dotbacknum) {
		this.rate = rate;
		this.unit = unit;
		this.dotbacknum = dotbacknum;

		int rank = ordinal();
		cmp = (int) Math.pow(10, rank + 3);
		div_100ns = (int) Math.pow(10, rank);
	}

	public static final HoldoffStage[] VALUES = values();

	public HoldoffStage previous() {
		int idx = ordinal() - 1;
		if (idx < 0)
			return this;
		return VALUES[idx];
	}

	public HoldoffStage next() {
		int idx = ordinal() + 1;
		if (idx >= VALUES.length)
			return this;
		return VALUES[idx];
	}

	// 加上step可能升级
	private static final int[] steps = { 1, 10, 100 };
	public static final int STEP_LENGTH = steps.length;

	private int getStep(int stepidx) {
		return steps[stepidx];
	}

	public int getMinStep() {
		return getStep(0);
	}

	/**
	 * @param bi
	 *            以100ns为单位
	 * @param etv
	 */
	public static void fromInt(int v, EnumNValue etv) {
		HoldoffStage hs = stg_xx_xus, tmp;
		int hov = 1;

		int cmp, div;
		while (true) {
			cmp = hs.cmp;
			div = hs.div_100ns;

			if (v < cmp) {
				hov = v / div;
				break;
			}
			tmp = hs;
			hs = hs.next();
			if (hs == tmp)
				return;
		}

		// while (true) {
		// hov = v / hs.div_100ns;
		// tmp = hs;
		// hs = hs.next();
		//
		// if (hov <= common_max)
		// break;
		//
		// if (hs == tmp)
		// return;
		// }

		/** 另一种写法，用于排错 */
		// if (v < 1000) {//1~999
		// hs = stg_xx_xus;
		// hov = v;
		// } else if (v < 10000) {//1000~9990
		// hs = stg_xxxus;
		// hov = v / 10;
		// } else if (v < 100000) {//10000~99900
		// hs = stg_x_xxms;
		// hov = v / 100;
		// } else if (v < 1000000) {//100000~999000
		// hs = stg_xx_xms;
		// hov = v / 1000;
		// } else if (v < 10000000) {//1000000~9990000
		// hs = stg_xxxms;
		// hov = v / 10000;
		// } else if (v < 100000000) {//10000000~99900000
		// hs = stg_x_xxs;
		// hov = v / 100000;
		// } else if (v < 1000000000) {//100000000~999000000
		// hs = stg_xx_xs;
		// hov = v / 1000000;
		// }
		etv.set(hs.ordinal(), hov);
	}

	/**
	 * 到100ns为单位的数值
	 * 
	 * @param hs
	 * @param vpart
	 * @return
	 */
	public static int ToInt(HoldoffStage hs, int vpart) {
		int r = hs.div_100ns * vpart;

		/** 另一种写法，用于排错 */
		// switch (hs) {
		// default:
		// case stg_xx_xus:
		// r = v;
		// break;
		// case stg_xxxus:
		// r = 10 * v;
		// break;
		// case stg_x_xxms:
		// r = 100 * v;
		// break;
		// case stg_xx_xms:
		// r = 1000 * v;
		// break;
		// case stg_xxxms:
		// r = 10000 * v;
		// break;
		// case stg_x_xxs:
		// r = 100000 * v;
		// break;
		// case stg_xx_xs:
		// r = 1000000 * v;
		// break;
		// }
		return r;
	}

	@Override
	public ETV_Holdoff getNext(int hov, int stepidx) {
		HoldoffStage stage = this;

		if (stage == stg_xx_xs)
			return null;
		hov = hov + getStep(stepidx);

		if (hov > common_max) {
			hov = common_min;
			stage = stage.next();
		}
		return HoldoffDelegate.createETV(stage, hov, stepidx);
	}

	@Override
	public ETV_Holdoff getPrevious(int hov, int stepidx) {
		// System.err.println(hov+","+getStep(stepidx));
		HoldoffStage stage = this;
		if (stage == stg_xx_xus) {
			if (hov == stg_xx_xus_min) {
				return null;
			}
			hov = hov - getStep(stepidx);
			if (hov < stg_xx_xus_min)
				hov = stg_xx_xus_min;
			return HoldoffDelegate.createETV(stage, hov, stepidx);
		}

		hov = hov - getStep(stepidx);
		if (hov < common_min) {
			stage = stage.previous();
			hov = common_max;
		}
		return HoldoffDelegate.createETV(stage, hov, stepidx);
	}

	@Override
	public int toInt(int vpart) {
		return ToInt(this, vpart);
	}

	@Override
	public String toString(int hov) {
		if (stg_xx_xus == this && hov >= stg_xx_xus_min
				&& hov <= stg_xx_xus_asnS_max) {
			return SFormatter.UIformat("%d ns", hov * 100);
		}
		double result = rate * hov;
		return SFormatter.UIformat("%." + dotbacknum + "f " + unit, result);
	}

}