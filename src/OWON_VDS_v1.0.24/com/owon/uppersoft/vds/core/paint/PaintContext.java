package com.owon.uppersoft.vds.core.paint;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Rectangle;

import com.owon.uppersoft.vds.core.GDefine;

/**
 * PaintContext，绘制上下文
 * 
 */
public abstract class PaintContext extends ScreenContext {

	public static final int DEFINE_AREA_WIDTH = GDefine.AREA_WIDTH;
	public static final int DEFINE_AREA_HEIGHT = 500 + 1;

	public int widthtrange = DEFINE_AREA_WIDTH;
	public int heightrange;

	/** 半屏幕的高宽 */
	public static final int hwidth = DEFINE_AREA_WIDTH >> 1;

	private int hheight;

	/** left & right */
	public static final int compositeTop = 5, compositeBottom = 1,
			chartTop = 9, chartBottom = 5, leftl = 5, leftr = 3, rightl = 3,
			rightr = 5;
	public static final Insets leftInsets = new Insets(compositeTop, leftl,
			compositeBottom, leftr);
	public static final Insets rightInsets = new Insets(compositeTop, rightl,
			compositeBottom, rightr);
	public static final int wrange = 500;

	/** 125~0~-125 */
	public static final int hrange = 250 + 1;
	public static final int lcenter = leftl + (wrange >> 1), rcenter = rightl
			+ (wrange >> 1);
	public static final int composite_hcenter = compositeTop + (hrange >> 1)
			+ 1;
	public static final int leftwrange = leftl + wrange + leftr,
			rightwrange = rightl + wrange + rightr;
	public static final int composite_height = compositeTop + hrange
			+ compositeBottom;
	public static final Rectangle lrect = new Rectangle(leftl, compositeTop,
			wrange, hrange), rrect = new Rectangle(rightl, compositeTop,
			wrange, hrange);

	/** chart */
	public static final Insets ScreenMode1_ChartInsets = new Insets(10, 12, 10,
			4);
	public static final Insets ScreenMode3_ChartInsets = new Insets(chartTop,
			12, chartBottom, 4);
	public static final Insets ScreenMode3_CompositInsets = new Insets(
			compositeTop, 12, compositeBottom, 4);

	public PaintContext() {
	}

	@Override
	protected void pack() {
		Insets insets;
		if (isScreenMode_3()) {
			setChartInsets(ScreenMode3_ChartInsets);

			insets = getChartInsets();

			heightrange = hrange;
			hheight = 125;
			setChartRectangle(insets.left, insets.top, DEFINE_AREA_WIDTH,
					heightrange);
		} else {
			setChartInsets(ScreenMode1_ChartInsets);

			insets = getChartInsets();

			heightrange = DEFINE_AREA_HEIGHT;
			hheight = 250;
			setChartRectangle(insets.left, insets.top, DEFINE_AREA_WIDTH,
					heightrange);
		}

		/** 中心点的概念即下一个半空间的起始点 */
		setHcenter(insets.top + hheight);
		setWcenter(insets.left + hwidth);
	}

	/** 适当时机设置DataHouse */
	private ILazy dh;

	public void setLazy(ILazy dh) {
		this.dh = dh;
	}

	public boolean allowLazyRepaint() {
		return dh.allowLazyRepaint();
	}

	private ColorProvider cp;

	public void setColorProvider(ColorProvider cp) {
		this.cp = cp;
	}

	public Color getGridColor() {
		return cp.getGridColor();
	}

	public static int getFFTScreenPixNum(boolean isFFTon) {
		return isFFTon ? PaintContext.wrange : PaintContext.DEFINE_AREA_WIDTH;
	}

}