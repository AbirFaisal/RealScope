package com.owon.uppersoft.vds.util;

import java.util.Calendar;

/**
 * TimeMeasure，计时器
 * 
 */
public class TimeMeasure {

	long startT;
	long stopT;

	/**
	 * 计时开始
	 */
	public void start() {
		startT = Calendar.getInstance().getTimeInMillis();
	}

	/**
	 * 计时结束
	 */
	public void stop() {
		stopT = Calendar.getInstance().getTimeInMillis();
	}

	/**
	 * 计算时间差，毫秒为单位
	 * 
	 * @return
	 */
	public long measure() {
		return stopT - startT;
	}

	/**
	 * 计算时间差，秒为单位
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