package com.owon.uppersoft.dso.wf;

public interface IChannel {

	void c_setBandLimit(boolean b);

	void c_setOn(boolean b);

	void c_setInverse(boolean selected);

	void setProbeMultiIdx(int chlidx);

	void c_setCoupling(int selectedIndex);

	boolean isOn();

	boolean isForcebandlimit();

	boolean isBandlimit();

	boolean isInverse();

	int getProbeMultiIdx();

	int getCouplingIdx();

	int getNumber();

}
