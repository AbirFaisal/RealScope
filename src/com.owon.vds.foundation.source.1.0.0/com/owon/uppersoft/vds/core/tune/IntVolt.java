package com.owon.uppersoft.vds.core.tune;

import com.owon.uppersoft.vds.util.format.UnitConversionUtil;

public class IntVolt {
	int v;

	public IntVolt(int i) {
		v = i;
	}

	@Override
	public String toString() {
		return UnitConversionUtil.getIntVoltageLabel_xmV(v);
	}
}