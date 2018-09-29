package com.owon.uppersoft.dso.util.ui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import com.owon.uppersoft.vds.core.paint.ScreenContext;
import com.owon.uppersoft.vds.data.LocRectangle;
import com.owon.uppersoft.vds.ui.paint.LineDrawTool;

public class LineUtil {
	public static final void paintOnShowPos0(Graphics2D g2d, ScreenContext pc,
			String divUnits, int pos0, int yb, Rectangle r) {
		int rectw = 8, recthh = 9;
		float PixsPerDiv = 25;
		String tpos = String.valueOf(pos0 / PixsPerDiv) + " " + divUnits;

		int up = r.y + (recthh >> 1), low = r.y + r.height - (recthh >> 1);

		int y, ty;
		int w = 7 * rectw;
		int h = 2 * recthh;
		int x = 3 * rectw;
		int tx = 3 * rectw + 5;
		if (yb <= up) {
			y = up - 3;
			ty = up + 10;
		} else if (yb >= low) {
			y = low - 15;
			ty = low - 1;
		} else {
			y = yb - 9;
			ty = yb + 4;
		}
		Color tmp = g2d.getColor();
		g2d.setColor(Color.BLACK);
		g2d.fillRect(x, y, w, h);
		g2d.setColor(tmp);

		g2d.drawRect(x, y, w, h);
		g2d.drawString(tpos, tx, ty);
	}

	public static void paintChannelLabel(int yb, int y, int bottom,
			Graphics2D g2d, String n, int x, boolean onFront) {
		int[] xPoints = int5s0;
		int[] yPoints = int5s1;

		int rectw = 8, recthh = 9, rectlw = 12;
		int taby, pn;
		if (yb <= y + (recthh >> 1)) {
			if (yb <= y) {
				yb = y;
			}
			taby = yb + 13;

			xPoints[0] = 0;
			yPoints[0] = yb;

			xPoints[1] = rectlw;
			yPoints[1] = yb;

			xPoints[2] = rectw;
			yPoints[2] = yb + 2 * recthh;

			xPoints[3] = 0;
			yPoints[3] = yb + 2 * recthh;

			pn = 4;
		} else if (yb >= bottom - (recthh >> 1)) {
			if (yb >= bottom) {
				yb = bottom;
			}
			taby = yb - 4;

			xPoints[0] = 0;
			yPoints[0] = yb - 2 * recthh;

			xPoints[1] = rectw;
			yPoints[1] = yb - 2 * recthh;

			xPoints[2] = rectlw;
			yPoints[2] = yb;

			xPoints[3] = 0;
			yPoints[3] = yb;

			pn = 4;
		} else {
			taby = yb + 4;
			xPoints[0] = 0;
			yPoints[0] = yb - recthh;

			xPoints[1] = 0;
			yPoints[1] = yb + recthh;

			xPoints[2] = rectw;
			yPoints[2] = yb + recthh;

			xPoints[3] = rectlw;
			yPoints[3] = yb;

			xPoints[4] = rectw;
			yPoints[4] = yb - recthh;

			pn = 5;
		}
		if (onFront) {
			Color c = g2d.getColor();
			g2d.fillPolygon(xPoints, yPoints, pn);
			g2d.setColor(Color.DARK_GRAY);
			// g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
			g2d.drawString(n, x, taby);
			g2d.setColor(c);
		} else {
			g2d.drawPolygon(xPoints, yPoints, pn);
			g2d.drawString(n, x, taby);
		}

	}

	private static final int[] int3s0 = new int[3], int3s1 = new int[3];

	public static void paintHorTrg(int x, int y, int x0, int x1, Graphics2D g2d) {
		int[] xPoints = int3s0;
		int[] yPoints = int3s1;
		if (x < x0) {
			xPoints[0] = x0;
			xPoints[1] = x0 + 5;
			xPoints[2] = x0 + 5;
			yPoints[0] = y >> 1;
			yPoints[1] = 0;
			yPoints[2] = y;
			g2d.fillPolygon(xPoints, yPoints, 3);
		} else if (x > x1) {
			xPoints[0] = x1 - 5;
			xPoints[1] = x1 - 5;
			xPoints[2] = x1;
			yPoints[0] = 0;
			yPoints[1] = y;
			yPoints[2] = y >> 1;
			g2d.fillPolygon(xPoints, yPoints, 3);
		} else {
			xPoints[0] = x - 5;
			xPoints[1] = x;
			xPoints[2] = x + 5;
			yPoints[0] = 1;
			yPoints[1] = y;
			yPoints[2] = 1;
			g2d.fillPolygon(xPoints, yPoints, 3);
		}
	}

