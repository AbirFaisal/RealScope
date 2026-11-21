package com.owon.uppersoft.dso.model.trigger.common;


/**
 * Trigger level value
 * 
 * TODO whether it is based on the zero point, whether to convert voltage values to pixel values when switching; it seems not really necessary either
 * 
 */
public class Voltsensor {
	private final int halfRange;

	public Voltsensor(int halfRange) {
		this.halfRange = halfRange;
	}

	private long voltsense = 0;

	public boolean c_setVoltsense(int v) {
		v = restrictValue(v, halfRange);

		if (voltsense == v)
			return false;

		voltsense = v;
		return true;
	}

	public int c_getVoltsense() {
		return restrictValue(voltsense, halfRange);
	}

	public long getVoltsense() {
		return voltsense;
	}

	/**
	 * Set the long directly and store it internally; it is clamped when used, so that it can be restored when back in the usable range
	 * 
	 * @param v
	 * @return
	 */
	public boolean setVoltsense(long v) {
		if (voltsense == v)
			return false;
		voltsense = v;
		return true;
	}

	public static final int restrictValue(long v, int HalfRange) {
		return restrictValue((int) v, HalfRange);
	}

	public static final int restrictValue(int v, int HalfRange) {
		if (v > HalfRange) {
			v = HalfRange;
			// System.out.println(vs + "_ " + del);
		} else if (v < -HalfRange) {
			v = -HalfRange;
		}
		return v;
	}
}