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
	 * 先验证波形数据值是否符合目标，如果是，则进展到下一个项目；否则，调整参数值，等到下一次获取数据的验证
	 * 
	 * @return 本次波形数据是否验证了当前项目的参数调整结果符合目标，取值见上表ROUT_DONE等
	 */
	int routOut();

	/**
	 * 用于判断是否还有校正项目
	 * 
	 * @return 负值为校正终结，正值为校正类型id
	 */
	int getRoutCalType();

}