	public static void paintHorTrgDetail(int x, int y, Rectangle r,
			Graphics2D g2d, String detail, boolean onShowHtp) {
		if (onShowHtp) {
			Color tmp = g2d.getColor();

			Rectangle2D r2 = g2d.getFontMetrics().getStringBounds(detail, g2d);
			int w = (int) r2.getWidth() + 6, h = (int) r2.getHeight();
			int ww = w >> 1;
			// 限界
			if (x < r.x + ww) {
				x = r.x + ww;
			} else if (x > r.x + r.width - ww) {
				x = r.x + r.width - ww;
			}
			// 定位
			x -= ww;
			y += 3;
			g2d.setColor(Color.BLACK);
			g2d.fillRoundRect(x, y, w, h, 5, 5);
			g2d.setColor(Color.ORANGE);
			g2d.drawRoundRect(x, y, w, h, 5, 5);
			x += 3;
			y += h - 3;
			g2d.drawString(detail, x, y);
			g2d.setColor(tmp);
		}

	}

	private static void paintTrgLevelRightTriangel(Graphics2D g2d, int y,
			int y0, int y1, int x1, Color c) {
		int[] xPoints = int3s0;
		int[] yPoints = int3s1;
		g2d.setColor(c);
		if (y < y0) {
			xPoints[0] = x1;
			xPoints[1] = x1 + 4;
			xPoints[2] = x1 + 8;
			yPoints[0] = y0 + 5;
			yPoints[1] = y0;
			yPoints[2] = y0 + 5;
			g2d.fillPolygon(xPoints, yPoints, 3);

		} else if (y > y1) {
			xPoints[0] = x1;
			xPoints[1] = x1 + 4;
			xPoints[2] = x1 + 8;
			yPoints[0] = y1 - 5;
			yPoints[1] = y1;
			yPoints[2] = y1 - 5;
			g2d.fillPolygon(xPoints, yPoints, 3);

		} else {
			xPoints[0] = x1;
			xPoints[1] = x1 + 8;
			xPoints[2] = x1 + 8;
			yPoints[0] = y;
			yPoints[1] = y - 5;
			yPoints[2] = y + 5;
			g2d.fillPolygon(xPoints, yPoints, 3);
		}
	}

	/**
	 * TrgLevel的高宽，Arrow边缘的宽
	 */
	public static final int TrgLevelWidth = 18, TrgLevelHeight = 12,
			HalfTrgLevelHeight = TrgLevelHeight >> 1, ArrowEdgeWidth = 4;

	public static void paintFFTLabel(int y, LocRectangle lr, Graphics2D g2d,
			Color c, String n) {
		int[] xPoints = int5s0;
		int[] yPoints = int5s1;

		int y0 = lr.y0, y1 = lr.y1;
		int x0 = lr.x0;
		int tlw = 15, tlh = TrgLevelHeight, htlh = HalfTrgLevelHeight, arrw = 5;

		int ny, temp = y, num;

		if (y < y0) {
			if (temp < y0)
				temp = y0;
			xPoints[0] = x0 + tlw;
			yPoints[0] = temp;

			xPoints[1] = x0;
			yPoints[1] = temp;

			xPoints[2] = x0 + arrw;
			yPoints[2] = temp + tlh;

			xPoints[3] = x0 + tlw;
			yPoints[3] = temp + tlh;

			ny = temp + 11;
			num = 4;
		} else if (y > y1) {
			if (temp > y1)
				temp = y1;
			xPoints[0] = x0 + tlw;
			yPoints[0] = temp - tlh;

			xPoints[1] = x0 + arrw;
			yPoints[1] = temp - tlh;

			xPoints[2] = x0;
			yPoints[2] = temp;

			xPoints[3] = x0 + tlw;
			yPoints[3] = temp;

			ny = temp - 1;
			num = 4;
		} else {
			xPoints[0] = x0 + tlw;
			yPoints[0] = y - htlh;

			xPoints[1] = x0 + arrw;
			yPoints[1] = y - htlh;

			xPoints[2] = x0;
			yPoints[2] = y;

			xPoints[3] = x0 + arrw;
			yPoints[3] = y + htlh;

			xPoints[4] = x0 + tlw;
			yPoints[4] = y + htlh;

			ny = y + 5;
			num = 5;
		}

		g2d.setColor(Color.BLACK);
		g2d.fillPolygon(xPoints, yPoints, num);
		g2d.setColor(c);
		g2d.drawPolygon(xPoints, yPoints, num);
		g2d.drawString(n, x0 + 5, ny);
	}

