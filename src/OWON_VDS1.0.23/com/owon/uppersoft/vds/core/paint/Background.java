package com.owon.uppersoft.vds.core.paint;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.owon.uppersoft.vds.ui.paint.LineDrawTool;

/**
 * Background，背景绘制器
 * 
 */
public class Background {
	protected int xleft, ytop, xright, ybottom, xcenter, ycenter;

	public Background() {
	}

	public void adjustView(Rectangle rect, boolean isScreenMode_3) {
		int w = rect.width;
		int h = rect.height;
		
		xleft = rect.x;
		ytop = rect.y;
		ybottom = h + ytop;
		xright = w + xleft;
		xcenter = (w >> 1) + xleft;
		ycenter = (h >> 1) + ytop;
	}

	private int yunitlen = 5;
	private int xunitlen = 10;

	private int gap = 9;// 10

	public void setGap(int gap) {
		this.gap = gap;
	}

	public void setYunitlen(int yunitlen) {
		this.yunitlen = yunitlen;
	}

	public void setXunitlen(int xunitlen) {
		this.xunitlen = xunitlen;
	}

	public static final int len_u = 2, LEN = 5, hlen_u = 1, len_5u = 4,
			hlen_5u = 2;

	protected void paintHGraduate(Graphics2D g2d, Color co) {
		g2d.setPaint(co);
		int y, i;
		y = ycenter;

		int tlen, thlen;

		int YUnitLength = yunitlen;
		int ygnum = yunitnum;
		/** 向上 */
		i = 1;
		y -= YUnitLength;
		while (y > ytop) {
			if (i % ygnum == 0) {
				LineDrawTool.drawSashLine(g2d, false, xcenter, xleft, xright,
						y, gap);
				tlen = len_5u;
				thlen = hlen_5u;
			} else {
				tlen = len_u;
				thlen = hlen_u;
			}

			LineDrawTool.drawLine(g2d, false, xleft, xleft + tlen, y);
			LineDrawTool.drawLine(g2d, false, xcenter - thlen, xcenter + thlen,
					y);
			LineDrawTool.drawLine(g2d, false, xright, xright - tlen, y);

			y -= YUnitLength;
			i++;
		}

		y = ycenter;

		/** 向下 */
		i = 1;
		y += YUnitLength;

		int ybottom = this.ybottom;
		while (y < ybottom) {
			if (i % ygnum == 0) {
				LineDrawTool.drawSashLine(g2d, false, xcenter, xleft, xright,
						y, gap);
				tlen = len_5u;
				thlen = hlen_5u;
			} else {
				tlen = len_u;
				thlen = hlen_u;
			}

			LineDrawTool.drawLine(g2d, false, xleft, xleft + tlen, y);
			LineDrawTool.drawLine(g2d, false, xcenter - thlen, xcenter + thlen,
					y);
			LineDrawTool.drawLine(g2d, false, xright, xright - tlen, y);

			y += YUnitLength;
			i++;
		}

	}

	protected void paintVGraduate(Graphics2D g2d, Color co) {
		g2d.setPaint(co);
		int x, i;
		x = xcenter;

		int tlen, thlen;
		int ybottom = this.ybottom - 1;
		int XUnitLength = xunitlen;
		int xgnum = xunitnum;
		i = 1;
		x += XUnitLength;
		while (x < xright) {
			if (i % xgnum == 0) {
				LineDrawTool.drawSashLine(g2d, true, ycenter, ytop, ybottom, x,
						gap);
				tlen = len_5u;
				thlen = hlen_5u;
			} else {
				tlen = len_u;
				thlen = hlen_u;
			}

			LineDrawTool.drawLine(g2d, true, ybottom - tlen, ybottom, x);
			LineDrawTool.drawLine(g2d, true, ycenter - thlen, ycenter + thlen,
					x);
			LineDrawTool.drawLine(g2d, true, ytop + tlen, ytop, x);

			x += XUnitLength;
			i++;
		}

		x = xcenter;
		i = 1;
		x -= XUnitLength;
		while (x > xleft) {
			if (i % xgnum == 0) {
				LineDrawTool.drawSashLine(g2d, true, ycenter, ytop, ybottom, x,
						gap);
				tlen = len_5u;
				thlen = hlen_5u;
			} else {
				tlen = len_u;
				thlen = hlen_u;
			}

			LineDrawTool.drawLine(g2d, true, ybottom - tlen, ybottom, x);
			LineDrawTool.drawLine(g2d, true, ycenter - thlen, ycenter + thlen,
					x);
			LineDrawTool.drawLine(g2d, true, ytop + tlen, ytop, x);

			x -= XUnitLength;
			i++;
		}
	}

	protected void paintLines(Graphics2D g2d, Color co) {
		/** 外轴 */
		g2d.setPaint(co);
		/** 画矩形时少一个像素，因为线条占一个 */
		g2d.drawRect(xleft, ytop, xright - xleft, ybottom - ytop - 1);
		g2d.drawLine(xleft, ycenter, xright, ycenter);
		g2d.drawLine(xcenter, ytop, xcenter, ybottom);
	}

	public void paintView(Graphics2D g2d, ColorProvider pc) {
		Color co = pc.getGridColor();
		paintLines(g2d, co);
		paintHGraduate(g2d, co);
		paintVGraduate(g2d, co);
	}

	/** Background */

	public static final int xunitnum = 5, yunitnum = 5;
}
