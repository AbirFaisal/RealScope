package com.owon.uppersoft.dso.source.comm;

import java.util.LinkedList;

import com.owon.uppersoft.dso.source.manager.SourceManager;
import com.owon.uppersoft.dso.util.DBG;
import com.owon.uppersoft.vds.core.aspect.base.VLog;
import com.owon.uppersoft.vds.core.comm.IRuntime;
import com.owon.uppersoft.vds.core.comm.JobQueueDispatcher;
import com.owon.uppersoft.vds.core.wf.rt.ChannelsTransportInfo;

public class Flow extends Thread implements Runnable {

	public static final int shortWait = 100, midWait = 50;

	protected SourceManager sm;
	protected AbsGetDataRunner gdr;
	protected JobQueueDispatcher df;
	protected IRuntime ir;

	private LinkedList<Runnable> missions = new LinkedList<Runnable>();
	protected VLog vl = new VLog();

	protected SourceManager getSourceManager() {
		return sm;
	}

	public Flow(SourceManager sm, AbsGetDataRunner gdr, JobQueueDispatcher df,
			IRuntime ir) {
		this.sm = sm;
		this.gdr = gdr;
		this.df = df;
		this.ir = ir;
	}

	/**
	 * Currently the task queue runs before the connection check, and the tasks mostly relate to the connected state, though some are done while stopped
	 */
	private void doStuff() {
		synchronized (missions) {
			while (missions.size() > 0) {
				Runnable r = missions.poll();
				r.run();
			}
		}
	}

	public void addMission(Runnable r) {
		DBG.dbgln("[!!!addMission!!!]: " + r);
		synchronized (missions) {
			missions.add(r);
		}
	}

	public void run() {
		try {
			//ServerControl.startSocketServer();
			while (true) {
				// vl.logln("doStuff: " + System.currentTimeMillis());
				/** Handle here the transfer-related matters that must be inserted first; ideally only call this at the very start at a fixed position and process the missionmission */
				doStuff();

				/** Check here whether the program is exiting; if so, exit the thread */
				if (ir.isExit())
					break;

				/**
				 * Enter usb detection only when there is no connection, so it does not happen on a network connection
				 */
				// vl.logln("sm.isConnected(): " + sm.isConnected());
				if (!sm.isConnected()) {

					// System.out.println("check");
					onNotConnecting();
					continue;
				}
				/** Put the preset run tasks after connecting, since they are only usable once connected */
				preMissionRun();
				// vl.logln("df.dealQueue()");

				if (ir.isRuntime()) {
					/** Handle here the matters related to sending and receiving commands; this could be improved by replacing the long wait for the subsequent data fetch with short polling, which can fill in JobUnit sending */
					if (df.dealQueue()) {
						// Wait for the command to take effect

						sleep(shortWait);

					}
				}

				boolean b = ir.isKeepGet() && (!ir.isAllChannelShutDown());
				if (!b) {
					// Keep waiting when no channel is open or when only commands are sent without requesting data
					sleep(midWait);
					continue;
				}

				/** Method to load data; the complete data-loading process ends on this line, and the result info including measurement and pass/fail can be obtained */
				ChannelsTransportInfo ci = gdr.getDataT();

				afterTryGet(ci);

			}
			vl.logln("once over");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected void afterTryGet(ChannelsTransportInfo ci)
			throws InterruptedException {
	}

	protected void onNotConnecting() {
	}

	/**
	 * Task that can only be called after connecting; find a chance to rename it later
	 */
	protected void preMissionRun() {
	}

	public ChannelsTransportInfo getChannelsTransportInfo() {
		return gdr.getChannelsTransportInfo();
	}

	public AbsGetDataRunner getAbsGetDataRunner() {
		return gdr;
	}

	public Runnable m_dislink = new Runnable() {
		public String toString() {
			return "m_dislink";
		}

		public void run() {
			ir.setKeepGet(false);
			sm.disconnectSource();
		}
	};

	public void askDisconnect() {
		addMission(m_dislink);
	}
}