package com.owon.uppersoft.vds.ui.widget.custom;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.plaf.basic.BasicButtonUI;

import com.owon.uppersoft.vds.ui.widget.help.RolloverProvider;



public class CLButton extends JButton {
	public static final Color bg = Color.LIGHT_GRAY, border = Color.DARK_GRAY;
	private Color bgColor;
	private Color borderColor;

	public CLButton() {
		setOpaque(false);
		setUI(new BasicButtonUI());
		
		bgColor = bg;
		borderColor = border;

		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent e) {
				if (!rollover.isOrNot()) {
					rollover.set(true);
					repaint();
				}
				if (rollvoerTextOn && rollvoerText != null) {
					setText(rollvoerText);
				}
			}

			public void mouseExited(MouseEvent e) {
				if (rollover.isOrNot()) {
					rollover.set(false);
					repaint();
				}
				if (rollvoerTextOn && originText != null) {
					setText(originText);

				}
			}
		});

		rollover = new RolloverProvider();
	}
	
	public void setBackgroundColor(Color c){
		bgColor = c;
	}
	
	public void setBorderColor(Color c){
		borderColor = c;
	}

	private RolloverProvider rollover;
	private String rollvoerText, originText;
	private boolean rollvoerTextOn;

	public void setRolloverProvider(RolloverProvider rollover) {
		this.rollover = rollover;
	}

	public void setRolloverText(String txt) {
		rollvoerTextOn = true;
		rollvoerText = txt;
		originText = getText();
	}

	@Override
	protected void paintComponent(Graphics g) {
		boolean b = rollover.isOrNot() && isEnabled();
		if (b) {
			Graphics2D g2d = (Graphics2D) g;
			int w = getWidth(), h = getHeight();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setColor(bgColor);
			g2d.fillRoundRect(0, 3, w, h - 6, 5, 5);
			g2d.setColor(borderColor);
			g2d.drawRoundRect(0, 3, w - 1, h - 6, 5, 5);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);
		}
		super.paintComponent(g);
	}
}
