package com.owon.uppersoft.vds.core;

/**
 * GDefine，全局定义，不同项目可以有所改变
 *
 */
//
public class GDefine {
	public static final int AREA_WIDTH = 1000;
	// 垂直每格对应的adc不同值个数
	public static final int PIXELS_PER_DIV = 25;
	/** 可能是机型相关的 */
	// public static final int adc_max = 125, adc_min = -125;
	public static final int BlockNum = 20;// 水平
	public static final int HalfAREA_PIXEL_HEIGHT = 125;

	// 水平每格对应的像素个数
	public static final int AREA_WIDTH_BLOCK_PIXES = 50;
	public static final int S2mS = 1000;// 秒换算成毫秒
	public static final int TriggerLevelDivRange = 5;
}
