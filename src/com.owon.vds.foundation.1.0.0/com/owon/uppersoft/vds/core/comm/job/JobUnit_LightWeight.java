package com.owon.uppersoft.vds.core.comm.job;

import com.owon.uppersoft.vds.core.comm.BufferredSourceManager;

public abstract class JobUnit_LightWeight implements JobUnit {
	@Override
	public boolean merge(JobUnit ju) {
		return false;
	}

	@Override
	public void doJob(BufferredSourceManager sm) {
	}

	@Override
	public String getName() {
		return "JobUnit_LightWeight";
	}
}
