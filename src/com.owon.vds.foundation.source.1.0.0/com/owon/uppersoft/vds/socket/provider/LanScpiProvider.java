package com.owon.uppersoft.vds.socket.provider;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.vds.socket.ScpiPool;

public class LanScpiProvider {

	private ControlManager cm;

	public LanScpiProvider(ControlManager ctrm) {
		this.cm = ctrm;
	}

	/** LAN */
	public String getIP() {
		return cm.getSysControl().getIP();
	}

	public String setIP(String cmd) {
		boolean rsp = cm.getSysControl().setIP(cmd);
		return rsp ? ScpiPool.Success : ScpiPool.Failed;
	}

	public String getGATeway() {
		return cm.getSysControl().getGATeway();
	}

	public String setGATeway(String cmd) {
		boolean rsp = cm.getSysControl().setGATeway(cmd);
		return rsp ? ScpiPool.Success : ScpiPool.Failed;
	}

	public String getSMASk() {
		return cm.getSysControl().getSMASk();
	}

	public String setSMASk(String cmd) {
		boolean rsp = cm.getSysControl().setSMASk(cmd);
		return rsp ? ScpiPool.Success : ScpiPool.Failed;
	}

	public String getPort() {
		return Integer.toString(cm.getSysControl().port);
	}

	public String setPort(String cmd) {
		int pt = -1;
		try {
			pt = Integer.parseInt(cmd);
		} catch (Exception e) {
			return ScpiPool.Failed;
		}
		if (pt < 1025 || pt > 65534)
			return ScpiPool.ErrNum + "," + "OUT OF 1025~65534";
		cm.getSysControl().port = pt;
		cm.getSysControl().c_network();
		return ScpiPool.Success;
	}

	public String rebootLan() {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Platform.getControlApps().interComm.rebootMachine();
		return ScpiPool.Success;
	}

}
