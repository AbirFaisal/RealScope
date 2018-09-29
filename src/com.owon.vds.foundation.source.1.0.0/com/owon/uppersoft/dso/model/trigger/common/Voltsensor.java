package com.owon.uppersoft.dso.model.trigger.common;


/**
 * 触发电平值
 * 
 * TODO 是否基于零点，是否使用电压值在开关的时候转换为像素值；好像也不是很必要
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
	 * 直接设置long，内部保存，使用时进行限界，这样当恢复到可用范围时就还能恢复回来
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