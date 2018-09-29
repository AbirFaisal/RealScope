package com.owon.uppersoft.vds.function.rule;

import java.util.StringTokenizer;

import com.owon.uppersoft.vds.util.format.SFormatter;

public class RuleDetail {

	public int chl;
	public double hor, ver;

	public RuleDetail(int chl, double hor, double ver) {
		set(chl, hor, ver);
		// System.out.println(toString());
	}

	public static final RuleDetail getInstance(String t, int channelNumber) {
		if (t == null || t.length() == 0)
			return null;
		StringTokenizer tst = new StringTokenizer(t, ",");
		if (tst.countTokens() < 3) {
			return null;
		}
		int chl = Integer.valueOf(tst.nextToken().trim());
		if (chl >= channelNumber) {
			return null;
			// 为避免出错，直接改掉chl
			// chl = channelNumber - 1;
		}
		double hor = Double.valueOf(tst.nextToken().trim());
		double ver = Double.valueOf(tst.nextToken().trim());
		return new RuleDetail(chl, hor, ver);
	}

	public static final RuleDetail getInstanceWithAdjust(String t,
			int channelNumber) {
		if (t == null || t.length() == 0)
			return null;
		StringTokenizer tst = new StringTokenizer(t, ",");
		if (tst.countTokens() < 3) {
			return null;
		}
		int chl = Integer.valueOf(tst.nextToken().trim());
		if (chl >= channelNumber) {
			// return null;
			// 为避免出错，直接改掉chl
			chl = channelNumber - 1;
		}
		double hor = Double.valueOf(tst.nextToken().trim());
		double ver = Double.valueOf(tst.nextToken().trim());
		return new RuleDetail(chl, hor, ver);
	}

	public void set(int chl, double hor, double ver) {
		this.chl = chl;
		this.hor = hor;
		this.ver = ver;
	}

	@Override
	public String toString() {
		return SFormatter.UIformat("CH%d, H:%.2fdiv, V:%.2fdiv", chl + 1, hor, ver);
	}

	public String toPersist() {
		return  SFormatter.dataformat("%d,%f,%f", chl, hor, ver);
	}
}
