package com.owon.uppersoft.dso.function;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ResourceBundle;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.mode.control.FFTControl;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.util.ui.LineUtil;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.aspect.IView;
import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.core.aspect.control.LocationLabelProvider;
import com.owon.uppersoft.vds.core.fft.FFTUtil;
import com.owon.uppersoft.vds.core.fft.WndType;
import com.owon.uppersoft.vds.core.paint.Background;
import com.owon.uppersoft.vds.core.paint.PaintContext;
import com.owon.uppersoft.vds.core.paint.ScreenContext;
import com.owon.uppersoft.vds.data.LocRectangle;
import com.owon.uppersoft.vds.util.ArrayLogger;
import com.owon.uppersoft.vds.util.format.SFormatter;
import com.owon.uppersoft.vds.util.format.UnitConversionUtil;

/**
 * FFTView, fft mode: the device is also in the fft state; send all fft setting commands down and get the fft data (2048 points by default),
 * 
 * The protocol uses the original receiving method for normal data; with multiple frames the last frame is used, and if the data is long 2048 points are taken,
 * 
 * Getting deep memory data after stopping is not supported
 * 
 */
public class FFTView implements IView, LocationLabelProvider, Localizable {

	/** windowing method */
	public static WndType[] wndTypes = WndType.values();

	public static final boolean FFTNew = true, oldFFTversion = false;

	public static final int fftlen = 1 << 11;// 2048
	private final int PIXELS_PER_DIV = GDefine.PIXELS_PER_DIV;
	private final int PIXELS_PER_HORIZDIV = GDefine.AREA_WIDTH_BLOCK_PIXES;

	private int[] data = new int[fftlen];
	private boolean use;
	private ControlManager cm;
	private WaveFormManager wfm;
	private Background bg;
	private FFTControl mc;

	private int len;// len, i.e. dnum is the number of sampling points needed to compute the fft for a full screen
	public static final int xbases = 20, ftxbases = 10;
	public int dBbaseidx, vrmsbaseidx;
	public String fftchlLack;

	private int xb;
	/** Starting x, y position of the waveform on the virtual screen */
	private int xoff, yoff;
	private double xgap;
	private int lastScaleIdx;
	private int hc, wc;
	private Rectangle rec;
	public double yScaleRate = 1;// If saving is not needed, initialize the zoom ratio to 1
	private int vrmsIdx;

	public FFTView(WaveFormManager wfm, ControlManager cm) {
		this.cm = cm;
		this.wfm = wfm;
		this.mc = cm.getFFTControl();

		bg = new Background();
		// bg.setGap(5);
		vrmsIdx = wfm.getWaveForm(mc.getFFTchl()).getVoltbaseIndex();
		vrmsbaseidx = vrmsIdx;
		dBbaseidx = xoff = yoff = 0;
		/** Draw the 1024 fft results onto 500 screen pixels */
		setXGap();
		lastScaleIdx = mc.fftscale;
	}

	private void setXGap() {
		int screenPix = getFFTScreenPixNum();
		xgap = screenPix / (double) 1024;
	}

	@Override
	public void localize(ResourceBundle rb) {
		fftchlLack = rb.getString("M.Math.FFT.chloffWarn");
	}

	public boolean isOn() {
		return mc.isFFTon();
	}

	public void updateFFT() {
		/**
		 * While running, applies directly to the next frame; when stopped, a refresh is needed
		 */
		if (cm.isKeepGet())
			return;
		receiveNewData();
		Platform.getMainWindow().updateShow();
	}

	public void receiveNewData() {
		/** Add a synchronization lock to prevent the data from being polluted when it is used for drag-refresh drawing while being processed */
		synchronized (data) {
			doReceiveNewData();
		}
	}

