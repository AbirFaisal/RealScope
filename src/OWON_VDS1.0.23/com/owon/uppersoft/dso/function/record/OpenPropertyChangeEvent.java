package com.owon.uppersoft.dso.function.record;

import java.beans.PropertyChangeEvent;

public class OpenPropertyChangeEvent extends PropertyChangeEvent {

	public OpenPropertyChangeEvent(Object source, String propertyName,
			Object oldValue, Object newValue) {
		super(source, propertyName, oldValue, newValue);
		newInt = 0;
	}

	public int newInt;
}