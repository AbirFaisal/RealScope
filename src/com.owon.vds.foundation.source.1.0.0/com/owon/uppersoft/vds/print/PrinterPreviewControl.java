package com.owon.uppersoft.vds.print;

import java.util.ResourceBundle;

import com.owon.uppersoft.vds.core.aspect.base.ResourceBundleProvider;
import com.owon.uppersoft.vds.util.Pref;

public class PrinterPreviewControl {
	public static ResourceBundleProvider rbp;

	public static final void setupResourceBundleProvider(
			ResourceBundleProvider rBP) {
		rbp = rBP;
	}

	public static final ResourceBundle bundle() {
		return rbp.bundle();
	}

	/** i18nkey */
	/** 配置文件key */
	public static final String PRINT_IS_PAINT_W_F_B_G = "Print.isPaintWFBG";
	public static final String PRINT_IS_VERTICAL = "Print.isVertical";
	public static final String PRINT_DOWN_EDGE_LENGTH = "Print.downEdgeLength";
	public static final String PRINT_UP_EDGE_LENGTH = "Print.upEdgeLength";
	public static final String PRINT_RIGHT_EDGE_LENGTH = "Print.rightEdgeLength";
	public static final String PRINT_LEFT_EDGE_LENGTH = "Print.leftEdgeLength";

	public boolean isVertical, isPaintWFBG;
	protected int leftEdgeLength = 20;
	protected int rightEdgeLength = 20;
	protected int upEdgeLength = 20;
	protected int downEdgeLength = 20;

	public PrinterPreviewControl(Pref p) {
		load(p);
	}

	public void persist(Pref p) {
		p.persistInt(PRINT_LEFT_EDGE_LENGTH, leftEdgeLength);
		p.persistInt(PRINT_RIGHT_EDGE_LENGTH, rightEdgeLength);
		p.persistInt(PRINT_UP_EDGE_LENGTH, upEdgeLength);
		p.persistInt(PRINT_DOWN_EDGE_LENGTH, downEdgeLength);
		p.persistBoolean(PRINT_IS_VERTICAL, isVertical);
		p.persistBoolean(PRINT_IS_PAINT_W_F_B_G, isPaintWFBG);
	}

	public void load(Pref p) {
		leftEdgeLength = p.loadInt(PRINT_LEFT_EDGE_LENGTH);
		rightEdgeLength = p.loadInt(PRINT_RIGHT_EDGE_LENGTH);
		upEdgeLength = p.loadInt(PRINT_UP_EDGE_LENGTH);
		downEdgeLength = p.loadInt(PRINT_DOWN_EDGE_LENGTH);
		isVertical = p.loadBoolean(PRINT_IS_VERTICAL);
		isPaintWFBG = p.loadBoolean(PRINT_IS_PAINT_W_F_B_G);
	}

}
