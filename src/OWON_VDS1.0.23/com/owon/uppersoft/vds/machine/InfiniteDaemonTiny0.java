package com.owon.uppersoft.vds.machine;

import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.machine.aspect.IStopable;
import com.owon.uppersoft.dso.source.comm.AbsInterCommunicator;
import com.owon.uppersoft.dso.source.comm.InfiniteDaemon;
import com.owon.uppersoft.dso.source.comm.USBDaemonHelper;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.vds.core.comm.JobQueueDispatcher;
import com.owon.uppersoft.vds.source.comm.data.GetDataRunner2;

public class InfiniteDaemonTiny0 extends InfiniteDaemon {
	public InfiniteDaemonTiny0(DataHouse dh, MainWindow mw,
			JobQueueDispatcher df, AbsInterCommunicator ic, IStopable ca,
			GetDataRunner2 gdr) {
		super(dh, mw, df, ic, ca, gdr);
		udh = new USBDaemonHelper(this, cm, mw, cs.getContextMenuManager());
	}

	public void askDisconnect() {
		udh.setNoDisturb(true);
		super.askDisconnect();
	}

	public void onClickStatus(int status_icon_xloc, int mouseBtn) {
		udh.onClickStatus(status_icon_xloc, mouseBtn);
	}

	protected void onNotConnecting() {
		udh.onNotConnecting();
	}

	private USBDaemonHelper udh;

}