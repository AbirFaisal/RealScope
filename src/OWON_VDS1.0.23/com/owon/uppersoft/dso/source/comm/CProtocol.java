package com.owon.uppersoft.dso.source.comm;

public interface CProtocol {
	public static final byte trg_edge_coupling = 2;
	public static final byte trg_edge_sweep = 3;
	public static final byte trg_edge_holdoff = 4;
	public static final byte trg_edge_raisefall = 5;

	public static final byte trg_video_module = 2;
	public static final byte trg_video_sync = 3;
	public static final byte trg_video_holdoff = 4;

	public static final byte trg_slope_uppest = 2;
	public static final byte trg_slope_sweep = 3;
	public static final byte trg_slope_holdoff = 4;
	public static final byte trg_slope_condition = 5;
	public static final byte trg_slope_lowest = 6;

	public static final byte trg_pulse_coupling = 2;
	public static final byte trg_pulse_sweep = 3;
	public static final byte trg_pulse_holdoff = 4;
	public static final byte trg_pulse_condition = 5;

	public static final byte trg_voltsense = 6;
}
