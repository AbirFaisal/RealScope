package com.owon.uppersoft.dso.wf.dm2;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.owon.uppersoft.dso.util.DBG;
import com.owon.uppersoft.dso.view.StorageView;
import com.owon.uppersoft.dso.wf.common.dm.CompressInfoUnit;
import com.owon.uppersoft.dso.wf.common.dm.IDiluteInfoUnit;
import com.owon.uppersoft.dso.wf.common.dm.InfoUnit;
import com.owon.uppersoft.dso.wf.common.dm.LocInfo;
import com.owon.uppersoft.vds.core.aspect.base.VLog;
import com.owon.uppersoft.vds.core.data.CByteArrayInputStream;
import com.owon.uppersoft.vds.core.machine.MachineType;
import com.owon.uppersoft.vds.core.rt.IDataMaxMin;
import com.owon.uppersoft.vds.core.rt.WFDrawRTUtil;
import com.owon.uppersoft.vds.core.wf.dm.DMDataInfo;
import com.owon.uppersoft.vds.data.MultiplyorAndDivisor;
import com.owon.uppersoft.vds.data.Point;
import com.owon.uppersoft.vds.util.BufferHandleUtil;
import com.owon.uppersoft.vds.util.PrimaryTypeUtil;

public class DiluteInfoUnitTiny2 implements InfoUnit, IDataMaxMin,
		IDiluteInfoUnit {
	public void setCenterPoint(int cpidx, int cpoff) {
		cpi.setCenterPoint(cpidx, cpoff);
	}

	private LocInfo li;

	private ByteBuffer b_adcbuf;
	private IntBuffer pixbuf;
	private IntBuffer i_adcbuf;

	public int[] array;

	public DiluteInfoUnitTiny2(LocInfo li) {
		this.li = li;

		pg = new Pluger2(this);
	}

	public void setPlug(boolean b) {
		pg.setPlug(b);
	}

	@Override
	public int getMax() {
		return Max_8bit;
	}

	@Override
	public int getMin() {
		return Min_8bit;
	}

	public int getXoffset() {
		return cpi.getXBloc();
	}

	int tbidx;

	public void release() {
		i_adcbuf = null;
		array = null;
	}

	@Override
	public void confirmADCBuffer(ByteBuffer bbuf) {
	}

	public void save2RefIntBuffer(IntBuffer adcbuf, int delpos0) {
		IntBuffer buf = this.i_adcbuf;
		int[] src = buf.array();
		int limit = buf.limit();
		int pos = buf.position();
		for (int i = pos; i < limit; i++) {
			adcbuf.put(src[i] - delpos0);
		}
		adcbuf.position(0);
		adcbuf.limit(limit - pos);
	}

	public void reinit() {
	}

	private void prepare() {
		if (i_adcbuf == null) {
			int capacity = 50000;// when trigger pull and interpolating, at most (20+50*2)*5*10 = 6000

			i_adcbuf = IntBuffer.allocate(capacity);
			array = i_adcbuf.array();

			b_adcbuf = ByteBuffer.allocate(capacity);

			pixbuf = IntBuffer.allocate(capacity);
			pixbuf.position(0);
			pixbuf.limit(0);
		}

		if (li.wasSlowMove) {
			databeg = li.datalen - li.slowmove;
		} else {
			databeg = 0;
		}
		cpi.setDatabegin(databeg);
	}

	public void initLoad(DMDataInfo cdi, int tbidx, MachineType mt) {
		prepare();
		this.tbidx = tbidx;

		/** When loading deep memory, if the timebase does not need interpolation, no interpolation is done afterwards either */
		int pixs = li.pixs;
		screendatalen = li.screendatalen;
		pg.initPlugInfo(cdi, mt);
		/** Theoretical full-screen count <= screen pixel width: stretch */
		/** Stretch ratio */
		// System.out.println("cpidx: " + cpidx);
		if (!pg.isPlug()) {
			System.err.println("err DiluteInfoUnitTiny initLoad !pg.isPlug()");
			return;
		}
		/** Only the case of interpolating for trigger pull goes through initLoad */
		IntBuffer handlebuf = IntBuffer.wrap(i_adcbuf.array());

		pg.processPlugForInit(handlebuf);

		/** The full-screen point count is used here, assuming interpolation is invisible to Dilute */
		diluteR.set(pixs, screendatalen);
		// cpi.computeCenterPointStuff(diluteR);
		// pg.setPlug(false);

		/** This method could be replaced by the commented-out one below, but this method lets us see whether the trigger-drag info is converted correctly into the internal coordinate system */
		// reDilute();
		int pos = handlebuf.position();
		int limit = handlebuf.limit();
		i_adcbuf.position(pos);
		i_adcbuf.limit(limit);
		lastHandle(i_adcbuf);
	}

	private CByteArrayInputStream getDeepMemoryStorage() {
		return li.getDeepMemoryStorage();
	}

	/**
	 * Algorithm feature: virtual points and pixels correspond one to one -...+
	 * 
	 * @param m
	 */
	public void computeMove(int m) {
		/**
		 * New position of the reference point relative to the center point
		 */
		cpi.addCenterPointOffset(m);
		cpi.computeCenterPointStuff(diluteR);

		if (pg.isPlug()) {
			pg.computeMove(m);
			pg.translate2ScreenDatalen(screendatalen, i_adcbuf, cpi);
			lastHandle(i_adcbuf);
		} else {
			reDilute();
		}
	}

	private MultiplyorAndDivisor diluteR = new MultiplyorAndDivisor();
	private Pluger2 pg;

	public Pluger2 getPluger() {
		return pg;
	}

	private void rebuf(int iPos, int len, IntBuffer i_adcbuf) {
		IntBuffer handlebuf = IntBuffer.wrap(i_adcbuf.array());

		int pos, limit;
		if (pg.isPlug()) {
			pg.translate2ScreenDatalen(screendatalen, handlebuf, cpi);
		} else {
			pg.setGap(li.pixs / (double) screendatalen);
			// No interpolation, when the initial scale is not a stretch
			retrieveBuf(iPos, 0, len, handlebuf);
		}
		pos = handlebuf.position();
		limit = handlebuf.limit();

		/** pos limitset limit first, to prevent pos exceeding the original limit and raising an error */
		i_adcbuf.limit(limit);
		i_adcbuf.position(pos);
		lastHandle(i_adcbuf);
	}

	public void retrieveBuf(int iPos, int beg, int len, IntBuffer handlebuf) {
		CByteArrayInputStream cba = getDeepMemoryStorage();
		int[] array = handlebuf.array();
		cba.reset(li.filePointer + iPos);
		cba.getByteAsIntArray(array, beg, len);
		handlebuf.position(0);
		handlebuf.limit(len);

		BufferHandleUtil.restrictForReverse(handlebuf, li.shouldReverse,
				li.loadPos0);
	}

	public void retrieveBuf(ByteBuffer buffer, int iPos) {
		/** len:real adc point count plus 100 trigger-drag points on each side; beg: position corresponding to htp in the deep memory data */
		CByteArrayInputStream cba = getDeepMemoryStorage();
		cba.reset(li.filePointer + iPos);
		cba.get(buffer);

		// ArrayLogger.outArray2Logable(vl, buffer.array(), buffer.position(),
		// buffer.remaining());

		BufferHandleUtil.restrictForReverse(buffer, li.shouldReverse,
				li.loadPos0);
	}

	public void resetDMIntBuf(BigDecimal vbmulti, int yb, boolean screenMode_3) {
		IntBuffer bbuf;
		IntBuffer intBuf;

		bbuf = i_adcbuf;
		intBuf = pixbuf;

		int p = bbuf.position();
		int l = bbuf.limit();

		int[] dma = bbuf.array();
		int[] ina = intBuf.array();
		int i = 0;

		// System.out.println("[d]: "+(l-p));
		// System.out.println("[Dilute]yb: " + yb + ", delpos0: " + delpos0);
		if (PrimaryTypeUtil.canHoldAsInt(vbmulti)) {
			int m = vbmulti.intValue();

			if (screenMode_3) {
				if (m == 1) {
					while (p < l) {
						ina[i] = yb - dma[p];
						p++;
						i++;
					}
				} else {
					while (p < l) {
						ina[i] = yb - dma[p] * m;
						p++;
						i++;
					}
				}
			} else {
				while (p < l) {
					ina[i] = yb - ((((int) dma[p]) << 1) * m);
					p++;
					i++;
				}
			}
		} else {
			double m = vbmulti.doubleValue();

			if (screenMode_3) {
				while (p < l) {
					ina[i] = yb - (int) (dma[p] * m);
					p++;
					i++;
				}
			} else {
				while (p < l) {
					ina[i] = yb - (int) ((((int) dma[p]) << 1) * m);
					p++;
					i++;
				}
			}
		}

		intBuf.position(0);
		intBuf.limit(i);
		// System.out.println("[i]: "+i);
	}

	private int databeg = 0;
	public int i0, i1;

	/**
	 * Determine cpidx, rate, cpoff; compute xbloc
	 */
	private void reDilute() {
		MultiplyorAndDivisor md = diluteR;
		Point p = cpi.reDilute2(databeg, li.datalen, md);
		i0 = p.x;
		i1 = p.y;
		// vl.logln("xbloc: " + cpi.getXBloc());
		/**
		 * Can be used by initLoad, replacing reDilute, supporting interpolation for slow sweep and trigger pull
		 */
		int len = i1 - i0;
		// vl.logln("i0: " + i0 + ", i1:" + i1 + ", len:" + len);
		/** i0's lower limit and i1's upper limit, together limit the case entirely outside the screen */

		rebuf(i0, len, i_adcbuf);
	}

	private VLog vl = new VLog();
	public boolean BeyondMax = false, BeyondMin = false;

	/** KNOW limit adc data points within bounds, storing from i_adcbuf into b_adcbuf */
	private void lastHandle(IntBuffer i_adcbuf) {
		int pos = i_adcbuf.position();
		int limit = i_adcbuf.limit();
		// ArrayLogger.outArray2Logable(vl, i_adcbuf.array(),
		// pos + cpi.getXBloc() + 450, 100);

		if (limit <= pos) {
			b_adcbuf.position(0);
			b_adcbuf.limit(0);
			return;
		}

		int[] array = i_adcbuf.array();

		BeyondMax = BeyondMin = false;
		byte[] barr = b_adcbuf.array();
		int j = 0, v;
		final byte max = (byte) Max_8bit, min = (byte) Min_8bit;
		for (int i = pos; i < limit; i++, j++) {
			v = array[i];
			if (v > max) {
				barr[j] = max;
				BeyondMax = true;
			} else if (v < min) {
				barr[j] = min;
				BeyondMin = true;
			} else {
				barr[j] = (byte) v;
			}
		}
		b_adcbuf.position(0);
		b_adcbuf.limit(j);
		confirmADCBuffer(b_adcbuf);
	}

	private void tbTranslate_(BigDecimal pre, BigDecimal nex, int tb) {
		MultiplyorAndDivisor md = MultiplyorAndDivisor.getFrom1_2_5TypeDivison(
				nex, pre);

		/** Inverse relationship, but fractional values are still lost */
		/** * preRate:nextRate=nextTb:preTb */
		MultiplyorAndDivisor nextRate = diluteR.divide_MD(md);
		/** * nextSdlen:preSdlen=nextTb:preTb */
		screendatalen = md.multiplyByInt(screendatalen);
		double cpoff = cpi.getCenterPointOffset();
		cpoff = md.divideByDouble(cpoff);

		tbidx = tb;
		cpi.setCenterPointOffset(cpoff);

		diluteR = nextRate;
	}

	/** Stretch to stretch */
	public void tbTranslate(BigDecimal pre, BigDecimal nex, int tbidx) {
		tbTranslate_(pre, nex, tbidx);

		/** Turning off interpolation here lets you see the non-interpolated effect when zooming other interpolating timebases */
		// pg.setPlug(false);
		reDilute();
	}

	/** Stretch to compression */
	public void tbTranslate2C(BigDecimal pre, BigDecimal nex,
			CompressInfoUnit ciu, int tbidx) {
		// rate / nex1_1rate= nex1_1/ pre
		// nex1_1rate == 1

		BigDecimal nex1_1 = diluteR.multiplyByBD(pre);
		/** first switch to the D1:1 compression case, then switch to C */
		tbTranslate_(pre, nex1_1, tbidx);
		if (pg.isPlug()) {
			pg.translate2ScreenDatalenWithoutLoad(screendatalen, cpi);
		}
		release();

		int cpindex = cpi.getCenterPointIndex();
		int cpoff = 0;
		if (pg.isPlug()) {
			cpindex = pg.ppi2CPI();
			cpoff = pg.remainPPI2CPI();

			if (cpoff > 0)
				cpindex++;
			System.out.println(cpindex + " () " + cpoff);
		}
		// cpoff need not be passed out
		ciu.fakeIn(cpindex, 1, li, cpoff);
		ciu.tbTranslate(nex1_1, nex, tbidx);
	}

	@Override
	public ByteBuffer getb_adcbuf() {
		return b_adcbuf;
	}

	public void fakeIn(int cpidx, int tbidx) {
		/** fake in time, check whether what was initially loaded is Dilute */
		prepare();

		screendatalen = li.pixs;
		cpi.setCenterPointIndex(cpidx);
		diluteR = new MultiplyorAndDivisor(1, 1);

		/** if the scale to switch to is not set, assign 0 */
		// if (spi_offs[tbidx] == invalidOff)
		// cpi.setCenterPointOffset(0);// setCenterPointOffset(spi_offs[tbidx]);
		cpi.setXBloc(0);
	}

	private CenterPointInfo2 cpi = new CenterPointInfo2();

	public void drawView(Graphics g, int xoff, int hh, int wlen, int hortrgidx) {
		Graphics2D g2d = (Graphics2D) g;
		int et = StorageView.edgetall;// bracket height
		int x0, x1, y0, y1;
		double r = diluteR.getDoubleValue();

		/** Horizontal line stretched to full, brackets shrink accordingly */
		/** ?? (i0 / (double) li.datalen) - xbloc / (li.datalen * r) */
		x0 = (int) (wlen
				* ((i0 / (double) li.datalen) - getXoffset() / (li.datalen * r)) + xoff);
		x1 = x0 + (int) (wlen * li.pixs / (li.datalen * r));

		g2d.setColor(Color.LIGHT_GRAY);

		/** Brackets */
		g2d.drawLine(x0, hh - et, x0, hh + et);// left bracket
		if (x1 != x0) {
			g2d.drawLine(x1, hh - et, x1, hh + et);// right bracket
		}

		/** Horizontal line */
		g2d.drawLine(xoff, hh, xoff + wlen, hh);

		/** T */
		g2d.setColor(Color.MAGENTA);
		int v = (int) ((double) wlen * hortrgidx / li.datalen + xoff);

		if (v <= xoff)
			v = xoff;
		else if (v >= xoff + wlen)
			v = xoff + wlen;

		y0 = hh - et;
		y1 = hh - (et << 1);
		g2d.drawLine(v, y0, v, y1);

		g2d.drawLine(v - 1, y1 + 2, v - 1, y1);
		g2d.drawLine(v + 1, y1 + 2, v + 1, y1);
	}

	/**
	 * @param x0
	 * @param wlen
	 * @param r
	 * @param xoff
	 * @Deprecated
	 */
	protected void printDrawViewArgs(int x0, int wlen, double r, int xoff) {
		DBG.outprintln("x0: " + x0);
		DBG.outprintln(",wlen: " + wlen);
		DBG.outprintln(",i0: " + i0);
		DBG.outprintln(",li.datalen: " + li.datalen);
		DBG.outprintln(",Xoffset: " + getXoffset());
		DBG.outprintln(",xoff: " + xoff);
	}

	private int screendatalen;

	@Override
	public int getDrawMode() {
		if (pg.isPlug()) {
			return pg.getDrawMode();
		} else {
			return WFDrawRTUtil.DrawModeDilute;
		}
	}

	public double getGap() {
		if (pg.isPlug()) {
			return pg.getGap();
		} else
			return diluteR.getDoubleValue();
	}

}