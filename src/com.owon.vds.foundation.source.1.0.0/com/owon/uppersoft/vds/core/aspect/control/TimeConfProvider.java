package com.owon.uppersoft.vds.core.aspect.control;

import java.math.BigDecimal;

public interface TimeConfProvider {
	boolean isOnSlowMoveTimebase(int tbidx);

	String getTimebaseLabel(int tbidx);

	BigDecimal getBDTimebase(int tbidx);

	int getTimebaseNumber();

	/**
	 * 从时基a到时基b，需要进行的缩放比率，a一般使用停止载入时的绝对时基
	 * 
	 * bdTIMEBASE[a].divide(bdTIMEBASE[b]);
	 * 
	 * @param a
	 * @param b
	 * @return >1代表拉伸，<1代表压缩
	 */
	BigDecimal ratio(int a, int b);
}
