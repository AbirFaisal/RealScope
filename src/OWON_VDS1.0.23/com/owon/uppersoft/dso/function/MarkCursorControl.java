package com.owon.uppersoft.dso.function;

import static com.owon.uppersoft.vds.util.format.UnitConversionUtil.getSimplifiedFrequencyLabel_Hz;
import static com.owon.uppersoft.vds.util.format.UnitConversionUtil.getSimplifiedTimebaseLabel_mS;
import static com.owon.uppersoft.vds.util.format.UnitConversionUtil.getSimplifiedVoltLabel_mV;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.model.ChartDecorater;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.view.ViewChart;
import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.aspect.control.HorizontalUnit;
import com.owon.uppersoft.vds.core.aspect.control.ITimeControl;
import com.owon.uppersoft.vds.core.paint.PaintContext;
import com.owon.uppersoft.vds.core.paint.ScreenContext;
import com.owon.uppersoft.vds.ui.resource.SwingResourceManager;
import com.owon.uppersoft.vds.util.Pref;

//import com.owon.uppersoft.vds.data.Point;

public class MarkCursorControl {
	public final static Color Hue = Color.PINK;
	public boolean ison;
	private final int r_y1in1 = PaintContext.ScreenMode1_ChartInsets.top,
			r_y3in1 = PaintContext.ScreenMode3_ChartInsets.top;

	private int x1pix, x2pix, y1pix, y2pix;// 基于1in1情况记录
	private boolean onTimeMeasure, onVoltMeasure;
	private boolean onx1, onx2, ony1, ony2;
	public int chNum = 0;
	private String x1val, x2val, y1val, y2val, dxval, dyval;
	private String frequence;

	private ViewChart vc;
	private MarkableProvider wfm;
	private ScreenContext pc;
	private HorizontalUnit tc;
	private final String initval = "?";
	private PropertyChangeSupport pcs;

