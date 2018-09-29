package com.owon.uppersoft.vds.core.aspect.control;

public interface VoltageProvider {
	// mV
	int getVoltage(int probe, int vbIndex);

	int getVoltageNumber();

	String getVoltageLabel(int vbIndex);

	int getPos0HalfRange(int currentVolt);

	int[] getVoltages(int probe);

	public Integer[] getProbeMulties();

}