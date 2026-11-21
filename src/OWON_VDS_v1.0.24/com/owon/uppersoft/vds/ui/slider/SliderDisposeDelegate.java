package com.owon.uppersoft.vds.ui.slider;

public interface SliderDisposeDelegate {

	/**
	 * Operation ended
	 */
	//void actionOff();

	/**
	 * Close and release
	 */
	void onDispose();

	/**
	 * "50%" instantaneous setting, does not trigger actionOff(), but the handling includes the endSync part
	 */
	//void on50percent();

}
