package com.owon.uppersoft.vds.util.ui;

import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.MutableComboBoxModel;

public class ListComboBoxModel extends AbstractListModel implements
		MutableComboBoxModel {
	private List c;
	private int sel;

	public ListComboBoxModel(List c) {
		this.c = c;
		sel = 0;
		if (c == null)
			c = new LinkedList();
	}

	@Override
	public void addElement(Object obj) {
		c.add(obj);
	}

	@Override
	public void insertElementAt(Object obj, int index) {
		c.add(index, obj);
	}

	@Override
	public void removeElement(Object obj) {
		c.remove(obj);
	}

	@Override
	public void removeElementAt(int index) {
		c.remove(index);
	}

	@Override
	public Object getSelectedItem() {
		if (sel >= 0 && sel < c.size()) {
			return c.get(sel);
		}
		
		return "";
	}

	@Override
	public void setSelectedItem(Object anItem) {
		int idx = c.indexOf(anItem);
		if (idx >= 0 && idx < c.size()) {
			sel = idx;
		}
	}

	@Override
	public Object getElementAt(int index) {
		return c.get(index);
	}

	@Override
	public int getSize() {
		return c.size();
	}

}
