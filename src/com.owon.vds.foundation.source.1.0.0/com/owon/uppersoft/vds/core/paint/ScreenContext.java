package com.owon.uppersoft.vds.core.paint;

import java.awt.Insets;
import java.awt.Rectangle;

public abstract class ScreenContext implements ColorProvider {

	public static final int ScreenMode_1 = 1;
	public static final int ScreenMode_3 = 3;

	protected abstract void pack();

	private int screenMode = ScreenMode_1;

	public void setScreenMode_3(boolean b) {
		screenMode = b ? ScreenMode_3 : ScreenMode_1;
		pack();
	}

	public final boolean isScreenMode_3() {
		return screenMode == ScreenMode_3;
	}

	/** 中心相对屏幕位置 */
	private int hcenter;
	private int wcenter;

	public int getHcenter() {
		return hcenter;
	}

	protected void setHcenter(int hcenter) {
		this.hcenter = hcenter;
	}

	protected int getWcenter() {
		return wcenter;
	}

	protected void setWcenter(int wcenter) {
		this.wcenter = wcenter;
	}

	/** chart屏幕的范围变量 */
	private Rectangle chartRectangle = new Rectangle();
	private Insets chartInsets = new Insets(0, 0, 0, 0);

	/**
	 * @return copy
	 */
	public Rectangle getChartRectangle() {
		return new Rectangle(chartRectangle);
	}

	protected void setChartRectangle(int x, int y, int width, int height) {
		chartRectangle.setBounds(x, y, width, height);
	}

	/**
	 * @return const
	 */
	public Insets getChartInsets() {
		return chartInsets;
	}

	protected void setChartInsets(Insets insets) {
		chartInsets.set(insets.top, insets.left, insets.bottom, insets.right);
	}

	public abstract boolean allowLazyRepaint();

	public abstract IPaintOne getIPaintOne();

}