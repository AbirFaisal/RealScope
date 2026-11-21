package com.owon.uppersoft.vds.core.comm.job;

import java.nio.ByteBuffer;

import com.owon.uppersoft.vds.core.comm.ICommunicateManager;

/**
 * :M Pack JobUnit
 * 
 */
public abstract class JobUnit_M extends JobUnit_C {

	public JobUnit_M(String name) {
		super(name);
	}

	/**
	 * Override in subclasses
	 * 
	 * @param bbuf
	 */
	protected abstract void packMJob(ByteBuffer bbuf);

	@Override
	protected void packCJob(ByteBuffer bbuf) {
		bbuf.position(6);

		packMJob(bbuf);

		int pos = bbuf.position();
		bbuf.position(0);
		bbuf.put((byte) ':').put((byte) 'M').putInt(pos - 6);

		// DBG.outArrayAlpha(bbuf.array(), bbuf.position(), pos - 6);

		bbuf.position(pos);
	}

	@Override
	protected void afterJob(ICommunicateManager sm) {
		/**
		 * :M prefix with first command byte M are classified as menu command update commands, answered uniformly
		 * 
		 * Methods of this kind must call the super method at the very start of the implementation to complete the response step
		 */
		// receiveResponse(sm, mresponse, mresponse.length);
		// DBG.outArrayAlpha(mresponse, 0, mresponse.length);
	}

	public int resNum = 0;

	protected void receiveResponse(ICommunicateManager sm, byte[] rsp, int len) {
		resNum = sm.acceptResponse(rsp, len);
	}

	@Override
	public boolean merge(JobUnit ju) {
		return false;
	}
}
