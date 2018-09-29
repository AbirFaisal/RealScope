package com.owon.uppersoft.vds.data;

import java.util.Locale;

public class LocaleObject {
	private final Locale l;

	public LocaleObject(Locale l) {
		this.l = l;
	}

	public Locale getLocale() {
		return l;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof LocaleObject))
			return false;

		LocaleObject lo = (LocaleObject) obj;
		return l.equals(lo.l);
	}

	@Override
	public String toString() {
		return l.getDisplayName(l);
	}

	public String getDisplayNameForChs() {
		return l.getDisplayName(Locale.CHINESE);
	}
}
