package com.owon.uppersoft.dso.model.machine;

import java.math.BigDecimal;

import com.owon.uppersoft.vds.core.machine.MachineType;

/**
 * @RF abstract class implementing three commonly used member attributes
 * 
 */
public abstract class AbsMacType implements MachineType {

	public static final int FreqRef_100M = 100000000;
	public static final int FreqRef_50M = 50000000;

	/** Perform comparison on the basis of everything except 1k */
	public static final int FreqRef_Lower_100M = FreqRef_100M;
	public static final int FreqRef_Upper_100M = 125000000;

	public static final int FreqRef_Lower_60M = FreqRef_Lower_100M / 2;
	public static final int FreqRef_Upper_60M = FreqRef_Upper_100M / 2;

	public static final int FreqResultMaxDivider = 4;

	public static final int V_100M = 100000000;
	public static final int V_250M = 250000000;
	public static final int V_500M = 500000000;

	private int maxSampleRateWhenChannelsOn;

	protected void setMaxSampleRateWhenChannelsOn(int v) {
		maxSampleRateWhenChannelsOn = v;
	}

	@Override
	public int getMaxSampleRateWhenChannelsOn() {
		return maxSampleRateWhenChannelsOn;
	}

	@Override
	public int getMaxFreqWhenChannelsOn() {
		return maxSampleRateWhenChannelsOn / FreqResultMaxDivider;
	}

	@Override
	public boolean isVideoTrgSupport() {
		return true;
	}

	@Override
	public boolean isExtTrgSupport() {
		return true;
	}

	@Override
	public boolean isMultiIOSupport() {
		return true;
	}

	@Override
	public boolean isTrgEdgeMiddleSupport() {
		return false;
	}

	/**
	 * Algorithm for the received frequency: frequency = frequency value / reference value * sampling rate factor (the sampling rate factor of 100M models is generally 125M and 100M)
	 * 
	 * 100M as the boundary: for vds3102 and 3104, if the current sampling rate <=100M, multiply by factor 100M; if the current sampling rate >100M, multiply by factor 125M.
	 * 125M as the boundary: for vds3102 and 3104, if the current sampling rate <125M, multiply by factor 100M; if the current sampling rate >=125M, multiply by factor 125M.
	 * Also, vds2062 and 2064 are 60M models; other conditions are unchanged, with factor 100M replaced by 50M and factor 125M replaced by 62.5M.
	 * 
	 * @param base
	 * @param ref
	 * @param sampleRate_kHz
	 *            current sampling rate, which depends on the timebase and memory depth
	 * @param threshold
	 *            sampling rate scale boundary, currently 100M, used to compare against the current sampling rate
	 * @param divUpper
	 *            vds3102 and 3104 are 125M, vds2062 and 2064 are 62.5M
	 * @param divLower
	 *            vds3102 and 3104 are 100M, vds2062 and 2064 are 50M
	 * @param max
	 *            Upper limit is the maximum sampling rate with all channels enabled, times 1/4
	 * @return
	 */
	protected static double doHandleFrequencyCompute(float base, float ref,
			BigDecimal sampleRate_kHz, int threshold, int divUpper, int divLower) {
		/** Here, the hardware frequency counter parameters are determined by sampling rate ranges */
		int div = divUpper;
		double dbv = sampleRate_kHz.doubleValue() * 1000;
		// System.out.println("sampleRate(dbv):" + dbv + ",threadsold:"
		// + threadsold);
		{
			if (dbv <= threshold)
				div = divLower;
		}
		double v = base / (double) ref * div;
		// System.out.println(",base:"+base+",ref:"+ref+",div"+div+",v:"+v);
		// div is generally 125K, actually needs to be multiplied by 125M
		return v;
	}

}
