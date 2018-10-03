package com.owon.vds.calibration.stuff;

import com.owon.vds.tiny.circle.GainArg;
import com.owon.vds.tiny.firm.pref.model.ArgType;

public interface ArgCreator {
	BaselineArg createBaselineArg(int chl, int vb, ArgType at);

	GainArg createGainArg(int chl, int vb);
}
