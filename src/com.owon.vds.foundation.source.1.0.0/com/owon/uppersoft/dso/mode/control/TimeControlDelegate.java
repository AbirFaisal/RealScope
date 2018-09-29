package com.owon.uppersoft.dso.mode.control;

public interface TimeControlDelegate {
	void onTimebaseChange(int oldidx, int newidx, boolean effect);

	void onHorTrgPosChange();

	void onHorTrgPosChangedByTimebase(int tbidx, int htp);
	
	void notifyUpdateHorTrgPosRange();
	
	void notifyUpdateCurrentSampleRate();
	
	void notifyInitSlowMove(boolean slowMove);

	void nofiyResetPersistence();
	
	Runnable getResetPersistenceRunnable();
}