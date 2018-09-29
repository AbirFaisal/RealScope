package com.owon.uppersoft.vds.core.aspect.control;

public interface LocationLabelProvider {
	String getXLocationLabel(int x);

	String getYLocationLabel(int y);

	String getXDeltaLocationLabel(int xd);

	String getYDeltaLocationLabel(int yd);
}