	/**
	 * 触发电平标签(含是否高亮)，是否出现箭头框，是否画线
	 * 
	 * @param y
	 * @param lr
	 * @param g2d
	 * @param xPoints
	 * @param yPoints
	 * @param c
	 * @param select
	 * @param drawArrow
	 * @param n
	 */
	public static void paintTrgLevel(int y, LocRectangle lr, Graphics2D g2d,
			Color c, boolean select, boolean drawArrow, String n) {
		int y0 = lr.y0, y1 = lr.y1;
		int x0 = lr.x0, x1 = lr.x1;
		int tlw = TrgLevelWidth, tlh = TrgLevelHeight, htlh = HalfTrgLevelHeight, arrw = ArrowEdgeWidth;

		int ny, temp = y, num;
		drawArrow = select || drawArrow;// 补上，在拖拽时箭头就不会奇怪消失

		Color tmp = g2d.getColor();
		paintTrgLevelRightTriangel(g2d, y, y0, y1, x1, c);
		if (drawArrow) {
			int[] xPoints = int5s0;
			int[] yPoints = int5s1;
			if (y < y0 + htlh) {
				if (temp < y0)
					temp = y0;
				xPoints[0] = x1 - tlw;
				yPoints[0] = temp;

				xPoints[1] = x1;
				yPoints[1] = temp;

				xPoints[2] = x1 - arrw;
				yPoints[2] = temp + tlh;

				xPoints[3] = x1 - tlw;
				yPoints[3] = temp + tlh;

				ny = temp + 11;
				num = 4;
			} else if (y > y1 - htlh) {
				if (temp > y1)
					temp = y1;
				xPoints[0] = x1 - tlw;
				yPoints[0] = temp - tlh;

				xPoints[1] = x1 - arrw;
				yPoints[1] = temp - tlh;

				xPoints[2] = x1;
				yPoints[2] = temp;

				xPoints[3] = x1 - tlw;
				yPoints[3] = temp;

				ny = temp - 1;
				num = 4;
			} else {
				xPoints[0] = x1 - tlw;
				yPoints[0] = y - htlh;

				xPoints[1] = x1 - arrw;
				yPoints[1] = y - htlh;

				xPoints[2] = x1;
				yPoints[2] = y;

				xPoints[3] = x1 - arrw;
				yPoints[3] = y + htlh;

				xPoints[4] = x1 - tlw;
				yPoints[4] = y + htlh;

				ny = y + 5;
				num = 5;
			}

			if (select) {
				g2d.setColor(c);
				g2d.fillPolygon(xPoints, yPoints, num);
				g2d.setColor(Color.BLACK);
				g2d.drawPolygon(xPoints, yPoints, num);
			} else {
				g2d.setColor(Color.BLACK);
				g2d.fillPolygon(xPoints, yPoints, num);
				g2d.setColor(c);
				g2d.drawPolygon(xPoints, yPoints, num);
			}

			g2d.drawString(n, x1 - 13, ny);
		}
		if (select) {
			if (y >= y0 && y <= y1) {
				g2d.setColor(c);
				LineDrawTool.drawNormalSashLine(g2d, false, x0, x1 - tlw, y);
			}
		}
		g2d.setColor(tmp);
	}

