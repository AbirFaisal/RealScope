package com.owon.uppersoft.dso.ref;

import java.nio.IntBuffer;

public interface IRefSource {

	IntBuffer save2RefIntBuffer();

	int getProbeMultiIdx();

	int getWaveType();

	int getPos0();

	int getVoltbaseIndex();

}