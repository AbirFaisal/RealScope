package com.owon.uppersoft.vds.ui.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

public class FillLayout implements LayoutManager {
	private Dimension sz = new Dimension();

	public FillLayout() {
	}

	@Override
	public void layoutContainer(Container parent) {
		parent.getSize(sz);
		Component c = parent.getComponent(0);
		c.setBounds(0, 0, sz.width, sz.height);
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		return new Dimension(parent.getComponent(0).getPreferredSize());
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