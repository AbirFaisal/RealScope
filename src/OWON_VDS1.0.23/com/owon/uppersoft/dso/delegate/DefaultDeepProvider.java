package com.owon.uppersoft.dso.delegate;

import com.owon.uppersoft.vds.core.aspect.control.DeepProvider;
import com.owon.uppersoft.vds.core.aspect.control.MachineInfoProvider;

public class DefaultDeepProvider implements DeepProvider {
	private MachineInfoProvider mip;

	public DefaultDeepProvider(MachineInfoProvider mip) {
		this.mip = mip;
	}

	public int getLength(int dmIndex) {
		return mip.getMachineInfo().DEEPValue[dmIndex];
	}

	@Override
	public String getLabel(int dmIndex) {
		return mip.getMachineInfo().DEEP[dmIndex];
	}

	@Override
	public int getDeepNumber() {
		return mip.getMachineInfo().DEEP.length;
	}
}