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
	 * 在继承中重写
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
		 * :M开头且指令第一字节为M的归为菜单类指令更新指令，统一应答
		 * 
		 * 之类方法必须在实现的最开始调用super方法以完成应答的步骤
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
