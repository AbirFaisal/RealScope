package com.owon.vds.tiny.ui.tune.control;

import com.owon.uppersoft.vds.core.tune.IntVolt;

public interface CalibrationArgType {

	/**
	 * 控件内容更新
	 */
	void contentUpdate();

	/**
	 * 同步当前到设备
	 */
	void sync2Device();

	/**
	 * 界面控件创建
	 * 
	 * @param con
	 */
	void createContent(IntVolt[] volts);

	/**
	 * 对应通道的电压档位改变时的回调方法
	 * 
	 * @param channel
	 *            TODO
	 * @param idx
	 */
	void onVoltbaseChange(int channel, int idx);

}