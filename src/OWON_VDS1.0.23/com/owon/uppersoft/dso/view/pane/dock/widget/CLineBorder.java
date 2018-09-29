package com.owon.uppersoft.dso.view.pane.dock.widget;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.border.AbstractBorder;

import com.owon.uppersoft.dso.pref.Define;

public class CLineBorder extends AbstractBorder {
	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width,
			int height) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Define.def.style.CO_DockContainer);
		g.fillRoundRect(x, y, width, height, 15, 15);
		g.setColor(Define.def.style.CO_DockTitle);
		g.drawRoundRect(x, y, width - 1, height - 1, 15, 15);
	}
}