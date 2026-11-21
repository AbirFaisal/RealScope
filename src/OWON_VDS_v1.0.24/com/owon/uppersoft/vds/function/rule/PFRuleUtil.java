package com.owon.uppersoft.vds.function.rule;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * PFRuleUtil generation of rule point sets and drawing of boundaries
 * 
 */
public class PFRuleUtil {
	/**
	 * Create the boundary points conforming to the rule
	 * 
	 * @param buf
	 * @param adc_min
	 * @param adc_max
	 * @param rule_min
	 * @param rule_max
	 * @param R_horizontalSet
	 * @param R_verticalSet
	 * @return
	 */
	public static final int createRulePoints(IntBuffer buf, int[] adc_min,
			int[] adc_max, int[] rule_min, int[] rule_max, int R_horizontalSet,
			int R_verticalSet) {
		// DBG.config("create");
		int dataNum = distributeADCPoints(buf, adc_min, adc_max);

		fillRulePoints(dataNum, adc_min, adc_max, rule_min, rule_max,
				R_horizontalSet, R_verticalSet);

		// DBG.config(String.format("dataNum:%d, %f", dataNum, r));
		return dataNum;
	}

	/**
	 * Create the boundary points conforming to the rule
	 * 
	 * @param buf
	 * @param adc_min
	 * @param adc_max
	 * @param rule_min
	 * @param rule_max
	 * @param R_horizontalSet
	 * @param R_verticalSet
	 * @return
	 */
	public static final int createRulePoints(ByteBuffer buf, int[] adc_min,
			int[] adc_max, int[] rule_min, int[] rule_max, int R_horizontalSet,
			int R_verticalSet) {
		// DBG.config("create");
		int dataNum = distributeADCPoints(buf, adc_min, adc_max);

		fillRulePoints(dataNum, adc_min, adc_max, rule_min, rule_max,
				R_horizontalSet, R_verticalSet);

		// DBG.config(String.format("dataNum:%d, %f", dataNum, r));
		return dataNum;
	}

	/**
	 * Take the extreme values of the adc points for each pixel and distribute them evenly into the array
	 * 
	 * @param buf
	 * @param adc_min
	 * @param adc_max
	 * @return
	 */
	public static final int distributeADCPoints(ByteBuffer buf, int[] adc_min,
			int[] adc_max) {
		int p = buf.position(), l = buf.limit();
		int len = l - p;
		byte[] arr = buf.array();

		double r, g = 0;
		r = (double) adc_max.length / len;

		// use the raw adc points, with the vertical center as 0, positive up and negative down
		// distribute the adc points evenly among the pixels and take the maximum and minimum values
		for (int i = p, j = 0, v; i < l; i++, g = g + r, j = (int) g) {
			v = arr[i];
			if (adc_min[j] > v) {// min
				adc_min[j] = v;
			}
			if (adc_max[j] < v) {// max
				adc_max[j] = v;
			}
		}
		int dataNum = (int) g;
		return dataNum;
	}

	/**
	 * Take the extreme values of the adc points and distribute them evenly into the array
	 * 
	 * @param buf
	 * @param adc_min
	 * @param adc_max
	 * @return
	 */
	public static final int distributeADCPoints(IntBuffer buf, int[] adc_min,
			int[] adc_max) {
		int p = buf.position(), l = buf.limit();
		int len = l - p;
		int[] arr = buf.array();

		double r, g = 0;
		r = (double) adc_max.length / len;

		// use the raw adc points, with the vertical center as 0, positive up and negative down
		// distribute the adc points evenly among the pixels and take the maximum and minimum values
		for (int i = p, j = 0, v; i < l; i++, g = g + r, j = (int) g) {
			v = arr[i];
			if (adc_min[j] > v) {// min
				adc_min[j] = v;
			}
			if (adc_max[j] < v) {// max
				adc_max[j] = v;
			}
		}
		int dataNum = (int) g;
		return dataNum;
	}

	/**
	 * Fill the data boundaries conforming to the rule from the adc extreme values
	 * 
	 * @param dataNum
	 * @param adc_min
	 * @param adc_max
	 * @param rule_min
	 * @param rule_max
	 * @param R_horizontalSet
	 * @param R_verticalSet
	 */
	private static final void fillRulePoints(int dataNum, int[] adc_min,
			int[] adc_max, int[] rule_min, int[] rule_max, int R_horizontalSet,
			int R_verticalSet) {
		int y_max, y_min, start, end;
		for (int i = 0; i < dataNum; i++) {
			// calculate the 2 lines
			y_max = adc_max[i] + R_verticalSet;// the upper line
			y_min = adc_min[i] - R_verticalSet;// the lower line

			// take the range extending PFhorizontalSet forward and backward centered on point i, clamped within the total data range
			if (i < R_horizontalSet) {
				start = 0;
			} else {
				start = i - R_horizontalSet;
			}

			if (i > dataNum - R_horizontalSet) {
				end = dataNum;
			} else {
				end = i + R_horizontalSet;
			}

			// iterate over the points in this range,
			for (int data_max, data_min, j = start; j < end; j++) {
				data_max = adc_max[j] + R_verticalSet;
				data_min = adc_min[j] - R_verticalSet;

				// on the upper line, find the larger value within a shift of PFhorizontalSet to the left or right and use it. This widens the upper bound outward
				if (y_max < data_max) {
					y_max = data_max;
				}

				// on the lower line, find the smaller value within a shift of PFhorizontalSet to the left or right and use it. This widens the lower bound outward
				if (y_min > data_min) {
					y_min = data_min;
				}
			}
			rule_max[i] = y_max;
			rule_min[i] = y_min;
		}
	}

