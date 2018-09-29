package com.owon.uppersoft.dso.pref;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Stroke;

import com.owon.uppersoft.dso.util.DBG;
import com.owon.uppersoft.vds.data.RGB;

/**
 * Define，预定义参数
 * 
 */
public class Define {
	/** def被广泛使用，必须先被初始化，初始化只能通过prepare方法 */
	public static Define def = new Define();

	public static void prepare(int i) {
		def.initStyle(i);
	}

	private void initStyle(int i) {
		STYLE_TYPE = i;
		style = new Style();
		switch (i) {
		default:
			style = new Style();
			DBG.errprintln("!!");
		case 0:
			style = new BlackStyle();
			break;
		case 1:
			style = new BlueStyle();
			break;
		}
	}

	private Define() {
	}

	public int STYLE_TYPE;

	public Style style;

//	/**
//	 * 首页，标题栏字体
//	 */
//	public Font titlefont = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
//	/**
//	 * 首页，标题栏字体
//	 */
//	public Font bigtitlefont = new Font(Font.SANS_SERIF, Font.PLAIN, 18);
//	/**
//	 * ComboBox字体
//	 */
//	public Font combofont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
//	/**
//	 * label字体
//	 */
//	public Font labelfont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
	/**
	 * 数值显示字体
	 */
	public Font alphafont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);

	/**
	 * 公共状态显示字体
	 */
	public Font commonStatusfont = new Font(Font.SANS_SERIF, Font.PLAIN, 17);

	/**
	 * 状态数值显示字体
	 */
	public Font statusCounterfont = new Font(Font.SANS_SERIF, Font.BOLD, 11);

	/**
	 * 快照显示字体
	 */
	public Font snapshotfont = new Font(Font.SANS_SERIF, Font.BOLD, 18);

	public Font snapshotfont_en = new Font(Font.SANS_SERIF, Font.BOLD, 16);

	public Font tipsTabbed = new Font(Font.SANS_SERIF, Font.BOLD, 13);

	// Helvetica
	// public static final AlphaComposite composite20 = getAlphaComposite(0.2f);
	// public static final AlphaComposite composite50 = getAlphaComposite(0.5f);
	// public static final AlphaComposite composite10 = getAlphaComposite(0.1f);
	// public static final AlphaComposite composite30 = getAlphaComposite(0.3f);
	// public static final AlphaComposite composite40 = getAlphaComposite(0.4f);

	
	public static final int ICON_BAR_WIDTH = 41;
	public static final int Dock_Heigth = 500;
	public static final int Dock_Width = 280;

	public int FRM_Width = 1024, FRM_Height = 778;
	public int Frame_Border_Width = 2;
	public Dimension PreferredSize = new Dimension(FRM_Width, FRM_Height);

	/** 250~0~-250 */
	public int AREA_HEIGHT = 500 + 1;
	public int AREA_W_BOUNDREST = 20;
	public int AREA_H_BOUNDREST = 20;

	public int TITLE_HEIGHT = 37;
	public int CHART_HEIGHT = AREA_HEIGHT + AREA_H_BOUNDREST;// 500+1+20
	public int TITLE_CHART_HEIGHT = TITLE_HEIGHT + CHART_HEIGHT;//37+521
	public int VIEW_HEIGHT = 15;
	public int DockFrameWidth = 250;

	// public int CHECKFRAME_STATE = 0;
	// public int UPDATEFRAME_STATE=0;

	public int DIVIDER_LEN = 2;
	public int DIVIDER_DEFAULT_LOC = 400;

	public int WND_SHAPE_ARC_2 = 27;

	public int WND_SHAPE_ARC = 15;
	public int DLG_SHAPE_ARC = 20;
	public int BORDER_WIDTH = 2;

	public Font controlfont = new Font(Font.SANS_SERIF, Font.BOLD, 14);

	public Font LightTextFont = new Font("serial", Font.BOLD, 14);

	public Font OnOffFont = new Font("serial", Font.BOLD, 14);
	public Font insideFont = new Font("serial", Font.PLAIN, 10);
	// public Color CO_TitleBarBottom =new RGB("343741").getColor();
	// public Color CO_TitleBarTop = new Color(148, 150, 155);

	public Color CO_DockBorder = new Color(173, 181, 212);
	// public Color CO_DockBack = new RGB("1a1c21").getColor();
	// pages background
	// public Color CO_DockTitle = new Color(61, 65, 97);
	// public Color CO_DockContainer = new RGB("272931").getColor(); // ItemPane
	public Color CO_DockFore = Color.WHITE;
	public Color CO_DockHomeCellTrag = new Color(218, 221, 225);

	// public Color CO_DockHomeCellBack = new RGB("3d4161").getColor();
	// public Color CO_DockHomeSeparator = new RGB("5b6090").getColor();
	// public Color CO_DockHomeCellFore = new Color(0, 0, 0);
	// public Color CO_DockHomeBack = new RGB("1a1c21").getColor();

	public Color CO_TITLE = Color.GRAY;
	public Color CO_COMBO_SEL = new RGB("39698A").getColor();

	public Color CO_BORDER = new Color(200, 200, 200);
	public Color CO_GRID = new Color(100, 100, 100);

	public Color CO_MARK = new Color(150, 160, 170);
	public Color CO_MARK_SELECT = new Color(200, 190, 180);

	public Color CO_XAXIS_CENTER = CO_BORDER;

	public Color CO_FADE_BG = new Color(60, 60, 60);

	public Color CO_INFOBLOCK_BORDER = new RGB("E1E1E1").getColor();

	public Color CO_FRM_Border = new Color(135, 137, 144);

	// public Color CO_INFOBLOCK_HIGHLIGHT = new Color(97, 102, 113);
	// "8C929D"+40%transparent

	public Stroke Stroke1 = new BasicStroke(1);
	public Stroke Stroke2 = new BasicStroke(2);
	public Stroke Stroke3 = new BasicStroke(3);

	/* 备用 */
	public float[] GRADIENT_FRACTIONS_BG = { 0.1f, 0.9f };
	public Color[] COGP_GRADIENT_BG = { Color.LIGHT_GRAY, Color.WHITE };

	public float[] GRADIENT_FRACTIONS_SliderBar = { 0.0f, 0.5f, 1.0f };
	public Color[] COGP_GRADIENT_SliderBar = { Color.LIGHT_GRAY,
			CO_XAXIS_CENTER, Color.WHITE };

//	public static void updateFont() {
//		String zh_cn = Locale.CHINA.getLanguage();
//		Locale lnew = Locale.getDefault();
//		// FontUtil.getFont();
//		String fn;
//		if (lnew.getLanguage().equals(zh_cn)) {
//			fn = "\u9ED1\u4F53";
//			def.labelfont = new Font(fn, 0, 16);
//			def.combofont = new Font(fn, 0, 15);
//			def.titlefont = new Font(fn, 1, 17);
//			def.bigtitlefont = new Font(fn, 1, 20);
//		} else {
//			fn = "sansserif";
//			def.labelfont = new Font(fn, 1, 15);
//			def.combofont = new Font(fn, 0, 14);
//			def.titlefont = new Font(fn, 1, 16);
//			def.bigtitlefont = new Font(fn, 1, 19);
//		}
//		// System.out.println(fn);
//		// UIDefaults ud = UIManager.getLookAndFeelDefaults();
//		// String sk = "ComboBox.font";
//		// ud.put(sk, f);
//	}
	
	
}