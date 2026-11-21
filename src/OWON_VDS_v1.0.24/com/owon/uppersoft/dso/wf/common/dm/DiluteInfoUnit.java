package com.owon.uppersoft.dso.wf.common.dm;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import com.owon.uppersoft.dso.util.DBG;
import com.owon.uppersoft.dso.view.StorageView;
import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.data.CByteArrayInputStream;
import com.owon.uppersoft.vds.core.machine.MachineType;
import com.owon.uppersoft.vds.core.paint.PaintContext;
import com.owon.uppersoft.vds.core.rt.IDataMaxMin;
import com.owon.uppersoft.vds.core.rt.WFDrawRTUtil;
import com.owon.uppersoft.vds.core.wf.dm.DMDataInfo;
import com.owon.uppersoft.vds.data.MultiplyorAndDivisor;
import com.owon.uppersoft.vds.util.PrimaryTypeUtil;

/**
 * 
 * KNOW refer to the algorithm notes of CompressInfoUnit, supplement:
 * 
 * frct(a:b) saves (distance between the center point and the previous sample point : stretch ratio) at different timebases; the switch keeps this ratio unchanged to fix the center point
 * 
 * 
 * Characteristics: small data volume, fast point picking, fixing the center point is the key when switching timebase
 * 
 * First determine the data boundary and x offset with virtual index points, then interpolate
 * 
 * Interpolation:
 * 
 * Interpolation is done only once, when receiving the deep memory file, inserting sine values when the full-screen count is <=200,
 * 
 * the possible full-screen counts are 200, 100, 50, 40, 25, 20, 10,
 * 
 * i.e. stretch ratios of 5, 10, 20, 25, 40, 50, 100,
 * 
 * the pixel spacing of points after interpolation is 1/2, 1, 2, 2.5, 4, 5, 10,
 * 
 * drawing is compressed, 1 to 1, stretched, stretched,
 * 
 * on this basis the stretched drawing is further linearly interpolated to 1 to 1
 * 
 * 
 * 
 * 
 * Processing can treat Pluger transparently, using its interpolated points only at the end when getting the drawing data points
 * 
 * There is no trigger pull and no interpolation in slow sweep
 * 
 */
