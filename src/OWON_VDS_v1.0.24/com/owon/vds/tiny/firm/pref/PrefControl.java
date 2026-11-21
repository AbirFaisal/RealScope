package com.owon.vds.tiny.firm.pref;

public interface PrefControl {
	/**
	 * The first two bytes are a special code
	 * 
	 * @param devPref
	 * @return whether the configuration is usable
	 */
	boolean loadSyncImageFromDevice(byte[] devPref);

	byte[] outputAsSyncImage();

	/** Synchronization is needed between the parameter object and flashmirror, then the external caller synchronizes flashmirror and flash */
	void rollFactory2Device();

	void save2DevicePart();

	void save2FactoryPart();
	
	void saveRegistry();
}
