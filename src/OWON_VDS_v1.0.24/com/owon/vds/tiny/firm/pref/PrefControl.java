package com.owon.vds.tiny.firm.pref;

public interface PrefControl {
	/**
	 * 前两个字节是特殊码
	 * 
	 * @param devPref
	 * @return 配置是否可用
	 */
	boolean loadSyncImageFromDevice(byte[] devPref);

	byte[] outputAsSyncImage();

	/** 需要在参数对象和flashmirror之间实现同步，然后由外部调用flashmirror和flash的同步 */
	void rollFactory2Device();

	void save2DevicePart();

	void save2FactoryPart();
	
	void saveRegistry();
}
