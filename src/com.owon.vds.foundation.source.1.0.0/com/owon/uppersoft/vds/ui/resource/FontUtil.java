package com.owon.uppersoft.vds.ui.resource;

import java.awt.Font;

public class FontUtil {
	private static String fontpath = "i:\\";
	private static java.io.File file = new java.io.File(fontpath
			+ "SIMYOU.TTF");

	public static Font getFont() {
		Font nf = null;
		try {
			java.io.FileInputStream fi = new java.io.FileInputStream(file);
			java.io.BufferedInputStream fb = new java.io.BufferedInputStream(fi);
			nf = Font.createFont(Font.TRUETYPE_FONT, fb);
			nf = nf.deriveFont(Font.BOLD, 14);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return nf;
	}

	public static void main(String args[]) {
		System.out.println(getFont());
	}
}