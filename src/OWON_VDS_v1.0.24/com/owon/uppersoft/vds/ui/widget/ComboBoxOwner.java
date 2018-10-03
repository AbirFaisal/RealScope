package com.owon.uppersoft.vds.ui.widget;

public interface ComboBoxOwner {

	void selected(int idx);

	void removeOwnedComboBox();

	void afterRemoved();
}
