package com.owon.uppersoft.dso.view.pane.dock.widget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Stroke;

import javax.swing.JComponent;

import com.owon.uppersoft.dso.pref.Define;
import com.owon.uppersoft.vds.ui.resource.FontCenter;
import com.owon.uppersoft.vds.ui.resource.ImagePaintUtil;

/**
 * HomePageListCellRenderer，HomePage列表的单元格绘图
 * 
 * @author Matt
 * 
 */
public class HomePageListCellRenderer extends JComponent {
	public static final int HomeListUp = 1, HomeListCenter = 2,
			HomeListDown = 3;

	public static final int CellHeight = 32;

	public static final int rw = 10, rh = 10, arcw = 10, arch = 10;
	public static final int trianglew = 10, triangleh = 14;

	private int type = -1;
	private String n;
	private boolean cellHasFocus = true;
	private Image img;

	public static final Image rrImg4in1 = ImagePaintUtil
			.getRoundRectangle4in1Image(rw, rh,
					Define.def.style.CO_DockHomeCellBack,
					Define.def.style.CO_DockHomeBack,
					Define.def.style.CO_DockHomeBack, arcw, arch);
	private static final Stroke sk = new BasicStroke(1);

	public HomePageListCellRenderer(int type, String n, boolean cellHasFocus,
			Image img) {
		this.type = type;
		this.n = n;
		this.cellHasFocus = cellHasFocus;
		this.img = img;
		setPreferredSize(new Dimension(0, CellHeight));
	}

	@Override
	public Font getFont() {
		return FontCenter.getTitleFont();
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		int w = getWidth(), h = getHeight();

		g2d.setColor(Define.def.style.CO_DockHomeCellBack);
		g2d.fillRect(0, 0, w, h);
		switch (type) {
		case HomeListUp:
			ImagePaintUtil.paintUpRoundRectangle4in1(g, w, rw, rh, rrImg4in1);
			break;
		case HomeListCenter:
			break;
		case HomeListDown:
			ImagePaintUtil.paintDownRoundRectangle4in1(g, w, h, rw, rh,
					rrImg4in1);
			break;
		}
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(cellHasFocus ? Color.ORANGE
				: Define.def.style.CO_DockHomeCellFore);
		g2d.drawString(n, 50, 23);

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);

		int trax = w - trianglew - 5, tray = (h - triangleh) >> 1;
		// g2d.drawImage(cellHasFocus ? tragonimg : tragimg, trax, tray, null);
		g2d.drawString(">", trax - 10, tray + 14);

		if (type != HomeListDown) {
			g2d.setColor(Define.def.style.CO_DockHomeSeparator);
			g2d.setStroke(sk);
			g2d.drawLine(5, h - 1, w - 5, h - 1);
		}

		if (img != null)
			g2d.drawImage(img, 5, 5, null);
	}

}