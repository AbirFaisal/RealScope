package com.owon.uppersoft.vds.wf;

import com.owon.uppersoft.dso.wf.common.dm.LocInfo;
import com.owon.uppersoft.dso.wf.dm.LocInfoTiny;
import com.owon.uppersoft.vds.core.aspect.control.Pos0_VBChangeInfluence;
import com.owon.uppersoft.vds.core.aspect.control.VoltageProvider;
import com.owon.uppersoft.vds.core.wf.WaveFormInfo;

public class WFI_tiny extends WaveFormInfo {

	public WFI_tiny(int number, VoltageProvider vp, Pos0_VBChangeInfluence pvi) {
		super(number, vp, pvi);
	}

	@Override
	protected LocInfo createLocInfo() {
		return new LocInfoTiny();
	}
}