package com.owon.uppersoft.dso.source.comm.effect;

import com.owon.uppersoft.dso.source.comm.TrgStatus;
import com.owon.uppersoft.vds.core.wf.dm.DMInfo;

public class ReConfigUtil {

	public static final int Status_RT_NoDM = -1;

	public static final boolean checkDMAvailable(DMInfo ci) {
		boolean b = ci.triggerStatus == TrgStatus.ReCfg.ordinal();
		if (b)
			ci.status = Status_RT_NoDM;
		return !b;
	}
}