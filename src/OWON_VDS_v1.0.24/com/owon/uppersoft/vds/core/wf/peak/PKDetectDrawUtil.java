package com.owon.uppersoft.vds.core.wf.peak;

import java.awt.Color;
import java.awt.Graphics2D;
import java.nio.IntBuffer;

/**
 * Currently only for Smart; Tiny may differ
 * 
 */
public class PKDetectDrawUtil {

	public static final int pk_detect_high_gap = 10;

	public static final int PK_DETECT_TYPE_NO = -1;

	public static final int PK_DETECT_TYPE_1K_MINMAXMINMAX_4PIX = 1;
	public static final int PK_DETECT_TYPE_4K_MINMAXMINMAX_1PIX = 2;
	public static final int PK_DETECT_TYPE_2K_MINMAX_1PIX = 3;

	/**
	 * Keep the ordinary drawing at 2500 for peak detect to verify the effect, then consider whether to study a drawing method that follows the sampling method more closely for peak detect,
	 * 
	 * Corresponds to the real adc acquisition filling the full screen with 2500 points, with 5 pairs of peaks placed on 4 pixels
	 */
	public static final int PK_DETECT_TYPE_2500_2_5p = 5;

	/** The point count is 1250, which is after interpolation; before interpolation it is 250, and this scale cannot meet the pk condition */
	public static final int PK_DETECT_TYPE_1250_4_5p = 6;

	public static final int PK_DETECT_TYPE_4K_BEGMINMAXEND_1PIX = 4;// DM

	public static final int PK_DETECT_TYPE_1K_MINMAX_2PIX = 7;

	/**
	 * Draw extrema without connecting lines, for testing
	 */
	public static final boolean draw_no_link = (0 == 1);

	/**
	 * At MINMAXMINMAX the data obtained may be offset by 2 data points, but it is still usable
	 * 
	 * @param g2d
	 * @param pixbuf
	 * @param xb
	 * @param yb
	 * @param screenheight
	 * @param pk_detect_type
	 * @param linelink
	 */
	public static final void paint_pkdetect(Graphics2D g2d, IntBuffer pixbuf,
			int xb, int yb, int screenheight, int pk_detect_type,
			boolean linelink) {
		int[] array = pixbuf.array();
		int pos = pixbuf.position();
		int limit = pixbuf.limit();
		if (limit <= pos)
			return;

		// System.err.println(pk_detect_type);

		int prt_gap, xgap;
		int maxbeg = pos + 1;
		int minbeg = pos;
		int maxend = limit;
		int minend = limit - 1;
		/** Draw upper and lower channels; enabling upper and lower channels in different colors allows checking for max and min being swapped */
		switch (pk_detect_type) {
		case PK_DETECT_TYPE_1K_MINMAX_2PIX: {
			/**
			 * min, max, data is only 1k, connect lines every two pixels, data 1k, most 1k cases, because taking points at 1k intervals cannot fill the screen, the rightmost is 1 pixel short
			 * 
			 * Values every 2 points, x coordinate every 2 points
			 */
			prt_gap = 2;
			xgap = 2;
			// System.err.println(limit - pos);
			maxbeg = pos + 1;
			minbeg = pos;
			maxend = limit;
			minend = limit - 1;

			paint_pk_shadow_border(g2d, xb, yb, screenheight, linelink, array,
					prt_gap, xgap, maxbeg, minbeg, maxend, minend);
			break;
		}
		case PK_DETECT_TYPE_1K_MINMAXMINMAX_4PIX: {
			// System.err.println("dd");
			/**
			 * min, max, min, max, data is only 1k, connect lines every 4 pixels, data 1k, only occurs for pk at 1k memory depth 500ns timebase,
			 * because taking points at 1k intervals cannot fill the screen, the rightmost is 3 pixels short
			 * 
			 * Values every 4 points, x coordinate every 4 points
			 */
			prt_gap = 4;
			xgap = 4;

			maxbeg = pos + 1;
			minbeg = pos;
			maxend = limit;
			minend = limit - 1;

			paint_pk_shadow_border(g2d, xb, yb, screenheight, linelink, array,
					prt_gap, xgap, maxbeg, minbeg, maxend, minend);
			break;
		}
		case PK_DETECT_TYPE_4K_MINMAXMINMAX_1PIX: {
			/**
			 * min,max,min,max->max,min, used on 1 pixel, data 4k
			 * 
			 * Values every 4 points, x coordinate every 1 point
			 */
			prt_gap = 4;
			xgap = 1;

			// Avoid zigzag lines pos += 2;
			maxbeg = pos + 1;
			minbeg = pos;
			maxend = limit;
			minend = limit - 1;

			paint_pk_shadow_border(g2d, xb, yb, screenheight, linelink, array,
					prt_gap, xgap, maxbeg, minbeg, maxend, minend);
			break;
		}
		case PK_DETECT_TYPE_2K_MINMAX_1PIX: {
			/**
			 * min,max, used on 1 pixel, data 2k
			 * 
			 * Values every 2 points, x coordinate every 1 point
			 */
			prt_gap = 2;
			xgap = 1;

			maxbeg = pos + 1;
			minbeg = pos;
			maxend = limit;
			minend = limit - 1;

			paint_pk_shadow_border(g2d, xb, yb, screenheight, linelink, array,
					prt_gap, xgap, maxbeg, minbeg, maxend, minend);
			break;
		}
		case PK_DETECT_TYPE_4K_BEGMINMAXEND_1PIX: {
			/**
			 * beg, min, max, end, data 4k, after deep memory compression
			 * 
			 * Values every 4 points, x coordinate every 1 point
			 */
			prt_gap = 4;
			xgap = 1;
			// System.err.println(pk_detect_type);
			pos += 1;

			maxbeg = pos + 1;
			minbeg = pos;
			maxend = limit;
			minend = limit - 1;

			paint_pk_shadow_border(g2d, xb, yb, screenheight, linelink, array,
					prt_gap, xgap, maxbeg, minbeg, maxend, minend);
			break;
		}
			// Study the acquisition principle
		case PK_DETECT_TYPE_1250_4_5p: {
			// prt_gap = 2;
			// xgap = 2;
			//
			// maxbeg = pos + 1;
			// minbeg = pos;
			// maxend = limit;
			// minend = limit - 1;
			//
			// paint_pk_shadow_border(g2d, xb, yb, screenheight, linelink,
			// array,
			// prt_gap, xgap, maxbeg, minbeg, maxend, minend);
			break;
		}
		case PK_DETECT_TYPE_2500_2_5p: {
			// prt_gap = 2;
			// xgap = 2;
			//
			// maxbeg = pos + 1;
			// minbeg = pos;
			// maxend = limit;
			// minend = limit - 1;
			//
			// paint_pk_shadow_border(g2d, xb, yb, screenheight, linelink,
			// array,
			// prt_gap, xgap, maxbeg, minbeg, maxend, minend);
			break;
		}
		}

	}

