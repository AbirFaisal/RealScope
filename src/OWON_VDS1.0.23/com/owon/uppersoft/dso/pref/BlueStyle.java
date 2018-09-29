package com.owon.uppersoft.dso.pref;

import java.awt.Color;

import com.owon.uppersoft.vds.data.RGB;

public class BlueStyle extends Style {
	public BlueStyle() {
		path = "/com/owon/uppersoft/dso/image/blue/";

		CO_INFOBLOCK_HIGHLIGHT = new Color(55, 55, 200);

		CO_TitleBarTop = new Color(176, 188, 204);
		CO_TitleBarBottom = new Color(109, 132, 162);

		CO_DockHomeCellFore = new Color(20, 20, 70);
		CO_DockHomeSeparator = new Color(159, 169, 252);
		CO_DockHomeCellBack = new RGB("8f97a2").getColor();
		CO_DockHomeBack = new Color(69, 78, 92);

		CO_DockTitle = new Color(129, 137, 150);
		CO_DockContainer = new RGB("323943").getColor();
		CO_DockBack = new RGB("46505a").getColor();
	}
}