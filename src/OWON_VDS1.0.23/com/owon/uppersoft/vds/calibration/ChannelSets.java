package com.owon.uppersoft.vds.calibration;

public class ChannelSets {
	public int channel;
	public int coupling;
	public boolean inverse;
	public int pos0;
	public int vbidx;
	public boolean on;

	@Override
	public String toString() {
		return "channel" + channel + ", coupling" + coupling + ", inverse"
				+ inverse + ", pos0" + pos0 + ", vbidx" + vbidx + ", on" + on;
	}
}
