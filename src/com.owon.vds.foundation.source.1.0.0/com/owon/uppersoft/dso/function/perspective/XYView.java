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
 * 只有三合一，即打开xy模式，x和y可设置，自动刷新
 */
public class XYView implements IView, Localizable {
	private WaveFormManager wfm;
	private Color co = Color.CYAN;

	private Background bg;
	private DisplayControl dc;

	public XYView(WaveFormManager wfm, DisplayControl dc) {
		this.wfm = wfm;
		this.dc = dc;
		bg = new Background();
		bg.setXunitlen(5);
	}

	private String xychlLack;

	@Override
	public void localize(ResourceBundle rb) {
		xychlLack = rb.getString("Label.XYModeChannelLack");
	}

	/** 临时变量区 */
	private byte[] array1, array2;
	private int p1, l1, p2, l2;
	private int wc, hc;

	@Override
	public void adjustView(ScreenContext pc, Rectangle bound) {
		bg.adjustView(bound, pc.isScreenMode_3());
		wc = bound.x + (bound.width >> 1);
		hc = bound.y + (bound.height >> 1);
	}

	public boolean isOn() {
		return dc.isXYModeOn();
	}

	@Override
	public void paintView(Graphics2D g2d, ScreenContext sc, Rectangle r) {
		g2d.scale(DataHouse.xRate, DataHouse.yRate);
		bg.paintView(g2d, sc);
		if (!dc.isXYModeOn()) {
			return;
		}

		String n = wfm.getClosedChannelName(dc.wfx, dc.wfy);
		if (n.length() != 0) {
			n = SFormatter.UIformat(xychlLack, n);
			LineUtil.paintPrompt(g2d, wc, hc, co, n);
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
		g2d.setColor(co);
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
