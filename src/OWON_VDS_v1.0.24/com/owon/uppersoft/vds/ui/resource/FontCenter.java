package com.owon.uppersoft.vds.ui.resource;

import java.awt.Font;
import java.util.Locale;

public class FontCenter {
	private static Font labelfont, combofont, titlefont, bigtitlefont;
	static {
		updateFont();
	}

	public static void updateFont() {
		updateFont(Locale.getDefault());
	}

	public static void updateFont(Locale lnew) {
		String zh_cn = Locale.CHINA.getLanguage();
		String fn;
		if (lnew.getLanguage().equals(zh_cn)) {
			fn = "\u9ED1\u4F53";
			labelfont = new Font(fn, 0, 16);
			combofont = new Font(fn, 0, 15);
			titlefont = new Font(fn, 1, 17);
			bigtitlefont = new Font(fn, 1, 20);
		} else {
			fn = "sansserif";
			labelfont = new Font(fn, 1, 15);
			combofont = new Font(fn, 0, 14);
			titlefont = new Font(fn, 1, 16);
			bigtitlefont = new Font(fn, 1, 19);
		}
		// System.out.println(fn);
		// UIDefaults ud = UIManager.getLookAndFeelDefaults();
		// String sk = "ComboBox.font";
		// ud.put(sk, f);
	}

	public static Font getLabelFont() {
		return labelfont;
	}

	public static Font getComboFont() {
		return combofont;
	}

	public static Font getTitleFont() {
		return titlefont;
	}

	public static Font getBigtitlefont() {
		return bigtitlefont;
	}
}