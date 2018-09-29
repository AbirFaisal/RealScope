package com.owon.uppersoft.dso.function.perspective;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.owon.uppersoft.dso.function.ref.ReferenceFile;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.model.WFTimeScopeControl;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.ref.IRefSource;
import com.owon.uppersoft.dso.util.ui.LineUtil;
import com.owon.uppersoft.vds.core.aspect.IView;
import com.owon.uppersoft.vds.core.aspect.control.VoltageProvider;
import com.owon.uppersoft.vds.core.aspect.help.WF;
import com.owon.uppersoft.vds.core.control.MathControl;
import com.owon.uppersoft.vds.core.paint.ScreenContext;
import com.owon.uppersoft.vds.core.rt.IDataMaxMin;

/**
 * CompositeWaveForm，复合波形，独立开辟数组空间来进行计算，可以达到更快的速度和刷新效果，以及更好的绘图算法
 * 
 * TODO 增加标签供拖拽改变零点
 */
public class CompositeWaveForm implements IView, IRefSource {

	static class Fraction {
		public int a, b;
		public double scale = 1;

		public Fraction() {
			this(-1, -1);
		}

		public Fraction(int a, int b) {
			this.a = a;
			this.b = b;
		}

		public Fraction(double a, double b) {
			this.scale = a / b;
		}

		public void set(int a, int b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public String toString() {
			return a + "/" + b;
		}

		public double getScaledValue(int adc) {
			// pix/pix_m=Vb_m/Vb
			double adc_m = adc * scale;
			// System.out.println(adc+"x"+scale+"="+adc_m);
			return adc_m;
		}
	}

	public static void main(String[] args) {
		Fraction p = new Fraction(0.22, 11);
		System.out.println(p.scale);
		System.out.println(p.getScaledValue(100));
	}

	public static final int CompositeWaveFormIndex = Integer.MAX_VALUE;

	public Color co = Color.green;
	public boolean linkline = true;
	private MathControl mc;
	private WaveFormManager wfm;
	private VoltageProvider vp;
	private ControlManager cm;

	public CompositeWaveForm(WaveFormManager wfm, MathControl mc,
			VoltageProvider vp, ControlManager cm) {
		this.wfm = wfm;
		this.mc = mc;
		this.vp = vp;
		this.cm = cm;
	}

	private IntBuffer pixbuf, adcbuf;

	public IntBuffer getADC_Buffer() {
		return adcbuf;
	}

	public void receiveNewData(ScreenContext pc, int chl) {
		if (mc.isInclude(chl))
			receiveNewData(pc);
	}

	@Override
	public int getPos0() {
		return getYloc();
	}

	@Override
	public int getProbeMultiIdx() {
		return 0;
	}

	@Override
	public int getVoltbaseIndex() {
		return mc.getMathvbidx();
	}

	@Override
	public int getWaveType() {
		return ReferenceFile.RefFile_Math;
	}

	public boolean isOn() {
		return mc.mathon;
	}

	public boolean onShowPos0;

	public void paintItem(Graphics2D g2d, ScreenContext pc, Rectangle r,
			ControlManager cm, boolean onFront) {
		if (!isOn())
			return;

		g2d.setColor(co);

		int yb = getPos0onChart(pc);

		int y = r.y;
		int bottom = r.y + r.height;
		/** 画左边的标尺 ,依次画顶 底 中 */
		LineUtil.paintChannelLabel(yb, y, bottom, g2d, "M", 1, onFront);

		if (onShowPos0)
			LineUtil.paintOnShowPos0(g2d, pc, Platform.getDataHouse().divUnits,
					getYloc(), yb, r);
	}

	public void receiveNewData(ScreenContext pc) {
		if (!mc.mathon) {
			pixbuf = null;
			adcbuf = null;
			return;
		}

		resetIntBuf(pc);
	}

	/**
	 * 给adc点进行乘法的时候应该乘a，除b，已经计入了衰减倍率
	 * 
	 * @param vb1
	 * @param vb
	 * @return
	 */
	Fraction getMultiply_0(WF ch1, int vb1, int vb) {
		Fraction p = new Fraction(1, 1);

		int probeIdx = ch1.getProbeMultiIdx();
		int[] ivt = vp.getVoltages(probeIdx);
		int probeRate = cm.getMachineInfo().ProbeMulties[probeIdx];

		int prm = cm.getMachineInfo().ProbeMulties[mc.probeIndex];
		if (vb1 == vb) {
			p.b = 1;
			p.a = 1;
		} else {
			int b, a;
			b = ivt[vb];
			a = ivt[vb1];
			while (b % 10 == 0 && a % 10 == 0) {
				b /= 10;
				a /= 10;
			}
			p.b = b;
			p.a = a;
		}
		p.b = p.b * prm;
		p.a = p.a * probeRate;
		return p;
	}

