package com.owon.uppersoft.vds.core.wf.peak;

import java.awt.Color;
import java.awt.Graphics2D;
import java.nio.IntBuffer;

/**
 * 目前之供Smart使用，Tiny可能有所不同
 * 
 */
public class PKDetectDrawUtil {

	public static final int pk_detect_high_gap = 10;

	public static final int PK_DETECT_TYPE_NO = -1;

	public static final int PK_DETECT_TYPE_1K_MINMAXMINMAX_4PIX = 1;
	public static final int PK_DETECT_TYPE_4K_MINMAXMINMAX_1PIX = 2;
	public static final int PK_DETECT_TYPE_2K_MINMAX_1PIX = 3;

	/**
	 * 保持2500在峰值检测时的普通画法，验证效果，再考虑是否针对峰值检测研究更遵循采样方式的画法，
	 * 
	 * 对应真实adc采集满屏2500个点的情况，5对峰值放在4个像素点上
	 */
	public static final int PK_DETECT_TYPE_2500_2_5p = 5;

	/** 点数为1250，这是插值后，插值前是250，此档达不到pk的条件 */
	public static final int PK_DETECT_TYPE_1250_4_5p = 6;

	public static final int PK_DETECT_TYPE_4K_BEGMINMAXEND_1PIX = 4;// DM

	public static final int PK_DETECT_TYPE_1K_MINMAX_2PIX = 7;

	/**
	 * 画最值而不连线，用于测试
	 */
	public static final boolean draw_no_link = (0 == 1);

	/**
	 * 在MINMAXMINMAX的时候拿到的可能会是偏了2个数据点的数据，但是仍然可用
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
		/** 画上下通道，开上下通道不同颜色的话可以检查max和min颠倒的情况 */
		switch (pk_detect_type) {
		case PK_DETECT_TYPE_1K_MINMAX_2PIX: {
			/**
			 * min, max，数据只有1k,隔两个像素连线, 数据1k, 大部分1k的情况, 由于1k间隔取点导致布不满屏幕，最右差1个像素
			 * 
			 * 数值隔2点，x坐标隔2点
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
			 * min, max， min, max，数据只有1k,隔4个像素连线, 数据1k, 仅出现在1k存深500ns时基pk时,
			 * 由于1k间隔取点导致布不满屏幕，最右差3个像素
			 * 
			 * 数值隔4点，x坐标隔4点
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
			 * min,max,min,max->max,min，用在1个像素上, 数据4k
			 * 
			 * 数值隔4点，x坐标隔1点
			 */
			prt_gap = 4;
			xgap = 1;

			// 防折线 pos += 2;
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
			 * min,max，用在1个像素上, 数据2k
			 * 
			 * 数值隔2点，x坐标隔1点
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
			 * beg, min, max, end, 数据4k,深存储压缩后
			 * 
			 * 数值隔4点，x坐标隔1点
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
			// 研究采集原理
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
		/** 画阴影 */
		if (!draw_no_link && linelink) {
			// g2d.setColor(Color.orange);
			paint_integral(g2d, xb, yb, screenheight, array, minbeg, maxbeg,
					minend, prt_gap, xgap);
			// 开启则可针对PK_DETECT_TYPE_MINMAX_2PIX，补全相邻间隔的像素
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

			/** 这里的判断的前提是不确定max和min各是哪个的情况，但是这里它们已经确定了，所以可以不用判断 */
			if (y < y0) {
				tmp = y;
				y = y0;
				y0 = tmp;
			}
			/** 对每一个x值，垂直放下上y值在(x-1000,x+500)的范围内每个定长画点 ，这一范围再次被缩小到(x%hgap, h) */
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
