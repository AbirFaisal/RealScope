package com.owon.uppersoft.vds.ui.widget.custom;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class ICLButton extends CLButton {
	public static final String INIT_ID = null;
	private String id;
	private boolean bgChangeToNotEnabled;
	private Color bgNotEnabledColor;

	public ICLButton() {
		this.id = INIT_ID;
		bgChangeToNotEnabled = false;
		bgNotEnabledColor = Color.LIGHT_GRAY;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setNotEnabled() {
		setEnabled(false);
		bgChangeToNotEnabled = true;
	}

	public void setNotEnabledColor(Color c) {
		bgNotEnabledColor = c;
	}

	@Override
	protected void paintComponent(Graphics g) {

		if (bgChangeToNotEnabled) {
			Graphics2D g2d = (Graphics2D) g;
			int w = getWidth(), h = getHeight();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setColor(bgNotEnabledColor);
			g2d.fillRoundRect(0, 0, w, h - 0, 0, 0);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);
		}
		super.paintComponent(g);
	}
}
