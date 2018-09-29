package com.owon.uppersoft.vds.core.comm.effect;

public enum P_Channel {
	ONOFF('o'), COUPLING('c'), VB('v'), POS0('z'), BANDLIMIT('b');

	public final byte code;

	private P_Channel(char c) {
		code = (byte) c;
	}
}