	private void doReceiveNewData() {
		// if(true){use = true;return;}
		// Arrays.fill(data, 0);

		use = false;
		if (!isOn()) {
			return;
		}
		WaveForm wf = wfm.getWaveForm(mc.getFFTchl());
		ByteBuffer bb = wf.getFFT_Buffer();

		// Provided for fft; currently the final pixel array for drawing points, which needs to be modified when actually running and in deep memory
		if (bb == null)
			return;

		/** TODO deep memory is not supported for now */
		byte[] adc = bb.array();
		int p = bb.position();
		int l = bb.limit();
		len = l - p;

		// System.err.println(len);

		if (len <= 0)
			return;
		// ArrayLogger.configArray(adc, p, len);
		/** While running, use a specified number of points from the adc data for fft */
		// if (dh.isRuntime()) {
		// int length = controlManager.mathControl.fftAvailablePoints();
		// if (length <= 0 || length > len)
		// return;
		// if (length < len) {
		// int half = length >> 1;
		// int center = (p + l) >> 1;
		// int start = center - half;
		//
		// p = start;
		// len = length;
		// l = p + len;
		// }
		// System.err.println(l+", "+p);
		// }
		int pos0 = wf.getPos0ForADC();// Define.def.HalfAREA_PIXEL_HEIGHT;

		if (len < fftlen) {
			FFTUtil.plugValues(adc, data, p, l, fftlen, pos0);
		} else {
			int half = fftlen >> 1;
			int center = (l + p) >> 1;
			int start = center - half;
			int end = center + half;

			for (int i = start, j = 0; i < end; i++, j++) {
				data[j] = adc[i] - pos0;
			}
		}

		/**
		 * fft algorithm: see the specific class, including windowing, computation, and conversion to dB or Vrms
		 * 
		 * This covers the peripheral algorithm, i.e. how to draw. The idea: determine the value range represented by the scale on that axis, so the full-scale range can be determined, and how to draw the waveform once loaded can also be determined
		 * 
		 * X axis:
		 * 
		 * Let dnum be the number of sampling points needed to compute the fft for a full screen, peroid_S the time range of a full screen in seconds, and xbases the number of divisions on the x axis
		 * 
		 * Then the frequency between sampling points is pfreq = dnum / peroid_S
		 * 
		 * Take (pfreq/2) / xbases as the reference value for the frequency scale under fft, i.e. set up a group of frequency scales in a 1, 2, 5 pattern,
		 * 
		 * Choose the custom one closest to this reference value as the current frequency scale, with the scales above and below as its zoom options; this gives the frequency scale of the X axis
		 * 
		 * Y axis:
		 * 
		 * Vrms gives the same unit of quantity as the currently acquired waveform values, and can be converted using the current voltage scale
		 * 
		 * dB passes in an extra floating-point number representing the voltage corresponding to the difference between sampling points and converts it to a dB value, with no need to correspond to the original voltage scale system; just divide the difference into steps according to the custom dB scale
		 */

		if (mc.fftvaluetype == 0) {
			FFTUtil.compute_rms(data, wndTypes[mc.getFFTwnd()]);
		} else {
			FFTUtil.compute_db(data, wndTypes[mc.getFFTwnd()],
					getVoltBetweenPix());// 0.1
		}

		// int len = fftlen >> 1;
		// ArrayLogger.configArray(data, 0, len);
		// int max = data[0];
		// idx = 0;
		// for (int i = 1; i < len; i++) {
		// if (data[i] > max) {
		// max = data[i];
		// idx = i;
		// }
		// }
		// System.err.println("idx: " + idx);

		use = true;
	}

	private BigDecimal getBDHzPerDiv() {
		BigDecimal dnum = BigDecimal.valueOf(len);
		BigDecimal peroid_S = cm.getTimeControl().getBDTimebase()
				.multiply(BigDecimal.valueOf(xbases));
		// System.out.println("fftTb:" + controlManager.timeControl.getBDTimebase());
		BigDecimal pfreq;
		if (true) {// controlManager.isRuntime()
			pfreq = BigDecimal.valueOf(0);
			if (oldFFTversion) {
				// If this case is enabled, methods such as MathControl.getFftCompressRate() must be enabled.
				// pfreq = controlManager.getSampleRate().getBDValue().divide(
				// BigDecimal.valueOf(mc.getFftCompressRate()));
			} else {
				pfreq = cm.getMachineInfo().BDFFTTimeBases[mc
						.getFFTTimebaseIndex()];
			}
			return pfreq;
		} else {// deep memory capable fft
			/** In deep memory, the frequency between sampling points is pfreq = dnum / peroid_S */
			pfreq = dnum.divide(peroid_S);

			/** (pfreq/2) / ftxbases gives the reference value for the frequency scale under fft */
			BigDecimal v = pfreq.divide(BigDecimal.valueOf(2)).divide(
					BigDecimal.valueOf(ftxbases));
			return v;
		}
	}

	private double getHzPerDiv() {
		return getBDHzPerDiv().doubleValue();
	}

	private double getHzPerPix() {
		return getHzPerDiv() / getPixPerHorizdiv();
	}

	private int getPixPerHorizdiv() {
		return cm.paintContext.isScreenMode_3() ? PIXELS_PER_HORIZDIV
				: PIXELS_PER_HORIZDIV << 1;
	}

	private double getVoltBetweenPix() {
		int probeIdx = wfm.getWaveForm(mc.getFFTchl()).wfi.ci
				.getProbeMultiIdx();
		return cm.getCoreControl().getVoltageProvider()
				.getVoltage(probeIdx, vrmsbaseidx)
				/ (double) PIXELS_PER_DIV;
	}

	private double getdBperPix() {
		return FFTControl.dBValuePerDiv[dBbaseidx] / (double) PIXELS_PER_DIV;
	}

