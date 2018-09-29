package com.owon.vds.calibration.stuff;

import com.owon.uppersoft.vds.core.tune.ICal;
import com.owon.vds.tiny.firm.pref.model.ArgType;

public interface CalArgTypeProvider {
	ICal getSimpleAdjustCMDType(ArgType at);
}
