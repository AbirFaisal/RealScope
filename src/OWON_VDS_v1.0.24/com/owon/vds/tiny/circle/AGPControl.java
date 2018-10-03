package com.owon.vds.tiny.circle;

public interface AGPControl {

	void init(String port);

	void turnChannels(boolean on);

	void genWFwithVB(int vbidx);

	void finish();
}