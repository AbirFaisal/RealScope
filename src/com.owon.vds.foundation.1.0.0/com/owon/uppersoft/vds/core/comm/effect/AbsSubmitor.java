package com.owon.uppersoft.vds.core.comm.effect;

import com.owon.uppersoft.vds.core.comm.JobQueueDispatcher;
import com.owon.uppersoft.vds.core.comm.job.JobUnit;

public abstract class AbsSubmitor implements Submitable {

	public void addJobUnit(JobUnit p) {
		jqd.addJobUnit(p);
		resetMark();
	}

	protected JobQueueDispatcher jqd;

	public AbsSubmitor(JobQueueDispatcher jqd) {
		this.jqd = jqd;
	}

	@Override
	public void apply() {
		applyThen(null);
	}

	@Override
	public void prepare() {
	}

	private boolean recommendOptimize;

	@Override
	public void recommendOptimize() {
		recommendOptimize = true;
	}

	public boolean isRecommendOptimize() {
		return recommendOptimize;
	}

	protected void resetMark() {
		recommendOptimize = false;
	}

	private Runnable rb;

	private Runnable getRegisterRunnable() {
		return rb;
	}

	private Runnable consumeRegisterRunnable() {
		Runnable run = getRegisterRunnable();
		registerRunnable(null);
		return run;
	}

	private void registerRunnable(Runnable run) {
		this.rb = run;
	}

}