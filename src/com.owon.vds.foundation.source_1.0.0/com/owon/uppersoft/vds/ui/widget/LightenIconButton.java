package com.owon.uppersoft.vds.ui.widget;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.plaf.basic.BasicButtonUI;

public class LightenIconButton extends JButton {
	public LightenIconButton(Icon ico) {
		setUI(new BasicButtonUI());
		setOpaque(false);
		setPreferredSize(new Dimension(ico.getIconWidth(), ico.getIconHeight()));
		setIcon(ico);
	}

	public void setFont_Text(Font fn, String text) {
		setFont(fn);
		setToolTipText(text);
	}

}