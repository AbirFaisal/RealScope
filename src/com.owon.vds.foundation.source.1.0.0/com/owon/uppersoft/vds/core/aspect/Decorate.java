package com.owon.uppersoft.vds.core.aspect;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.owon.uppersoft.vds.core.paint.ScreenContext;

/**
 * Decorator
 * 
 */
public interface Decorate {

	/**
	 * Adjustments required when the size changes
	 * 
	 * @param pc
	 */
	void adjustView(ScreenContext pc, Rectangle bound);

	/**
	 * 
	 * @param pc
	 */
	void paintView(Graphics2D g2d, ScreenContext pc, Rectangle bound);
}
