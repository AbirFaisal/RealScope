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
 * FFTView, fft时，下位机也处在fft的状态，发所有fft设置指令下去，拿fft的数据(默认2048个点)，
 * 
 * 协议采用原有普通数据的接收方式，多帧则使用最后一帧，数据长则取2048个点，
 * 
 * 停止后不支持拿深存储数据
 * 
 */
public class FFTView implements IView, LocationLabelProvider, Localizable {

	/** 加窗方式 */
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

	private int len;// len,即dnum为满屏需要计算fft的采样点数
	public static final int xbases = 20, ftxbases = 10;
	public int dBbaseidx, vrmsbaseidx;
	public String fftchlLack;

	private int xb;
	/** 在虚拟的屏幕上波形的x，y起始位置 */
	private int xoff, yoff;
	private double xgap;
	private int lastScaleIdx;
	private int hc, wc;
	private Rectangle rec;
	public double yScaleRate = 1;// 若不需保存，初始化缩放比例为1
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
		/** 将1024个fft计算结果画在500个屏幕像素上 */
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
		 * 在运行时直接适用到下一幅，停止的话需要刷新
		 */
		if (cm.isKeepGet())
			return;
		receiveNewData();
		Platform.getMainWindow().updateShow();
	}

	public void receiveNewData() {
		/** 加同步锁，防止处理data的同时被用来拖拽刷图，data可能被污染 */
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

		// 提供给fft用的，目前是最终画点的像素数组，需要真的运行时和深存储进行修改
		if (bb == null)
			return;

		/** TODO 暂不支持深存储 */
		byte[] adc = bb.array();
		int p = bb.position();
		int l = bb.limit();
		len = l - p;

		// System.err.println(len);

		if (len <= 0)
			return;
		// ArrayLogger.configArray(adc, p, len);
		/** 在运行时使用adc数据中的指定个数点作fft */
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
		 * fft算法可查看具体的类，包括加窗，运算，转到dB或Vrms
		 * 
		 * 这里涉及的是外围算法，如何画图，思路是，确定该轴上的档位所代表的值范围，这样满量程的范围就可以确定，同时一载入后如何画波形也可以确定
		 * 
		 * X轴：
		 * 
		 * 设dnum为满屏需要计算fft的采样点数，peroid_S为以秒为单位的满屏的时间范围，xbases为x轴的格数
		 * 
		 * 则采样点间的频率pfreq = dnum / peroid_S
		 * 
		 * 将(pfreq/2) / xbases作为对应到fft下频率档位的参考值，即设定一组1，2，5为模式的频率档位，
		 * 
		 * 选择和这一参考值最接近的自定义为当前对应的频率档位，上下档位作为其缩放的选项，这样就得到了X轴的频率档位
		 * 
		 * Y轴：
		 * 
		 * Vrms得到和当前波形采集的值相同的数量单位，可使用当前电压档位进行换算
		 * 
		 * dB通过传入额外的浮点数，代表采样点间差值所对应的电压值，转换为dB数，无需和原来的电压档位系统对应，根据自定义的dB档位进行差值分档即可
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
				// 要是启用该情况,需开启MathControl.getFftCompressRate()等方法。
				// pfreq = controlManager.getSampleRate().getBDValue().divide(
				// BigDecimal.valueOf(mc.getFftCompressRate()));
			} else {
				pfreq = cm.getMachineInfo().BDFFTTimeBases[mc
						.getFFTTimebaseIndex()];
			}
			return pfreq;
		} else {// 可用于深存储的fft
			/** 深存储下采样点间的频率pfreq = dnum / peroid_S */
			pfreq = dnum.divide(peroid_S);

			/** (pfreq/2) / ftxbases 对应到fft下频率档位的参考值 */
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
			 * KNOW VRMS缩放比率 分运行和停止两情况：
			 * 
			 * ⒈运行时：缩放率是 实时获取当前通道电压档位，与控件选择的电压档位的比值
			 * 
			 * ⒉停止时：缩放率是 载入时当前通道电压档位， 与控件选择的电压档位的比值
			 * (载入时，控件设置为当前通道电压档位，即初始rate为1)
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
		setXGap();// adc每点间隔重调
		resetXoff();// adc起点偏移量重调
		cm.fftctr.resetCursor(bound);// 光标位置重调
		bg.setXunitlen(pc.isScreenMode_3() ? 10 : 20);// 网格重调
	}

	public void increX(int del) {
		xoff += del;
		// 需刷新mark
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
		// 需刷新mark

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
		// 画FFT光标测量线 和 FFT信息显示框
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
		g2d.drawString("T", get_T_x0(r), r.y + 11);// 水平触发位置
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
		// 可用于深存储的fft
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
