package com.owon.uppersoft.vds.core.rt;

import java.awt.Graphics2D;
import java.nio.IntBuffer;

import com.owon.uppersoft.dso.util.DBG;

public class WFDrawRTUtil {
	public static final int DEFINE_AREA_WIDTH = 1000;

	public static final int FirstValueIdx = 0;
	public static final int MinValueIdx = 1;
	public static final int MaxValueIdx = 2;
	public static final int LastValueIdx = 3;

	public static final int FirstValueIdxRev = -3;
	public static final int MinValueIdxRev = -2;
	public static final int MaxValueIdxRev = -1;
	public static final int LastValueIdxRev = 0;

	/** TODO All of the following connect adjacent pixels with lines; could be optimized to draw vertical lines */
	public static void paint4in1(Graphics2D g2d, int p, int len, int xb,
			int[] array, boolean linkline) {// 4k
		int x = xb, end = p + len;

		if (linkline) {
			if ((p < end)) {
				/** Connect all values so that it can be checked whether the starting value exceeds the extreme range */
				g2d.drawLine(x, array[p // + FirstValueIdx, removed because it is 0, optimization
						], x, array[p + MinValueIdx]);
				g2d.drawLine(x, array[p + MaxValueIdx], x, array[p
						+ LastValueIdx]);

				g2d.drawLine(x, array[p + MinValueIdx], x, array[p
						+ MaxValueIdx]);
				int lastv = array[p + LastValueIdx];
				p += 4;
				x++;

				while (p < end) {
					/** Using x for both here makes a vertical line */
					//Not drawing the line on the same x makes the waveform break at the ns scale after stretching
					g2d.drawLine(x - 1, lastv, x , array[p
					// + FirstValueIdx, removed because it is 0, optimization
							]);

					/** Connect all values so that it can be checked whether the starting value exceeds the extreme range */
					g2d.drawLine(x, array[p
					// + FirstValueIdx, removed because it is 0, optimization
							], x, array[p + MinValueIdx]);
					g2d.drawLine(x, array[p + MaxValueIdx], x, array[p
							+ LastValueIdx]);

					g2d.drawLine(x, array[p + MinValueIdx], x, array[p
							+ MaxValueIdx]);
					lastv = array[p + LastValueIdx];
					p += 4;
					x++;
				}
			}
		} else {
			int y;
			while (p < end) {
				y = array[p++];
				g2d.drawLine(x, y, x, y);
				y = array[p++];
				g2d.drawLine(x, y, x, y);
				y = array[p++];
				g2d.drawLine(x, y, x, y);
				y = array[p++];
				g2d.drawLine(x, y, x, y);
				x++;
			}
		}
	}

	/**
	 * Reverse traversal point drawing algorithm, not used yet.
	 * 
	 * TODO All of the following connect adjacent pixels with lines; could be optimized to draw vertical lines
	 */
	protected static void paint4in1Rev(Graphics2D g2d, int p, int l, int xbe,
			int[] array, boolean linkline) {// 4k
		int x = xbe;

		if ((p < l)) {
			g2d.drawLine(x, array[l + MinValueIdxRev], x, array[l
					+ MaxValueIdxRev]);
			int lastv = array[l];// + LastValueIdxRev
			l -= 4;
			x--;

			while (p < l) {
				g2d.drawLine(x, array[l + MinValueIdxRev], x, array[l
						+ MaxValueIdxRev]);
				/** Using x for both here makes a vertical line */
				g2d.drawLine(x - 1, lastv, x, array[l]);
				lastv = array[l];// + LastValueIdxRev

				l -= 4;
				x--;
			}
		}

	}

	/**
	 * Draw the line on the same x to avoid slanted lines g2d.drawLine(x0, y0, x, y);
	 */
	public static void paintScreen1k(Graphics2D g2d, int p, int l, int xb,
			int[] array, boolean linkline) {// 1k,999
		int x = xb, y;
		// System.err.print(x + "->");
		if (linkline) {
			int y0 = array[p++];
			x++;
			while (p < l) {
				y = array[p++];
				g2d.drawLine(x-1, y0, x, y);//Not drawing the line on the same x makes the waveform break at the ns scale after stretching
				y0 = y;
				x++;
			}
		} else {
			while (p < l) {
				y = array[p++];
				g2d.drawLine(x, y, x, y);
				x++;
			}
		}
		// System.err.print(x + "->");
	}

