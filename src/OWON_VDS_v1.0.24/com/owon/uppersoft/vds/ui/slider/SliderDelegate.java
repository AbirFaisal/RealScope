package com.owon.uppersoft.vds.ui.slider;

/**
 * SliderDelegate
 * 
 */
public interface SliderDelegate extends SliderDisposeDelegate{
	public static final int BtnStatusNO = 0, BtnStatus0 = 1, BtnStatus50 = 2,
			BtnStatusBoth = 3;

//	/**
//	 * Operation ended
//	 */
	void actionOff();
//
//	/**
//	 * Close and release
//	 */
//	void onDispose();

	/**
	 * Used for continuous value changes, may include changes to the default value (e.g. 0), but does not trigger actionOff()
	 * 
	 * @param v
	 */
	void valueChanged(int oldV, int newV);

//	/**
//	 * "50%" instantaneous setting, does not trigger actionOff(), but the handling includes the endSync part
//	 */
	void on50percent();

}
