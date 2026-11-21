package com.owon.uppersoft.dso.function;

/** Unimplemented methods
 *
 * To avoid grid flicker, the grid is drawn on canvas bi
 *  
 * TYPE_USHORT_565_RGB approach: each fade() redraws the grid and cursors, so the grid and cursors are above the waveform.
 * TODO
 * A better effect: the grid and cursors below the waveform.
 * 1. When to draw the grid and cursors? 
 *    When the GridBar color is dragged, persistence is cleared, the canvas is reset, and so on.
 * 2. Will the grid color be added onto the canvas? 
 *    In fade(), check and filter the positions that match black, filtering out the grid color to avoid darkening the grid.
 * 	  Specific method: store the color value of the grid after GrideBar is dragged in the canvas's TYPE_USHORT_565_RGB
 * format, pass the variable holding that value to fade(), and when fading, break out if the pixel equals that variable's value.
 * */

/**: modifications to the original approach:
 * TYPE_USHORT_565_RGB format is an opaque canvas, so the grid below is covered; there are 3 ways to handle it
 * 
 * 1. Add the grid to the canvas and filter the canvas color
 * 
 * 2. Add the grid to the canvas, check the grid coordinates, and redraw the grid if it is not covered by the waveform
 * 
 * 3. Change to a transparent canvas (the current TYPE_INT_ARGB approach)
 */
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.util.Random;

import com.owon.uppersoft.vds.core.aspect.help.IPersistentDisplay;
import com.owon.uppersoft.vds.core.aspect.help.ImageHandler;
import com.owon.uppersoft.vds.data.RGB;

/**
 * PersistenceFader, persistence fader
 * 
 * @author Matt
 * 
 */
public class PersistenceFader implements ImageHandler, Runnable {
	private final int FADING = 0, COVER = 1, OFF = 2;
	private int STATE = OFF;
	private IPersistentDisplay pd;
	public int delaytime = 20;
	private boolean onInfinite = false;
	private boolean phosphor = false;

	public boolean isOnInfinite() {
		return onInfinite;
	}

	public boolean isPhosphor() {
		return phosphor;
	}

	public void setCover() {
		STATE = COVER;
	}

	public void setOff() {
		STATE = OFF;
	}

	public boolean isRunning() {
		return STATE != OFF;
	}

	public PersistenceFader(IPersistentDisplay pd) {
		this.pd = pd;
	}