	/**
	 * @param rule_bi
	 * @param pc
	 * @param rule_min
	 * @param rule_max
	 * @param data_num
	 */
	public static final void paintRuleArea(Graphics2D g2d, boolean ScreenMode_3,
			Rectangle r, int[] rule_min, int[] rule_max, int data_num,
			int xoffset, int rulewidth) {
		Shape s = g2d.getClip();
		g2d.setClip(r);

		// DBG.config("paint\n");
		// Rectangle r = pc.getCurrentLocInfo();
		int xb = r.x + xoffset, h = r.height, hh = h >> 1, yb = r.y + hh;

		int ty1, ty2;
		int y1, y2;
		int x1, x2;
		x1 = x2 = xb;
		g2d.setColor(Color.MAGENTA);

		int i = xoffset, end = xoffset + rulewidth;
		if (ScreenMode_3) {
			ty1 = yb - rule_min[i];
			ty2 = yb - rule_max[i];
			x1++;
			x2++;
			i++;
			for (; i < end; i++) {
				y1 = yb - rule_min[i];
				y2 = yb - rule_max[i];
				g2d.drawLine(x1, ty1, ++x1, y1);
				g2d.drawLine(x2, ty2, ++x2, y2);

				ty1 = y1;
				ty2 = y2;
			}
		} else {
			ty1 = yb - (rule_min[i] << 1);
			ty2 = yb - (rule_max[i] << 1);
			x1++;
			x2++;
			i++;
			for (; i < end; i++) {
				y1 = yb - (rule_min[i] << 1);
				y2 = yb - (rule_max[i] << 1);
				g2d.drawLine(x1, ty1, ++x1, y1);
				g2d.drawLine(x2, ty2, ++x2, y2);

				ty1 = y1;
				ty2 = y2;
			}

		}

		coverRuleArea(g2d, rule_min, rule_max, ScreenMode_3, r.x, hh, r.y,
				xoffset, rulewidth, h);

		g2d.setClip(s);
		g2d.dispose();
	}

	/**
	 * @param g2d
	 * @param rule_min
	 * @param rule_max
	 * @param ScreenMode_3
	 * @param xb
	 * @param hh
	 * @param ry
	 * @param rulewidth
	 * @param h
	 */
	public static final void coverRuleArea(Graphics2D g2d, int[] rule_min,
			int[] rule_max, boolean ScreenMode_3, int xb, int hh, int ry,
			int xoffset, int rulewidth, int h) {
		int yb = ry + hh, y0 = ry;
		int yl = ry + h;
		g2d.setColor(Color.MAGENTA);
		int pfx = 0 + xb, v, x;
		boolean even;
		int i = xoffset, end = xoffset + rulewidth;
		if (ScreenMode_3) {
			for (; i < end; i++) {
				even = (i % 2 == 0);
				x = pfx + i;
				// can be optimized further by merging the odd/even check steps
				v = yb - rule_min[i];
				coverHalfArea_decr(v, yl, x, -2, -1, g2d, even);
				v = yb - rule_max[i];
				coverHalfArea_incr(v, y0, x, 2, 1, g2d, even);
			}
		} else {
			for (; i < end; i++) {
				even = (i % 2 == 0);
				x = pfx + i;
				// can be optimized further by merging the odd/even check steps
				v = yb - (rule_min[i] << 1);
				coverHalfArea_decr(v, yl, x, -2, -1, g2d, even);
				v = yb - (rule_max[i] << 1);
				coverHalfArea_incr(v, y0, x, 2, 1, g2d, even);
			}
		}
	}

	/**
	 * @param v
	 * @param y0
	 * @param x
	 * @param del
	 * @param del2
	 * @param g2d
	 * @param even
	 */
	public static final void coverHalfArea_incr(int v, int y0, int x, int del,
			int del2, Graphics2D g2d, boolean even) {
		if (even) {
			for (int j = y0; j <= v;) {
				g2d.drawLine(x, j, x, j);
				j = j + del;
			}
		} else {
			for (int j = y0; j <= v;) {
				g2d.drawLine(x, j + del2, x, j + del2);
				j = j + del;
			}
		}
	}

	/**
	 * @param v
	 * @param y0
	 * @param x
	 * @param del
	 * @param del2
	 * @param g2d
	 * @param even
	 */
	public static final void coverHalfArea_decr(int v, int y0, int x, int del,
			int del2, Graphics2D g2d, boolean even) {
		if (even) {
			for (int j = y0; j >= v;) {
				g2d.drawLine(x, j, x, j);
				j = j + del;
			}
		} else {
			for (int j = y0; j >= v;) {
				g2d.drawLine(x, j + del2, x, j + del2);
				j = j + del;
			}
		}
	}

}
