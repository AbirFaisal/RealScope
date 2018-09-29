package com.owon.uppersoft.dso.util.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import com.owon.uppersoft.dso.pref.Define;

public class FloatGlassP extends JPanel {
	private Color background;
	private float diaphaneity;

	// private int w,h;
	public FloatGlassP(int x, int y, final int w, final int h) {
		setBounds(x, y, w, h);

		background = Define.def.style.CO_DockContainer;
		diaphaneity = (float) 0.5;
		setOpaque(false);
		setVisible(true);
	}

	public void setColor(Color bg) {
		this.background = bg;
	}

	public void setDiaphaneity(float v) {
		this.diaphaneity = v;
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				diaphaneity));
		g2d.setColor(background);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.fillRoundRect(2, 0, getWidth() - 4, getHeight() - 8, 24, 24);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
	}

}