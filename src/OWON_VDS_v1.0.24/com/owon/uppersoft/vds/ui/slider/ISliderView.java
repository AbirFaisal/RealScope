package com.owon.uppersoft.vds.ui.slider;

import java.awt.Graphics2D;

/**
 * setValue() and getValue() methods are not exposed
 * 
 * 50% value currently cannot be set inside the slider, but is changed externally by SliderDelegate
 * 
 */
public interface ISliderView {

	/**
	 * Set to default; the exact meaning is defined by the model itself
	 */
	void setDefault();

	/**
	 * Delta setting during adjustment
	 * 
	 * @param delta
	 */
	void adjustAdd(int delta);

	/**
	 * Draw the content
	 * 
	 * @param g
	 */
	void paintSelf(Graphics2D g);

	/**
	 * Operation release, such as mouse release; it only means the drag has ended, not that the UI is closed or no longer operated
	 * 
	 * Used to stop after single-task synchronization produced via adjustAdd(delta) (mouse drag case & mouse click case)
	 */
	void actionOff();

}
