package com.owon.uppersoft.vds.core.comm;

public interface IRuntime {
	boolean isRuntime();

	boolean isExit();

	boolean isKeepGet();

	void setKeepGet(boolean b);

	boolean isRecentRunThenStop();

	void setRecentStop(boolean b);

	boolean isAllChannelShutDown();
}
