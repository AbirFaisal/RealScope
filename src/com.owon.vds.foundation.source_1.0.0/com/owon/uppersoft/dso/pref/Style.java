package com.owon.uppersoft.dso.pref;

import java.awt.Color;

public class Style {
	// ButtonPane
	public static String _3in1name;
	public static String _3in1_pname;
	public static String Toolname;
	public static String Tool_pname;
	// MainWindow
	public static String MWBGimgname;

	// TriggerInfoPane
	public static String Trgvoltname;
	// DetailPane
	public static String DetailPname;
	// TitlePane
	public static String Minimizename;
	public static String Minimize_pname;
	public static String Closename;
	public static String Close_pname;
	// InfoBlock
	public static String selname;
	public static String unsname;

	static {
		// ButtonPane
		_3in1name = "_3in1.png";
		_3in1_pname = "_3in1_p.png";
		Toolname = "tool.png";
		Tool_pname = "tool_p.png";
		// MainWindow
		// 注释掉的那张图片，最后可以删掉
		MWBGimgname = "titlebar.png";// "background.png";
		// TriggerInfoPane
		//Trgvoltname = "trgvolt.png";
		// DetailPane
		DetailPname = "horpos.png";
		// TitlePane
		Minimizename = "minimize.png";
		Minimize_pname = "minimize_p.png";
		Closename = "close.png";
		Close_pname = "close_p.png";
		// InfoBlock
		selname = "sel";
		unsname = "uns";
	}

	public String path;

	// variable
	public Color CO_INFOBLOCK_HIGHLIGHT;
	// DockDialog
	// HomePane
	public Color CO_TitleBarBottom;
	public Color CO_TitleBarTop;
	public Color CO_DockHomeBack;
	public Color CO_DockHomeCellBack;
	public Color CO_DockHomeSeparator;
	public Color CO_DockHomeCellFore;
	// GroupPane 、 ImagePaintUtil 、FunctionPanel
	public Color CO_DockTitle;
	public Color CO_DockBack;
	public Color CO_DockContainer;

}
