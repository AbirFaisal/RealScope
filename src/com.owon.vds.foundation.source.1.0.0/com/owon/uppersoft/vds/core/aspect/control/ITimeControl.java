package com.owon.uppersoft.vds.core.aspect.control;

public interface ITimeControl extends HorizontalUnit {
	public static final String TIMEBASE_INDEX = "Timebase.index";
	public static final String HOR_TRG_POS = "HorTrgPos";

	public static final String onHorTrgPosChangedByTimebase = "onHorTrgPosChangedByTimebase";

	/** 同时fire出newidx, oldidx，用于产生波形的同步影响 */
	public static final String onTimebaseEffect = "onTimebaseEffect";

	/** 同时fire出newidx, oldidx */
//	public static final String onTimebaseChanged = "onTimebaseChanged";

	/** 无需fire出newidx, oldidx */
	public static final String onTimebaseUpdated = "onTimebaseUpdated";

	public static final String onHTPChanged = "onHTPChanged";

	int getTimebaseIdx();

	void c_setHorizontalTriggerPosition(int htp);

	void c_setTimebase_HorTrgPos(int tbIndex, int htp);

	String getHTPLabel(int tbIndex, int htp);

}
