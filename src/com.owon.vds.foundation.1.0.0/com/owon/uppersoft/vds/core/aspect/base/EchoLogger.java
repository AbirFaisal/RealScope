package com.owon.uppersoft.vds.core.aspect.base;

import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.util.PropertiesItem;

public class EchoLogger implements Logable {

	public boolean on = true;

	@Override
	public void log(Object o) {
		if (on)
			Platform.getControlManager().pcs.firePropertyChange(
					PropertiesItem.APPEND_TXT, null, o.toString());
	}

	@Override
	public void logln(Object o) {
		if (on)
			Platform.getControlManager().pcs.firePropertyChange(
					PropertiesItem.APPEND_TXTLINE, null, o.toString());
	}
}