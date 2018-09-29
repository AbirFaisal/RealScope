package com.owon.uppersoft.dso.util.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.owon.uppersoft.dso.pref.Define;
import com.sun.awt.AWTUtilities;

public class FloatGlassPane extends JDialog {
	private Color background;
	private float diaphaneity;
	private int width, height;

	public FloatGlassPane(Window p, int x, int y, final int w, final int h) {
		super(p);
		width = w;
		height = h;
		setBounds(x, y, w, h);
		background = Define.def.style.CO_DockContainer;
		diaphaneity = (float) 0.5;

		JPanel cp = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D) g;
				paintContent(g2d);
			}
		};
		setContentPane(cp);
		setUndecorated(true);
		setVisible(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		AWTUtilities.setWindowOpaque(this, false);
	}

	public void setColor(Color bg) {
		this.background = bg;
	}

	public void setDiaphaneity(float v) {
		this.diaphaneity = v;
	}

	public void paintBackGround(Graphics2D g2d) {
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				diaphaneity));
		g2d.setColor(background);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.fillRoundRect(2, 0, width - 4, height - 8, 24, 24);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	/**
	 * 用于绘画玻璃面板上的文字图片等
	 */
	public void paintContent(Graphics2D g) {
		paintBackGround(g);
	}

}