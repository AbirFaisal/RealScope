package com.owon.uppersoft.vds.ui.widget.custom;

import java.awt.Color;

import javax.swing.JButton;
import javax.swing.plaf.basic.BasicButtonUI;

public class LButton extends JButton {

	public LButton(String text) {
		this();
		setText(text);
		setForeground(Color.white);
	}

	public LButton() {
		setOpaque(false);
		setUI(new BasicButtonUI());
	}

}
