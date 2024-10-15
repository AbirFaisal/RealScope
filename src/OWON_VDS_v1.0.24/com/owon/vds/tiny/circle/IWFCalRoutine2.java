package com.owon.vds.tiny.circle;

public interface IWFCalRoutine2 {
	public static final int ROUT_DONE = -1;
	public static final int ROUT_ZERO = 0;
	public static final int ROUT_ONE = 1;

	/**
	 * Ready
	 */
	void getReady();

	/**
	 *Enter a voltage position
	 * 
	 * @param vb
	 */
	void forVB(int vb);

	/**
	 * Perform a calibration attempt based on waveform data values：
	 * 
	 * First verify that the waveform data value meets the target, and if so,
	 * progress to the next item; otherwise, adjust the parameter value and
	 * wait until the next time the data is validated.
	 * 
	 * @return Whether the waveform data of this current data verifies that the
	 * parameter adjustment result of the current project meets the target, and
	 * the value is shown in the above table ROUT_DONE, etc.
	 */
	int routOut();

	/**
	 * Check if there are still calibration items left
	 * 
	 * @return Negative values indicate calibration completion, positive values represent calibration type ID.
	 */
<intEND>
	int getRoutCalType();

}