package com.owon.uppersoft.dso.view;

import com.owon.uppersoft.dso.source.comm.TrgStatus;

public interface ITitleStatus {

	void exposeTrgStatus();

	void updateTrgStatus(int c, TrgStatus ts);

	void setTempStatus(boolean b);

	void updateTrgStatus(TrgStatus ts);

	void confirmStopStatus();

	void enableBtns();

	void enableAllButtons(boolean b);

//	void askStop(boolean dm);

//	void askRun(boolean syncRunStatus);

	void askSweepOnce();
	
	boolean switchRS();

	void applyStop();

	void updateView();

}