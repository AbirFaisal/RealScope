package com.owon.uppersoft.vds.util;

import com.owon.uppersoft.vds.core.aspect.base.Logable;

public class ArrayLogger {
	private static final boolean debug = true;

	public static final void configArray(int[] arr, int start, int len) {
		if (!debug)
			return;
		int end = start + len;
		for (int i = start, j = 0; i < end; i++, j++) {
			if (j % 100 == 0)
				configln();
			if (j % 4 == 0)
				config("," + (arr[i]) + " ");
			else
				config((arr[i]) + " ");
		}
		config("\r\n");
	}

	public static final void configArray(byte[] arr, int start, int len) {
		if (!debug)
			return;
		int end = start + len;
		for (int i = start, j = 0; i < end; i++, j++) {
			if (j % 100 == 0)
				configln();

			if (j % 10 == 0)
				config("," + (arr[i]) + " ");
			else
				config((arr[i]) + " ");
		}
		config("\r\n");
	}

	public static final void outArray2Logable(Logable lg, byte[] arr,
			int start, int len) {
		if (!debug)
			return;
		int end = start + len;
		int v;
		for (int i = start, j = 0; i < end; i++, j++) {
			v = arr[i];// & 0xff;
			if (j % 5 == 0)
				lg.log(", " + v + " ");
			else
				lg.log(v + " ");

			if ((j + 1) % 30 == 0)
				lg.logln("");
		}
		lg.logln("");
	}

	public static final void outArray2LogableHex(Logable lg, byte[] arr,
			int start, int len) {
		if (!debug)
			return;
		int end = start + len;
		int v;
		for (int i = start, j = 0; i < end; i++, j++) {
			v = arr[i] & 0xff;
			if (j % 5 == 0)
				lg.log(", 0x" + Integer.toHexString(v) + " ");
			else
				lg.log("0x" + Integer.toHexString(v) + " ");

			if ((j + 1) % 30 == 0)
				lg.logln("");
		}
		lg.logln("");
	}

	public static final void outArray2Logable(Logable lg, int[] arr, int start,
			int len) {
		if (!debug)
			return;
		int end = start + len;
		int v;
		for (int i = start, j = 0; i < end; i++, j++) {
			v = arr[i];
			if (j % 5 == 0)
				lg.log(", " + v + " ");
			else
				lg.log(v + " ");

			if ((j + 1) % 30 == 0)
				lg.logln("");
		}
		lg.logln("");
	}

	public static final void outArrayAlpha(byte[] arr, int start, int len) {
		if (!debug)
			return;
		int end = start + len;
		byte b;
		String n;
		for (int i = start, j = 0; i < end; i++, j++) {
			if (j % 10 == 0)
				configln();

			if (j % 4 == 0)
				config(",");

			b = arr[i];
			if (PrimaryTypeUtil.isAlpha(b)) {
				n = b + "(" + (char) b + ") ";
			} else {
				n = b + " ";
			}
			config(n);
		}
		configln();
	}

	public static final void dbgArrayAlpha(byte[] arr, int start, int len) {
		if (!debug)
			return;
		int end = start + len;
		byte b;
		String n;
		for (int i = start, j = 0; i < end; i++, j++) {
			if (j % 4 == 0)
				dbg(",");

			b = arr[i];
			if (PrimaryTypeUtil.isAlpha(b)) {
				n = b + "(" + (char) b + ") ";
			} else {
				n = b + " ";
			}
			dbg(n);
		}
		dbgln("");
	}

	public static final void dbgArray(byte[] arr, int start, int len) {
		if (!debug)
			return;
		int end = start + len;
		for (int i = start, j = 0; i < end; i++, j++) {
			if (j % 4 == 0)
				dbg("," + (arr[i]) + " ");
			else
				dbg((arr[i]) + " ");
		}
		dbgln("");
	}

	private static void configln() {
		System.err.println();
	}

	private static void config(String t) {
		System.err.print(t);
	}

	private static void dbg(String string) {
	}

	private static void dbgln(String string) {
	}

	public static final void dbgArray(int[] arr, int start, int len) {
		if (!debug)
			return;
		int end = start + len;
		for (int i = start, j = 0; i < end; i++, j++) {
			dbg((arr[i]) + " ");
		}
		dbgln("");
	}
}