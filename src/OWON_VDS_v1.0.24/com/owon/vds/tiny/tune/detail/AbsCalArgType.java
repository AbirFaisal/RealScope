package com.owon.vds.tiny.tune.detail;

import com.owon.uppersoft.vds.core.tune.Cending;
import com.owon.uppersoft.vds.core.tune.ICal;
import com.owon.vds.tiny.firm.pref.model.ArgType;

public abstract class AbsCalArgType implements ICal {
	private Cending cdn;
	private int id;

	public AbsCalArgType(int id, Cending cdn) {// 
		this.id = id;
		this.cdn = cdn;
//		this.k = k;
	}

	public int getId() {
		return id;
	}

	public Cending cending() {
		return cdn;
	}

	public void setCending(Cending cdn) {
		this.cdn = cdn;
	}

//	private double k;

//	public double getK() {
//		return k;
//	}

	public abstract int getArg(int... arr);

	public abstract int[][] getArgs();

	/**
	 * @return Returns the leading text from the txt string
	 */
	public abstract String prekey();

	public String getType() {
		return ArgType.VALUES[id].name();
	}

}