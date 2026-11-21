package com.owon.uppersoft.vds.core;

/**
 * GDefine, global definitions, may vary between projects
 *
 */
//
public class GDefine {
	public static final int AREA_WIDTH = 1000;
	// Number of distinct adc values per vertical division
	public static final int PIXELS_PER_DIV = 25;
	/** May be model-dependent */
	// public static final int adc_max = 125, adc_min = -125;
	public static final int BlockNum = 20;// horizontal 
	public static final int HalfAREA_PIXEL_HEIGHT = 125;

	// Number of pixels per horizontal division
	public static final int AREA_WIDTH_BLOCK_PIXES = 50;
	public static final int S2mS = 1000;// convert seconds to milliseconds
	public static final int TriggerLevelDivRange = 5;
}
