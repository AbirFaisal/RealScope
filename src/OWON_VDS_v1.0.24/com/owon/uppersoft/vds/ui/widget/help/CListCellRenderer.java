package com.owon.uppersoft.vds.ui.widget.help;

import java.io.Serializable;

import javax.swing.DefaultListCellRenderer;
import javax.swing.SwingConstants;

/**
 * CListCellRenderer，自定义ComboBox的Render，使用自定义字体，前后别景色，防锯齿
 * 
 * @author Matt
 * 
 */
public class CListCellRenderer extends DefaultListCellRenderer implements
		Serializable {
	public static final CListCellRenderer clc = new CListCellRenderer();
	public static final CListCellRenderer rclc = new CListCellRenderer();
	static {
		rclc.setHorizontalAlignment(SwingConstants.RIGHT);
	}

	private CListCellRenderer() {
		super();
	}

	// @Override
	// public Component getListCellRendererComponent(JList list, Object value,
	// int index, boolean isSelected, boolean cellHasFocus) {
	// // list.setForeground(Color.white);
	// // list.setBackground(Color.DARK_GRAY);
	// Component c = super.getListCellRendererComponent(list, value, index,
	// isSelected, cellHasFocus);
	// return c;
	// }

	// @Override
	// protected void paintComponent(Graphics g) {
	// Graphics2D g2d = (Graphics2D) g;
	// g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	// RenderingHints.VALUE_ANTIALIAS_ON);
	// super.paintComponent(g);
	// g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	// RenderingHints.VALUE_ANTIALIAS_ON);
	// }

}