package com.owon.uppersoft.vds.util.ui;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JComboBox;

public class ComboboxMouseWheelSupport implements MouseWheelListener {
	private JComboBox jcb;

	public ComboboxMouseWheelSupport(JComboBox jcb) {
		this.jcb = jcb;
		jcb.addMouseWheelListener(this);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (!jcb.isFocusOwner())
			return;
		int c = jcb.getItemCount();
		if (c <= 0)
			return;
		int i = e.getWheelRotation();
		i = jcb.getSelectedIndex() + i;
		if (i < 0) {
			i = 0;
		} else {
			if (i >= c)
				i = c - 1;
		}
		jcb.setSelectedIndex(i);
	}
}
