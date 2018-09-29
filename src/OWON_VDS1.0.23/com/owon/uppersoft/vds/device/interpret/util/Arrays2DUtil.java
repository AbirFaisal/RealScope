package com.owon.uppersoft.vds.device.interpret.util;

import java.nio.ByteBuffer;

import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.util.PropertiesItem;

public class Arrays2DUtil {

	/**
	 * 只传递非负数，否则用0
	 * 
	 * @param dest
	 * @param bb
	 */
	public static void loadFromBytes(int[][] dest, ByteBuffer bb,
			short defaultValue) {
		int x = dest.length, y = dest[0].length;
		int v;
		for (int i = 0; i < x; i++) {

			log(i + ": ");

			for (int j = 0; j < y; j++) {
				v = bb.getShort();
				if (v <= 0)
					v = defaultValue;
				dest[i][j] = v;

				log("[" + j + ']' + dest[i][j] + ", ");
			}

			logln("");
		}

		logln("");
	}

	private static void log(String txt) {
		System.out.print(txt);

		Platform.getControlManager().pcs.firePropertyChange(
				PropertiesItem.APPEND_TXT, null, txt);
	}

	private static void logln(String txt) {
		System.out.println(txt);

		Platform.getControlManager().pcs.firePropertyChange(
				PropertiesItem.APPEND_TXTLINE, null, txt);
	}

	/**
	 * 只传递非负数，否则用0
	 * 
	 * @param bb
	 * @param src
	 */
	public static void fillBytes(ByteBuffer bb, int[][] src) {
		logln("fillBytes " + bb.position());
		int x = src.length, y = src[0].length;
		int v;
		for (int i = 0; i < x; i++) {

			log(i + ": ");

			for (int j = 0; j < y; j++) {
				v = src[i][j];
				if (v < 0)
					v = 0;
				bb.putShort((short) v);

				log("[" + j + ']' + src[i][j] + ", ");
			}

			logln("");
		}

		logln("");
	}

	public static void copyArrays(int[][] src, int[][] dest) {
		int x = src.length, y = src[0].length;
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				dest[i][j] = src[i][j];
			}
		}
	}
}
