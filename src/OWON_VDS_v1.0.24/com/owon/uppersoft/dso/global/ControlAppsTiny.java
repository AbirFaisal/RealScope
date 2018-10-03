package com.owon.uppersoft.dso.global;

import com.owon.uppersoft.dso.source.comm.AbsInterCommunicator;
import com.owon.uppersoft.dso.source.comm.InfiniteDaemon;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.vds.core.comm.ICommunicateManager;
import com.owon.uppersoft.vds.core.comm.JobQueueDispatcher;
import com.owon.uppersoft.vds.machine.InfiniteDaemonTiny0;
import com.owon.uppersoft.vds.machine.PrincipleTiny;
import com.owon.uppersoft.vds.machine.TinyMachine;
import com.owon.uppersoft.vds.socket.server.ServerControl;
import com.owon.uppersoft.vds.source.comm.InterCommTiny;
import com.owon.uppersoft.vds.source.comm.SourceManagerTiny;
import com.owon.uppersoft.vds.source.comm.data.GetDataRunner2;
import com.owon.uppersoft.vds.source.front.AbsPreHandler;

public class ControlAppsTiny extends ControlApps {
	private InterCommTiny ict;

	public ControlAppsTiny(DataHouse dh, MainWindow mw) {
		super(dh, mw);
	}

	@Override
	public InfiniteDaemon createInfiniteDaemon(final DataHouse dh,
			MainWindow mw, JobQueueDispatcher df, AbsInterCommunicator interComm) {
		TinyMachine tm = ((TinyMachine) cm.getMachine());
		GetDataRunner2 gdr = createGetDataRunner(tm, dh, cm.sourceManager);

		InfiniteDaemonTiny0 dmn = new InfiniteDaemonTiny0(dh, mw, df,
				interComm, this, gdr){		
			@Override
			public void run() {
				cm.scpiServer.startServer();
				
				cm.reloadManager.reload();
				super.run();
			}
	};

		return dmn;
	}

	private GetDataRunner2 createGetDataRunner(final TinyMachine tm,
			DataHouse dh, final ICommunicateManager ism) {
		PrincipleTiny pt = (PrincipleTiny) cm.getPrinciple();
		AbsPreHandler ph = tm.createPreHandler(cm, pt.getLowControlManger(cm));
		GetDataRunner2 gdr = new GetDataRunner2(dh, ism, ph,
				new GetDataRunner2.DataReceiveHandler() {
					@Override
					public void onSweepOutAsOnce() {
					}

					@Override
					public void onReceive() {
						ict.firePropertyChange(InterCommTiny.NEW_DATA_RECEIVE,
								null, null);
					}
				}, pt.getMachineType());
		return gdr;
	}

	@Override
	public AbsInterCommunicator createInterCommunicator(DataHouse dh,
			MainWindow mw) {
		PrincipleTiny pt = (PrincipleTiny) cm.getPrinciple();
		return ict = new InterCommTiny(dh, mw, this,
				(SourceManagerTiny) cm.sourceManager, pt);
	}

	@Override
	protected void reload() {
		cm.reloadManager.reload();
	}
}