	Fraction getMultiply_1(WF ch1, int vb1, int vb) {

		int probeIdx = ch1.getProbeMultiIdx();
		int[] ivt = vp.getVoltages(probeIdx);
		double[] ivt_Math = mc.dbMathVoltage;
		int a = ivt[vb1];
		double m = ivt_Math[vb];
		Fraction p = new Fraction(a, m);
		// System.out.println(ch1.toString() + " ,a:" + a + "/m:" + m + ","
		// + p.scale);

		return p;// 通道/math记录
	}

	private int yloc;

	public void setYlocIncrement(int yl, boolean ScreenMode_3, int hc) {
		yloc = yl;
		resetIntBuf(ScreenMode_3, hc);
	}

	public int getYloc() {
		return yloc;
	}

	public int getPos0onChart(ScreenContext pc) {
		return getPos0onChart(pc.isScreenMode_3(), pc.getHcenter());
	}

	public int getPos0onChart(boolean ScreenMode_3, int hc) {
		if (ScreenMode_3)
			return hc - yloc;
		else
			return hc - (yloc << 1);
	}

	private void complexCompute(WF ch1, WF ch2, boolean ScreenMode_3, int hc) {
		int vb = mc.getMathvbidx();
		int vb1 = ch1.getVoltbaseIndex(), vb2 = ch2.getVoltbaseIndex();
		// System.err.println(MachineInfo.intVOLTAGE[0][vb]);
		Fraction m1 = getMultiply_1(ch1, vb1, vb), m2 = getMultiply_1(ch2, vb2,
				vb);

		int[] pixArray = pixbuf.array();
		int[] adcArray = adcbuf.array();

		int p1, l1, p2;
		byte[] array1, array2;
		ByteBuffer buf1 = ch1.getADC_Buffer();
		ByteBuffer buf2 = ch2.getADC_Buffer();
		if (buf1 == null || buf2 == null)
			return;

		array1 = buf1.array();
		array2 = buf2.array();
		p1 = buf1.position();
		l1 = buf1.limit();
		p2 = buf2.position();

		int yb = getPos0onChart(ScreenMode_3, hc);

		int pos1, pos2;
		if (cm.isRuntime()) {
			pos1 = ch1.getPos0ForADC();
			pos2 = ch2.getPos0ForADC();
		} else {
			pos1 = ch1.getFirstLoadPos0();
			pos2 = ch2.getFirstLoadPos0();
		}

		boolean b = ScreenMode_3;
		int l = l1 - p1;
		/** 拿全部的获取点，计算一次即可 */
		int i = 0;
		// int m1a = m1.a, m1b = m1.b, m2a = m2.a, m2b = m2.b;
		double adc;
		switch (wfm.getMathOperation()) {
		case 0: {// '+' yb-((a1-p1)+(a2-p2))
			if (b) {
				while (p1 < l1) {
					adc = m1.getScaledValue(array1[p1++] - pos1)
							+ m2.getScaledValue(array2[p2++] - pos2);
					adc = restrict_max_min_yb(adc);
					adcArray[i] = (int) adc;
					pixArray[i] = yb - ((int) adc);
					i++;
				}
			} else {
				while (p1 < l1) {
					adc = m1.getScaledValue(array1[p1++] - pos1)
							+ m2.getScaledValue(array2[p2++] - pos2);
					adc = restrict_max_min_yb(adc);
					adcArray[i] = (int) adc;
					pixArray[i] = yb - ((int) adc << 1);
					i++;
				}
			}
		}
			break;
		case 1: {// '-' yb-((a1-p1)-(a2-p2))
			if (b) {
				while (p1 < l1) {
					adc = m1.getScaledValue(array1[p1++] - pos1)
							- m2.getScaledValue(array2[p2++] - pos2);
					adc = restrict_max_min_yb(adc);
					adcArray[i] = (int) adc;
					pixArray[i] = yb - ((int) adc);

					i++;
				}
			} else {
				while (p1 < l1) {
					adc = (m1.getScaledValue(array1[p1++] - pos1) - m2
							.getScaledValue(array2[p2++] - pos2));
					adc = restrict_max_min_yb(adc);
					adcArray[i] = (int) adc;
					pixArray[i] = yb - ((int) adc << 1);
					i++;
				}
			}
		}
			break;
		case 2: {// '*' yb-(a1-p1)*(a2-p2)
			if (b) {
				while (p1 < l1) {
					adc = m1.getScaledValue(array1[p1++] - pos1)
							* m2.getScaledValue(array2[p2++] - pos2);
					adc = restrict_max_min_yb(adc);
					adcArray[i] = (int) adc;
					pixArray[i] = yb - (int) adc;

					i++;
				}
			} else {
				while (p1 < l1) {
					adc = (m1.getScaledValue(array1[p1++] - pos1) * m2
							.getScaledValue(array2[p2++] - pos2));
					adc = restrict_max_min_yb(adc);
					adcArray[i] = (int) adc;
					pixArray[i] = yb - ((int) adc << 1);
					i++;
				}
			}
		}
			break;
		case 3: {// '/' yb-(a1-p1)/(a2-p2)
			/** 约去b可能带来的乘2 */
			if (b) {
				while (p1 < l1) {
					double tmp = m2.getScaledValue(array2[p2++] - pos2);
					if (tmp == 0) {
						adcArray[i] = 0;
						pixArray[i] = yb;
					} else {
						adc = m1.getScaledValue(array1[p1] - pos1) / tmp;
						adc = restrict_max_min_yb(adc);
						adcArray[i] = (int) adc;
						pixArray[i] = yb - (int) adc;
					}
					p1++;
					i++;
				}
			} else {
				while (p1 < l1) {
					double tmp = m2.getScaledValue(array2[p2++] - pos2);
					if (tmp == 0) {
						adcArray[i] = 0;
						pixArray[i] = yb;
					} else {
						adc = m1.getScaledValue(array1[p1] - pos1) / tmp;
						// System.out.println(adc+":"+m1.getScaledValue(array1[p1]
						// - pos1)+","+tmp);
						adc = restrict_max_min_yb(adc);
						adcArray[i] = (int) adc;
						pixArray[i] = yb - ((int) adc << 1);
					}
					p1++;
					i++;
				}
			}
		}
			break;
		}
		pixbuf.position(0);
		pixbuf.limit(l);
		adcbuf.position(0);
		adcbuf.limit(l);
	}

