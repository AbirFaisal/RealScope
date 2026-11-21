package com.owon.uppersoft.dso.model.trigger.common;

import static com.owon.uppersoft.dso.model.trigger.common.Voltsensor.restrictValue;

import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.util.format.UnitConversionUtil;

/**
 * Threshold upper and lower limits
 * 
 */
public class Thredshold {
	private long uppest = 1;
	private long lowest = 1;
	private final int halfRange;

	public Thredshold(int halfRange) {
		this.halfRange = halfRange;
	}

	/**
	 * Unbounded values can be used
	 * 
	 * @param inverse
	 * @param voltbase
	 * @param pos0
	 * @return
	 */
	public String getUppestLabel(boolean inverse, int voltbase, int pos0) {
		return getThresholdLabel(inverse, voltbase, pos0, c_getUppest());
	}

	/**
	 * Unbounded values can be used
	 * 
	 * @param inverse
	 * @param voltbase
	 * @param pos0
	 * @return
	 */
	public String getLowestLabel(boolean inverse, int voltbase, int pos0) {
		return getThresholdLabel(inverse, voltbase, pos0, c_getLowest());
	}

	public String getThresholdLabel(boolean inverse, int voltbase, int pos0,
			int value) {
		double volt = value - pos0;
		volt = volt * voltbase / GDefine.PIXELS_PER_DIV;
		return UnitConversionUtil.getSimplifiedVoltLabel_mV(inverse ? -volt
				: volt);
	}

	/**
	 * Set the long directly and store it internally; it is clamped when used or sent, so that it can be restored when back in the usable range
	 * 
	 */
	public boolean setUppest(long v) {
		if (uppest == v)
			return false;
		uppest = v;
		return true;
	}

	/**
	 * Set the long directly and store it internally; it is clamped when used or sent, so that it can be restored when back in the usable range
	 * 
	 */
	public boolean setLowest(long v) {
		if (lowest == v)
			return false;
		lowest = v;
		return true;
	}

	public long getUppest() {
		return uppest;
	}

	public long getLowest() {
		return lowest;
	}

	/**
	 * c_setXXX or c_getXXX both limit the value to the specified range on input or output,
	 * 
	 * They also change the internal value without c_, because there is actually only one stored value
	 */
	public boolean c_setUppest(int v) {
		v = restrictValue(v, halfRange);

		if (uppest == v)
			return false;

		uppest = v;
		// within the settable value range
		if (v < lowest) {
			lowest = v;
		}
		return true;
	}

	public boolean c_setLowest(int v) {
		v = restrictValue(v, halfRange);

		if (lowest == v)
			return false;

		lowest = v;
		// within the settable value range
		if (v > uppest) {
			uppest = v;
		}
		return true;
	}

	public int c_getUppest() {
		return restrictValue(uppest, halfRange);
	}

	public int c_getLowest() {
		return restrictValue(lowest, halfRange);
	}

}