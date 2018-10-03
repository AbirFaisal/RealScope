package com.owon.vds.tiny.ui.tune.control;

import com.owon.uppersoft.vds.core.tune.IntVolt;

public interface CalibrationArgType {

	/**
	 * Control content update
	 */
	void contentUpdate();

	/**
	 * Sync current to device
	 */
	void sync2Device();

	/**
	 * Interface control creation
	 * 
	 * @param con
	 */
	void createContent(IntVolt[] volts);

	/**
	 * Callback method when the voltage position of the corresponding channel is changed
	 * 
	 * @param channel
	 *            TODO
	 * @param idx
	 */
	void onVoltbaseChange(int channel, int idx);

}