	/**
	 * Reverse traversal point drawing algorithm, not used yet.
	 * 
	 * Draw the line on the same x to avoid slanted lines g2d.drawLine(x0, y0, x, y);
	 */
	protected static void paintScreen1kRev(Graphics2D g2d, int p, int l,
			int xbe, int[] array, boolean linkline) {// 1k,999

		int x = xbe, y;
		l--;
		if (linkline) {
			int y0 = array[l--];
			x--;
			while (p <= l) {
				y = array[l--];
				g2d.drawLine(x, y0, x, y);
				y0 = y;
				x--;
			}
		} else {
			while (p <= l) {
				y = array[l--];
				g2d.drawLine(x, y, x, y);
				x--;
			}
		}
	}

	public static void paintScreen2k(Graphics2D g2d, int p, int l, int xb,
			int[] array, boolean linkline) {// 2k, 1999

		int x = xb, y;
		int y0;

		int ll = l;
		if ((l - p) % 2 != 0)
			ll -= 1;

		if (linkline) {
			if ((p < l)) {
				y = array[p++];
				y0 = array[p++];
				g2d.drawLine(x, y0, x, y);

				while (p < ll) {
					/** y and y0 are used alternately as the current point */
					y = array[p++];
					/** Using x+1 for both here makes a vertical line */
					g2d.drawLine(x, y0, ++x, y);

					y0 = array[p++];
					g2d.drawLine(x, y0, x, y);
				}
				if (p < l - 1) {
					y = array[p++];
					/** Using x+1 for both here makes a vertical line */
					g2d.drawLine(x, y0, ++x, y);
				}
			}
		} else {
			while (p < ll) {
				y = array[p++];
				g2d.drawLine(x, y, x, y);

				y = array[p++];
				g2d.drawLine(x, y, x, y);

				x++;
			}
			if (p < l - 1) {
				y = array[p++];
				g2d.drawLine(x, y, x, y);
			}

		}
	}

	public static void paintScreen2_5k(Graphics2D g2d, int p, int len, int xb,
			int[] array, boolean linkline) {// 2.5k
		if (linkline) {
			int y, y0, x = xb, end = p + len, tx;
			if (p < end) {
				y0 = array[p++];
				y = array[p++];
				g2d.drawLine(x, y, x, y0);
				tx = x++;

				y0 = array[p++];
				g2d.drawLine(tx, y, x, y0);
				y = array[p++];
				g2d.drawLine(x, y, x, y0);
				y0 = array[p++];
				g2d.drawLine(x, y, x, y0);
				tx = x++;

				y0 = y;
				while (p < end) {
					y = array[p++];
					g2d.drawLine(x, y, tx, y0);

					y0 = array[p++];
					g2d.drawLine(x, y, x, y0);
					tx = x++;

					y = array[p++];
					g2d.drawLine(x, y, tx, y0);
					y0 = array[p++];
					g2d.drawLine(x, y, x, y0);
					y = array[p++];
					g2d.drawLine(x, y, x, y0);
					tx = x++;

					y0 = y;
				}
			}
		} else {
			int y, x = xb, end = p + len;
			while (p < end) {
				y = array[p++];
				g2d.drawLine(x, y, x, y);
				y = array[p++];
				g2d.drawLine(x, y, x, y);
				x++;

				y = array[p++];
				g2d.drawLine(x, y, x, y);
				y = array[p++];
				g2d.drawLine(x, y, x, y);
				y = array[p++];
				g2d.drawLine(x, y, x, y);
				x++;
			}
		}
	}

