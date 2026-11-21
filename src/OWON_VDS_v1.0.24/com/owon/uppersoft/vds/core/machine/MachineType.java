package com.owon.uppersoft.vds.core.machine;

import java.math.BigDecimal;

import com.owon.uppersoft.vds.core.paint.IPaintOne;

public interface MachineType {

	/**
	 * Saved file's id
	 * 
	 * @return
	 */
	int saveID();

	/**
	 * Model name
	 * 
	 * @return
	 */
	String name();

	/**
	 * Series name
	 * 
	 * @return
	 */
	String series();

	/**
	 * Whether bandwidth limit is supported
	 * 
	 * @return
	 */
	boolean bandLimit();

	/**
	 * Based on base, ref and the current sample rate in kHz, compute the floating-point hardware frequency count
	 * 
	 * @param base
	 * @param ref
	 * @param sampleRate_kHz
	 * @return
	 */
	double doHandleFrequencyCompute(int base, int ref, BigDecimal sampleRate_kHz);

	/**
	 * Frequency limit computed during measurement, generally 1.2 times the bandwidth; a negative value can be understood as unlimited
	 * 
	 * @return
	 */
	double getLimitFrequency();

	/**
	 * Max sample rate when all channels are enabled
	 * 
	 * @return
	 */
	int getMaxSampleRateWhenChannelsOn();

	/**
	 * Upper limit of the hardware frequency count when all channels are enabled
	 * 
	 * @return unit Hz
	 */
	int getMaxFreqWhenChannelsOn();

	boolean isVideoTrgSupport();

	boolean isExtTrgSupport();

	boolean isMultiIOSupport();

	boolean isTrgEdgeMiddleSupport();

	IPaintOne getPaintOne();

	boolean passSlowOnNoneSlowTimebase();

	boolean isPhosphorOn();

	boolean isSupportNetwork();

}