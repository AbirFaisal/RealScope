package com.owon.uppersoft.vds.core.aspect.help;

public interface IExportableWF {

	int getDatalen();

	double voltAt(int i);

	String getFreqLabel();

	boolean isOn();

	String getChannelLabel();
}
