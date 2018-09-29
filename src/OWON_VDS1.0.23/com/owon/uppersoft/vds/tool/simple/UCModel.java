package com.owon.uppersoft.vds.tool.simple;

import java.awt.Color;
import java.awt.Graphics2D;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.List;

import com.owon.uppersoft.dso.util.DBG;
import com.owon.uppersoft.vds.core.rt.WFDrawRTUtil;
import com.owon.uppersoft.vds.ui.paint.LineDrawTool;

public class UCModel {
	public class ChlModel {
		public int number = -1;
		public static final int _4k = 4000;
		public IntBuffer pixbuf = IntBuffer.allocate(_4k);

		public void resetNtailorBuf(ByteBuffer bb, int yb) {
			int p = bb.position();
			int l = p + _4k;
			int i = 0;
			DBG.dbgln(p + "," + l);
			pixbuf.position(i);
			int[] pixarr = pixbuf.array();
			byte[] arr = bb.array();
			while (p < l) {
				byte v = arr[p];
				if (v > adcmax)
					v = adcmax;
				else if (v < adcmin)
					v = adcmin;
				pixarr[i] = yb - v;
				// System.out.println(p + ",pixa:" + pixa[i]);
				p++;
				i++;
			}
			pixbuf.limit(i);

			bb.position(p);
		}

		public int trg;

		public void setNdrawTrglevel(int v) {
			trg = v;
			// if (trglevel <= 127) {
			// } else
			if (trg > 127) {
				trg = trg - 256;// -(256-trglevel)
			}
			// System.out.println(trglevel);
			trg = UsbComControl.hcenter - trg;
		}

		public Color co = null;

		public void paint(Graphics2D g2d) {
			int xb = 0;
			boolean linkline = false;
			int drawMode = WFDrawRTUtil.DrawMode4in1;

			drawTrgTriangle(g2d, co, trg);
			WFDrawRTUtil.paintDrawMode(g2d, pixbuf, drawMode, xb, linkline);
		}

		private void drawTrgTriangle(Graphics2D g2d, Color c, int trglevel) {
			g2d.setColor(c);
			g2d.drawLine(0, trglevel, 10, trglevel);
			g2d.drawLine(0, trglevel, 4, trglevel - 4);
			g2d.drawLine(0, trglevel, 4, trglevel + 4);
			String s = (UsbComControl.hcenter - trglevel) + "";
			g2d.drawString(s + "", 15, trglevel + 5);
		}

		public boolean on = false;
	}

	private List<ChlModel> chls = new LinkedList<ChlModel>();

	private static final int adcmin = -125;

	private static final int adcmax = 125;

	public static final int _1k = 1000;

	public UCModel() {
		int i = 0;
		chls.add(new ChlModel());
		chls.add(new ChlModel());
		for (ChlModel chl : chls) {
			chl.number = i;

			if (i == 0) {
				chl.on = true;
				chl.co = Color.RED;
			} else {
				chl.on = true;
				chl.co = Color.YELLOW;
			}
			i++;
		}
	}

	public void resetNtailorBuf(ByteBuffer bb, int yb) {
		for (ChlModel chl : chls) {
			if (!chl.on)
				continue;

			bb.position(bb.position() + _1k);
			chl.resetNtailorBuf(bb, yb);
		}
	}

	protected void viewchartClear() {
		for (ChlModel chl : chls) {

			chl.pixbuf.clear();
		}
	}

	public void paint(Graphics2D g2d) {
		drawGrids(g2d);

		for (ChlModel chl : chls) {
			if (!chl.on)
				continue;
			chl.paint(g2d);
		}
	}

	public ChlModel getChl(int v) {
		for (ChlModel chl : chls) {
			if (chl.number == v)
				return chl;
		}
		return null;
	}

	private void drawGrids(Graphics2D g2d) {
		g2d.setColor(Color.white);
		// g2d.drawLine(0, UsbComControl.hcenter, 20, UsbComControl.hcenter);
		LineDrawTool.drawSashLine(g2d, false, 500, 0, 1000, UsbComControl.hcenter, 9);
		LineDrawTool.drawSashLine(g2d, true, UsbComControl.hcenter, 0, 250, 1500 / 4, 9);
	}

	public void setNdrawTrglevel(String add, String bytes, String value) {
		boolean drawtrg0 = add.equalsIgnoreCase("2E") && bytes.equals("1");
		boolean drawtrg1 = add.equalsIgnoreCase("30") && bytes.equals("1");

		int trg = Integer.parseInt(value, 16);
		if (drawtrg0)
			getChl(0).setNdrawTrglevel(trg);
		else if (drawtrg1)
			getChl(1).setNdrawTrglevel(trg);
	}

}