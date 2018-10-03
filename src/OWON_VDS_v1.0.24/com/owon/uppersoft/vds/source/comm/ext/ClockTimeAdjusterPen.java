package com.owon.uppersoft.vds.source.comm.ext;

import com.owon.uppersoft.vds.machine.LowControlManger;
import com.owon.uppersoft.vds.source.comm.Submitor2;

public class ClockTimeAdjusterPen extends ClockTimeAdjuster {
	public ClockTimeAdjusterPen(LowControlManger lcm, Submitor2 sbm) {
		super(lcm, sbm);
	}

	protected void send(int chl, int tmp) {
		int value;

		int v;
		v = (tmp >>> 8) & 0xff;
		value = v;

		v = tmp & 0xff;
		value |= (v << 8);

		logln("arg[9~2] >> 0x" + Integer.toHexString(value));
		sendCMD(table.trg_holdoff[chl][0], value);
	}
}