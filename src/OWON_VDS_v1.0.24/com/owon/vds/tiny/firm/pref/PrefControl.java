package com.owon.vds.tiny.firm.pref;

public interface PrefControl {
	/**
	 * The first two bytes are special codes.
	 * 
	 * @param devPref Device preference data in byte array format.
	 * @return boolean indicating whether the configuration is valid.
	 */
	boolean loadSyncImageFromDevice(byte[] devPref);

	byte[] outputAsSyncImage();

	/** Synchronization between parameter object and flashmirror must be implemented,
	    then external call to synchronize flashmirror and flash. */
	void rollFactory2Device();

	void save2DevicePart();

	void save2FactoryPart();
	
	void saveRegistry();
}
