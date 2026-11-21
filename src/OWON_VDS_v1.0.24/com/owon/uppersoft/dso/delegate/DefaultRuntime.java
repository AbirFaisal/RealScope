package com.owon.uppersoft.dso.delegate;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.vds.core.comm.IRuntime;

public class DefaultRuntime implements IRuntime {
	private boolean exit = false;
	/** Automatically get state */
	public static final int RT_STOP = 0;
	public static final int RT_AUTOGET = 1;
	private int rtStatus = RT_STOP;
	private ControlManager cm;

	public DefaultRuntime(ControlManager cm) {
		this.cm = cm;
	}

	/** exit */
	public void setExit() {
		exit = true;
	}

	@Override
	public boolean isExit() {
		return exit;
	}

	/** Implement the running-state case first */
	public boolean isRuntime() {
		return isKeepGet() && !exit;
	}

	public boolean isKeepGet() {
		return rtStatus == RT_AUTOGET;
	}

	public boolean isRuntimeStop() {
		return rtStatus == RT_STOP
				&& Platform.getControlApps().getDaemon().isAfterStop();
	}

	public void setKeepGet(boolean b) {
		if (b) {
			rtStatus = RT_AUTOGET;
		} else {
			rtStatus = RT_STOP;
		}

		/** Determine that it has really stopped, otherwise do not allow timebase combobox or mouse wheel shortcut operations */
		boolean recentStop = !b;
		this.recentRunThenStop = recentStop;
		cm.changeKeepget(b);
	}

	/** recentRunThenStop */
	private boolean recentRunThenStop = false;

	public boolean isRecentRunThenStop() {
		return recentRunThenStop;
	}

	public void setRecentStop(boolean recentStop) {
		this.recentRunThenStop = recentStop;
	}

	public boolean isAllChannelShutDown() {
		boolean b = cm.getWaveFormInfoControl().getWaveFormFlag() == 0;
		return b;
	}
}