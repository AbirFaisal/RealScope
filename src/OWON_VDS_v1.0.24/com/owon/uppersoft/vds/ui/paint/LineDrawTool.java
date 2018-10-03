package com.owon.uppersoft.vds.ui.paint;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

public class LineDrawTool {
	public static AlphaComposite getAlphaComposite(float v) {
		return AlphaComposite.getInstance(AlphaComposite.SRC_OVER, v);
	}

	/**
	 * 在给定的维度值loc1上，从中间向两边画虚线
	 * 
	 * @param g2d
	 * @param vertical
	 *            是否垂直
	 * @param center
	 *            中间的维度坐标
	 * @param start
	 *            起始的维度坐标
	 * @param end
	 *            终止的维度坐标
	 * @param loc1
	 *            另一维度的坐标
	 */
	public static final void drawSashLine(Graphics2D g2d, boolean vertical,
			int center, int start, int end, int loc1, int gap) {
		if (start == end)
			return;
		if (start > end) {
			int t = start;
			start = end;
			end = t;
		}
		int linelen = 1;// 0
		int loc0 = center;
		if (vertical) {
			while (loc0 > start) {
				g2d.drawLine(loc1, loc0, loc1, loc0 -= linelen);
				loc0 -= gap;
			}
			loc0 = center;
			while (loc0 < end) {
				g2d.drawLine(loc1, loc0, loc1, loc0 += linelen);
				loc0 += gap;
			}
		} else {
			while (loc0 > start) {
				g2d.drawLine(loc0, loc1, loc0 -= linelen, loc1);
				loc0 -= gap;
			}
			loc0 = center;
			while (loc0 < end) {
				g2d.drawLine(loc0, loc1, loc0 += linelen, loc1);
				loc0 += gap;
			}
		}
	}

	/**
	 * 在给定的维度值loc1上画实线
	 * 
	 * @param g2d
	 * @param vertical
	 *            是否垂直
	 * @param start
	 *            起始的维度坐标
	 * @param end
	 *            终止的维度坐标
	 * @param loc1
	 *            另一维度的坐标
	 */
	public static final void drawLine(Graphics2D g2d, boolean vertical,
			int start, int end, int loc1) {
		if (vertical) {
			g2d.drawLine(loc1, start, loc1, end);
		} else {
			g2d.drawLine(start, loc1, end, loc1);
		}
	}

	/**
	 * 在给定的维度值loc1上，从中间向两边画虚线
	 * 
	 * @param g2d
	 * @param vertical
	 *            是否垂直
	 * @param center
	 *            中间的维度坐标
	 * @param start
	 *            起始的维度坐标
	 * @param end
	 *            终止的维度坐标
	 * @param loc1
	 *            另一维度的坐标
	 */
	public static final void drawNormalSashLine(Graphics2D g2d,
			boolean vertical, int start, int end, int loc1) {
		if (start == end)
			return;
		if (start > end) {
			int t = start;
			start = end;
			end = t;
		}
		int linelen = 2, gap = 6, v = start, v1;
		if (vertical) {
			while (v < end) {
				v1 = v + linelen;
				if (v1 >= end)
					v1 = end;
				g2d.drawLine(loc1, v, loc1, v1);
				v += gap;
			}
		} else {
			while (v < end) {
				v1 = v + linelen;
				if (v1 >= end)
					v1 = end;
				g2d.drawLine(v, loc1, v1, loc1);
				v += gap;
			}
		}
	}

	public static BufferedImage fillTriangle(int w, int h, int[] xPoints,
			int[] yPoints, Color c) {
		BufferedImage pi = new BufferedImage(30, 32,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = pi.createGraphics();
		g2d.setColor(c);
		g2d.fillPolygon(xPoints, yPoints, 3);
		g2d.dispose();
		return pi;
	}

	public static void drawUnequalDashesLine(Graphics2D g2d, int start,
			int loc, int end) {
		final int l = 5, gap = 3, s = 2;
		int tempEnd = start + l;
		while (tempEnd < end) {
			g2d.drawLine(loc, start, loc, tempEnd);
			start = tempEnd + gap;
			tempEnd = start + s;
			if (tempEnd >= end)
				return;
			g2d.drawLine(loc, start, loc, tempEnd);
			start = tempEnd + gap;
			tempEnd = start + l;
		}
	}

	public static ImageIcon getRolloverIcon(ImageIcon icon) {
		BufferedImage pi = new BufferedImage(icon.getIconWidth() + 1, icon
				.getIconHeight() + 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = pi.createGraphics();
		// g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		// RenderingHints.VALUE_ANTIALIAS_ON);
		// int w = pi.getWidth(), h = pi.getHeight();
		// g2d.setColor(Color.LIGHT_GRAY);
		// g2d.fillRoundRect(2, 0, w, h , 5, 5);
		// g2d.setColor(Color.DARK_GRAY);
		// g2d.drawRoundRect(2, 0, w, h, 5, 5);
		g2d.drawImage(icon.getImage(), 1, 1, null);
		// g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		// RenderingHints.VALUE_ANTIALIAS_OFF);
		g2d.dispose();
		return new ImageIcon(pi);
	}

}
