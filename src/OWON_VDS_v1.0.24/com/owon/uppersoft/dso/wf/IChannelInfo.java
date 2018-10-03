package com.owon.uppersoft.dso.wf;

import java.awt.Color;

import com.owon.uppersoft.vds.core.aspect.help.ILoadPersist;
import com.owon.uppersoft.vds.core.comm.effect.Submitable;

public interface IChannelInfo extends IChannel, ILoadPersist {

	boolean isOn();

	int getCouplingIdx();

	boolean isInverse();

	int getVoltbaseIndex();

	int getPos0();

	void c_setZero(int pos0, boolean b);

	void c_setOn(boolean on);

	void c_setCoupling(int coupling);

	void c_setInverse(boolean inverse);

	void c_setVoltage(int channel, Runnable r);

	void setOnWithoutSync(boolean b);

	boolean isGround();

	Color getColor();

	int getProbeMultiIdx();

	int getNumber();

	String getName();

	void c_SyncChannel(Submitable sbm);

	void c_setBandLimit(boolean b);

	void setProbeMultiIdx(int chlidx);

	boolean isForcebandlimit();

	boolean isBandlimit();

}