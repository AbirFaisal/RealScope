package com.owon.vds.tiny.tune;

import com.owon.uppersoft.vds.core.comm.effect.JobUnitDealer;

public interface TinyTuneDelegate {

	void argChange(int type, int chl, int vb);

	void vbChangeForChannel(int chl, int vbidx);

	void selfCalibration();

	/**
	 * Respond to factory settings and sync
	 */
	void resumeFactoryNSync();

	/**
	 * Write parameters and sync
	 */
	void writeDeviceNSync();

	/**
	 * Write all information to factory settings and sync
	 */
	void writeFactoryNSync();

	void writeRegistryNSync();

	JobUnitDealer getJobUnitDealer();
}