package com.owon.vds.tiny.tune;

import com.owon.uppersoft.vds.core.comm.effect.JobUnitDealer;

public interface TinyTuneDelegate {

	void argChange(int type, int chl, int vb);

	void vbChangeForChannel(int chl, int vbidx);

	void selfCalibration();

	/**
	 * Restore the factory settings and synchronize
	 */
	void resumeFactoryNSync();

	/**
	 * Write the parameters and synchronize
	 */
	void writeDeviceNSync();

	/**
	 * Write all information to the factory settings and synchronize
	 */
	void writeFactoryNSync();

	void writeRegistryNSync();

	JobUnitDealer getJobUnitDealer();
}