package com.owon.uppersoft.dso.model.machine;

import java.math.BigDecimal;

import com.owon.uppersoft.vds.core.machine.MachineType;

/**
 * @RF 抽象类实现三个常用的成员属性
 * 
 */
public abstract class AbsMacType implements MachineType {

	public static final int FreqRef_100M = 100000000;
	public static final int FreqRef_50M = 50000000;

	/** 在除了1k的基础上，进行比较操作 */
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
	 * 接收到的频率算法：频率 = 频率值/参考值*采样率因子 （100M机型采样率因子一般是125M和100M）
	 * 
	 * 100M为界:vds3102和3104当前的采样率<=100M,则乘以因子100M; 当前的采样率>100M,则乘以因子125M 。
	 * 125M为界:vds3102和3104当前的采样率<125M,则乘以因子100M; 当前的采样率>=125M,则乘以因子125M 。
	 * 另外,vds2062和2064是60M机型,其他条件不变,因子100M替换为50M;因子125M替换为62.5M。
	 * 
	 * @param base
	 * @param ref
	 * @param sampleRate_kHz
	 *            当前采样率,它与时基、存储深度有关
	 * @param threshold
	 *            采样率档位分界,目前以100M为界，用来比较当前采样率
	 * @param divUpper
	 *            vds3102和3104为125M,vds2062和2064为62.5M
	 * @param divLower
	 *            vds3102和3104为100M,vds2062和2064为50M
	 * @param max
	 *            上限为所有通道都开启后的最大采样率的1/4
	 * @return
	 */
	protected static double doHandleFrequencyCompute(float base, float ref,
			BigDecimal sampleRate_kHz, int threshold, int divUpper, int divLower) {
		/** 这里作针对硬件频率计的参数按采样率区分判断 */
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
		// div一般为125K，实际要乘125M
		return v;
	}

}
