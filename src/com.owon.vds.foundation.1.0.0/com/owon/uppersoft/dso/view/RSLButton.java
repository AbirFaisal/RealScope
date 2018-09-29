package com.owon.uppersoft.dso.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;

import com.owon.uppersoft.vds.ui.widget.custom.LButton;

public class RSLButton extends LButton implements MouseListener {
	private final Color bg = Color.LIGHT_GRAY, border = Color.DARK_GRAY;
	private boolean rsRollover = false;
	private ITitleStatus its;

	public RSLButton(ITitleStatus its) {
		this.its = its;
		addMouseListener(this);
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		rsRollover = true;
		repaint();
	}

	public void mouseExited(MouseEvent e) {
		rsRollover = false;
		repaint();
	}

	public void askSwitchRS() {
		if (isEnabled())
			its.switchRS();
	}

	@Deprecated
	public void mouseClicked(MouseEvent e) {
		askSwitchRS();
	}

	public void paintForButton(JButton btn, Graphics g) {
		if (rsRollover && btn.isEnabled()) {
			Graphics2D g2d = (Graphics2D) g;
			int w = btn.getWidth(), h = btn.getHeight();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setColor(bg);
			g2d.fillRoundRect(0, 3, w, h - 6, 5, 5);
			g2d.setColor(border);
			g2d.drawRoundRect(0, 3, w - 1, h - 6, 5, 5);
			// g2d.drawImage(((ImageIcon) btnRS.getIcon()).getImage(),
			// 7,8, null);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		paintForButton(this, g);
		super.paintComponent(g);
	}
}