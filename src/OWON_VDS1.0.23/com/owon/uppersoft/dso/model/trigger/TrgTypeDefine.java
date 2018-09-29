package com.owon.uppersoft.dso.model.trigger;


/**
 * TrgTypeDefine
 * 
 */
public enum TrgTypeDefine {
	Edge, Video, Slope, Pulse;

	public final String key;

	private TrgTypeDefine() {
		String e = name();
		e = e.substring(0, 1).toUpperCase();
		key = "M.Trg." + e;
	}

	public static final TrgTypeDefine[] VALUES = values();

	public static final TrgTypeDefine getExtTrgDefine(int idx) {
		if (idx < 0 || idx >= VALUES.length) {
			System.err.println("ExtTrgDefine getExtTrgDefine(int idx) err!");
			return TrgTypeDefine.Edge;
		}
		return TrgTypeDefine.VALUES[idx];
	}

//	@Override
//	public String toString() {
//		return rbp.bundle().getString(key);
//	}
//
//	private static ResourceBundleProvider rbp;
//
//	public static final void setResourceBundleProvider(ResourceBundleProvider r) {
//		rbp = r;
//	}

}
