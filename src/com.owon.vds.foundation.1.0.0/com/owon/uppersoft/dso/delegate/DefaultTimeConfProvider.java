package com.owon.uppersoft.dso.delegate;

import java.math.BigDecimal;

import com.owon.uppersoft.vds.core.aspect.control.MachineInfoProvider;
import com.owon.uppersoft.vds.core.aspect.control.TimeConfProvider;

public class DefaultTimeConfProvider implements TimeConfProvider {
	private MachineInfoProvider mip;

	public DefaultTimeConfProvider(MachineInfoProvider mip) {
		this.mip = mip;
	}

	@Override
	public BigDecimal getBDTimebase(int tbidx) {
		return mip.getMachineInfo().bdTIMEBASE[tbidx];
	}

	@Override
	public String getTimebaseLabel(int tbidx) {
		return mip.getMachineInfo().TIMEBASE[tbidx];
	}

	@Override
	public int getTimebaseNumber() {
		return mip.getMachineInfo().TIMEBASE.length;
	}

	@Override
	public boolean isOnSlowMoveTimebase(int tbidx) {
		return mip.getMachineInfo().isSlowMove(tbidx);
	}

	@Override
	public BigDecimal ratio(int a, int b) {
		return mip.getMachineInfo().ratio(a, b);
	}
}