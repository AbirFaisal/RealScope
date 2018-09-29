package com.owon.uppersoft.vds.ui.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

public class OneRowLayout implements LayoutManager {
	private Dimension sz = new Dimension();
	private Insets insets;
	private int gap;

	public OneRowLayout() {
		insets = new Insets(0, 0, 0, 0);
	}

	public OneRowLayout(int align) {
		insets = new Insets(0, align, 0, 0);
	}

	public OneRowLayout(Insets insets, int gap) {
		this.insets = insets;
		this.gap = gap;
	}

	public OneRowLayout(int align, int gap) {
		this(align);
		this.gap = gap;
	}
	
	@Override
	public void layoutContainer(Container parent) {
		parent.getSize(sz);

		int count = parent.getComponentCount();
		int w, y = insets.top, h = sz.height - insets.top - insets.bottom, x = insets.left;
		for (int i = 0; i < count; i++) {
			Component c = parent.getComponent(i);
			if (!c.isVisible())
				continue;
			Dimension sz = c.getPreferredSize();
			w = sz.width;
			c.setBounds(x, y, w, h);
			x += w + gap;
		}

	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		int count = parent.getComponentCount();
		int x = insets.left, y = 0;
		for (int i = 0; i < count; i++) {
			Component c = parent.getComponent(i);
			Dimension sz = c.getPreferredSize();
			y = Math.max(y, sz.height + insets.top + insets.bottom);
			if (!c.isVisible())
				continue;
			x += sz.width + gap;
		}
		x += -gap + insets.right;

		return new Dimension(x, y);
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return parent.getPreferredSize();
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	@Override
	public void removeLayoutComponent(Component comp) {
	}
}