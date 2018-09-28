package com.owon.uppersoft.dso.delegate;

import com.owon.uppersoft.vds.core.aspect.control.MachineInfoProvider;
import com.owon.uppersoft.vds.core.aspect.control.VoltageProvider;
import com.owon.uppersoft.vds.util.format.UnitConversionUtil;

public class DefaultVoltageProvider implements VoltageProvider {
	private MachineInfoProvider mip;

	public DefaultVoltageProvider(MachineInfoProvider mip) {
		this.mip = mip;
	}

	@Override
	public int getVoltage(int probe, int vbIndex) {
		return mip.getMachineInfo().intVOLTAGE[probe][vbIndex];
	}

	@Override
	public int getVoltageNumber() {
		return mip.getMachineInfo().intVOLTAGE[0].length;
	}

	@Override
	public String getVoltageLabel(int vbIndex) {
		return UnitConversionUtil.getIntVoltageLabel_mV(getVoltage(0, vbIndex));
	}

	@Override
	public int getPos0HalfRange(int currentVolt) {
		return mip.getMachineInfo().getPos0HalfRange(currentVolt);
	}

	@Override
	public int[] getVoltages(int probe) {
		return mip.getMachineInfo().intVOLTAGE[probe];
	}

	public Integer[] getProbeMulties() {
		return mip.getMachineInfo().ProbeMulties;
	}
}