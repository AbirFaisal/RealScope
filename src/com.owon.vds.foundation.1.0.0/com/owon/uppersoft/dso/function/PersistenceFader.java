package com.owon.uppersoft.dso.function;

/**未实现的方法
 *
 *为避免网格闪烁，网格画在画布bi上
 *  
 * TYPE_USHORT_565_RGB做法：每次fade(),画一遍网格、标尺，故网格、标尺在波形之上。
 * TODO
 * 更理想效果：网格、标尺在波形下面。
 * 1、什么时候画网格、标尺？ 
 *    当GridBar颜色拖动、清除余辉、重置画布时 等等。
 * 2、画布上会增加网格的颜色？ 
 *    可以在fade()中，判断过滤黑色的相同位置，过滤掉网格的颜色，避免网格变暗。
 * 	  具体方法：把网格GrideBar拖动后的颜色值以画布的TYPE_USHORT_565_RGB
 * 格式存储，把存该值的变量传递给fade(),渐淡时像素为该变量值则跳出。
 * */

/**：对原做法的修改：
 * TYPE_USHORT_565_RGB格式为不透明画布，网格在下方被遮盖，有3种处理方法
 * 
 * 1.添加网格到画布，过滤画布颜色
 * 
 * 2.添加网格到画布，判断网格坐标，若没被波形遮盖，重画网格
 * 
 * 3.改成透明画布(现在的TYPE_INT_ARGB做法)
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
 * PersistenceFader，余辉渐变器
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
				co[i] = 0;// 设为透明
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
		// test(bi);// 自画 红黄紫蓝绿 测试余辉
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
		// 在这里对各通道波形进行：补亮度的操作，若原来波形像素已亮，则加亮，若原是无光，则画出默认亮度。
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
				if (tdata[i] == 0)// 临时画布该像素无色，余晖画布减暗
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
					pdata[i] = 0;// 设为透明
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
			/** 以红色为例：11111(最高位)变至0则变黑，既31变0，那么从红变黑至少需要200*31ms */
			/** Red 11111111 11111111 00000000 00000000 (0xFF FF0000(ARGB)) */
			if ((v & 0xFF0000) != 0 && (v & 0xFFFF) == 0) {
				/** 红色减淡到无限余晖最低值0xFF3F0000时，跳出本次进入下次循环，停止该像素颜色减淡。 */
				/** 11111111 00111111 00000000 00000000 0xFF3F0000判断无限余辉最低值 */
				if (onInfinite && v <= 0xFF3F0000) {
					continue;
				}
				/** R位减1 */
				v -= 0x10000;
				/** 设为透明 */
				if ((v & 0xFF0000) == 0) {
					v = 0;
				}
				co[i] = v;
			}
			/**
			 * Yellow 11111111 11111111 11111111 00000000 (0xFF FFFF00(ARGB))
			 */
			if ((v & 0xFF) == 0 && (v & 0xFF00) != 0 && (v & 0xFF0000) != 0) {
				/** 11111111 00111111 00111111 00000000 0xFF3F3F00判断无限余辉最低值 */
				if (onInfinite && v <= 0xFF3F3F00) {
					continue;
				}
				/** RG位同时减1 */
				v -= 0x10100;
				/** 设为透明 */
				if ((v & 0xFF0000) == 0) {
					v = 0;
				}
				co[i] = v;
			}
			/** CYAN 00000000 01100110 11001100 11111111 (0xFF 66CCFF(ARGB)) */
			if ((v & 0xFF00) != 0 && (v & 0xFF) != 0) {
				/** B位递减到 0011 1111 时，无限余辉 */
				if (onInfinite && (v & 0xFF) <= 0x3F) {
					continue;
				}
				/**
				 * 首先34遍(R位从102到0):R减3且G减1,得R:0 G:170 B:255;后170遍G低位减1
				 * ,且同时B位交替减2或减1
				 */
				if ((v & 0xFF0000) != 0) {
					v -= 0x30100;
				} else {
					if ((v & 0x100) != 0)
						v -= 0x102;// 01 00000010
					else
						v -= 0x101;// 01 00000001
				}
				/** 设为透明 */
				if ((v & 0xFF00) == 0) {
					v = 0;
				}
				co[i] = v;
			}
			/**
			 * Violet 11111111 11001100 00000000 11111111 (0xFF CC00FF(ARGB))
			 */
			if ((v & 0xFF00) == 0 && (v & 0xFF) != 0 && (v & 0xFF0000) != 0) {
				/** B位递减到 0011 1111 时，无限余辉 */
				if (onInfinite && (v & 0xFF) <= 0x3F) {
					continue;
				}
				/** B位减1，到R和B相等时，RB一起减1 */
				if (((v & 0xFF0000) >>> 16) != (v & 0xFF))
					v -= 0x1;
				else
					v -= 0x10001;
				/** 设为透明 */
				if ((v & 0xFF0000) == 0)
					v = 0;
				co[i] = v;
			}
			/** GREEN 11111111 00000000 11111111 00000000 (0xFF 00FF00(ARGB)) */
			if ((v & 0xFF0000) == 0 && (v & 0xFF) == 0 && (v & 0x00FF00) != 0) {
				/** G位递减到 0011 1111 时，无限余辉 */
				if (onInfinite && (v & 0xFF00) <= 0x3F00) {
					continue;
				}
				v -= 0x100;
				/** 设为透明 */
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
				/** 红色减淡到无限余晖最低值0xFF3F0000时，跳出本次进入下次循环，停止该像素颜色减淡。 */
				/** 11111111 00111111 00000000 00000000 0xFF3F0000判断无限余辉最低值 */
				if (onInfinite && v <= 0xFF3F0000) {
					continue;
				}
				/** R位减1 */
				v -= 0x10000;
				/** 设为透明 */
				if ((v & 0xFF0000) == 0) {
					v = 0;
				}
				co[i] = v;
			}
			/**
			 * Yellow 11111111 11111111 11111111 00000000 (0xFF FFFF00(ARGB))
			 */
			if ((v & 0xFF) == 0 && (v & 0xFF00) != 0 && (v & 0xFF0000) != 0) {
				/** 11111111 00111111 00111111 00000000 0xFF3F3F00判断无限余辉最低值 */
				if (onInfinite && v <= 0xFF3F3F00) {
					continue;
				}
				/** RG位同时减1 */
				v -= 0x10100;
				/** 设为透明 */
				if ((v & 0xFF0000) == 0) {
					v = 0;
				}
				co[i] = v;
			}
			/** GREEN 11111111 00000000 11111111 00000000 (0xFF 00FF00(ARGB)) */
			if ((v & 0xFF0000) == 0 && (v & 0xFF) == 0 && (v & 0x00FF00) != 0) {
				/** G位递减到 0011 1111 时，无限余辉 */
				if (onInfinite && (v & 0xFF00) <= 0x3F00) {
					continue;
				}
				v -= 0x100;
				/** 设为透明 */
				if ((v & 0xFF00) == 0)
					v = 0;
				co[i] = v;
			}
		}

	}

}