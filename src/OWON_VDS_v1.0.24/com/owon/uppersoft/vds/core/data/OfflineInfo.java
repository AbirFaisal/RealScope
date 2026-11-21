package com.owon.uppersoft.vds.core.data;

import com.owon.uppersoft.vds.util.format.SFormatter;

public class OfflineInfo {
	/** for reading only */
	/* Zero position 4 bytes */
	public int pos0;
	/* Voltage scale 4 bytes */
	public int vbIdx;
	/* Attenuation ratio exponent 4 bytes */
	public int probeMultiIdx;
	/* Frequency 4 bytes */
	public float frequency;
	/* Period 4 bytes */
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