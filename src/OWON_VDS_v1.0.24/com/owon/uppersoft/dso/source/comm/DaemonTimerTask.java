package com.owon.uppersoft.dso.source.comm;

public class DaemonTimerTask {
	private long holdt, startt;
	private Runnable holdjob;

	public DaemonTimerTask() {
		startt = holdt = -1;
	}

	public void oncheck() {
		if (holdt < 0 || startt < 0)
			return;

		long c = System.currentTimeMillis();
		if (c - startt > holdt) {
			holdjob.run();
			/**KNOW Would reset the value set by reset, causing the clc loop to break, so it is set in finish() instead */
			// startt = holdt = -1;
		}
	}

	public void rewind() {
		startt = holdt = -1;
	}

	public void reset(Runnable r, int ms) {
		startt = System.currentTimeMillis();
		holdjob = r;
		holdt = ms;
	}
}