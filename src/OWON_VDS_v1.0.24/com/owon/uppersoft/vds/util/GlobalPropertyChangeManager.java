package com.owon.uppersoft.vds.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * GlobalPropertyChangeManager, abstracts out all places that involve these two methods
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
