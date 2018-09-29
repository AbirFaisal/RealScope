package com.owon.uppersoft.dso.wf.dm;

import com.owon.uppersoft.dso.wf.common.dm.IDiluteInfoUnit;
import com.owon.uppersoft.dso.wf.common.dm.LocInfo;
import com.owon.uppersoft.vds.core.wf.dm.DMInfo;

public class LocInfoTiny extends LocInfo {

	public LocInfoTiny() {
	}

	@Override
	protected void prepareBeforeInitLoadInfoUnit(DMInfo cti) {
	}

	@Override
	protected IDiluteInfoUnit createDiluteInfoUnit(LocInfo li) {
		return new DiluteInfoUnitTiny(li);
	}
}