package com.owon.uppersoft.vds.ui.widget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.plaf.basic.BasicButtonUI;

public class LightenButton extends JButton implements MouseListener {
	public LightenButton() {
		setUI(new BasicButtonUI());
		setOpaque(false);
		setForeground(Color.WHITE);

		pressed = false;
		addMouseListener(this);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		setForeground(Color.ORANGE);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		setForeground(Color.WHITE);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		pressed = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		pressed = false;
	}

	private boolean pressed;

	@Override
	public void setText(String text) {
		super.setText(text);

		int w = getFontMetrics(getFont()).stringWidth(text) + 8;
		int h = 25;
		setPreferredSize(new Dimension(w, h));
		// revalidate();
		setToolTipText(getText());
	}

	public static final Stroke Stroke2 = new BasicStroke(2);

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int w = getWidth(), h = getHeight();
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.setStroke(Stroke2);
		g2d.drawRoundRect(2, 2, w - 4, h - 4, 8, 8);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	public void setFont_Text(Font fn, String text) {
		setFont(fn);
		setText(text);
	}

}