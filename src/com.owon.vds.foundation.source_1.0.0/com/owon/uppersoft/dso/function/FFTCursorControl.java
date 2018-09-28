package com.owon.uppersoft.dso.function;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.vds.util.Pref;

public class FFTCursorControl {
	public int x1 = 100, x2 = 200, y1 = 80, y2 = 160;
	private boolean onFrebaseMark, onVoltbaseMark;
	public boolean onFFTx1 = false, onFFTx2 = false, onFFTy1 = false,
			onFFTy2 = false;
	private String xvalues[] = new String[3], yvalues[] = new String[3];
	private ControlManager cm;
	private MarkCursorControl mcctr;
	private Rectangle r;

	public FFTCursorControl(ControlManager cm, Pref p) {
		this.cm = cm;
		this.mcctr = cm.mcctr;
		load(p);
	}

	public boolean getOnFrebaseMark() {
		return onFrebaseMark;
	}

	public boolean getOnVoltbaseMark() {
		return onVoltbaseMark;
	}

	public void setOnFrebaseMark(boolean on) {
		onFrebaseMark = on;
	}

	public void setOnVoltbaseMark(boolean on) {
		onVoltbaseMark = on;
	}

	public void drawFFTCursor(Graphics2D g2d, Rectangle r) {
		this.r = r;
		Color tmp = g2d.getColor();
		g2d.setColor(Color.pink);
		int w = r.width, y = r.height, rh = 16;
		int wx1 = r.x + 9, wx2 = r.x + 90, wx3 = r.x + 180;

		boolean p = onFrebaseMark || onVoltbaseMark;

		if (onVoltbaseMark) {
			String[] Yvalues = getYvalues();
			// 画Y标尺线
			y1 = limitYEdge(y1, r);//
			y2 = limitYEdge(y2, r);
			g2d.drawLine(r.x, y1, r.x + w, y1);
			g2d.drawLine(r.x, y2, r.x + w, y2);
			// Y值显示
			g2d.drawString("Y1:" + Yvalues[0], wx1, y);
			g2d.drawString("Y2:" + Yvalues[1], wx2, y);
			mcctr.drawTriangle(g2d, wx3 - 10, y);
			g2d.drawString("Y:" + Yvalues[2], wx3, y);
			y -= rh;
		}// else
		if (onFrebaseMark) {
			String[] Xvalues = getXvalues();
			// 画X标尺线
			x1 = limitXEdge(x1, r);//
			x2 = limitXEdge(x2, r);
			g2d.drawLine(x1, r.y, x1, r.y + r.height);
			g2d.drawLine(x2, r.y, x2, r.y + r.height);
			// X值显示
			g2d.drawString("X1:" + Xvalues[0], wx1, y);
			g2d.drawString("X2:" + Xvalues[1], wx2, y);
			mcctr.drawTriangle(g2d, wx3 - 10, y);
			g2d.drawString("X:" + Xvalues[2], wx3, y);
			y -= rh;
		}
		// 矩形框
		if (p) {
			int ww = 254;
			g2d.drawRoundRect(r.x + 2, y, ww, r.y + r.height - y - 2, 10, 8);
		}

		g2d.setColor(tmp);
	}

	public void isOnCursorChecking(MouseEvent e, Component v) {
		int x = e.getX();
		int y = e.getY();
		x /= DataHouse.xRate;
		y /= DataHouse.yRate;
		int cursor = Cursor.HAND_CURSOR;
		if (onFrebaseMark) {
			x1 = limitXEdge(x1, r);//
			x2 = limitXEdge(x2, r);
			if (x > x1 - 5 && x < x1 + 5) {
				onFFTx1 = true;
				cursor = Cursor.E_RESIZE_CURSOR;
			} else if (x > x2 - 5 && x < x2 + 5) {
				onFFTx2 = true;
				cursor = Cursor.E_RESIZE_CURSOR;
			} else {
				onFFTx1 = false;
				onFFTx2 = false;
			}
		}
		// else
		if (onVoltbaseMark) {
			y1 = limitYEdge(y1, r);//
			y2 = limitYEdge(y2, r);
			if (y > y1 - 5 && y < y1 + 5) {
				onFFTy1 = true;
				cursor = Cursor.S_RESIZE_CURSOR;
			} else if (y > y2 - 5 && y < y2 + 5) {
				onFFTy2 = true;
				cursor = Cursor.S_RESIZE_CURSOR;
			} else {
				onFFTy1 = false;
				onFFTy2 = false;
			}
		}
		v.setCursor(Cursor.getPredefinedCursor(cursor));
	}