	final int Max = IDataMaxMin.Max_8bit;
	final int Min = IDataMaxMin.Min_8bit;

	private double restrict_max_min_yb(double adc) {
		/** yloc是基于屏幕中心，adc基于yloc，padc基于屏幕中心 */
		double padc = yloc + adc;
		padc = restrict_max_min(padc);

		return padc - yloc;
	}

	private double restrict_max_min(double adc) {
		if (adc < Min) {
			adc = Min;
		} else if (adc > Max) {
			adc = Max;
		}
		return adc;
	}

	public IntBuffer save2RefIntBuffer() {
		int[] src = adcbuf.array();
		int limit = adcbuf.limit();
		int pos = adcbuf.position();

		IntBuffer adc_buf = IntBuffer.allocate(limit - pos);
		int[] dest = adc_buf.array();
		int j = 0;

		for (int i = pos; i < limit; i++, j++) {
			dest[j] = yloc + src[i];
		}

		adc_buf.limit(j);
		adc_buf.position(0);
		return adc_buf;
	}

	public void resetIntBuf(ScreenContext pc) {
		resetIntBuf(pc.isScreenMode_3(), pc.getHcenter());
	}

	public void prepare() {
		if (pixbuf == null)
			pixbuf = IntBuffer.allocate(5000);
		if (adcbuf == null)
			adcbuf = IntBuffer.allocate(5000);
	}

	public void resetIntBuf(boolean ScreenMode_3, int hc) {
		prepare();

		WF ch1 = wfm.getM1(), ch2 = wfm.getM2();
		int vb1 = ch1.getVoltbaseIndex(), vb2 = ch2.getVoltbaseIndex();
		mc.updateMathVbIdx(vb1, vb2);
		// if (true) {
		complexCompute(ch1, ch2, ScreenMode_3, hc);
		// } else {
		// computeWithoutPosy0(ch1, ch2, ScreenMode_3, hc);
		// }
	}

	@Override
	public void adjustView(ScreenContext pc, Rectangle bound) {
		if (!mc.mathon)
			return;

		/** RT时直接等下一幅，后续可以再作加锁方式的各种支持 */
		if (!pc.allowLazyRepaint()) {
			resetIntBuf(pc);
		}
	}

	@Override
	public void paintView(Graphics2D g2d, ScreenContext pc, Rectangle r) {
		if (!(wfm.isM1M2Support() && mc.mathon))
			return;

		if (pixbuf == null)
			return;
		// System.err.println("paintView");
		g2d.setColor(co);

		int xb = r.x;
		// WaveForm wf = wfm.getM1();
		boolean linkline = wfm.getDataHouse().isLineLink();
		Shape tmp = g2d.getClip();
		g2d.setClip(r);

		WFTimeScopeControl wftsc = wfm.getWFTimeScopeControl();
		boolean pk_detect = wftsc.isPK_Detect();

		// 运行时后续再处理
		// RTLocInfoManager rtlim = wf.getRTLocInfoManager();
		int dm = wftsc.getDrawMode();
		xb = wftsc.computeXoffset(xb, dm);

		pc.getIPaintOne().paintONE(g2d, dm, xb, linkline, r.y, r.height,
				pk_detect, wftsc, pixbuf, wftsc.getPK_detect_type());

		g2d.setClip(tmp);
	}

}