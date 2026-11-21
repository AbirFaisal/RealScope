package com.owon.uppersoft.vds.util;

import java.util.Calendar;

/**
 * TimeMeasure, timer
 * 
 */
public class TimeMeasure {

	long startT;
	long stopT;

	/**
	 * Start timing
	 */
	public void start() {
		startT = Calendar.getInstance().getTimeInMillis();
	}

	/**
	 * Stop timing
	 */
	public void stop() {
		stopT = Calendar.getInstance().getTimeInMillis();
	}

	/**
	 * Compute the time difference, in milliseconds
	 * 
	 * @return
	 */
	public long measure() {
		return stopT - startT;
	}

	/**
	 * Compute the time difference, in seconds
	 * 
	 * @return
	 */
	public long measureInSecond() {
		return measure() / 1000;
	}

	public static void main_hide(String[] args) {
		System.out.println((2 << 1) + 1);
	}
}