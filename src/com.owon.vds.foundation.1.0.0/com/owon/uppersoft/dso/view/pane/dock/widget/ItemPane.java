package com.owon.uppersoft.dso.view.pane.dock.widget;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JPanel;

import com.owon.uppersoft.vds.ui.layout.OneRowLayout;

public class ItemPane extends JPanel {
	protected ItemPane(LayoutManager mgr) {
		setOpaque(false);
		setLayout(mgr);
	}

	protected ItemPane() {
		this(new OneRowLayout(new Insets(3, 3, 3, 3), 5));
	}

	protected ItemPane(int h) {
		this();
		setPreferredSize(new Dimension(0, h));
	}

}
