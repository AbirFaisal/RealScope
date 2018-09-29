package com.owon.uppersoft.vds.core.sample;

import java.math.BigDecimal;

import com.owon.uppersoft.vds.util.format.ParseUtil;

public enum SampleRate {
	SAMPLE_3_2G, /***/
	SAMPLE_2G, /***/
	SAMPLE_1_6G, /***/
	SAMPLE_1G, /***/
	SAMPLE_800M, /***/

	SAMPLE_500M, /***/
	SAMPLE_400M, /***/
	SAMPLE_250M, /***/
	SAMPLE_200M, /***/
	SAMPLE_125M, /***/
	SAMPLE_100M, /***/
	SAMPLE_50M, /***/
	SAMPLE_25M, /***/

	SAMPLE_12_5M, /***/
	SAMPLE_10M, /***/
	SAMPLE_5M, /***/
	SAMPLE_2_5M, /***/
	SAMPLE_1_25M, /***/
	SAMPLE_1M, /***/
	SAMPLE_500K, /***/

	SAMPLE_250K, /***/
	SAMPLE_125K, /***/
	SAMPLE_100K, /***/
	SAMPLE_50K, /***/
	SAMPLE_25K, /***/
	SAMPLE_12_5K, /***/
	SAMPLE_10K, /***/

	SAMPLE_5K, /***/
	SAMPLE_2_5K, /***/
	SAMPLE_1_25K, /***/
	SAMPLE_1K, /***/
	SAMPLE_500, /***/
	SAMPLE_250, /***/

	SAMPLE_125, /***/
	SAMPLE_100, /***/
	SAMPLE_50, /***/
	SAMPLE_25, /***/
	SAMPLE_12_5, /***/
	SAMPLE_10, /***/
	SAMPLE_5, /***/

	SAMPLE_2_5, /***/
	SAMPLE_1_25, /***/
	SAMPLE_1, /***/
	SAMPLE_0_5/***/
	;
	private BigDecimal bdvalue;
	private String label;

	public static final String sampleRateUpdated = "sampleRateUpdated";
	
	private SampleRate() {
		String n = name();
		// System.err.println(n);
		n = n.substring("SAMPLE_".length());
		n = n.replace("_", ".");
		// System.err.println(n);
		bdvalue = ParseUtil.translate_KMGValue(n);
		bdvalue = bdvalue.divide(BigDecimal.valueOf(1000));
		// System.err.println(bdvalue);
		// System.err.println();
		label = '(' + n + "S/s)";
		// System.err.println(label);
	}

	public String getSampleRateTxt() {
		return label;
	}

	/**
	 * @return kHz
	 */
	public BigDecimal getBDValue_kHz() {
		return bdvalue;
	}

	public BigDecimal getBDValue_Hz() {
		return bdvalue.multiply(BigDecimal.valueOf(1000));
	}

	/**
	 * 满屏数 = 采样率*时基*20
	 * 
	 * @param tb
	 *            单位为S
	 * @return
	 */
	public int compute(BigDecimal tb, int blockNum) {
		BigDecimal sample = bdvalue;// kS/s
		BigDecimal v = sample.multiply(tb);
		v = v.multiply(BigDecimal.valueOf(blockNum * 1000));
		return v.intValue();
	}

	public static void main(String[] args) {
		// System.err.println(new BigDecimal("5.0E+5").compareTo(BigDecimal
		// .valueOf(500000)));
		getSampleRateByDiv1000(new BigDecimal("5.0E+8"));
		// new BigDecimal("250000")//BigDecimal.valueOf(100000)
		if (true)
			return;
		// for (SampleRate sr : values()) {
		// System.out.println(sr + "\\\t" + sr.ordinal());
		// }
	}

