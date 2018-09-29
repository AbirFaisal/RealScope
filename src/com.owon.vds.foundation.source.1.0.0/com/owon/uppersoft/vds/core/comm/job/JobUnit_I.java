package com.owon.uppersoft.vds.core.comm.job;

import com.owon.uppersoft.vds.core.comm.BufferredSourceManager;
import com.owon.uppersoft.vds.core.comm.ICommunicateManager;

/**
 * Interaction Command JobUnit
 * 
 */
public class JobUnit_I implements JobUnit {

	private String name;

	private byte[] arr = null;
	protected byte[] re_arr = null;

	/**
	 * arr
	 * 
	 * @param arr
	 *            发送的数组
	 */
	public JobUnit_I(byte[] arr) {// String name,
		// this.name = name;
		this.arr = arr;
		init();
	}

	/**
	 * arr & re_arr
	 * 
	 * @param arr
	 *            发送的数组
	 * @param re_arr
	 *            接收返回信息的数组
	 */
	public JobUnit_I(byte[] arr, byte[] re_arr) {// String name,
		// this.name = name;
		this.arr = arr;
		this.re_arr = re_arr;
		init();
	}

	protected void init() {
		name = new String(arr, 0, arr.length);
	}

	protected void prepareJob() {
	}

	public void doJob(BufferredSourceManager sm) {
		// System.err.println("delJobUnit: " + name);
		// System.err.println("delJobUnit: " + name);
		if (!sm.isConnected())
			return;

		if (!shouldDoJob())
			return;

		prepareJob();
		doPreparedArrayJob(sm);
		afterJob(sm);
	}

	protected void doPreparedArrayJob(ICommunicateManager sm) {
		if (re_arr == null) {
			sm.write(arr, arr.length);
		} else {
			interact(sm, arr, re_arr);
		}
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

	private void sendRequest(ICommunicateManager sm, byte[] req) {
		reqNum = sm.write(req, req.length);
	}

	public int resNum = 0, reqNum = 0;

	private void receiveResponse(ICommunicateManager sm, byte[] rsp) {
		resNum = sm.acceptResponse(rsp, rsp.length);
	}

	public void resetSession() {
		resNum = 0;
		reqNum = 0;
	}

	public int getResNum() {
		return resNum;
	}

	public int getReqNum() {
		return reqNum;
	}

	protected void interact(ICommunicateManager sm, byte[] req, byte[] rsp) {
		resetSession();
		sendRequest(sm, req);

		betweenReqNRes();

		receiveResponse(sm, rsp);
	}

	protected void betweenReqNRes() {
	}

	protected boolean shouldDoJob() {
		return true;
	}

	public void setName(String name) {
		this.name = name;
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
