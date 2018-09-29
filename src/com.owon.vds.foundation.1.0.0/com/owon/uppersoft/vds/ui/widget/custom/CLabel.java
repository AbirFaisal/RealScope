package com.owon.uppersoft.vds.ui.widget.custom;

import java.awt.Color;
import java.util.ResourceBundle;

import javax.swing.JLabel;

import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.ui.resource.FontCenter;

/**
 * 带冒号结束的起始文本，SufColonLabel
 * 
 */
public class CLabel extends JLabel implements Localizable {
	public static final Color CO_DockFore = Color.WHITE;
	
	public CLabel() {
		setFont(FontCenter.getLabelFont());
		setForeground(CO_DockFore);
	}

//	@Override
//	protected void paintComponent(Graphics g) {
//		Graphics2D g2d = (Graphics2D) g;
//		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//				RenderingHints.VALUE_ANTIALIAS_ON);
//		super.paintComponent(g);
//		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//				RenderingHints.VALUE_ANTIALIAS_OFF);
//	}

	@Override
	public void localize(ResourceBundle rb) {
		setFont(FontCenter.getLabelFont());
		String n = getName();
		if (n != null) {
			n = rb.getString(n);
			if (n != null)
				setText(n + ": ");
		}
	}
}
