package com.owon.uppersoft.dso.function.perspective;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.nio.ByteBuffer;
import java.util.ResourceBundle;

import com.owon.uppersoft.dso.function.DisplayControl;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.util.ui.LineUtil;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.aspect.IView;
import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.core.paint.Background;
import com.owon.uppersoft.vds.core.paint.ScreenContext;
import com.owon.uppersoft.vds.util.format.SFormatter;

/**
 * Only three in one, open xy mode, x and y can be set, auto refresh
 */
public class XYView implements IView, Localizable {
	private WaveFormManager wfm;
	private Color cyan = Color.CYAN;

	private Background background;
	private DisplayControl displayControl;

	public XYView(WaveFormManager wfm, DisplayControl displayControl) {
		this.wfm = wfm;
		this.displayControl = displayControl;
		background = new Background();
		background.setXunitlen(5);
	}

	private String xychlLack;

	@Override
	public void localize(ResourceBundle rb) {
		xychlLack = rb.getString("Label.XYModeChannelLack");
	}

	/** Temporary variable area */
	private byte[] array1, array2;
	private int p1, l1, p2, l2;
	private int wc, hc;

	@Override
	public void adjustView(ScreenContext pc, Rectangle bound) {
		background.adjustView(bound, pc.isScreenMode_3());
		wc = bound.x + (bound.width >> 1);
		hc = bound.y + (bound.height >> 1);
	}

	public boolean isOn() {
		return displayControl.isXYModeOn();
	}

	@Override
	public void paintView(Graphics2D g2d, ScreenContext sc, Rectangle r) {
		g2d.scale(DataHouse.xRate, DataHouse.yRate);
		background.paintView(g2d, sc);
		if (!displayControl.isXYModeOn()) {
			return;
		}

		String n = wfm.getClosedChannelName(displayControl.wfx, displayControl.wfy);
		if (n.length() != 0) {
			n = SFormatter.UIformat(xychlLack, n);
			LineUtil.paintPrompt(g2d, wc, hc, cyan, n);
			return;
		}

		WaveForm chx = wfm.getCHX();
		WaveForm chy = wfm.getCHY();

		ByteBuffer buf1 = chx.getADC_Buffer();
		ByteBuffer buf2 = chy.getADC_Buffer();

		if (buf1 == null || buf2 == null)
			return;

		array1 = buf1.array();
		p1 = buf1.position();
		l1 = buf1.limit();

		array2 = buf2.array();
		p2 = buf2.position();
		l2 = buf2.limit();

		if (l1 - p1 != l2 - p2)
			return;

		g2d.setClip(r);
		g2d.setColor(cyan);
		int x, y;
		while (p1 < l1) {
			x = wc + array1[p1];
			y = hc - array2[p2];
			g2d.drawLine(x, y, x, y);
			p1++;
			p2++;
		}
	}
}
