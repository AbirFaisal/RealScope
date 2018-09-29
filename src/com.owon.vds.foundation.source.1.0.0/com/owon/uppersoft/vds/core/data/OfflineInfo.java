package com.owon.uppersoft.vds.core.data;

import com.owon.uppersoft.vds.util.format.SFormatter;

public class OfflineInfo {
	/** 读取专用 */
	/* 零点位置 4 字节 */
	public int pos0;
	/* 电压档位 4 字节 */
	public int vbIdx;
	/* 衰减倍率指数 4 字节 */
	public int probeMultiIdx;
	/* 频率 4 字节 */
	public float frequency;
	/* 周期 4 字节 */
	public float cycle;

	public void logDM() {
		config(SFormatter.dataformat("pos0: %d\r\n", pos0));
		config(SFormatter.dataformat("vbIdx: %d\r\n", vbIdx));
		config(SFormatter.dataformat("probeMultiIdx: %d\r\n", probeMultiIdx));
		config(SFormatter.dataformat("frequency: %.2f\r\n", frequency));
		config(SFormatter.dataformat("cycle: %.2f\r\n", cycle));
	}

	private void config(String format) {
	}
}