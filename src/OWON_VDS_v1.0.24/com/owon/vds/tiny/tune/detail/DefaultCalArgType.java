package com.owon.vds.tiny.tune.detail;

import com.owon.uppersoft.vds.core.tune.Cending;
import com.owon.vds.tiny.firm.pref.model.ArgType;

/**
 * CoarseGain, ZeroAmplitude, ZeroCompensation三者共用
 */
public class DefaultCalArgType extends AbsCalArgType {
	protected int[][] args;
	public final int channelNumber, vbNum;

	public DefaultCalArgType(int id, Cending cdn, int channelNumber, int vbNum) {
		super(id, cdn);

		this.channelNumber = channelNumber;
		this.vbNum = vbNum;

		args = new int[channelNumber][vbNum];
	}

	// public DefaultCalArgType(int id, Cending cdn, int channelNumber, int
	// vbNum) {
	// this(id, cdn, channelNumber, vbNum);
	// }

	public int[][] getArgs() {
		return args;
	}

	@Override
	public String prekey() {
		return ArgType.VALUES[getId()].prekey();
	}

	@Override
	public int getArg(int... arr) {
		int len = arr.length;

		if (len < 2)
			return 0;

		return args[arr[0]][arr[1]];
	}

}