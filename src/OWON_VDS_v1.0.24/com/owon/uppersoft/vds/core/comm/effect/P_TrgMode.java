package com.owon.uppersoft.vds.core.comm.effect;

public enum P_TrgMode {
	edge('e'), video('v'), slope('s'), pulse('p');

	public final byte code;

	private P_TrgMode(char c) {
		code = (byte) c;
	}

}