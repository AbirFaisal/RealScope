package com.owon.uppersoft.vds.util.ui;

import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractListModel;

public class CListModel extends AbstractListModel {
	private List c;
	private int sel;

	public CListModel(List c) {
		setList(c);
	}

	public void setList(List c) {
		this.c = c;
		sel = 0;
		if (c == null)
			c = new LinkedList();
	}

	@Override
	public Object getElementAt(int index) {
		return (Object) c.get(index);
	}

	@Override
	public int getSize() {
		return c.size();
	}

}
