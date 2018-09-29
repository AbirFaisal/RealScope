package com.owon.uppersoft.vds.source.comm.ext;

public class Arg_En {
	public int arg;
	public int en;

	public Arg_En(int arg, int en) {
		this.arg = arg;
		this.en = en;
	}

	@Override
	public String toString() {
		return arg + ", " + en;
	}
}