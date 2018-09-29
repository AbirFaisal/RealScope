package com.owon.uppersoft.vds.socket.provider;

import static com.owon.uppersoft.dso.util.PropertiesItem.APPLY_SAMPLING;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.source.comm.TrgStatus;
import com.owon.uppersoft.vds.core.wf.rt.ChannelsTransportInfo;
import com.owon.uppersoft.vds.socket.ScpiPool;

public class AcquireScpiProvider {
	private ControlManager cm;

	public AcquireScpiProvider(ControlManager ctrm) {
		this.cm = ctrm;
	}

	/** :ACQuire */
	public String getACQuireState() {
		ChannelsTransportInfo cti = Platform.getControlApps().getDaemon()
				.getChannelsTransportInfo();
		int trgIdx = cti.triggerStatus;
		int len = TrgStatus.values().length;
		if (trgIdx < 0 || trgIdx > len - 1)
			return ScpiPool.Failed;
		String state = TrgStatus.VALUES[trgIdx].toString();
		if (state.equals(TrgStatus.Ready.toString()))
			state = "SINGle";
		//修复：当它断开连接，只会查询到最后一次设置的triggerStatus，而不是offline
		if(!Platform.getControlManager().sourceManager.isConnected())
			state = "Offline";
		return state;
	}

	// TODO 未实现控制状态
	public String setACQuireState(String arg) {
		// Platform.getMainWindow().getTitlePane().askSwitchRS();
		return ScpiPool.Failed;
	}

	public String getACQuireType() {
		int idx = cm.getSampleControl().getModelIdx();
		String[] spm = cm.getSampleControl().SAMPleMode;
		if (idx < 0 && idx >= spm.length)
			return ScpiPool.Failed;
		return spm[idx];
	}

	public String setACQuireType(String args) {
		int idx = cm.getSampleControl().getSAMPleModeIdx(args);
		if (idx < 0)
			return ScpiPool.Failed;
		cm.getSampleControl().c_setModelIdx(idx);
		cm.pcs.firePropertyChange(APPLY_SAMPLING, null, null);
		return ScpiPool.Success;
	}

	public String getACQuireAVERage() {
		int count = cm.getSampleControl().getAvgTimes();
		return String.valueOf(count);
	}

	public String setACQuireAVERage(String args) {
		int count = -1;
		try {
			count = Integer.parseInt(args);
		} catch (Exception e) {
			return ScpiPool.ErrNum;
		}
		if (count < 1 || count > 128)
			return ScpiPool.ErrNum;
		cm.getSampleControl().c_setAvgTimes(count);
		cm.pcs.firePropertyChange(APPLY_SAMPLING, null, null);
		return ScpiPool.Success;
	}

	public String getACQuireMDEPth() {
		// int mdepth = getDeepDataLen();
		return cm.getDeepMemoryControl().getDeepLabel();
	}

	public String setACQuireMDEPth(String args) {
		String[] deep = cm.getCoreControl().getMachineInfo().DEEP;
		for (int i = 0; i < deep.length; i++) {
			if (args.equalsIgnoreCase(deep[i])) {
				Platform.getMainWindow().getToolPane().getDetailPane()
						.dmselected(i);
				return ScpiPool.Success + " , " + deep[i];
			}
		}
		return ScpiPool.Failed;
	}

}
