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
	 * 目前的任务队列是在判断连接之前进行，而任务主要是和连接后相关的，也有部分是停止下做的内容
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
				/** 这里处理和传输有关并且必须插入先行的事务，尽量只在最开始的位置固定调用并处理mission */
				doStuff();

				/** 这里判断程序是否退出,是则退出线程 */
				if (ir.isExit())
					break;

				/**
				 * 只在无连接的情况下进入usb检测，则网络连接时不会
				 */
				// vl.logln("sm.isConnected(): " + sm.isConnected());
				if (!sm.isConnected()) {

					// System.out.println("check");
					onNotConnecting();
					continue;
				}
				/** 将预置的运行任务放在连接后，因其可用的前提是已经连接 */
				preMissionRun();
				// vl.logln("df.dealQueue()");

				if (ir.isRuntime()) {
					/** 这里处理和指令收发有关事务，这里可以改善，将之后获取数据的长等待改为短轮询，可以填充JobUnit的发送 */
					if (df.dealQueue()) {
						// 等待指令生效

						sleep(shortWait);

					}
				}

				boolean b = ir.isKeepGet() && (!ir.isAllChannelShutDown());
				if (!b) {
					// 当无通道打开或只发指令不要数据时持续等待
					sleep(midWait);
					continue;
				}

				/** 载入数据的方法，载入数据的完整过程，在这一句结束，可以得到包括测量，通过失败的结果信息 */
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
	 * 在已经连接后才可调用的任务，之后找机会重命名之
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