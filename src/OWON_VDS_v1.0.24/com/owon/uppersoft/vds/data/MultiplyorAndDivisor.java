package com.owon.uppersoft.vds.data;

import java.math.BigDecimal;

/**
 * Shared by Dilute's Pluger and the ordinary case
 * 
 */
public class MultiplyorAndDivisor {

	/** Dividend, unit stretch range */
	private int multiplyor = 1;

	/** Divisor, number of points within the unit stretch range */
	private int divisor = 1;

	public MultiplyorAndDivisor() {
		this(1, 1);
	}

	public MultiplyorAndDivisor(int ml, int di) {
		set(ml, di);
	}

	public MultiplyorAndDivisor set(int ml, int di) {
		// this.multiplyor = ml;
		// this.divisor = di;

		int gcd = GcdTest.gcd1(ml, di);
		this.multiplyor = ml / gcd;
		this.divisor = di / gcd;

		// if (isInt()) {
		// multiplyor = multiplyor / divisor;
		// divisor = 1;
		// } else if (isIntDiv()) {
		// divisor = divisor / multiplyor;
		// multiplyor = 1;
		// }
		return this;
	}

	public MultiplyorAndDivisor set(int rate) {
		return set(rate, 1);
	}

	public int getMultiplyor() {
		return multiplyor;
	}

	public int getDivisor() {
		return divisor;
	}

	public double divideByDouble(double v) {
		// v / (multiplyor / divisor);
		return v * divisor / multiplyor;
	}

	public double multiplyByDouble(double v) {
		return v * multiplyor / divisor;
	}

	/**
	 * v / multiplyor * divisor; when divisor = 1
	 * 
	 * The remainder must be removed first and then multiplied; the wrong order gives wrong results
	 * 
	 * Can be used to get the front and back offset point counts at the cpidx level
	 * 
	 * @param v
	 * @return
	 */
	public int divideDirectByInt(int v) {
		return v / multiplyor * divisor;
	}

	/**
	 * md: the front and back offset counts of the corresponding pix_block
	 * 
	 * @param v
	 * @return
	 */
	public int divideByMD_blockOffset(int v) {
		return v / multiplyor;
	}

	/**
	 * @param v
	 * @return
	 */
	public int modeByIntOffset(int v) {
		return v % multiplyor;
	}

	public int multiplyByInt(int v) {
		return v * multiplyor / divisor;
	}

	/** ***** */
	public boolean isInt() {
		return (multiplyor % divisor) == 0;
	}

	public boolean isIntDiv() {
		return (divisor % multiplyor) == 0;
	}

	@Override
	public String toString() {
		return multiplyor + "/" + divisor;
	}

	public double getDoubleValue() {
		return multiplyor / (double) divisor;
	}

	public BigDecimal getBDValue() {
		return new BigDecimal(multiplyor).divide(new BigDecimal(divisor));
	}

	public BigDecimal multiplyByBD(BigDecimal v) {
		return v.multiply(getBDValue());
	}

	public MultiplyorAndDivisor divide_MD(MultiplyorAndDivisor md) {
		return new MultiplyorAndDivisor(multiplyor * md.divisor, divisor
				* md.multiplyor);
	}

	public MultiplyorAndDivisor multiply_MD(MultiplyorAndDivisor md) {
		return new MultiplyorAndDivisor(multiplyor * md.multiplyor, divisor
				* md.divisor);
	}

	public static MultiplyorAndDivisor getFrom1_2_5TypeDivison(BigDecimal v1,
			BigDecimal v2) {
		// BigDecimal v = v1.max(v2);
		// boolean less = false;
		// if (!v1.equals(v)) {
		// v = v1;
		// v1 = v2;
		// v2 = v;
		// less = true;
		// }
		// v1 = v1.stripTrailingZeros();
		// v2 = v2.stripTrailingZeros();
		// System.err.println(v1 + ", " + v2);
		int scale = Math.max(v1.scale(), v2.scale());
		v1 = v1.movePointRight(scale);
		v2 = v2.movePointRight(scale);
		// System.err.println(v1 + ", " + v2);
		return new MultiplyorAndDivisor(v1.intValue(), v2.intValue());
	}

	public static void main(String[] args) {
		// BigDecimal v1 = new BigDecimal("10E-4");
		// BigDecimal v2 = new BigDecimal("20E-7");
		// BigDecimal v3 = new BigDecimal("50");
		// System.err.println(v1.movePointRight(2));
		// System.err.println(getFrom1_2_5TypeDivison(v1, v2));

		MultiplyorAndDivisor md = new MultiplyorAndDivisor(1250, 0);
		System.err.println(md);
	}
}