	private static final BigDecimal[] bdSamplesALL = {
			// 以kS/s为单位
			BigDecimal.valueOf(3200000), BigDecimal.valueOf(2000000),
			BigDecimal.valueOf(1600000), BigDecimal.valueOf(1000000),

			BigDecimal.valueOf(800000), BigDecimal.valueOf(500000),
			BigDecimal.valueOf(400000), BigDecimal.valueOf(250000),
			BigDecimal.valueOf(200000), BigDecimal.valueOf(125000),
			BigDecimal.valueOf(100000), BigDecimal.valueOf(50000),
			BigDecimal.valueOf(25000), BigDecimal.valueOf(12500),
			BigDecimal.valueOf(10000), BigDecimal.valueOf(5000),
			BigDecimal.valueOf(2500), BigDecimal.valueOf(1250),
			BigDecimal.valueOf(1000),

			BigDecimal.valueOf(500), BigDecimal.valueOf(250),
			BigDecimal.valueOf(125), BigDecimal.valueOf(100),
			BigDecimal.valueOf(50), BigDecimal.valueOf(25),
			new BigDecimal("12.5"), BigDecimal.valueOf(10),
			BigDecimal.valueOf(5), new BigDecimal("2.5"),
			new BigDecimal("1.25"), BigDecimal.valueOf(1),

			new BigDecimal("0.5"), new BigDecimal("0.25"),
			new BigDecimal("0.125"), new BigDecimal("0.1"),
			new BigDecimal("0.05"), new BigDecimal("0.025"),
			new BigDecimal("0.0125"), new BigDecimal("0.01"),
			new BigDecimal("0.005"), new BigDecimal("0.0025"),
			new BigDecimal("0.00125"), new BigDecimal("0.001"),
			new BigDecimal("0.0005")

	};

	public static final SampleRate getSampleRateByDiv1000(BigDecimal bd) {
		bd = bd.divide(BigDecimal.valueOf(1000));
		return getSampleRate(bd);
	}

	public static final SampleRate getSampleRate(BigDecimal bd) {
		for (SampleRate sr : VALUES) {
			if (bd.compareTo(sr.bdvalue) == 0) {
				// System.out.println(i);
				return sr;
			}
		}
		System.err.println("err:" + bd);
		return null;
	}

	public static final SampleRate[] VALUES = values();

	private static final String[] SamplesALL = { "(3.2GS/s)", // SAMPLE_3_2G,
			"(2GS/s)", // SAMPLE_2G,
			"(1.6GS/s)", // SAMPLE_1_6G,
			"(1GS/s)", // SAMPLE_1G,
			"(800MS/s)", // SAMPLE_800M,
			"(500MS/s)", // SAMPLE_500M,
			"(400MS/s)", // SAMPLE_400M,
			"(250MS/s)", // SAMPLE_250M,
			"(200MS/s)", // SAMPLE_200M,
			"(125MS/s)", // SAMPLE_125M,
			"(100MS/s)", // SAMPLE_100M,
			"(50MS/s)", // SAMPLE_50M,
			"(25MS/s)", // SAMPLE_25M,
			"(12.5MS/s)", // SAMPLE_12_5M,
			"(10MS/s)", // SAMPLE_10M,
			"(5MS/s)", // SAMPLE_5M,
			"(2.5MS/s)", // SAMPLE_2_5M,
			"(1.25MS/s)", // SAMPLE_1_25M,
			"(1MS/s)", // SAMPLE_1M,
			"(500KS/s)", // SAMPLE_500K,
			"(250KS/s)", // SAMPLE_250K,
			"(125KS/s)", // SAMPLE_125K,
			"(100KS/s)", // SAMPLE_100K,
			"(50KS/s)", // SAMPLE_50K,
			"(25KS/s)", // SAMPLE_25K,
			"(12.5KS/s)", // SAMPLE_12_5K,
			"(10KS/s)", // SAMPLE_10K,
			"(5KS/s)", // SAMPLE_5K,
			"(2.5KS/s)", // SAMPLE_2_5K,

			"(1.25KS/s)", // SAMPLE_1_25K,

			"(1KS/s)", // SAMPLE_1K,
			"(500S/s)", // SAMPLE_500,
			"(250S/s)", // SAMPLE_250,

			"(125S/s)", // SAMPLE_125,

			"(100S/s)", // SAMPLE_100,
			"(50S/s)", // SAMPLE_50,
			"(25S/s)", // SAMPLE_25,

			"(12.5S/s)", // SAMPLE_12_5,

			"(10S/s)", // SAMPLE_10,
			"(5S/s)", // SAMPLE_5,
			"(2.5S/s)", // SAMPLE_2_5,

			"(1.25S/s)", // SAMPLE_1_25,

			"(1S/s)", // SAMPLE_1,
			"(0.5S/s)" // SAMPLE_0_5,};
	};
}