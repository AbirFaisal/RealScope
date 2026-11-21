package com.owon.uppersoft.vds.ui.slider;

/**
 * Use this in zoom
 * 
 */
public interface SliderDelegate2 extends SliderDisposeDelegate {

	// /**
	// * Operation ended
	// */
	// void actionOff();
	//
	// /**
	// * Close and release
	// */
	// void onDispose();

	/**
	 * Used for continuous value "delta" changes, may include changes to the default value (e.g. 0), but does not trigger actionOff()
	 * 
	 * @param v
	 */
	void valueIncret(int delta);

	/**
	 * Instantaneous setting of the default value (e.g. 0), triggers actionOff(), only used in zoom
	 */
	void onReset0();

}
