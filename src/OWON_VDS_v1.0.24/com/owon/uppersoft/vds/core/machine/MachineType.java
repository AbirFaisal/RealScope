package com.owon.uppersoft.vds.core.machine;

import java.math.BigDecimal;

import com.owon.uppersoft.vds.core.paint.IPaintOne;

public interface MachineType {

	/**
	 * 存盘文件的id
	 * 
	 * @return
	 */
	int saveID();

	/**
	 * 机型名
	 * 
	 * @return
	 */
	String name();

	/**
	 * 系列名
	 * 
	 * @return
	 */
	String series();

	/**
	 * 是否支持带宽限制
	 * 
	 * @return
	 */
	boolean bandLimit();

	/**
	 * 根据base，ref以及当前采样率的kHz，计算出浮点型的硬件频率计数
	 * 
	 * @param base
	 * @param ref
	 * @param sampleRate_kHz
	 * @return
	 */
	double doHandleFrequencyCompute(int base, int ref, BigDecimal sampleRate_kHz);

	/**
	 * 测量时计算出来的频率限界值，一般为带宽的1.2倍，负值可理解为不限制
	 * 
	 * @return
	 */
	double getLimitFrequency();

	/**
	 * 所有通道开启时的最大采样率
	 * 
	 * @return
	 */
	int getMaxSampleRateWhenChannelsOn();

	/**
	 * 所有通道开启时的硬件频率计数上限
	 * 
	 * @return 单位Hz
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