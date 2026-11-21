package com.owon.uppersoft.vds.core.aspect.control;

import java.math.BigDecimal;

public interface TimeConfProvider {
	boolean isOnSlowMoveTimebase(int tbidx);

	String getTimebaseLabel(int tbidx);

	BigDecimal getBDTimebase(int tbidx);

	int getTimebaseNumber();

	/**
	 * The scaling ratio needed from timebase a to timebase b; a generally uses the absolute timebase at the time the stop was loaded
	 * 
	 * bdTIMEBASE[a].divide(bdTIMEBASE[b]);
	 * 
	 * @param a
	 * @param b
	 * @return >1 means stretch, <1 means compression
	 */
	BigDecimal ratio(int a, int b);
}
