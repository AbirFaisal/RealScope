package com.owon.uppersoft.dso.machine.aspect;

import com.owon.uppersoft.dso.global.OperateBlocker;

public interface IStopable {

	void stopkeep();

	void stopkeepNForbidDM();

	void resumeKeep();

	void releaseConnect();

	// void load();

	void keepload();

	void setDMDataGotAlready(boolean b);

	boolean isDMDataGotAlready();

	OperateBlocker getOperateBlocker();
}