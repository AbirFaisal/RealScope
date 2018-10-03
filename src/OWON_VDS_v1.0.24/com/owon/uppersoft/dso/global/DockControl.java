package com.owon.uppersoft.dso.global;

import java.awt.Window;
import java.beans.PropertyChangeSupport;

import javax.swing.JComponent;

public interface DockControl {
	void init(ControlManager cm);

	void initialize(Window wnd, PropertyChangeSupport pcs, JComponent toolbtn);

	void dockDlgOnOff();

	void dockDialogQuickOpenHide(String pageName);

	void dockDialog2HomePage();
}