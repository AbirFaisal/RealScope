package com.owon.uppersoft.vds.socket.provider;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.vds.core.aspect.control.ITimeControl;
import com.owon.uppersoft.vds.core.zoom.AssitControl;
import com.owon.uppersoft.vds.socket.NB;
import com.owon.uppersoft.vds.socket.ScpiPool;

public class TimebaseScpiProvider {
	private ControlManager cm;

	public TimebaseScpiProvider(ControlManager ctrm) {
		this.cm = ctrm;
	}

	/** :TIMebase命令子系统 */
	public String getTIMebaseSCALe() {
		return cm.getCoreControl().getTimeControl().getTimebaseLabel();
	}

	public String setTIMebaseSCALe(String args) {
		boolean operatable = Platform.getControlApps().interComm
				.isTimeOperatableNTryGetDM();
		if (!operatable)
			return ScpiPool.Disable;
		if (cm.getZoomAssctr().isonZoom() && !AssitControl.FastMWstwich)
			return ScpiPool.Disable;
		String[] tb = cm.getCoreControl().getMachineInfo().TIMEBASE;
		for (int i = 0; i < tb.length; i++) {
			if (args.equalsIgnoreCase(tb[i])) {
				cm.getTimeControl().c_setTimebaseIdx(i, true);
				return ScpiPool.Success + " , " + tb[i];
			}
		}

		return ScpiPool.Failed;
	}

	public String getTIMebaseHOFFset() {
		int v = cm.getCoreControl().getTimeControl()
				.getHorizontalTriggerPosition();
		return String.valueOf(v);
	}

	public String setTIMebaseHOFFset(String args) {
		if (!NB.isNum(args))
			return ScpiPool.ErrNum;
		int v = (int) Double.parseDouble(args);

		if (!cm.shouldAdjustHorTrgPos())
			return "NOT ENABLE TO SET,TRY TO ADJUST TIMEBASE";
		cm.getTimeControl().c_setHorizontalTriggerPosition(v);
		if (!cm.getZoomAssctr().isonAssistSet())
			cm.pcs.firePropertyChange(ITimeControl.onHTPChanged, null, null);
		cm.mcctr.computeXValues();
		// Zoom下更新zhtp
		cm.getZoomAssctr().updateZoomHtp();
		if (v >= Integer.MAX_VALUE || v <= Integer.MIN_VALUE)
			return ScpiPool.Success + ", " + v;
		return ScpiPool.Success;
	}

}
