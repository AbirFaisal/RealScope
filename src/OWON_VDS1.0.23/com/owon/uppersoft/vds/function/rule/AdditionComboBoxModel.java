package com.owon.uppersoft.vds.function.rule;

import java.io.Serializable;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.MutableComboBoxModel;

public class AdditionComboBoxModel extends AbstractListModel implements
		MutableComboBoxModel, Serializable {

	private String li;

	public String getLastItem() {
		return li;
	}

	private List list;
	private Object selobj;

	public AdditionComboBoxModel(List list, String lastItem) {
		this.list = list;
		li = lastItem;
	}

	@Override
	public Object getSelectedItem() {
		return selobj;
	}

	@Override
	public void setSelectedItem(Object anItem) {
		selobj = anItem;
		fireContentsChanged(this, -1, -1);
	}

	@Override
	public Object getElementAt(int index) {
		if (index == list.size())
			return getLastItem();
		return list.get(index);
	}

	@Override
	public int getSize() {
		return list.size() + 1;
	}

	@Override
	public void addElement(Object obj) {
		list.add(obj);
		int idx = getSize() - 1;
		fireIntervalAdded(this, idx, idx);
	}

	@Override
	public void insertElementAt(Object obj, int index) {
		list.listIterator(index).add(obj);
		fireIntervalAdded(this, index, index);
	}

	@Override
	public void removeElement(Object obj) {
		list.remove(obj);

		if (obj.equals(getLastItem())) {
			int idx = getSize() - 1;
			fireIntervalRemoved(this, idx, idx);
			return;
		}

		int idx = list.indexOf(obj);
		if (idx != -1)
			fireIntervalRemoved(this, idx, idx);
	}

	@Override
	public void removeElementAt(int index) {
		list.remove(index);
		fireIntervalRemoved(this, index, index);
	}

	@Override
	public void fireContentsChanged(Object source, int index0, int index1) {
		super.fireContentsChanged(source, index0, index1);
	}

	@Override
	public void fireIntervalAdded(Object source, int index0, int index1) {
		super.fireIntervalAdded(source, index0, index1);
	}

	@Override
	public void fireIntervalRemoved(Object source, int index0, int index1) {
		super.fireIntervalRemoved(source, index0, index1);
	}
}
