package com.owon.uppersoft.vds.source.comm.ext;

public interface IClockTimeAdjuster {

	public abstract void c_trg_holdoffArg(int chl, int arg, int en);

	public abstract void c_trg_condtionArg(int chl, int cdt, int arg, int en);

}