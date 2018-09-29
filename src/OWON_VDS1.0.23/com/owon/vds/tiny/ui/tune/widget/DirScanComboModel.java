package com.owon.vds.tiny.ui.tune.widget;

import java.io.File;
import java.io.FilenameFilter;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

public class DirScanComboModel extends AbstractListModel implements
		ComboBoxModel {
	private File dir;
	private String[] fileNames;
	private String sel = "";

	public DirScanComboModel(File dir) {
		this.dir = dir;

		refreshDir();
	}

	public void refreshDir() {
		fileNames = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".txt");
			}
		});
		setSelectedItem(getElementAt(0));
		fireContentsChanged(this, 0, getSize());
	}

	@Override
	public Object getElementAt(int index) {
		if (index < 0 || index >= getSize())
			return " ";
		return fileNames[index];
	}

	@Override
	public int getSize() {
		return fileNames.length;
	}

	@Override
	public Object getSelectedItem() {
		return sel;
	}

	@Override
	public void setSelectedItem(Object anItem) {
		sel = anItem.toString();
	}
}