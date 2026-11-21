package com.owon.uppersoft.vds.core.comm.job;

import java.nio.ByteBuffer;

import com.owon.uppersoft.vds.core.comm.BufferredSourceManager;
import com.owon.uppersoft.vds.core.comm.ICommunicateManager;

/**
 * Customize JobUnit
 * 
 */
public abstract class JobUnit_C implements JobUnit {

	private String name;

	public JobUnit_C(String name) {
		this.name = name;
	}

	/**
	 * While invoking the task downlink, the buffer is reset and reused repeatedly; since the process is serial, no usage conflict occurs
	 * 
	 */
	protected abstract void packCJob(ByteBuffer bbuf);

	public void doJob(BufferredSourceManager sm) {
		// System.err.println("delJobUnit: " + name);
		if (!sm.isConnected())
			return;

		if (!shouldDoJob()) {
			return;
		}

		/**
		 * Does not use the command packing method provided by this class; instead overrides packJob() to fully customize the content of the command sent
		 */
		ByteBuffer bbuf = sm.clearByteBuffer();
		packCJob(bbuf);
		// DBG.dbgArray(bbuf.array(), 0, bbuf.position());
		sm.write(bbuf.array(), bbuf.position());
		// Comment out and disable the connection check to debug the commands sent in isolation

		// long t1 = System.currentTimeMillis();

		afterJob(sm);

		// long t2 = System.currentTimeMillis();
		// DBG.outprintln("response_time(ms): " + (t2 - t1));
	}

	/**
	 * Override in subclasses
	 * 
	 * Perform some actions after the task's common behavior
	 * 
	 * @param cm
	 * @param sm
	 */
	protected void afterJob(ICommunicateManager sm) {
	}

	protected final boolean shouldDoJob() {
		return true;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean merge(JobUnit ju) {
		return false;
	}
}