	public void setVrmsIdx(int idx) {
		vrmsIdx = idx;
		updateYScaleRate();
	}

	public void updateYScaleRate() {
		if (!isOn())
			return;
		switch (mc.fftvaluetype) {
		case 0:
			/**
			 * KNOW VRMS zoom ratio, two cases: running and stopped:
			 * 
			 * ⒈While running: the zoom ratio is the ratio of the current channel voltage scale, obtained in real time, to the voltage scale selected in the control
			 * 
			 * ⒉When stopped: the zoom ratio is the ratio of the channel voltage scale at load time to the voltage scale selected in the control
			 * (At load time the control is set to the current channel voltage scale, so the initial rate is 1)
			 */
			boolean isRuntime = cm.isRuntime();
			WaveForm wf = wfm.getWaveForm(mc.getFFTchl());
			int vbIdx;
			if (isRuntime) {
				vbIdx = wf.getVoltbaseIndex();
			} else {// if(isDMLoad)
				vbIdx = wf.basevbidx;
			}
			yScaleRate = cm.getMachineInfo().getVoltagesRatio(vbIdx, vrmsIdx);
			break;
		case 1:
			yScaleRate = FFTControl.dBValuePerDiv[0]
					/ (double) FFTControl.dBValuePerDiv[dBbaseidx];
			break;
		}
	}

	@Override
	public void adjustView(ScreenContext pc, Rectangle bound) {
		rec = bound;
		bg.adjustView(bound, pc.isScreenMode_3());
		xb = bound.x;
		wc = xb + (bound.width >> 1);
		hc = bound.y + (bound.height >> 1);
		setXGap();// adc point spacing readjusted
		resetXoff();// adc start offset readjusted
		cm.fftctr.resetCursor(bound);// cursor position readjusted
		bg.setXunitlen(pc.isScreenMode_3() ? 10 : 20);// grid readjusted
	}

	public void increX(int del) {
		xoff += del;
		// need to refresh mark
	}

	public void scale() {
		int idx = mc.fftscale;
		if (idx < 0 || idx >= FFTControl.scale.length)
			return;

		int txoff = xoff;
		double rate = (double) FFTControl.SCALES_VALUE[idx]
				/ FFTControl.SCALES_VALUE[lastScaleIdx];
		xgap = xgap * rate;
		int hwr = getFFTScreenPixNum() >> 1;
		int xc = hwr - txoff;
		xc = (int) (xc * rate);

		int nxoff = hwr - xc;
		xoff = nxoff;
		// need to refresh mark

		lastScaleIdx = idx;
	}

	public void increY(int del) {
		yoff += del;
	}

	public String getXLabel(int pix) {
		// if (oldFFTversion) {
		String xl = UnitConversionUtil.getSimplifiedFrequencyLabel_Hz(pix
				* getHzPerPix());
		return xl;
		// }
		// return pix + " pix";
	}

	public String getVrmsLabel(int pix) {
		return UnitConversionUtil.getSimplifiedVoltLabel_mV(pix
				* getVoltBetweenPix());
	}

	public String getdBLabel(int pix) {
		return SFormatter.UIformat("%.2f", pix * getdBperPix()) + "dB";
	}

	private LocRectangle lr = new LocRectangle();

	public int getMarkX0Location() {
		return xb + xoff;
	}

	public int getMarkY0Location() {
		return hc + yoff;
	}

	@Override
	public void paintView(Graphics2D g2d, ScreenContext sc, Rectangle r) {
		g2d.scale(DataHouse.xRate, DataHouse.yRate);
		bg.paintView(g2d, sc);
		WaveForm wf = wfm.getWaveForm(mc.getFFTchl());

		int x0 = r.x, y0 = r.y, rw = r.width, rh = r.height;

		boolean av = isOn();
		if (!av)
			return;

		av = wf.isOn();
		if (!av) {
			String n = SFormatter.UIformat(fftchlLack, wf.wfi.ci.getName());
			LineUtil.paintPrompt(g2d, wc, hc, Color.RED, n);
			return;
		}

		if (!use)
			return;

		int x1 = x0 + rw, y1 = y0 + rh;

		lr.set(x0, x1, y0, y1);

		int yo = getMarkY0Location();

		LineUtil.paintFFTLabel(yo, lr, g2d, Color.GREEN, "M");

		g2d.setClip(r);
		int xo = getMarkX0Location();

		synchronized (data) {
			paint_500(g2d, xo, yo);
		}
		// Draw the FFT cursor measurement lines and the FFT info display box
		cm.fftctr.drawFFTCursor(g2d, rec);
		drawFFTInfoBoard(g2d, rec);
	}