public abstract class DiluteInfoUnit implements InfoUnit, IDataMaxMin,
		IDiluteInfoUnit {

	public static final int DEFINE_AREA_WIDTH = GDefine.AREA_WIDTH;

	/** re is 0: draw 3 first, otherwise draw 2 */
	public final int re = 0;

	/** Distance from the starting drawing point to the left boundary */
	private int xbloc;

	/** Index of the reference point */
	private int cpidx;
	/** Position of the reference point when the center point is 0 -...+ */
	private int cpoff;

	private LocInfo li;

	public ByteBuffer b_adcbuf;
	private IntBuffer pixbuf;
	private IntBuffer i_adcbuf;

	public int[] array;

	public DiluteInfoUnit(LocInfo li) {
		this.li = li;

		pg = createPluger(this);
	}

	protected abstract IPluger createPluger(DiluteInfoUnit di);

	public void setCenterPoint(int cpidx, int cpoff) {
		this.cpidx = cpidx;
		this.cpoff = cpoff;
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
		return xbloc;
	}

	protected int getXBloc() {
		return xbloc;
	}

	protected void setXBloc(int v) {
		xbloc = v;
	}

	protected void addXBloc(int del) {
		xbloc += del;
	}

	public int getCenterPointIndex() {
		return cpidx;
	}

	public void setCenterPointIndex(int v) {
		cpidx = v;
	}

	public int getCenterPointOffset() {
		return cpoff;
	}

	public void setCenterPointOffset(int v) {
		cpoff = v;
	}

	int tbidx;

	/**
	 * Position of the horizontal trigger
	 * 
	 * @param hortrgidx
	 * @return
	 */
	private int getHorTrgPos(int hortrgidx) {
		int del = hortrgidx - cpidx;
		int v = diluteR.multiplyByInt(del) + cpoff;

		// 2.5 MISS floating point values may be lost here
		switch (del % diluteR.getDivisor()) {
		case 1:
			v += 3;
			break;
		case -1:
			v -= 2;
			break;
		}

		return v;
	}

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
		resetSPIOFF();
	}

	/**
	 * Used to remember the cpoff of each timebase scale; not modified when no panning is done; filled only if not yet set
	 */
	private void resetSPIOFF() {
		spi_offs = new int[li.getMachineInfo().TIMEBASE.length];
		Arrays.fill(spi_offs, 0, spi_offs.length, invalidOff);
	}

	private void prepare() {
		if (i_adcbuf == null) {
			int capacity = 15000;// with trigger pull and interpolation, at most (20+50*2)*5*10 = 6000

			i_adcbuf = IntBuffer.allocate(capacity);
			array = i_adcbuf.array();

			b_adcbuf = ByteBuffer.allocate(capacity);

			pixbuf = IntBuffer.allocate(capacity);
			pixbuf.position(0);
			pixbuf.limit(0);
		}

		// System.err.println(li.wasSlowMove);
		if (li.wasSlowMove) {
			databeg = li.datalen - li.slowmove;
		} else {
			databeg = 0;
		}
	}

	private final static int invalidOff = Integer.MAX_VALUE;
	private int[] spi_offs;

	public void initLoad(DMDataInfo cdi, int tbidx, MachineType mt) {
		prepare();
		this.tbidx = tbidx;

		/** When deep memory is loaded at a timebase that needs no interpolation, no interpolation is done afterward either */
		int pixs = li.pixs;
		screendatalen = li.screendatalen;
		pg.initPlugInfo(cdi, mt);
		/** Theoretical full-screen count <= screen pixel width, stretch */
		/** Stretch ratio */
		// System.out.println("cpidx: " + cpidx);
		// reDilute();
		if (pg.isPlug()) {
			IntBuffer handlebuf = IntBuffer.wrap(i_adcbuf.array());
			int filePointer = li.filePointer;
			CByteArrayInputStream cba = li.getDeepMemoryStorage();
			boolean shouldReverse = li.shouldReverse;
			int loadPos0 = li.loadPos0;
			int datalen = li.datalen;

			pg.processPlugForInit(datalen, screendatalen, filePointer,
					shouldReverse, loadPos0, handlebuf, cba, li.halfpixs);

			/** The full-screen point count is used here, assuming interpolation is invisible to Dilute */
			diluteR.set(pixs, screendatalen);
			computeCenterPointStuff(cpoff);
			// spi_offs[tbidx] = getCenterPointOffset();
			xbloc = 0;

			loadcp();

			int pos = handlebuf.position();
			int limit = handlebuf.limit();

			i_adcbuf.position(pos);
			i_adcbuf.limit(limit);

			lastHandle(i_adcbuf);
		} else {
		}
	}

	public void loadcp() {
		// System.err.println("idx: " + cpidx + ", off: " + cpoff);
	}

	protected void computeCenterPointStuff(int cpoff_new) {
		int nxo = cpoff_new;
		boolean right = true;
		if (nxo < 0) {// -...
			right = false;
			nxo = -nxo;
		}

		int num = diluteR.divideDirectByInt(nxo);
		int re = diluteR.modeByIntOffset(nxo);

		int cpoff;
		int cpidx = this.cpidx;
		if (right) {
			cpidx -= num;
			cpoff = re;
		} else {
			cpidx += num;
			cpoff = -re;
		}
		this.cpidx = cpidx;
		this.cpoff = cpoff;
		// System.err.println("cpidx: " + cpidx);
	}

	/**
	 * Algorithm characteristics: virtual points and pixels map one-to-one  -...+
	 * 
	 * @param m
	 */
	public void computeMove(int m) {
		resetSPIOFF();
		/**
		 * New position of the reference point relative to the center point
		 */
		int nxo = cpoff + m;
		computeCenterPointStuff(nxo);
		reDilute();
	}

	private MultiplyorAndDivisor diluteR = new MultiplyorAndDivisor();
	private IPluger pg;

	public IPluger getPluger() {
		return pg;
	}

	private void rebuf(int iPos, int len, IntBuffer i_adcbuf) {
		IntBuffer handlebuf = IntBuffer.wrap(i_adcbuf.array());

		// System.err.println(String.format("iPos:%d, len:%d, rate:%d", iPos,
		// len, rate));

		if (pg.isPlug()) {
			int filePointer = li.filePointer;
			CByteArrayInputStream cba = li.getDeepMemoryStorage();
			boolean shouldReverse = li.shouldReverse;
			int loadPos0 = li.loadPos0;
			int datalen = li.datalen;

			pg.processPlug(iPos, len, datalen, screendatalen, filePointer,
					shouldReverse, loadPos0, handlebuf, cba);
		} else {
			int filePointer = li.filePointer;
			CByteArrayInputStream cba = li.getDeepMemoryStorage();
			boolean shouldReverse = li.shouldReverse;
			int loadPos0 = li.loadPos0;

			// No interpolation, when the initial scale is not stretched
			retrieveBuf(iPos, 0, len, handlebuf, filePointer, cba);
			restrictForReverse(handlebuf, shouldReverse, loadPos0);
		}

		int pos = handlebuf.position();
		int limit = handlebuf.limit();

		/** pos limit: set limit first, to prevent an error when pos exceeds the old limit */
		i_adcbuf.limit(limit);
		i_adcbuf.position(pos);

		lastHandle(i_adcbuf);
	}

	public void retrieveBuf(int iPos, int beg, int len, IntBuffer handlebuf,
			int filePointer, CByteArrayInputStream cba) {
		int[] array = handlebuf.array();
		cba.reset(filePointer + iPos);
		cba.getByteAsIntArray(array, beg, len);
		handlebuf.position(0);
		handlebuf.limit(len);
	}

	public void retrieveBuf(byte[] barr, int iPos, int beg, int len) {
		/** len: real adc point count plus 100 trigger pull points on each side; beg: position corresponding to htp in the deep memory data */
		CByteArrayInputStream cba = li.getDeepMemoryStorage();
		cba.reset(li.filePointer + iPos);
		cba.get(barr, beg, len);
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
		// System.err.println("reDilute");
		// PaintContext pc = Platform.getMainWindow().getChartScreen()
		// .getPaintContext();
		/** Start and midpoint of the screen */
		final int pw = DEFINE_AREA_WIDTH, p0 = 0, wc = PaintContext.hwidth;

		/** Start index of the data */
		/** i0, i1 are unbounded point indices, not limited by the specific zero value and memory depth; x1 is the undrawable right boundary */
		final int cpidx = this.cpidx;
		// System.err.println("cpidx: " + cpidx);
		i1 = i0 = cpidx;
		loadcp();
		/** cpidx x coordinate, starting from the far left, -...+ */
		final int cpx = wc + cpoff;
		// ...

		if (cpx < pw) {
			// i1 extends to the right screen boundary
			int range = pw - cpx;
			int num = diluteR.divideDirectByInt(range);
			i1 = cpidx + num;

			// if (range % diluteR.getMultiplyor() != 0)
			i1 += diluteR.getDivisor();
		}
		if (cpx > p0) {
			// i0 extends to the left screen boundary; on initial load xbloc is also set as a result
			int range = cpx - p0;
			int num = diluteR.divideDirectByInt(range);
			int rem = diluteR.modeByIntOffset(range);
			i0 = cpidx - num;
			int xb = p0;
			// if (rem != 0) {
			i0 -= diluteR.getDivisor();
			xb += rem - diluteR.getMultiplyor();
			// }
			xbloc = xb;
		}

		loadcp();
		restrictAfterDilute();
	}

	/**
	 * Can be used by initLoad in place of reDilute, supports interpolation for slow sweep and trigger pull
	 */
	private void restrictAfterDilute() {
		// System.err.println("restrictAfterDilute");
		// System.err.println("i(0,1):" + i0 + ", " + i1);
		// i1 += 10;// pad a bit more at the screen end

		int datalen = li.datalen;
		int data0 = databeg;

		// limit back to the data boundary again
		if (i1 > datalen)
			i1 = datalen;

		// limit back to the data boundary again
		if (i0 < data0) {
			/** MISS floating point values may be lost here */
			addXBloc(diluteR.multiplyByInt(data0 - i0));
			i0 = data0;
		}
		// System.err.println("xbloc: " + xbloc);

		int len = i1 - i0;
		// System.out.println("i0: " + i0 + ", i1:" + i1 + ", len:" + len);
		/** i0 lower limit and i1 upper limit, taken together, limit the case of being completely off-screen */
		if (len > 0) {
			if (i0 + len < datalen)
				len++;

			rebuf(i0, len, i_adcbuf);
		} else {
			i_adcbuf.position(0);
			i_adcbuf.limit(0);
			lastHandle(i_adcbuf);
		}
		// DBG.outprintln("i_adcbuf_len:" + i_adcbuf.remaining());
	}

	/**
	 * Adjustments for inversion
	 * 
	 * @param pos
	 * @param limit
	 */
	public static final void restrictForReverse(IntBuffer i_adcbuf,
			boolean reverse, int loadPos0) {
		/** Hand over to the drawing screen, to handle the inversion issue */
		reverse = false;

		/** Do the necessary inversion processing before the adc data is taken out for drawing */
		/** Internally this also converts intArray to byteArray here, so both can be done at once */
		if (reverse) {
			int pos = i_adcbuf.position();
			int limit = i_adcbuf.limit();

			int[] array = i_adcbuf.array();
			final int dpos0 = loadPos0 << 1;
			for (int i = pos; i < limit; i++) {
				array[i] = dpos0 - array[i];
			}
		}
	}

	public boolean BeyondMax = false, BeyondMin = false;

	/** KNOW limit adc data points not to cross the boundary, store from i_adcbuf into b_adcbuf */
	private void lastHandle(IntBuffer i_adcbuf) {
		int pos = i_adcbuf.position();
		int limit = i_adcbuf.limit();

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
		// DBG.configArray(barr, 0, j);
		confirmADCBuffer(b_adcbuf);
	}

	private void tbTranslate_(BigDecimal pre, BigDecimal nex, int tb) {
		// rate, cpoff both change
		MultiplyorAndDivisor md = MultiplyorAndDivisor.getFrom1_2_5TypeDivison(
				nex, pre);

		/** Inversely proportional, but decimal value loss can still occur */
		/** * preRate:nextRate=nextTb:preTb */
		MultiplyorAndDivisor nextRate = diluteR.divide_MD(md);
		/** * nextSdlen:preSdlen=nextTb:preTb */
		screendatalen = md.multiplyByInt(screendatalen);
		int cpoff;

		cpoff = this.cpoff;

		/** Save the current scale's cpoff */
		spi_offs[tbidx] = cpoff;

		// System.err.println("cur:" + tbidx + " tb:" + tb);
		if (spi_offs[tb] == invalidOff) {
			/** If the scale to switch to is not set, compute it from this scale */
			// System.err.println("invalid: " + cpoff);
			/** MISS floating point values may be lost here */
			cpoff = (int) nextRate.multiplyByDouble(diluteR
					.divideByDouble(cpoff));

			// System.err.println("to: " + cpoff);
		} else {
			/** If the scale to switch to is already set, use it directly */
			cpoff = spi_offs[tb];
			// System.err.println("valid: " + cpoff);
		}
		tbidx = tb;
		this.cpoff = cpoff;

		diluteR = nextRate;
	}

	/** Stretch to stretch */
	public void tbTranslate(BigDecimal pre, BigDecimal nex, int tbidx) {
		tbTranslate_(pre, nex, tbidx);

		reDilute();
	}

	/** Stretch to compress */
	public void tbTranslate2C(BigDecimal pre, BigDecimal nex,
			CompressInfoUnit ciu, int tbidx) {
		// rate / nex1_1rate= nex1_1/ pre
		// nex1_1rate == 1

		BigDecimal nex1_1 = diluteR.multiplyByBD(pre);
		/** First switch to the D1:1 compression case, then switch to C */
		tbTranslate_(pre, nex1_1, tbidx);

		release();

		// cpoff need not be passed out
		ciu.fakeIn(cpidx, 1, li, 0);
		ciu.tbTranslate(nex1_1, nex, tbidx);
	}

	public void fakeIn(int cpidx, int tbidx) {
		/** fake in case: check whether the one loaded at the start is Dilute */
		prepare();

		screendatalen = li.pixs;
		this.cpidx = cpidx;
		diluteR = new MultiplyorAndDivisor(1, 1);

		/** If the scale to switch to is not set, assign 0 */
		if (spi_offs[tbidx] == invalidOff) {
// setCenterPointOffset(spi_offs[tbidx]);
			cpoff = 0;
		}
		xbloc = 0;
	}

	public void drawView(Graphics g, int xoff, int hh, int wlen, int hortrgidx) {
		Graphics2D g2d = (Graphics2D) g;
		int et = StorageView.edgetall;// bracket height
		int x0, x1, y0, y1;
		double r = diluteR.getDoubleValue();

		/** The horizontal line is stretched to full, and the bracket shrinks accordingly */
		/** ?? (i0 / (double) li.datalen) - xbloc / (li.datalen * r) */
		x0 = (int) (wlen
				* ((i0 / (double) li.datalen) - xbloc / (li.datalen * r)) + xoff);
		x1 = x0 + (int) (wlen * li.pixs / (li.datalen * r));

		g2d.setColor(Color.LIGHT_GRAY);

		// System.err.println(x0 + ", " + x1);
		/** Bracket */
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
		DBG.outprintln(",xbloc: " + xbloc);
		DBG.outprintln(",xoff: " + xoff);
	}

	public static final int PlugMode_None = 0, PlugMode_Compress = 1,
			PlugMode_Dilute = 2;

	private int screendatalen;

	public int getScreendatalen() {
		return screendatalen;
	}

	@Override
	public int getDrawMode() {
		int screendatalen = this.screendatalen;
		// System.err.println(screendatalen);

		// if (pg.canPaintSinePlug(screendatalen)) {
		// if (screendatalen == 250)
		// return pg.getDrawModeAsEachPlug();
		// else
		// return WFDrawRTUtil.DrawMode1p;
		// } else {
		// return WFDrawRTUtil.DrawModeDilute;
		// }

		if (pg.isPlug()) {
			return pg.getDrawModeFromLength(screendatalen);
		} else {
			return WFDrawRTUtil.DrawModeDilute;
		}
	}

	public double getGap() {
		int screendatalen = this.screendatalen;

		if (pg.canPaintSinePlug(screendatalen)) {
			return pg.getGap();
		} else
			return diluteR.getDoubleValue();
	}

}