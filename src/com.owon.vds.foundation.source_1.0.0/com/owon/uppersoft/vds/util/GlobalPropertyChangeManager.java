package com.owon.uppersoft.vds.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * GlobalPropertyChangeManager，将所以涉及到这两个方法的地方抽象出来
 *
 */
public class GlobalPropertyChangeManager {
	public static final void addPropertyChangeListener(
			PropertyChangeSupport pcs, String propertyName,
			PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(propertyName, listener);
	}

	public static final void addPropertyChangeListener(
			PropertyChangeSupport pcs, PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}
}
