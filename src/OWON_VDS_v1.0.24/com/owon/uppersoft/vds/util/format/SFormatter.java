package com.owon.uppersoft.vds.util.format;

import java.util.Locale;

public class SFormatter {

	public SFormatter() {
		// TODO Auto-generated constructor stub
	}

	public static String UIformat(String format, Object... args) {
		return String.format(format, args);
	}

	public static String dataformat(String format, Object... args) {
		Locale l = Locale.ENGLISH;
		return String.format(l, format, args);
	}

	public static String getRestrictSubString(String old, int charCount) {
		String new_str;
		if (old.length() > charCount)
			new_str = old.substring(0, charCount) + "...";
		else
			new_str = old;
		return new_str;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