	public MarkCursorControl(Pref p, int ChannelsNumber, HorizontalUnit tc,
			PropertyChangeSupport pcs, MarkableProvider wfm) {
		onx1 = onx2 = ony1 = ony2 = false;
		x1val = x2val = y1val = y2val = dxval = dyval = initval;
		this.tc = tc;
		this.wfm = wfm;
		this.pcs = pcs;
		load(p, ChannelsNumber);

		pcs.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				String pn = evt.getPropertyName();
				if (pn.equals(ITimeControl.onTimebaseUpdated)) {
					computeXValues();
				}
			}
		});
	}

	/** TODO 最好改用其他方式引入ViewChart和DataHouse,避免代码耦合 */
	public void acceptImporter(ViewChart vc, DataHouse dh) {
		this.vc = vc;
		this.pc = vc.getScreenContext();
	}

	public boolean is3in1() {
		return pc.isScreenMode_3();
	}

	private Rectangle getLocInfo() {
		return vc.getLocInfo();
	}

	public void persist(Pref p) {
		p.persistInt("MarkCursor.x1", x1pix);
		p.persistInt("MarkCursor.x2", x2pix);
		p.persistInt("MarkCursor.y1", y1pix);
		p.persistInt("MarkCursor.y2", y2pix);
		// persistBoolean( "MarkCursor.onTimebase", onTimeMeasure);
		// persistBoolean( "MarkCursor.onVoltbase", onVoltMeasure);
		p.persistInt("MarkCursor.CHNum", chNum);
	}

	public void load(Pref p, int ChannelsNumber) {
		x1pix = p.loadInt("MarkCursor.x1");
		x2pix = p.loadInt("MarkCursor.x2");
		y1pix = p.loadInt("MarkCursor.y1");
		y2pix = p.loadInt("MarkCursor.y2");
		onTimeMeasure = false;// loadBoolean( "MarkCursor.onTimebase");
		onVoltMeasure = false;// loadBoolean( "MarkCursor.onVoltbase");
		ison = onTimeMeasure || onVoltMeasure;
		chNum = p.loadInt("MarkCursor.CHNum");
		if (chNum >= ChannelsNumber)
			chNum = ChannelsNumber - 1;
	}

	private final int detectGap = 7;

	public int checkMark(boolean ScreenMode_3, int x, int y) {
		int MCy1, MCy2, MCx1, MCx2;

		if (!ScreenMode_3) {
			MCy1 = y1pix;
			MCy2 = y2pix;
		} else {
			MCy1 = y1pix >> 1;
			MCy2 = y2pix >> 1;
		}
		MCx1 = x1pix;
		MCx2 = x2pix;
		int code = Cursor.DEFAULT_CURSOR;

		if (onTimeMeasure) {
			if ((x > MCx1 - detectGap && x < MCx1 + detectGap)
					|| (x > MCx2 - detectGap && x < MCx2 + detectGap)) {
				code = Cursor.E_RESIZE_CURSOR;
				if (x > MCx1 - detectGap && x < MCx1 + detectGap) {
					onx1 = true;
				} else if (x > MCx2 - detectGap && x < MCx2 + detectGap) {
					onx2 = true;
				}

				if (MCx1 >= MCx2 - detectGap && MCx1 <= MCx2 + detectGap)
					onx2 = false;
			} else {
				onx1 = false;
				onx2 = false;
			}
		}
		if (onVoltMeasure) {
			if ((y > MCy1 - detectGap && y < MCy1 + detectGap)
					|| (y > MCy2 - detectGap && y < MCy2 + detectGap)) {
				code = Cursor.N_RESIZE_CURSOR;
				if (y > MCy1 - detectGap && y < MCy1 + detectGap) {
					ony1 = true;
				} else if (y > MCy2 - detectGap && y < MCy2 + detectGap) {
					ony2 = true;
				}

				if (MCy1 >= MCy2 - detectGap && MCy1 <= MCy2 + detectGap)
					ony2 = false;
			} else {
				ony1 = false;
				ony2 = false;
			}
		}
		return code;
	}

	public boolean getOnTimebaseM() {
		return onTimeMeasure;
	}

	public void setOnTimebaseM(Boolean b) {
		onTimeMeasure = b;
		ison = onTimeMeasure || onVoltMeasure;
		pcs.firePropertyChange(PropertiesItem.UPDATE_MARKBULLETIN, null, null);
	}

	public boolean getOnVoltbaseM() {
		return onVoltMeasure;
	}

	public void setOnVoltbaseM(Boolean b) {
		onVoltMeasure = b;
		ison = onTimeMeasure || onVoltMeasure;
		pcs.firePropertyChange(PropertiesItem.UPDATE_MARKBULLETIN, null, null);
	}
	
	public void turnOnMarkCursor(boolean b) {
		setOnTimebaseM(b);
		setOnVoltbaseM(b);
		pcs.firePropertyChange(PropertiesItem.UPDATE_CURSOR, null, null);
		Platform.getMainWindow().getChartScreen().re_paint();
	}

	/**
	 * @param pixtime_mS
	 *            波形界面x轴像素的mS值
	 */
	public void computeXValues() {
		if (!onTimeMeasure)
			return;

		int htp = tc.getHorizontalTriggerPosition();
		double pixtime_mS = tc.getPixelTime_mS();

		Rectangle r = getLocInfo();

		int dhwl = (r.width >> 1) + r.x - htp;

		// MarkCursor X1,X2 的像素值。
		double x1 = dhwl - x1pix;
		double x2 = dhwl - x2pix;

		// 1000个像素，20格，每格50像素; bdtimebase当前时基。
		x1 = x1 * pixtime_mS;
		x2 = x2 * pixtime_mS;
		// 计算Delta X
		int x = Math.abs(x2pix - x1pix);
		double dx = x * pixtime_mS;

		// 规范单位
		x1val = getSimplifiedTimebaseLabel_mS(x1);
		x2val = getSimplifiedTimebaseLabel_mS(x2);
		dxval = getSimplifiedTimebaseLabel_mS(dx);
		if (dx != 0) {
			double temp = GDefine.S2mS / dx;
			frequence = getSimplifiedFrequencyLabel_Hz(temp);
		} else
			frequence = "?Hz";
		pcs.firePropertyChange(PropertiesItem.UPDATE_MARKBULLETIN, null, null);
	}

	public void computeYValues(int pos, int voltbase) {
		if (!onVoltMeasure)
			return;

		// System.err.println("y1: " + y1pix + " y2: " + y2pix);
		Rectangle r = getLocInfo();
		double y1, y2;
		int r_centreY = r.height >> 1;
		// System.err.println("Pos:" + pos + "r_centreY: " + r_centreY + " r.y:
		// " + r.y);
		int pos0pix;
		if (!pc.isScreenMode_3()) {
			/** 若左上角为坐标原点,计算零点位置相对原点的垂直像素值。 */
			pos0pix = r_centreY - 2 * pos + r.y;
			/**
			 * 若左上角为坐标原点, y1pix、y2pix为y1标尺、y2标尺相对原点的垂直像素值。
			 * pos-y1pix,得到标尺们相对于零点位置的像素正负偏移量。
			 */
			y1 = pos0pix - y1pix;
			y2 = pos0pix - y2pix;
		} else {
			pos0pix = r_centreY - pos + r.y;
			y1 = (pos0pix - (((y1pix - r_y1in1) >> 1) + r_y3in1)) << 1;
			y2 = (pos0pix - (((y2pix - r_y1in1) >> 1) + r_y3in1)) << 1;
		}

		/**
		 * 将Y尺的像素正负偏移量,计算成电压对应值
		 */
		y1 = y1 / 50 * voltbase;
		y2 = y2 / 50 * voltbase;
		double y = Math.abs(y2 - y1);

		y1val = getSimplifiedVoltLabel_mV(y1);
		y2val = getSimplifiedVoltLabel_mV(y2);
		dyval = getSimplifiedVoltLabel_mV(y);
		
		pcs.firePropertyChange(PropertiesItem.UPDATE_MARKBULLETIN, null, null);
	}

	// 画X,Y标尺线
	private void drawXLines(Graphics2D g2d, Rectangle r) {
		int MCx1 = x1pix;
		int MCx2 = x2pix;

		int Recy = r.x - 2;
		g2d.drawLine(MCx1, Recy, MCx1, Recy + r.height);// MarkLine x1
		g2d.drawLine(MCx2, Recy, MCx2, Recy + r.height);// MarkLine x2
	}

	private void drawYLines(Graphics2D g2d, Rectangle r) {
		int MCy1 = y1pix;
		int MCy2 = y2pix;

		int y1 = MCy1, y2 = MCy2;// rectangle 高减半, MarkCursor Y 也减半。
		if (pc.isScreenMode_3()) {
			y1 = ((MCy1 - r_y1in1) >> 1) + r_y3in1;
			y2 = ((MCy2 - r_y1in1) >> 1) + r_y3in1;
		}
		g2d.drawLine(r.x, y1, r.x + r.width, y1);// MarkLine y1
		g2d.drawLine(r.x, y2, r.x + r.width, y2);// MarkLine y2
	}

	/** 画X,Y标尺值显示 */
	private int tia1[] = new int[3], tia2[] = new int[3];// Triangle_Coordinates
	final int tiaIncr = 8;

	/**
	 * 画 Delta Δ \u0394
	 * */
	protected void drawTriangle(Graphics2D g2d, int x, int y) {
		tia1[0] = x;
		tia1[1] = x + tiaIncr / 2;
		tia1[2] = x + tiaIncr;
		tia2[0] = y;
		tia2[1] = y - tiaIncr;
		tia2[2] = y;
		g2d.drawPolygon(tia1, tia2, 3);
	}

	public void drawMarkCursor(Graphics2D g2d, Rectangle r) {
		// Markable ci = wfm.get(chNum);
		if (onTimeMeasure) {
			// 画X标尺线
			drawXLines(g2d, r);
			// showXValues(g2d, r);// X标尺值显示
		}
		// else
		if (onVoltMeasure) {
			// 画Y标尺线
			drawYLines(g2d, r);
			// if (ci.isOn()) {
			// showYValues(g2d, ci.getPos0(), ci.getVoltValue(), r);// Y标尺值显示
			// } else
			// showYReminder(g2d, ci.getName(), r);
		}
//		if (!ison)
//			drawCursorEnterIcon(g2d, r);
		pcs.firePropertyChange(PropertiesItem.UPDATE_MARKBULLETIN, null, null);
	}
	
	private void drawCursorEnterIcon(Graphics2D g2d, Rectangle r) {
		String path = "/com/owon/uppersoft/dso/image/restore.gif";
		Image img = SwingResourceManager.getIcon(ChartDecorater.class, path)
				.getImage();
		g2d.drawImage(img, r.x, r.height - 8, null, null);
	}

	public int limitXEdge(Rectangle r, int value) {
		if (value < r.x)
			value = r.x;
		else if (value > r.x + r.width)
			value = r.x + r.width;

		return value;
	}

	public int limitYEdge(Rectangle r, int y) {
		int upedge = r.y, downedge = r.y + r.height;
		if (pc.isScreenMode_3())
			downedge = (r.height << 1) + r.y;

		if (y < upedge)
			y = upedge;
		else if (y > downedge)
			y = downedge;

		// System.out.println("r.y: " + r.y + ",h: " + r.height + ",y: " + y
		// + ",downedge:" + downedge);

		return y;
	}

	public void dragMarkCursor(int x, int y, Point loc0) {
		Rectangle r = getLocInfo();

		if (onTimeMeasure) {
			if (onx1) {
				int dx = x - loc0.x;
				x1pix += dx;
				x1pix = limitXEdge(r, x1pix);
			}
			if (onx2) {
				int dx = x - loc0.x;
				x2pix += dx;
				x2pix = limitXEdge(r, x2pix);
			}
			computeXValues();
		}
		// else
		if (onVoltMeasure) {
			if (ony1) {
				int dy = y - loc0.y;
				int tmp = y1pix;
				if (!pc.isScreenMode_3()) {
					tmp += dy;
				} else {
					tmp += (dy << 1);
				}
				y1pix = limitYEdge(r, tmp);
			}
			if (ony2) {
				int dy = y - loc0.y;
				int tmp = y2pix;
				if (!pc.isScreenMode_3()) {
					tmp += dy;
				} else {
					tmp += (dy << 1);
				}
				y2pix = limitYEdge(r, tmp);
			}

			Markable ci = wfm.get(chNum);
			computeYValues(ci.getPos0(), ci.getVoltValue());
		}
	}

	public void closeMarkCursor() {
		setOnTimebaseM(false);
		setOnVoltbaseM(false);
		pcs.firePropertyChange(PropertiesItem.UPDATE_MARKBULLETIN, null, null);
		pcs.firePropertyChange(PropertiesItem.UPDATE_CURSOR, null, null);
		Platform.getMainWindow().getChartScreen().re_paint();// Screen
	}

	public void updateMarkValue(Graphics2D g2d) {
		int x = 3, y = 23, width = 1000;
		if (onTimeMeasure) {
			if (x1val.equals(initval)) {
				computeXValues();
			}
			if (!onVoltMeasure)
				y += 5;
			g2d.drawString("X1: " + x1val, x + 8, y);
			g2d.drawString("X2: " + x2val, x + 4 + width / 10, y);
			drawTriangle(g2d, x + 4 + width / 5, y - 1);
			g2d.drawString("X: " + dxval, x + 4 + width / 5 + tiaIncr, y);

			g2d.drawString("1/", x + 4 + width / 20 * 6, y);
			drawTriangle(g2d, x + 15 + width / 20 * 6, y - 1);
			g2d.drawString("X: " + frequence, x + 24 + width / 20 * 6, y);
		}
		if (onVoltMeasure) {
			Markable ci = wfm.get(chNum);
			y += 18;
			if (ci.isOn()) {
				if (y1val.equals(initval)) {
					computeYValues(ci.getPos0(), ci.getVoltValue());
				}
				if (!onTimeMeasure)
					y -= 13;
				g2d.drawString("Y1: " + y1val, x + 8, y);
				g2d.drawString("Y2: " + y2val, x + 4 + 2 * width / 20, y);
				drawTriangle(g2d, x + 4 + width / 5, y - 1);
				g2d.drawString("Y: " + dyval, x + width / 5 + 12, y);
			} else
				g2d.drawString(ci.getName()
						+ ' '
						+ I18nProvider.bundle()
								.getString("M.Channel.OffRemind"), 26, y);
		}
	}
}