	private static void paint_pk_shadow_border(Graphics2D g2d, int xb, int yb,
			int screenheight, boolean linelink, int[] array, int prt_gap,
			int xgap, int maxbeg, int minbeg, int maxend, int minend) {
		/** Draw shadow */
		if (!draw_no_link && linelink) {
			// g2d.setColor(Color.orange);
			paint_integral(g2d, xb, yb, screenheight, array, minbeg, maxbeg,
					minend, prt_gap, xgap);
			// If enabled, can fill in adjacent interval pixels for PK_DETECT_TYPE_MINMAX_2PIX
			// paint_integral(g2d, xb + 1, yb, screenheight, array, minbeg,
			// maxbeg, minend, prt_gap, xgap);
		}

		// maxs
		if (draw_no_link)
			g2d.setColor(Color.magenta);
		paint_terminus_xIncrt(g2d, xb, array, maxbeg, maxend, prt_gap, xgap,
				linelink);

		// mins
		if (draw_no_link)
			g2d.setColor(Color.pink);
		paint_terminus_xIncrt(g2d, xb, array, minbeg, minend, prt_gap, xgap,
				linelink);
	}

	private static void paint_terminus_xIncrt(Graphics2D g2d, int xb,
			int[] array, int beg, int end, int ptrIncr, int xIncr,
			boolean linelink) {
		if (linelink) {
			int x0 = xb;
			int i = beg;
			int y;
			int y0 = array[i];
			int x;// = x0;
			i += ptrIncr;
			while (i < end) {
				x = x0 + xIncr;
				y = array[i];
				g2d.drawLine(x0, y0, x, y);
				x0 = x;
				y0 = y;
				i += ptrIncr;
			}
			// System.err.println(xb + ", " + x0 + "; " + beg + ", " + i + "; "
			// + ptrIncr + ", " + xIncr);
		} else {
			int x = xb;
			int i = beg;
			int y;
			while (i < end) {
				y = array[i];
				g2d.drawLine(x, y, x, y);
				x += xIncr;
				i += ptrIncr;
			}
		}
	}

	private static void paint_integral(Graphics2D g2d, int xb, int yb,
			int screenhight, int[] array, int minPos, int maxPos, int minEnd,
			int ptrIncr, int xIncr) {
		int i = minPos;// min
		int j = maxPos;// max
		int tmp, yv;

		int y = 0;// max
		int x = xb;
		int y0;// min

		int maxy = screenhight + yb;
		int hgap = pk_detect_high_gap;
		while (i < minEnd) {
			y0 = array[i];
			y = array[j];

			/** The premise of the judgment here is that it is uncertain which of max and min is which, but here they are already determined, so the judgment can be omitted */
			if (y < y0) {
				tmp = y;
				y = y0;
				y0 = tmp;
			}
			/** For each x value, vertically draw a point at every fixed length for y values within the range (x-1000,x+500); this range is narrowed again to (x%hgap, h) */
			for (yv = (x % hgap) + yb; yv < maxy; yv += hgap) {
				if ((yv > y0) && (yv < y)) {
					g2d.drawLine(x, yv, x, yv);
				}
			}

			i += ptrIncr;
			j += ptrIncr;
			x += xIncr;
		}
	}

}
