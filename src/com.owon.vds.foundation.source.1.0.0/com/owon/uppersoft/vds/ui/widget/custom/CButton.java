package com.owon.uppersoft.vds.ui.widget.custom;

import java.awt.FontMetrics;
import java.awt.Insets;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.JButton;

import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.ui.resource.FontCenter;

public class CButton extends JButton implements Localizable {

	public CButton() {
		super();
		setFont(FontCenter.getLabelFont());

	}

	@Override
	public void localize(ResourceBundle rb) {
		setFont(FontCenter.getLabelFont());
		String n = getName();
		if (n != null) {
			setText(rb.getString(n));
			setToolTip(this);
		}

	}

	// @Override
	// public void setText(String text) {
	// super.setText(text);
	// setToolTip(this);
	// }

	public static void setToolTip(AbstractButton bt) {
		Insets i = bt.getInsets();
		FontMetrics fm = bt.getFontMetrics(bt.getFont());
		int tlen = fm.stringWidth(bt.getText());
		int lab_limit = bt.getPreferredSize().width - (i.right + i.left);// -28;
		if (true) {// tlen > lab_limit
			bt.setToolTipText(bt.getText());
		}
		// System.err.println(bt.getName() + " = " + bt.getText().trim() + " :"
		// + tlen + "," + lab_limit);
	};

}