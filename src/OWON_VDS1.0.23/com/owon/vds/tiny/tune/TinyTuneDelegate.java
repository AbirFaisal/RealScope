package com.owon.vds.tiny.tune;

import com.owon.uppersoft.vds.core.comm.effect.JobUnitDealer;

public interface TinyTuneDelegate {

	void argChange(int type, int chl, int vb);

	void vbChangeForChannel(int chl, int vbidx);

	void selfCalibration();

	/**
	 * 回复厂家设置并同步
	 */
	void resumeFactoryNSync();

	/**
	 * 将参数写入并同步
	 */
	void writeDeviceNSync();

	/**
	 * 将所有信息写入厂家设置并同步
	 */
	void writeFactoryNSync();

	void writeRegistryNSync();

	JobUnitDealer getJobUnitDealer();
}