	protected void drawFFTInfoBoard(Graphics2D g2d, Rectangle r) {
		int bw = 115, bh = 65, x0 = r.width - bw, y0 = r.height - bh, hGap = bh >> 2;
		Color tmp = g2d.getColor();
		// g2d.setColor(Color.BLACK);
		// g2d.fillRoundRect(r.width / 10 * 8 - 6, r.height / 10 * 8 + 8,
		// r.width / 5 + 8, r.height / 10 * 2 - 5, 15, 15);

		WaveForm wf = wfm.getWaveForm(mc.getFFTchl());
		g2d.setColor(Color.PINK);
		if (wf != null && wf.isOn())
			g2d.setColor(wf.getColor());
		g2d.drawString("T", get_T_x0(r), r.y + 11);// horizontal trigger position
		g2d.drawRoundRect(x0, y0, bw, bh, 15, 15);
		x0 += 5;
		drawHorizPosition(g2d, x0, y0 += hGap);
		drawMarkpos0(g2d, x0, y0 += hGap);
		drawFFTTimeBases(g2d, x0, y0 += hGap);
		drawVoltBase(g2d, x0, y0 += hGap);
		g2d.setColor(tmp);
	}

	protected void drawHorizPosition(Graphics2D g2d, int x0, int y0) {
		int hpixs = getPixPerHorizdiv() * 5 - xoff;
		double hz = getHzPerPix() * hpixs;
		// String fre;
		// fre = hz + " pix";
		// if (oldFFTversion) {
		String fre = UnitConversionUtil.getSimplifiedFrequencyLabel_Hz(hz);
		// }
		g2d.drawString("T: " + fre, x0, y0);
	}

	protected void drawMarkpos0(Graphics2D g2d, int x0, int y0) {
		int yd = -yoff;
		double d = yd / (double) PIXELS_PER_DIV;
		g2d.drawString("M: " + d + " Divs", x0, y0);
	}

	protected void drawFFTTimeBases(Graphics2D g2d, int x0, int y0) {
		String fre;
		// deep memory capable fft
		// if (oldFFTversion) {
		double fpd = getHzPerDiv()
				* FFTControl.SCALES_VALUE[cm.getFFTControl().fftscale];
		fre = UnitConversionUtil.getSimplifiedFrequencyLabel_Hz(fpd);
		// } else {
		// fre = controlManager.getMachineInfo().FFTTimeBases[mc.getFFTTimebaseIndex()];
		// }

		g2d.drawString("H: " + fre + " / Div", x0, y0);// "Fre:"
	}

	protected void drawVoltBase(Graphics2D g2d, int x0, int y0) {
		String v;
		switch (mc.fftvaluetype) {
		case 0:
			v = mc.VOLTAGEs[vrmsbaseidx].toString();
			g2d.drawString("V: " + v + "  / Div", x0, y0);// "Vrms:"
			break;
		case 1:
			v = mc.dBPerDiv[dBbaseidx];
			g2d.drawString("V: " + v + "  / Div", x0, y0);// "dB:"
			break;
		}
	}

	private void paint_500(Graphics2D g2d, int xloc, int yloc) {
		// if (true)return;
		g2d.setColor(Color.green);

		int i = 0, tx, ty, x, y;
		double xp;
		xp = tx = xloc;
		ty = yloc - data[i++];
		i++;
		int end = fftlen >> 1;

		while (i < end) {
			xp += xgap;
			x = (int) xp;
			y = (int) (yloc - data[i++] * yScaleRate);// getYScaleRate() //
			g2d.drawLine(tx, ty, x, y);
			tx = x;
			ty = y;
		}
	}

	@Override
	public String getYDeltaLocationLabel(int yd) {
		switch (mc.fftvaluetype) {
		case 0:// vrms
			return getVrmsLabel(yd);
		case 1:// db
			return getdBLabel(yd);
		default:
			return "";
		}
	}

	@Override
	public String getYLocationLabel(int y) {
		int my = getMarkY0Location();
		return getXLabel(my - y);
	}

	@Override
	public String getXDeltaLocationLabel(int xd) {
		return getXLabel(xd);
	}

	@Override
	public String getXLocationLabel(int x) {
		int mx = getMarkX0Location();
		return getXDeltaLocationLabel((x - mx));
	}

	private void resetXoff() {
		xoff = cm.paintContext.isScreenMode_3() ? xoff >> 1 : xoff << 1;
	}

	private int getFFTScreenPixNum() {
		boolean isFFTon = cm.paintContext.isScreenMode_3();
		return PaintContext.getFFTScreenPixNum(isFFTon);
		// return controlManager.paintContext.isScreenMode_3() ? PaintContext.wrange : 1000;
	}

	private int get_T_x0(Rectangle r) {
		int tx = r.x + r.width >> 1;
		return cm.paintContext.isScreenMode_3() ? --tx : tx + 3;
	}

}
