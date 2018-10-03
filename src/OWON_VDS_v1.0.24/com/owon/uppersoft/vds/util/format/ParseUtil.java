package com.owon.uppersoft.vds.util.format;

import java.math.BigDecimal;

public class ParseUtil {
	private static final BigDecimal VALUE_OF_1G = BigDecimal
			.valueOf(1000000000);
	private static final BigDecimal VALUE_OF_1M = BigDecimal.valueOf(1000000);
	private static final BigDecimal VALUE_OF_1k = BigDecimal.valueOf(1000);

	/**
	 * 去引号
	 * 
	 * @param org
	 * @return
	 */
	public static final String trimQuotes(String org) {
		org = org.trim();
		int last = org.length() - 1;
		if (org.charAt(0) == '"' && org.charAt(last) == '"')
			return org.substring(1, last);
		return org;
	}

	/**
	 * km数值
	 * 
	 * @param org
	 * @return
	 */
	public static final int translate_KM(String org) {
		org = org.trim();
		int last = org.length() - 1;
		char u = org.charAt(last);
		int v = -1;
		try {
			v = Integer.parseInt(org.substring(0, last));
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return v;
		}
		if (u == 'K' || u == 'k') {
			v = v * 1000;
		} else if (u == 'm' || u == 'M') {
			v = v * 1000000;
		}
		return v;
	}

	/**
	 * kmg的hz
	 * 
	 * @param org
	 * @return
	 */
	public static final BigDecimal translate_KMG(String org) {
		org = org.trim();
		int last = org.indexOf(' ') - 1;
		if (last < 0)
			last = org.toLowerCase().indexOf('h') - 1;
		char u = org.charAt(last);
		// System.out.println(u);

		if (Character.isDigit(u)) {
			last += 1;
		}
		String orgDigit = org.substring(0, last);
		// System.out.println(orgDigit);
		BigDecimal v = new BigDecimal(orgDigit);
		if (u == 'k' || u == 'K') {
			v = v.multiply(VALUE_OF_1k);
		} else if (u == 'm' || u == 'M') {
			v = v.multiply(VALUE_OF_1M);
		} else if (u == 'g' || u == 'G') {
			v = v.multiply(VALUE_OF_1G);
		}
		return v;
	}

	/**
	 * kmg的数值
	 * 
	 * @param org
	 * @return
	 */
	public static final BigDecimal translate_KMGValue(String org) {
		org = org.trim();
		String orgDigit;
		int last = org.length() - 1;
		char u = org.charAt(last);
		// System.err.println(org+", "+u);
		if (!Character.isDigit(u)) {
			orgDigit = org.substring(0, last);
		} else
			orgDigit = org;
		// System.err.println(orgDigit);
		BigDecimal v = new BigDecimal(orgDigit);
		if (u == 'k' || u == 'K') {
			v = v.multiply(VALUE_OF_1k);
		} else if (u == 'm' || u == 'M') {
			v = v.multiply(VALUE_OF_1M);
		} else if (u == 'g' || u == 'G') {
			v = v.multiply(VALUE_OF_1G);
		}
		return v;
	}

	public static final int getInStringArray(String[] sa, String v) {
		v = v.trim();
		int j = 0;
		for (String s : sa) {
			if (v.equalsIgnoreCase(s))
				return j;
			j++;
		}

		return -1;
	}

	/**
	 * 以秒为单位的准确的浮点数
	 * 
	 * @param org
	 * @return
	 */
	public static final BigDecimal translate_numS(String org) {
		org = org.trim();
		int last_1 = org.length() - 2;
		char u = org.charAt(last_1);
		int v = -1;
		if (Character.isDigit(u)) {
			last_1++;
			try {
				v = Integer.parseInt(org.substring(0, last_1));
				return BigDecimal.valueOf(v);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return BigDecimal.valueOf(-1);
			}
		}
		try {
			v = Integer.parseInt(org.substring(0, last_1));
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return BigDecimal.valueOf(-1);
		}
		if (u == 'm' || u == 'M') {
			return BigDecimal.valueOf(v).divide(VALUE_OF_1k);
		} else if (u == 'u' || u == 'U') {
			return BigDecimal.valueOf(v).divide(VALUE_OF_1M);
		} else if (u == 'n' || u == 'N') {
			return BigDecimal.valueOf(v).divide(VALUE_OF_1G);
		} else
			return BigDecimal.valueOf(-1);
	}

	public static final int translateInt(String org) {
		org = org.trim();
		int v = -1;
		try {
			v = Integer.parseInt(org);
			return v;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return v;
		}
	}

}