	/**
	 * 画触发电平标志的信息框
	 * 
	 * @param g2d
	 * @param r
	 * @param trgpos
	 * @param info
	 */
	public static void paintTrgLevelDetail(Graphics2D g2d, LocRectangle r,
			int trgpos, String info, Color c) {
		int gap = 4;
		Rectangle2D infoRec = g2d.getFontMetrics().getStringBounds(info, g2d);
		int infow = (int) infoRec.getWidth() + (gap << 1), infoh = (int) infoRec
				.getHeight();
		int x = r.x1 - infow - 25 - gap;// "T"箭头框间隔25
		// 限制值、框边界
		if (trgpos <= r.y0 + infoh)
			trgpos = r.y0 + infoh;
		else if (trgpos >= r.y1)
			trgpos = r.y1;

		g2d.setColor(Color.BLACK);
		g2d.fillRect(x, trgpos - infoh, infow, infoh);
		g2d.setColor(c);
		g2d.drawString(info, x + gap, trgpos - 2);
		g2d.drawRect(x, trgpos - infoh, infow, infoh);
	}

	/**
	 * 阈值上下限标签，是否画线，是否高亮
	 * 
	 * @param y
	 * @param lr
	 * @param g2d
	 * @param xPoints
	 * @param yPoints
	 * @param lineTrgLevel
	 * @param highlight
	 * @param c
	 * @param n
	 */
	public static void paintThredsholds(int y, LocRectangle lr, Graphics2D g2d,
			boolean lineTrgLevel, boolean highlight, Color c, String n) {
		int[] xPoints = int5s0;
		int[] yPoints = int5s1;
		int y0 = lr.y0, y1 = lr.y1;
		int x0 = lr.x0, x1 = lr.x1;
		int tlw = TrgLevelWidth, tlh = TrgLevelHeight, htlh = HalfTrgLevelHeight, arrw = ArrowEdgeWidth;

		int ny, temp = y, num;

		if (y < y0 + htlh) {
			if (temp < y0)
				temp = y0;
			xPoints[0] = x1 - tlw;
			yPoints[0] = temp;

			xPoints[1] = x1;
			yPoints[1] = temp;

			xPoints[2] = x1 - arrw;
			yPoints[2] = temp + tlh;

			xPoints[3] = x1 - tlw;
			yPoints[3] = temp + tlh;

			num = 4;
			ny = temp + 11;
		} else if (y > y1 - htlh) {
			if (temp > y1)
				temp = y1;
			xPoints[0] = x1 - tlw;
			yPoints[0] = temp - tlh;

			xPoints[1] = x1 - arrw;
			yPoints[1] = temp - tlh;

			xPoints[2] = x1;
			yPoints[2] = temp;

			xPoints[3] = x1 - tlw;
			yPoints[3] = temp;

			num = 4;
			ny = temp - 1;
		} else {
			xPoints[0] = x1 - tlw;
			yPoints[0] = y - htlh;

			xPoints[1] = x1 - arrw;
			yPoints[1] = y - htlh;

			xPoints[2] = x1;
			yPoints[2] = y;

			xPoints[3] = x1 - arrw;
			yPoints[3] = y + htlh;

			xPoints[4] = x1 - tlw;
			yPoints[4] = y + htlh;

			num = 5;
			ny = y + 5;
		}

		Color tmp = g2d.getColor();
		if (highlight) {
			g2d.setColor(c);
			g2d.fillPolygon(xPoints, yPoints, num);
			g2d.setColor(Color.BLACK);
		} else {
			g2d.setColor(Color.BLACK);
			g2d.fillPolygon(xPoints, yPoints, num);
			g2d.setColor(c);
			g2d.drawPolygon(xPoints, yPoints, num);
		}

		g2d.drawString(n, x1 - 16, ny);

		if (lineTrgLevel) {
			if (y >= y0 && y <= y1) {
				g2d.setColor(c);
				LineDrawTool.drawNormalSashLine(g2d, false, x0, x1 - tlw, y);
			}
		}
		g2d.setColor(tmp);
	}

	public static final void paintPrompt(Graphics2D g2d, int widthcenter,
			int highcenter, Color c, String n) {
		FontMetrics fm = g2d.getFontMetrics();
		Rectangle2D r2d = fm.getStringBounds(n, g2d);
		int x = widthcenter - ((int) r2d.getWidth() >> 1);
		int y = highcenter;// + fm.getDescent();
		g2d.setColor(c);
		g2d.drawString(n, x, y);
	}

	private static final int[] int5s0 = new int[5];
	private static final int[] int5s1 = new int[5];
}
