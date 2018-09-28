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
			/**KNOW 会重置reset設置的值，導致clc循环中断，故改到finish()里设置 */
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