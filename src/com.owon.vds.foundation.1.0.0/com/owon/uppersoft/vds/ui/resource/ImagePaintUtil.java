package com.owon.uppersoft.vds.ui.resource;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImagePaintUtil {
	public static void main(String[] args) {
		// Define.prepare(0);
		// output1pix(new File("i:\\d.png"), "PNG");
	}

	public static void output1pix(File file, String format, Paint pt,
			Paint bgin, Paint bgout) {
		int rw = 10, rh = 10, arcw = 15, arch = 15;
		BufferedImage rr4in1img = ImagePaintUtil
				.getRoundRectangleEmpty4in1Image(rw, rh, pt, bgin, bgout, arcw,
						arch);
		try {
			ImageIO.write(rr4in1img, format, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取图片作为TexturePaint
	 * 
	 * @param path
	 * @return
	 */
	public static final TexturePaint getTexturePaint(String path) {
		Image img = SwingResourceManager.getIcon(ImagePaintUtil.class, path)
				.getImage();
		Rectangle d = new Rectangle(img.getWidth(null), img.getHeight(null));
		BufferedImage bi = new BufferedImage(d.width, d.height,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = bi.createGraphics();
		g2d.drawImage(img, 0, 0, null);
		g2d.dispose();
		TexturePaint tp = new TexturePaint(bi, d);
		return tp;
	}

	/**
	 * 使用既定Paint作为TexturePaint
	 * 
	 * @param gp
	 * @param w
	 * @param h
	 * @return
	 */
	public static final TexturePaint getTexturePaint(Paint gp, int w, int h) {
		Rectangle d = new Rectangle(w, h);
		BufferedImage bi = new BufferedImage(d.width, d.height,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = bi.createGraphics();
		g2d.setPaint(gp);
		g2d.fillRect(0, 0, w, h);
		g2d.dispose();
		TexturePaint tp = new TexturePaint(bi, d);
		return tp;
	}

	/**
	 * 画指向右的三角形
	 * 
	 * @param w
	 * @param h
	 * @param co
	 * @return
	 */
	public static final BufferedImage getTriangleImage(int w, int h, Color co) {
		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = bi.createGraphics();
		int[] xps = { 0, 0, w };
		int[] yps = { 0, h, h >> 1 };
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.setColor(co);
		g2d.fillPolygon(xps, yps, xps.length);
		g2d.dispose();
		return bi;
	}

	/**
	 * 4个圆角边沿合在一个Image中
	 * 
	 * @param hw
	 * @param hh
	 * @param pt
	 * @param bgup
	 * @param bglow
	 * @return
	 */
	public static final BufferedImage getRoundRectangle4in1Image(int hw,
			int hh, Paint pt, Paint bgup, Paint bglow, int arcw, int arch) {
		int w = hw << 1, h = hh << 1;
		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = bi.createGraphics();
		g2d.setPaint(bgup);
		g2d.fillRect(0, 0, w, hh);

		/** 这里使用pt或是bglow，可以画出半/全圆角矩阵 */
		g2d.setPaint(bglow);
		g2d.fillRect(0, hh, w, hh);

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.setPaint(pt);
		g2d.fillRoundRect(0, 0, w, h, arcw, arch);
		g2d.dispose();
		return bi;
	}

	/**
	 * 4个圆角边沿合在一个Image中
	 * 
	 * @param hw
	 * @param hh
	 * @param pt
	 * @param bgup
	 * @param bglow
	 * @return
	 */
	public static final BufferedImage getRoundRectangleEmpty4in1Image(int hw,
			int hh, Paint pt, Paint bgin, Paint bgout, int arcw, int arch) {
		int w = hw << 1, h = hh << 1;
		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = bi.createGraphics();
		g2d.setPaint(bgout);
		g2d.fillRect(0, 0, w, h);

		int st = 2;
		Stroke s = new BasicStroke(2);

		g2d.setPaint(bgin);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.fillRoundRect(1, 1, w - st, h - st, arcw, arch);

		g2d.setPaint(pt);
		g2d.setStroke(s);
		g2d.drawRoundRect(0, 0, w - st, h - st, arcw, arch);
		g2d.dispose();
		return bi;
	}

	/**
	 * 基于已知边界画出四边圆角
	 * 
	 * @param g
	 * @param width
	 * @param height
	 * @param rw
	 * @param rh
	 * @param img
	 */
	public static final void paintRoundRectangle4in1(Graphics g, int width,
			int height, int rw, int rh, Image img) {
		paintUpRoundRectangle4in1(g, width, rw, rh, img);
		paintDownRoundRectangle4in1(g, width, height, rw, rh, img);
	}

	/**
	 * 基于已知边界画出上边圆角
	 * 
	 * @param g
	 * @param width
	 * @param rw
	 * @param rh
	 * @param img
	 */
	public static final void paintUpRoundRectangle4in1(Graphics g, int width,
			int rw, int rh, Image img) {
		/** up */
		g.drawImage(img, 0, 0, rw, rh, 0, 0, rw, rh, null);
		g.drawImage(img, width - rw, 0, width, rh, rw, 0, rw << 1, rh, null);
	}

	/**
	 * 基于已知边界画出下边圆角
	 * 
	 * @param g
	 * @param width
	 * @param height
	 * @param rw
	 * @param rh
	 * @param img
	 */
	public static final void paintDownRoundRectangle4in1(Graphics g, int width,
			int height, int rw, int rh, Image img) {
		/** down */
		g.drawImage(img, 0, height - rh, rw, height, 0, rh, rw, rh << 1, null);
		g.drawImage(img, width - rw, height - rh, width, height, rw, rh,
				rw << 1, rh << 1, null);
	}
}
