package com.owon.uppersoft.vds.core.wf.rt;

public class PlugInfo {
	public int plugDataLength;
	public int sinePlugRate;
	public double linearPlugRate;

	public int pluggedTrgOffset;

	public PlugInfo() {
	}

	public void copyFrom(PlugInfo pi) {
		plugDataLength = pi.plugDataLength;
		sinePlugRate = pi.sinePlugRate;
		linearPlugRate = pi.linearPlugRate;
		pluggedTrgOffset = pi.pluggedTrgOffset;
	}

	public void reset() {
		plugDataLength = 0;
		sinePlugRate = 1;
		linearPlugRate = 1;
		pluggedTrgOffset = 0;
	}
}