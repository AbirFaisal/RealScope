package com.owon.uppersoft.dso.view.pane.dock.widget;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.Stroke;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.owon.uppersoft.dso.pref.Define;
import com.owon.uppersoft.vds.ui.layout.OneColumnLayout;
import com.owon.uppersoft.vds.ui.resource.ImagePaintUtil;

public class GroupPane extends JPanel {
	public static final int rw = 10, rh = 10, arcw = 15, arch = 15, row0h = 35;
	public static final Image rr4in1img = ImagePaintUtil
			.getRoundRectangle4in1Image(rw, rh, Define.def.style.CO_DockTitle,
					Define.def.style.CO_DockBack, Define.def.style.CO_DockBack,
					arcw, arch);

	public static final Image rr4in1imge = ImagePaintUtil
			.getRoundRectangleEmpty4in1Image(rw, rh,
					Define.def.style.CO_DockTitle,
					Define.def.style.CO_DockContainer,
					Define.def.style.CO_DockBack, arcw, arch);

	private int borderwidth = 2;
	private int ipnum = 0;
	public ItemPane ip;

	protected GroupPane() {
		setOpaque(false);
		// setBackground(Color.white);
		setLayout(new OneColumnLayout(new Insets(borderwidth, borderwidth,
				borderwidth, borderwidth), 0));
	}

	public void n1st(JComponent c) {
		ipnum++;
		add(c);
	}

	public ItemPane nrip(LayoutManager mgr) {
		if (ipnum == 0) {
			ip = new ItemPane(row0h);
		} else {
			if (mgr == null) {
				ip = new ItemPane();
			} else {
				ip = new ItemPane(mgr);
			}
		}
		ipnum++;
		add(ip);
		return ip;
	}

	public ItemPane nrip() {
		return nrip(null);
	}

	public ItemPane nrip_notitle() {
		ipnum++;
		return nrip(null);
	}

	@Override
	protected void paintComponent(Graphics g) {
		// super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		int w = getWidth(), h = getHeight(), rowh = row0h;
		Stroke s = Define.def.Stroke2;

		if (h <= row0h + (borderwidth << 1)) {
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(Define.def.style.CO_DockTitle);
			g2d.fillRoundRect(0, 0, w, h, arcw, arcw);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);
			return;
		}

		g2d.setColor(Define.def.style.CO_DockContainer);
		g2d.fillRoundRect(1, 1, w - 2, h - 2, 10, 10);

		g.setColor(Define.def.style.CO_DockTitle);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setStroke(s);
		g2d.drawRoundRect(0, 0, w - 1, h - 1, arcw, arch);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);

		g2d.fillRoundRect(1, 1, w - 2, rowh - 2, 10, 10);
		g2d.fillRect(1, rowh - rh, w - 2, rh);
	}
}