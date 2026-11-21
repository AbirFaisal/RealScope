package com.owon.uppersoft.vds.core.measure;

import java.util.Arrays;

/**
 * MeasureT, enum values for measurement types; does not use the values() method but the VALUES member
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

		/** Since the enum's values() array has no standard specified order, sorting of the enum is added here */
		Arrays.sort(VALUES);

		Count = VALUES.length;
	}

	public static void main(String[] args) {
		for (int i = 0; i < VALUES.length; i++) {
			System.out.println(VALUES[i] + " " + VALUES[i].idx);
		}
	}
}
