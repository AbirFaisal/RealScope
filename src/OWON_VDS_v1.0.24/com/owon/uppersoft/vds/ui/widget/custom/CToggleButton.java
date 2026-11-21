package com.owon.uppersoft.vds.ui.widget.custom;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;

import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.ui.resource.FontCenter;
import com.owon.uppersoft.vds.ui.widget.help.ToggleListener;

/**
 * Custom CToggleButton
 * 
 */
public class CToggleButton extends JButton implements Localizable {
	private String selkey, uslkey;
	private ResourceBundle rb;

	public CToggleButton(String selkey, String uslkey, boolean sel,
			ResourceBundle rb) {
		this(selkey, uslkey, null, sel, rb);
	}

	public CToggleButton(final String selkey, final String uslkey,
			final ToggleListener al, boolean sel, ResourceBundle rb) {
		this.selkey = selkey;
		this.uslkey = uslkey;
		this.rb = rb;
		/** The three parameters could also be object members; the saved range differs little */
		setFont(FontCenter.getLabelFont());

		setSelected(sel);
		if (al != null) {
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setSelected(al.toggle(e, isSelected()));
				}
			});
		} else {
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setSelected(!isSelected());
				}
			});
		}
	}

	public void setSelected(boolean sel) {
		/** Overridden method, adds custom text-change logic, does not trigger ActionEvent */
		super.setSelected(sel);
		u(selkey, uslkey);
	}

	private void u(String selkey, String uslkey) {
		String key = isSelected() ? selkey : uslkey;
		String n = rb.getString(key);
		setText(n);
	}

	@Override
	public void localize(ResourceBundle rb) {
		setFont(FontCenter.getLabelFont());
	}
}