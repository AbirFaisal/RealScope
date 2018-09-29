package com.owon.uppersoft.vds.ui.widget.custom;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ResourceBundle;

import javax.swing.JTextArea;

import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.ui.resource.FontCenter;

/**
 * 
 * 多行文本，CTextArea
 * 
 * @author Matt
 * 
 */
public class HCLabel extends JTextArea implements Localizable {

	public HCLabel(Color fg, Color bg, Color sel) {
		setBorder(null);
		setPreferredSize(new Dimension(250, 50));

		setForeground(fg);
		setBackground(bg);
		setSelectionColor(sel);
		setLineWrap(true);
		setWrapStyleWord(true);
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		super.paintComponent(g);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	@Override
	public void localize(ResourceBundle rb) {
		setFont(FontCenter.getLabelFont());
		String n = getName();
		if (n != null) {
			n = rb.getString(n);
			if (n != null)
				setText(n);
		}
	}
}
