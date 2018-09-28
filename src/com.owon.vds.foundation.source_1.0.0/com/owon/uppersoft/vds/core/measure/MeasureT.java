package com.owon.uppersoft.vds.core.measure;

import java.util.Arrays;

/**
 * MeasureT，测量类型的枚举值，不适用values()方法而使用VALUES成员
 * 
 */
public enum MeasureT {

	PERiod, FREQuency,

	RTime, FTime, PWIDth, NWIDth, PDUTy,

	NDUTy, 

	/***/

	PKPK, MAX, MIN, VAMP, VTOP,

	VBASe, OVERshoot, PREShoot, AVERage, CYCRms;
	
	public final int idx;
	public final static int FAILURE = -1;
	public final static int RISE = 1;
	public final static int FALL = 0;

	private MeasureT() {
		idx = ordinal();
	}
	
	public static final MeasureT[] VALUES;
	public static final int Count;

	static {
		VALUES = values();

		/** 由于enum对values返回数组并无顺序的标准说明，故这里加入了对枚举的排序 */
		Arrays.sort(VALUES);

		Count = VALUES.length;
	}

	public static void main(String[] args) {
		for (int i = 0; i < VALUES.length; i++) {
			System.out.println(VALUES[i] + " " + VALUES[i].idx);
		}
	}
}
