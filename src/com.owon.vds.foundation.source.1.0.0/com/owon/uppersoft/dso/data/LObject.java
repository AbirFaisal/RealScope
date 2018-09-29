package com.owon.uppersoft.dso.data;

import com.owon.uppersoft.dso.i18n.I18nProvider;

public class LObject {
	private String key;

	public LObject(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	@Override
	public String toString() {
		return I18nProvider.bundle().getString(getKey());
	}

	public static final LObject[] getLObjects(String[] keys) {
		int len = keys.length;
		LObject[] los = new LObject[len];
		for (int i = 0; i < len; i++) {
			los[i] = new LObject(keys[i]);
		}
		return los;
	}
	
	static final LObject[] getLObjects(LObject[] keys) {
		return null;
	}
}