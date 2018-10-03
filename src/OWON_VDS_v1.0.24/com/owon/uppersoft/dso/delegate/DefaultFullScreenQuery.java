package com.owon.uppersoft.dso.delegate;

import com.owon.uppersoft.vds.core.aspect.control.FullScreenQuery;
import com.owon.uppersoft.vds.core.aspect.control.MachineInfoProvider;

public class DefaultFullScreenQuery implements FullScreenQuery {
	private MachineInfoProvider cc;

	public DefaultFullScreenQuery(MachineInfoProvider cc) {
		this.cc = cc;
	}

	@Override
	public int getFullScreen(int chls_sample_config, int dmidx, int tbidx) {
		return cc.getMachineInfo().getFullScreen(chls_sample_config, dmidx)[tbidx];
	}
}