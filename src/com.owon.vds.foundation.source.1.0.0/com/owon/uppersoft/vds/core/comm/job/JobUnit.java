package com.owon.uppersoft.vds.core.comm.job;

import com.owon.uppersoft.vds.core.comm.BufferredSourceManager;

public interface JobUnit {
	void doJob(BufferredSourceManager sm);

	String getName();
	
	boolean merge(JobUnit ju);
}
