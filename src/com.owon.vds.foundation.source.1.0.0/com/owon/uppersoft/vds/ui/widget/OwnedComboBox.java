package com.owon.uppersoft.vds.ui.widget;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.owon.uppersoft.vds.ui.widget.custom.CComboBox;

/**
 * OwnedComboBox，弹出式的ComboBox
 * 
 * @author Matt
 * 
 */
public class OwnedComboBox extends CComboBox implements ItemListener,
		PopupMenuListener {// FocusListener,
	private ComboBoxOwner cbo;

	public OwnedComboBox(Object[] os, int idx, ComboBoxOwner cbo,
			boolean orienteRight) {
		super(os, orienteRight);
		this.cbo = cbo;

		setSelectedIndex(idx);
		// addFocusListener(this);
		addItemListener(this);
		addPopupMenuListener(this);
	}

	public void onReleaseFocus() {
		cbo.removeOwnedComboBox();
		cbo.afterRemoved();
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED)
			cbo.selected(getSelectedIndex());
	}

	// @Override
	// public void focusGained(FocusEvent e) {
	// }
	//
	// @Override
	// public void focusLost(FocusEvent e) {
	// onReleaseFocus();
	// }

	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
	}

	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		onReleaseFocus();
	}

	@Override
	public void popupMenuCanceled(PopupMenuEvent e) {
	}
}