	public boolean dragFFTCursor(MouseEvent e) {
		int x = e.getX(), y = e.getY();
		int del;

		x /= DataHouse.xRate;
		y /= DataHouse.yRate;

		if (onFFTx1) {
			x1 = limitXEdge(x, r);
			return true;
		} else if (onFFTx2) {
			x2 = limitXEdge(x, r);
			return true;
		} else if (onFFTy1) {
			y1 = limitYEdge(y, r);
			return true;
		} else if (onFFTy2) {
			y2 = limitYEdge(y, r);
			return true;
		} else {
			return false;
		}
	}

	private String[] getXvalues() {
		FFTView fftv = Platform.getDataHouse().getWaveFormManager()
				.getFFTView();
		int mx = fftv.getMarkX0Location();
		x1 = limitXEdge(x1, r);//
		x2 = limitXEdge(x2, r);
		xvalues[0] = fftv.getXLabel((x1 - mx));
		xvalues[1] = fftv.getXLabel((x2 - mx));
		xvalues[2] = fftv.getXLabel((x2 - x1));
		return xvalues;
	}

	private String[] getYvalues() {
		FFTView fftv = Platform.getDataHouse().getWaveFormManager()
				.getFFTView();
		int my = fftv.getMarkY0Location();
		y1 = limitYEdge(y1, r);
		y2 = limitYEdge(y2, r);
		switch (cm.getFFTControl().fftvaluetype) {
		case 0:
			yvalues[0] = fftv.getVrmsLabel(my - y1);// -r.y-(r.height>>1)
			yvalues[1] = fftv.getVrmsLabel(my - y2);// -r.y-(r.height>>1)
			yvalues[2] = fftv.getVrmsLabel(y1 - y2);
			break;
		case 1:
			yvalues[0] = fftv.getdBLabel(my - y1);
			yvalues[1] = fftv.getdBLabel(my - y2);
			yvalues[2] = fftv.getdBLabel(y1 - y2);
			break;
		}
		return yvalues;
	}

	private int limitXEdge(int x, Rectangle r) {
		if (x < r.x)
			x = r.x;
		else if (x > r.x + r.width)
			x = r.x + r.width - 1;
		return x;
	}

	private int limitYEdge(int y, Rectangle r) {
		if (y < r.y)
			y = r.y;
		else if (y > r.y + r.height)
			y = r.y + r.height - 1;

		return y;
	}

	public void persist(Pref p) {
		boolean _1in1 = !cm.paintContext.isScreenMode_3();
		if (_1in1) {
			x1 = ((x1 - 12) >> 1) + 3;
			x2 = ((x2 - 12) >> 1) + 3;
			y1 >>= 1;
			y2 >>= 1;
		}
		p.persistInt("FFTCursor.x1", x1);
		p.persistInt("FFTCursor.x2", x2);
		p.persistInt("FFTCursor.y1", y1);
		p.persistInt("FFTCursor.y2", y2);
		p.persistBoolean("FFTCursor.onFrebaseMark", onFrebaseMark);
		p.persistBoolean("FFTCursor.onVoltbaseMark", onVoltbaseMark);
	}

	public void load(Pref p) {
		x1 = p.loadInt("FFTCursor.x1");
		x2 = p.loadInt("FFTCursor.x2");
		y1 = p.loadInt("FFTCursor.y1");
		y2 = p.loadInt("FFTCursor.y2");
		onFrebaseMark = p.loadBoolean("FFTCursor.onFrebaseMark");
		onVoltbaseMark = p.loadBoolean("FFTCursor.onVoltbaseMark");
	}

	private boolean preScreenMode = true;

	/** preScreenMode作用:保证只有真正在1、3窗口切换时,x、y标尺才按2倍率缩放,其他情况不缩放 */
	public void resetCursor(Rectangle r) {
		boolean b = cm.paintContext.isScreenMode_3();
		if (b != preScreenMode) {
			x1 = limitXEdge((b ? ((x1 - 12) >> 1) + 3 : ((x1 - 3) << 1) + 12),
					r);
			x2 = limitXEdge((b ? ((x2 - 12) >> 1) + 3 : ((x2 - 3) << 1) + 12),
					r);
			y1 = limitYEdge((b ? y1 >> 1 : y1 << 1), r);
			y2 = limitYEdge((b ? y2 >> 1 : y2 << 1), r);
			preScreenMode = b;
		}

	}

}
