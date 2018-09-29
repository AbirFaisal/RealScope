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
	 * 在调用任务下传的过程中，缓冲区被反复重置使用，由于过程是串行的，不会出现使用冲突
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
		 * 不使用本类提供的指令打包方法，而是重写packJob()来完全自定义发送指令的内容
		 */
		ByteBuffer bbuf = sm.clearByteBuffer();
		packCJob(bbuf);
		// DBG.dbgArray(bbuf.array(), 0, bbuf.position());
		sm.write(bbuf.array(), bbuf.position());
		// 注释并关闭连接检测就可以单独调试发送的指令

		// long t1 = System.currentTimeMillis();

		afterJob(sm);

		// long t2 = System.currentTimeMillis();
		// DBG.outprintln("response_time(ms): " + (t2 - t1));
	}

	/**
	 * 在继承中重写
	 * 
	 * 在任务通用行为之后执行一些动作
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
