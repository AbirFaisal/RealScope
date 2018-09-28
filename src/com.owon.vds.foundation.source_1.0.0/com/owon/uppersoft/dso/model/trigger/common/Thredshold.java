package com.owon.uppersoft.dso.model.trigger.common;

import static com.owon.uppersoft.dso.model.trigger.common.Voltsensor.restrictValue;

import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.util.format.UnitConversionUtil;

/**
 * 阈值上下限
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
	 * 可以用无限界的值
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
	 * 可以用无限界的值
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
	 * 直接设置long，内部保存，使用、发送时进行限界，这样当恢复到可用范围时就还能恢复回来
	 * 
	 */
	public boolean setUppest(long v) {
		if (uppest == v)
			return false;
		uppest = v;
		return true;
	}

	/**
	 * 直接设置long，内部保存，使用、发送时进行限界，这样当恢复到可用范围时就还能恢复回来
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
	 * c_setXXX或c_getXXX均在输入或输出的时候限制了值在指定的范围内，
	 * 
	 * 同时也会改变无c_的内部值，因为其实只有一个保存值的
	 */
	public boolean c_setUppest(int v) {
		v = restrictValue(v, halfRange);

		if (uppest == v)
			return false;

		uppest = v;
		// 在可设置的值的范围内
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
		// 在可设置的值的范围内
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