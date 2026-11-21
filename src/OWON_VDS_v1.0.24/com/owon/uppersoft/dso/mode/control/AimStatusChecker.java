package com.owon.uppersoft.dso.mode.control;

public interface AimStatusChecker {
	/**
	 * The verification method is generally:
	 * 
	 * When the current index reaches the maximum boundary value and delta is positive, returns Max
	 * 
	 * When the current index reaches the minimum boundary value and delta is negative, returns Min
	 * 
	 * If the current value is not a boundary value, after modifying it by delta it can be automatically corrected into a suitable range, and Pass can still be returned, indicating that the delta can be accepted and handled
	 * 
	 * @param delta
	 * @return
	 */
	AimStatus validateDeltaIndex(int delta);
}