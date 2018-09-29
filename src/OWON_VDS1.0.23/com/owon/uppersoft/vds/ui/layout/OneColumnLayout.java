package com.owon.uppersoft.vds.ui.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

public class OneColumnLayout implements LayoutManager {
	private Dimension sz = new Dimension();
	private Insets insets;
	private int gap;

	public OneColumnLayout() {
		insets = new Insets(0, 0, 0, 0);
	}

	public OneColumnLayout(int align) {
		insets = new Insets(align, 0, 0, 0);
	}

	public OneColumnLayout(Insets insets, int gap) {
		this.insets = insets;
		this.gap = gap;
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	@Override
	public void layoutContainer(Container parent) {
		parent.getSize(sz);

		int count = parent.getComponentCount();
		int h, y = insets.top, w = sz.width - insets.left - insets.right, x = insets.left;
		for (int i = 0; i < count; i++) {
			Component c = parent.getComponent(i);
			if (!c.isVisible())
				continue;
			Dimension sz = c.getPreferredSize();
			h = sz.height;
			c.setBounds(x, y, w, h);
			y += h + gap;
		}
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return parent.getPreferredSize();
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		int count = parent.getComponentCount();
		int x = 0, y = insets.top;
		for (int i = 0; i < count; i++) {
			Component c = parent.getComponent(i);
			Dimension sz = c.getPreferredSize();
			x = Math.max(x, sz.width + insets.left + insets.right);
			if (!c.isVisible())
				continue;
			y += sz.height + gap;
		}
		y += -gap + insets.bottom;

		return new Dimension(x, y);
	}

	@Override
	public void removeLayoutComponent(Component comp) {
	}
}