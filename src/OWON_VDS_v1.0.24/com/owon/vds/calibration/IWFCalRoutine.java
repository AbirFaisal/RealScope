package com.owon.vds.calibration;

public interface IWFCalRoutine {
	public static final int ROUT_DONE = -1;
	public static final int ROUT_ZERO = 0;
	public static final int ROUT_ONE = 1;

	/**
	 * Ready
	 */
	public abstract void getReady();

	/**
	 * Perform one calibration attempt based on waveform data values:
	 * 
	 * First verify whether the waveform data values meet the target; if so, advance to the next item; otherwise, adjust the parameter value and wait for verification on the next data acquisition
	 * 
	 * @return whether this waveform data verified that the parameter adjustment result of the current item meets the target; for values see the table above (ROUT_DONE, etc.)
	 */
	public abstract int routOut();

	/**
	 * Used to determine whether there are more calibration items
	 * 
	 * @return negative means calibration finished, positive is the calibration type id
	 */
	public abstract int getRoutCalType();

}