	public void run() {
		try {
			STATE = FADING;
			// long t1, t2,delt;
			OUT: while (true) {
				if (pd.isExit())
					return;

				// t1 = System.currentTimeMillis();

				switch (STATE) {
				case FADING:
					if (!pd.isRuntime()) {
						Thread.sleep(500);
						continue;
					}
					// drawtest(delt);
					pd.syncDealPersistImage(this);
					pd.re_paint();

					Thread.sleep(delaytime);
					break;
				case COVER:
					pd.resetPersistBufferImage();
					STATE = FADING;
					break;
				case OFF:
					// clearPersist();
					// pd.re_paint();
					break OUT;
				}
				// t2 = System.currentTimeMillis();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void setFadeTime(int s) {
		if (pd != null)
			pd.resetPersistBufferImage();

		onInfinite = phosphor = false;
		switch (s) {
		case 0:
			break;

		case 1:// 0.5s
			delaytime = 10;
			break;
		case 2:// 1s
			delaytime = 45;
			break;
		case 3:// 2s
			delaytime = 95;
			break;
		case 4:// 5s
			delaytime = 295;
			break;
		case 5:// Infinite
			onInfinite = true;
			delaytime = 200;
			break;
		case 6:// phosphor
			phosphor = true;
			delaytime = 2;
			break;
		}
	}

	private void fade_ch(BufferedImage bi, BufferedImage bi2) {
		int v;
		WritableRaster wr = bi.getRaster();
		DataBufferInt dbi = (DataBufferInt) wr.getDataBuffer();
		int[] co = dbi.getData();
		int len = co.length;
		Color c;
		float[] hsbvals = new float[3];
		for (int i = 0; i < len; i++) {
			v = co[i];
			if (v == 0)
				continue;
			c = new Color(v);

			Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsbvals);
			hsbvals[2] -= 0.05;
			if (hsbvals[2] <= 0.25) {
				if (onInfinite)
					continue;
				co[i] = 0;// set to transparent
			} else
				co[i] = Color.HSBtoRGB(hsbvals[0], hsbvals[1], hsbvals[2]);
		}

	}

	@Override
	public void handle(BufferedImage persistbi, BufferedImage tempbi) {
		if (phosphor)
			phosphor_ch(persistbi, tempbi);
		else
			fade_ch(persistbi, tempbi);
		// test(bi);// hand-draw red, yellow, purple, blue, green to test persistence
	}

	private long testt = 0;

	public void drawtest(long delt) {
		testt += delt;
		if (testt > 12500) {
			pd.syncDealPersistImage(this);
		}
	}

	public void test(BufferedImage bi) {
		Graphics g = bi.getGraphics();
		int ow = bi.getWidth(), oh = bi.getHeight();

		Graphics2D g2d = (Graphics2D) g;
		g2d.setStroke(new BasicStroke(10));
		Random rd = new Random();

		int y0 = rd.nextInt(oh);
		int y1 = rd.nextInt(oh);
		g.setColor(Color.RED);// new Color(255, 0, 0)
		g.drawLine(0, y0, ow, y1);

		y0 = rd.nextInt(oh);
		y1 = rd.nextInt(oh);
		g.setColor(Color.YELLOW);// new Color(255, 255, 0)
		g.drawLine(0, y0, ow, y1);

		y0 = rd.nextInt(oh);
		y1 = rd.nextInt(oh);
		g.setColor(new Color(102, 204, 255));// cyan
		g.drawLine(0, y0, ow, y1);

		y0 = rd.nextInt(oh);
		y1 = rd.nextInt(oh);
		g.setColor(new Color(204, 0, 255));// violet
		g.drawLine(0, y0, ow, y1);

		y0 = rd.nextInt(oh);
		y1 = rd.nextInt(oh);
		g.setColor(Color.GREEN);// new Color(0, 255, 0)
		g.drawLine(0, y0, ow, y1);

		g.dispose();
		testt = 0;
	}

	float[] hsbp = new float[3];
	float[] hsbt = new float[3];

	private void phosphor_ch(BufferedImage persistbi, BufferedImage tempbi) {
		// Here, for each channel waveform, a brightness top-up operation: if the original waveform pixel is already lit, brighten it; if it was dark, draw the default brightness.
		if (tempbi == null) {
			return;
		}
		synchronized (tempbi) {
			DataBufferInt pdbi = (DataBufferInt) persistbi.getRaster()
					.getDataBuffer();
			DataBufferInt tdbi = (DataBufferInt) tempbi.getRaster()
					.getDataBuffer();
			int[] pdata = pdbi.getData();
			int[] tdata = tdbi.getData();
			int plen = pdata.length;

			for (int i = 0; i < plen; i++) {
				if (tdata[i] == 0)// this pixel of the temporary canvas has no color, dim the persistence canvas
					continue;

				RGB.toHSB(pdata[i], hsbp);
				RGB.toHSB(tdata[i], hsbt);
				// System.out.println(hsbp[0] + "," + hsbt[0] + "  s:  " +
				// hsbp[1] + "," + hsbt[1]);
				if (Math.abs(hsbp[0] - hsbt[0]) < 0.1
						&& Math.abs(hsbp[1] - hsbt[1]) < 0.1) {
					hsbp[2] += 0.09;
					if (hsbp[2] > 1)
						hsbp[2] = 1;
					pdata[i] = Color.HSBtoRGB(hsbp[0], hsbp[1], hsbp[2]);
				} else {
					hsbt[2] = 0.35f;
					pdata[i] = Color.HSBtoRGB(hsbt[0], hsbt[1], hsbt[2]);
				}

			}
			for (int i = 0; i < plen; i++) {
				if (pdata[i] == 0)
					continue;
				RGB.toHSB(pdata[i], hsbp);

				hsbp[2] -= 0.07;
				if (hsbp[2] <= 0.25) {
					if (onInfinite)
						continue;
					pdata[i] = 0;// set to transparent
				} else
					pdata[i] = Color.HSBtoRGB(hsbp[0], hsbp[1], hsbp[2]);
			}

		}

	}

	@Deprecated
	private void fade_4ch(BufferedImage bi) {
		int v;
		WritableRaster wr = bi.getRaster();
		DataBufferInt dbi = (DataBufferInt) wr.getDataBuffer();
		int[] co = dbi.getData();
		int len = co.length;

		for (int i = 0; i < len; i++) {
			v = co[i];
			if (v == 0)
				continue;
			/** Taking red as an example: when 11111 (highest bit) goes to 0 it turns black, i.e. 31 goes to 0, so going from red to black takes at least 200*31ms */
			/** Red 11111111 11111111 00000000 00000000 (0xFF FF0000(ARGB)) */
			if ((v & 0xFF0000) != 0 && (v & 0xFFFF) == 0) {
				/** When red fades to the infinite persistence minimum value 0xFF3F0000, skip to the next loop iteration and stop fading this pixel. */
				/** 11111111 00111111 00000000 00000000 0xFF3F0000 used to judge the infinite persistence minimum value */
				if (onInfinite && v <= 0xFF3F0000) {
					continue;
				}
				/** R bit decremented by 1 */
				v -= 0x10000;
				/** set to transparent */
				if ((v & 0xFF0000) == 0) {
					v = 0;
				}
				co[i] = v;
			}
			/**
			 * Yellow 11111111 11111111 11111111 00000000 (0xFF FFFF00(ARGB))
			 */
			if ((v & 0xFF) == 0 && (v & 0xFF00) != 0 && (v & 0xFF0000) != 0) {
				/** 11111111 00111111 00111111 00000000 0xFF3F3F00 used to judge the infinite persistence minimum value */
				if (onInfinite && v <= 0xFF3F3F00) {
					continue;
				}
				/** RG bits both decremented by 1 */
				v -= 0x10100;
				/** set to transparent */
				if ((v & 0xFF0000) == 0) {
					v = 0;
				}
				co[i] = v;
			}
			/** CYAN 00000000 01100110 11001100 11111111 (0xFF 66CCFF(ARGB)) */
			if ((v & 0xFF00) != 0 && (v & 0xFF) != 0) {
				/** B bit decrements to 0011 1111: infinite persistence */
				if (onInfinite && (v & 0xFF) <= 0x3F) {
					continue;
				}
				/**
				 * First 34 passes (R bits from 102 to 0): R decremented by 3 and G by 1, giving R:0 G:170 B:255; then 170 passes with the G low bit decremented by 1
				 * , and at the same time the B bits alternately decrement by 2 or 1
				 */
				if ((v & 0xFF0000) != 0) {
					v -= 0x30100;
				} else {
					if ((v & 0x100) != 0)
						v -= 0x102;// 01 00000010
					else
						v -= 0x101;// 01 00000001
				}
				/** set to transparent */
				if ((v & 0xFF00) == 0) {
					v = 0;
				}
				co[i] = v;
			}
			/**
			 * Violet 11111111 11001100 00000000 11111111 (0xFF CC00FF(ARGB))
			 */
			if ((v & 0xFF00) == 0 && (v & 0xFF) != 0 && (v & 0xFF0000) != 0) {
				/** B bit decrements to 0011 1111: infinite persistence */
				if (onInfinite && (v & 0xFF) <= 0x3F) {
					continue;
				}
				/** B bit decremented by 1 until R equals B, then RB are decremented together by 1 */
				if (((v & 0xFF0000) >>> 16) != (v & 0xFF))
					v -= 0x1;
				else
					v -= 0x10001;
				/** set to transparent */
				if ((v & 0xFF0000) == 0)
					v = 0;
				co[i] = v;
			}
			/** GREEN 11111111 00000000 11111111 00000000 (0xFF 00FF00(ARGB)) */
			if ((v & 0xFF0000) == 0 && (v & 0xFF) == 0 && (v & 0x00FF00) != 0) {
				/** G bit decrements to 0011 1111: infinite persistence */
				if (onInfinite && (v & 0xFF00) <= 0x3F00) {
					continue;
				}
				v -= 0x100;
				/** set to transparent */
				if ((v & 0xFF00) == 0)
					v = 0;
				co[i] = v;
			}
		}

	}

	@Deprecated
	private void fade_2ch(BufferedImage bi) {
		int v;
		WritableRaster wr = bi.getRaster();
		DataBufferInt dbi = (DataBufferInt) wr.getDataBuffer();
		int[] co = dbi.getData();
		int len = co.length;

		for (int i = 0; i < len; i++) {
			v = co[i];
			if (v == 0)
				continue;

			/** Red 11111111 11111111 00000000 00000000 (0xFF FF0000(ARGB)) */
			if ((v & 0xFF0000) != 0 && (v & 0xFFFF) == 0) {
				/** When red fades to the infinite persistence minimum value 0xFF3F0000, skip to the next loop iteration and stop fading this pixel. */
				/** 11111111 00111111 00000000 00000000 0xFF3F0000 used to judge the infinite persistence minimum value */
				if (onInfinite && v <= 0xFF3F0000) {
					continue;
				}
				/** R bit decremented by 1 */
				v -= 0x10000;
				/** set to transparent */
				if ((v & 0xFF0000) == 0) {
					v = 0;
				}
				co[i] = v;
			}
			/**
			 * Yellow 11111111 11111111 11111111 00000000 (0xFF FFFF00(ARGB))
			 */
			if ((v & 0xFF) == 0 && (v & 0xFF00) != 0 && (v & 0xFF0000) != 0) {
				/** 11111111 00111111 00111111 00000000 0xFF3F3F00 used to judge the infinite persistence minimum value */
				if (onInfinite && v <= 0xFF3F3F00) {
					continue;
				}
				/** RG bits both decremented by 1 */
				v -= 0x10100;
				/** set to transparent */
				if ((v & 0xFF0000) == 0) {
					v = 0;
				}
				co[i] = v;
			}
			/** GREEN 11111111 00000000 11111111 00000000 (0xFF 00FF00(ARGB)) */
			if ((v & 0xFF0000) == 0 && (v & 0xFF) == 0 && (v & 0x00FF00) != 0) {
				/** G bit decrements to 0011 1111: infinite persistence */
				if (onInfinite && (v & 0xFF00) <= 0x3F00) {
					continue;
				}
				v -= 0x100;
				/** set to transparent */
				if ((v & 0xFF00) == 0)
					v = 0;
				co[i] = v;
			}
		}

	}

}