	/**
	 * 1,1,1,2; the last pixel draws 2 points
	 * 
	 * @param g2d
	 * @param linkline
	 * @param array
	 * @param xb
	 * @param beg
	 * @param len
	 */
	public static final void paint4_5(Graphics2D g2d, boolean linkline,
			int[] array, int xb, int beg, int len) {
		if (linkline) {
			int p = beg, y, y0, x = xb, end = p + len, tx;
			if (p < end) {
				y0 = array[p++];
				y = array[p++];
				tx = x++;
				g2d.drawLine(x, y, tx, y0);
				tx = x++;

				y0 = array[p++];
				g2d.drawLine(tx, y, x, y0);
				tx = x++;

				y = array[p++];
				g2d.drawLine(x, y, tx, y0);
				tx = x++;

				y0 = array[p++];
				g2d.drawLine(tx, y, x, y0);
				y = array[p++];
				g2d.drawLine(x, y, x, y0);
				tx = x++;
				while (p < end) {
					y0 = array[p++];
					g2d.drawLine(tx, y, x, y0);
					tx = x++;

					y = array[p++];
					g2d.drawLine(x, y, tx, y0);
					tx = x++;

					y0 = array[p++];
					g2d.drawLine(tx, y, x, y0);
					tx = x++;

					y = array[p++];
					g2d.drawLine(x, y, tx, y0);
					y0 = array[p++];
					g2d.drawLine(x, y, x, y0);

					tx = x++;
				}
			}
		} else {
			int p = beg, y, x = xb, end = p + len;
			while (p < end) {
				y = array[p++];
				g2d.drawLine(x, y, x, y);
				x++;
				y = array[p++];
				g2d.drawLine(x, y, x, y);
				x++;
//				y = array[p++];
//				g2d.drawLine(x, y, x, y);
//				x++;
				y = array[p++];
				g2d.drawLine(x, y, x, y);
				x++;

				y = array[p++];
				g2d.drawLine(x, y, x, y);
				y = array[p++];
				g2d.drawLine(x, y, x, y);
				x++;
			}
		}
	}

	public static void paintLess1k(Graphics2D g2d, int p, int l, int xb,
			int[] array, boolean linkline, int rate) {// 25,50,100,200,250,500

		int x = xb, y;
		if (linkline) {
			int y0 = array[p++];
			while (p < l) {
				y = array[p++];
				g2d.drawLine(x, y0, x += rate, y);
				y0 = y;
			}
		} else {
			while (p < l) {
				y = array[p++];
				g2d.drawLine(x, y, x, y);
				x += rate;
			}
		}
	}

	/**
	 * 2+3
	 * 
	 * @param g2d
	 * @param pc
	 * @param p
	 * @param l
	 * @param xb
	 * @param array
	 * @param linkline
	 * @param re
	 */
	public static void paint400(Graphics2D g2d, int p, int l, int xb,
			int[] array, boolean linkline, int re) {

		int x = xb, y;

		int c0 = const3, c1 = const2;
		if (re % 2 != 0) {
			int tmp = c0;
			c0 = c1;
			c1 = tmp;
		}
		if (linkline) {
			int y0 = array[p++];
			while (p < l) {
				y = array[p++];
				g2d.drawLine(x, y0, x += c0, y);

				if (p < l) {
					y0 = array[p++];
					g2d.drawLine(x, y, x += c1, y0);
				} else
					break;
			}
		} else {
			while (p < l) {
				y = array[p++];
				g2d.drawLine(x, y, x, y);
				x += c0;

				if (p < l) {
					y = array[p++];
					g2d.drawLine(x, y, x, y);
					x += c1;
				} else
					break;
			}
		}
	}

	public static void paintLess1k_double(Graphics2D g2d, int p, int l, int xb,
			int[] array, boolean linkline, double rate) {

		double x = xb;
		int y, xx;
		if (linkline) {
			int y0 = array[p++];
			while (p < l) {
				y = array[p++];
				xx = (int) (x + rate);
				g2d.drawLine((int) x, y0, xx, y);
				x += rate;
				y0 = y;
			}
		} else {
			while (p < l) {
				y = array[p++];
				xx = (int) x;
				g2d.drawLine(xx, y, xx, y);
				x += rate;
			}
		}
	}

	public static final int const3 = 3;
	public static final int const2 = 2;

	/**
	 * For waveforms with custom spacing
	 * 
	 * @param g2d
	 * @param pixbuf
	 * @param xb
	 * @param linkline
	 * @param gap
	 */
	public static void paintByGap(Graphics2D g2d, IntBuffer pixbuf, int xb,
			boolean linkline, double gap) {
		if (pixbuf == null)
			return;

		int[] array = pixbuf.array();
		int p = pixbuf.position();
		int l = pixbuf.limit();
		paintByGap(g2d, array, p, l, xb, linkline, gap);
	}

