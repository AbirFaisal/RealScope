package com.owon.uppersoft.vds.ui.widget.help;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.border.Border;

public final class RoundRectBorder implements Border {
	private Insets bi;
	private Color color;
	private int arc;

	public RoundRectBorder(int thickness, Color color) {
		this(thickness, color, 20);
	}

	public RoundRectBorder(Color color) {
		this(6, color, 20);
	}

	private RoundRectBorder(int thickness, Color c, int arc) {
		this.color = c;
		this.arc = arc;
		bi = new Insets(thickness, thickness, thickness, thickness);
	}

	@Override
	public Insets getBorderInsets(Component c) {
		return bi;
	}

	@Override
	public boolean isBorderOpaque() {
		return true;
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width,
			int height) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(color);
		g.fillRoundRect(x, y, width, height, arc, arc);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
	}
}