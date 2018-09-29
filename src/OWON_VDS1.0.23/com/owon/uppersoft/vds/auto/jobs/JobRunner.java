package com.owon.uppersoft.vds.auto.jobs;

import java.util.List;

import com.owon.uppersoft.vds.auto.WFAutoRoutine;
import com.owon.uppersoft.vds.device.interpret.CMDResponser;

public interface JobRunner {
	void replaceRunnable(Runnable run);

	void tail();

	// void resumeCustomize();

	void doArrangeChannels(List<WFAutoRoutine> next_wfrlist, int tbi);

	void queryVideoTrgd(CMDResponser responser);
}