	/**
	 * For waveforms with custom spacing
	 * 
	 * @param g2d
	 * @param array
	 * @param p
	 * @param l
	 * @param xb
	 * @param linkline
	 * @param gap
	 */
	public static void paintByGap(Graphics2D g2d, int[] array, int p, int l,
			int xb, boolean linkline, double gap) {
		// int y, y0, x = xb, tx;
		int y, y0;// ++
		double x = xb, tx;// ++

		if (l <= p)
			return;

		if (linkline) {
			if (p < l) {
				y = array[p++];
				y0 = y;
				// g2d.drawLine(x, y, x, y0);
				g2d.drawLine((int) x, y, (int) x, y0);// ++
				tx = x;
				// x = (int) (x + gap);
				x = (x + gap);// ++
				while (p < l) {
					y = array[p++];
					g2d.drawLine((int) tx, y0, (int) x, y);
					tx = x;
					y0 = y;
					// x = (int) (x + gap);
					x = (x + gap);// ++
				}

			}
		} else {
			while (p < l) {
				y = array[p];
				// g2d.drawLine(x, y, x, y);
				g2d.drawLine((int) x, y, (int) x, y);// ++
				// x = (int) (x + gap);
				x = (x + gap);// ++
				p++;
			}
		}

	}

	/**
	 * Draw according to DrawMode
	 * 
	 * @param g2d
	 * @param pixbuf
	 * @param dm
	 * @param xb
	 * @param linkline
	 */
	public static void paintDrawMode(Graphics2D g2d, IntBuffer pixbuf, int dm,
			int xb, boolean linkline) {
		if (pixbuf == null)
			return;

		int[] array = pixbuf.array();
		int p = pixbuf.position();
		int l = pixbuf.limit();
		//DBG.errprintln(p + "," + l);
		//ArrayLogger.configArray(array, p + 500, l-p);

		if (l <= p)
			return;
		// DBG.dbgArray(array, p + 500, 10);
		switch (dm) {
		case DrawMode4in1:
			paint4in1(g2d, p, l, xb, array, linkline);
			break;
		case DrawMode2_5p:
			// WFDrawRTUtil.paintByGap(g2d, pixbuf, xb, linkline, 0.4);
			paintScreen2_5k(g2d, p, l, xb, array, linkline);
			break;
		case DrawMode2p:
			paintScreen2k(g2d, p, l, xb, array, linkline);
			break;
		case DrawMode4_5:
			// WFDrawRTUtil.paintByGap(g2d, pixbuf, xb, linkline, 0.8);
			paint4_5(g2d, linkline, array, xb, p, l - p);
			break;
		case DrawMode1p:
			paintScreen1k(g2d, p, l, xb, array, linkline);
			break;
		case DrawModeDouble:
			WFDrawRTUtil.paintByGap(g2d, pixbuf, xb, linkline,
					DEFINE_AREA_WIDTH / (double) (l - p));
			break;
		}
	}

	/** Actually the screen point spacing mode, Interval */
	public static final int DrawMode4_5 = 6;
	public static final int DrawMode1p = 1;
	public static final int DrawMode2p = 2;
	public static final int DrawMode2_5p = 3;
	public static final int DrawMode4in1 = 4;

	public static final int DrawModeDilute = 7;
	public static final int DrawModeDouble = 5;

	public static final int DrawModeLess1k = 21;
	public static final int DrawMode400 = 22;

	public static final int getDrawModeFromLength(int length) {
		int drawMode = -1;

		switch (length) {
		case 4000:// 4k
			drawMode = DrawMode4in1;
			break;
		case 2500:// 2.5k
			drawMode = DrawMode2_5p;
			break;
		case 2048:
			// case 1999:
		case 2000:// 2k,1999
			drawMode = DrawMode2p;
			break;
		case 1000:
			// case 999:// 1k,999
			drawMode = DrawMode1p;
			break;
		case 1250:
			drawMode = DrawMode4_5;
			break;
		default:
			DBG.errprintln("Tlen: can't be matched " + length);
			break;
		}
		return drawMode;
	}
}
