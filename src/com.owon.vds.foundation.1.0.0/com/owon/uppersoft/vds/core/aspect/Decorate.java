package com.owon.uppersoft.vds.core.aspect;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.owon.uppersoft.vds.core.paint.ScreenContext;

/**
 * 装饰器
 * 
 */
public interface Decorate {

	/**
	 * 大小变化时需进行的调整
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
