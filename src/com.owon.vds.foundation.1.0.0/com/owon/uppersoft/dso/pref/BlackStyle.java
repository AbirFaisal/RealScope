package com.owon.uppersoft.dso.pref;

import java.awt.Color;

import com.owon.uppersoft.vds.data.RGB;

public class BlackStyle extends Style {
	public BlackStyle() {
		path = "/com/owon/uppersoft/dso/image/black/";

		CO_INFOBLOCK_HIGHLIGHT = new Color(97, 102, 113); // DetailPane

		CO_TitleBarTop = new Color(148, 150, 155);
		CO_TitleBarBottom = new RGB("343741").getColor();

		CO_DockHomeCellFore = new Color(250, 250, 250);
		CO_DockHomeSeparator = new RGB("5b6090").getColor();
		CO_DockHomeCellBack = new RGB("3d4161").getColor();
		CO_DockHomeBack = new RGB("1a1c21").getColor();

		CO_DockTitle = new Color(61, 65, 97);
		CO_DockContainer = new RGB("272931").getColor();
		CO_DockBack = new RGB("1a1c21").getColor();
	}
}