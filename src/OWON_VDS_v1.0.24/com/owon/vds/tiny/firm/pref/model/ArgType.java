package com.owon.vds.tiny.firm.pref.model;

public enum ArgType {
	Gain, Step, Compensation;

	static String[] keys = { "$auto_self_cal coarsegain_",
			"$auto_self_cal step_", "$auto_self_cal zero_" };

	public String prekey() {
		return keys[ordinal()];
	}
	
	public static final ArgType[] VALUES = values();
}