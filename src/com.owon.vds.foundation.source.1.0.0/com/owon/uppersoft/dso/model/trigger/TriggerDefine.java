package com.owon.uppersoft.dso.model.trigger;

public interface TriggerDefine {

	public static final String[] SINGLE_ALT = { ("M.Trg.Single"),
			("M.Trg.Alternate") };

	public static final String[] CONDITIONS_PULSE = { ("M.Trg.P.Condition.P>"),
			("M.Trg.P.Condition.P="), ("M.Trg.P.Condition.P<"),
			("M.Trg.P.Condition.N>"), ("M.Trg.P.Condition.N="),
			("M.Trg.P.Condition.N<") };

	public static final String[] CONDITIONS_SLOPE = { ("M.Trg.S.Condition.R>"),
			("M.Trg.S.Condition.R="), ("M.Trg.S.Condition.R<"),
			("M.Trg.S.Condition.F>"), ("M.Trg.S.Condition.F="),
			("M.Trg.S.Condition.F<") };

	public static final String[] SWEEP = { ("M.Trg.TrigMode.Auto"),
			("M.Trg.TrigMode.Normal"), ("M.Trg.TrigMode.Once") };

	public static final String[] HOLDOFF_STEP_TIME_UNIT = new String[] { "+",
			"++", "+++" };

	public static final String[] CONDITION_STEP_TIME_UNIT = new String[] { "+",
			"++", "+++" };

	public static final String[] COUPLING = new String[] { "DC", "AC", "HF",
			"LF" };

	public static final int TrgModeSingleIndex = 0;
	public static final int TrgModeAltIndex = 1;

	public static final int TrigOnceIndex = 2;
	public static final int TrigNormalIndex = 1;
	public static final int TrigSweepAutoIndex = 0;

}