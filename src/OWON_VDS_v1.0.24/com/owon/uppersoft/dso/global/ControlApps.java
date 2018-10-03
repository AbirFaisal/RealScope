package com.owon.uppersoft.dso.global;

import com.owon.uppersoft.dso.machine.aspect.IStopable;
import com.owon.uppersoft.dso.source.comm.AbsInterCommunicator;
import com.owon.uppersoft.dso.source.comm.InfiniteDaemon;
import com.owon.uppersoft.dso.source.comm.effect.SubmitorFactory;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.vds.core.comm.IRuntime;
import com.owon.uppersoft.vds.core.comm.JobQueueDispatcher;
import com.owon.uppersoft.vds.core.comm.effect.JobUnitDealer;
import com.owon.uppersoft.vds.util.Pref;

public abstract class ControlApps implements IStopable {
	protected JobQueueDispatcher df;
	public AbsInterCommunicator interComm;
	private InfiniteDaemon daemon;
	protected ControlManager cm;
	private OperateBlocker ob;

	public ControlApps(DataHouse dh, MainWindow mw) {
		cm = dh.controlManager;
		ir = cm.getIRuntime();
		ob = new OperateBlocker();

		df = new JobQueueDispatcher(cm.sourceManager, cm.pcs);

		SubmitorFactory.setSourceManager(df, cm);

		interComm = createInterCommunicator(dh, mw);
		daemon = createInfiniteDaemon(dh, mw, df, interComm);
		// controlManager.reloadManager.reload();
		reload();
		daemon.start();
	}

	protected abstract InfiniteDaemon createInfiniteDaemon(DataHouse dh,
			MainWindow mw, JobQueueDispatcher df2,
			AbsInterCommunicator interComm2);

	protected abstract AbsInterCommunicator createInterCommunicator(
			DataHouse dh, MainWindow mw);

	protected abstract void reload();

	public OperateBlocker getOperateBlocker() {
		return ob;
	}

	public JobQueueDispatcher getJobQueueDispatcher() {
		return df;
	}

	public JobUnitDealer getJobUnitDealer() {
		return df;
	}

	public InfiniteDaemon getDaemon() {
		return daemon;
	}

	public ControlManager getControlManager() {
		return cm;
	}

	public void onExit() {
		releaseConnect();
	}

	public void onPersist(Pref p) {
		daemon.onExit(p);
	}

	private IRuntime ir;

	public void releaseConnect() {
		/** 运行状态已开启则忽略当前装载的行为 */
		if (!ir.isKeepGet())
			return;
		// 先停下，再断开
		InfiniteDaemon id = daemon;
		id.addMission(id.m_dislink);
	}

	public void stopkeep() {
		if (!ir.isKeepGet())
			return;
		ir.setKeepGet(false);

		/** KNOW 停止后马上减到单帧 */
		cm.reduceFrame();
		isDMprepared = (true);
	}

	public void stopkeepNForbidDM() {
		if (!ir.isKeepGet())
			return;
		ir.setKeepGet(false);

		isDMprepared = (false);
	}

	private boolean isDMDataGotAlready = false;
	private boolean isDMprepared = false;

	/** 设置深存储是否拿过一次 */
	public void setDMDataGotAlready(boolean b) {
		isDMDataGotAlready = b;
	}

	public boolean isDMDataGotAlready() {
		return isDMDataGotAlready;
	}

	public boolean isDMAvailable() {
		return isDMprepared;
	}

	public void resumeKeep() {
		ir.setKeepGet(true);
	}

	// public void load() {
	// /** 运行状态已开启则忽略当前装载的行为 */
	// if (ir.isKeepGet())
	// return;
	//
	// InfiniteDaemon id = getDaemon();
	// id.addMission(id.m_getdataonce);
	// }

	public void keepload() {
		/** 运行状态已开启则忽略当前装载的行为 */
		if (ir.isKeepGet())
			return;

		// 先运行，再计数
		InfiniteDaemon id = daemon;
		id.addMission(id.m_startGet);
	}

}