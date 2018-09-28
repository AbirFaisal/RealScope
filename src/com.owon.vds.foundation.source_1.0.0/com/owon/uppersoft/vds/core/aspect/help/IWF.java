package com.owon.uppersoft.vds.core.aspect.help;

import java.nio.ByteBuffer;

import com.owon.uppersoft.vds.core.measure.MeasureADC;

public interface IWF {
	int getChannelNumber();

	boolean isOn();

	ByteBuffer getADC_Buffer();

	boolean isADCBeyondMin();

	boolean isADCBeyondMax();

	int getFirstLoadPos0();

	MeasureADC getMeasureADC();

	int getProbeMultiIdx();

	int getVoltbaseIndex();

	int getPos0ForADC();
}
