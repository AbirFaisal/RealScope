package com.owon.uppersoft.dso.wf.common.dm;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.view.StorageView;
import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.wf.dm.DMDataInfo;
import com.owon.uppersoft.vds.data.Range;
import com.owon.uppersoft.vds.util.PrimaryTypeUtil;

/**
 * KNOW
 * 
 * CompressInfoUnit algorithm notes (abandoned):
 * 
 * Analysis of the 4 result-set cases: 2.5 (special); compression spans more than one screen and needs segmented compression; compression ratio fits one screen, compressed once; compression into one point
 * 
 * 
 * 
 * Aspect 1: to support center-point zoom, the position of the center point in the memory depth must be known,
 * 
 * held directly here; screen left/right boundaries and actual drawing pixels are computed only at compression time
 * 
 * 
 * Aspect 2: blank screen edges always appear when reaching the data head or tail, so limiting is needed, and usually computing comes before limiting
 * 
 * but computing first may overflow int for large memory depths
 * 
 * 
 * Aspect 3: switching between sample points and pixel points is always needed, to ensure drawing and compression point picking
 * 
 * 
 * Aspect 4: from aspects 2 and 3, consider using two assumptions,
 * 
 * first, array bounds are not limited, i.e. an array index is treated as possibly negative or beyond the memory depth, and is limited only in the last step of compression,
 * 
 * which saves what has to be considered in zooming and panning
 * 
 * second, build virtual points (ideal virtual pixel points) on top of the actual array index points, i.e. the index points of the compressed virtual array, likewise without array bounds,
 * 
 * the compressed values become smaller and do not overflow, and the compressed virtual point index can map to pixel points (or virtual ones), which makes it convenient to compute the indent, i.e. the horizontal trigger position
 * 
 * 
 * Aspect 5: the 2.5 case uses the compression ratio of aspect 5, each two pixels correspond to one virtual point index, a workaround for aspect 4,
 * 
 * this produces possible panning deviation, it cannot always map to the center point, and needs handling
 * 
 * 
 * Aspect 6: the start point is not necessarily an integer multiple of the compression ratio, so the center point is not necessarily one either. The drawing effect should, after entering this timebase,
 * 
 * be consistent with run time (i.e. compress from the start point),
 * 
 * and also stay consistent after horizontal moves (i.e. whichever point compression by the ratio starts from, that point always maps correctly),
 * 
 * and, based on the design of aspect 5, consider truncating extra head and tail data, filling it into the array in the compression step, and into the indent and remainder-point drawing in the drawing step
 * 
 * (or compress the extra head and tail data when insufficient)
 * 
 * 
 * Aspect 7: load handoff, i.e. the initial read state has the compression content preset,
 * 
 * the initial read considers head/tail remainder points and drawing indent, and sets all states to valid values, to smoothly enter panning and zooming
 * 
 * 
 * Aspect 8: timebase switch handoff; considering a switch may come from the other of the stretch/compress states, all states need valid values set like in load handoff
 * 
 * 
 * Aspect 9: each compression reads directly from the file; after panning and zooming, determine the pixel value corresponding to the center point, extend to the left/right boundaries, and compress again,
 * 
 * incremental compression and temp file caching of the whole compressed point set were not implemented as optimizations; performance is acceptable
 * 
 * 
 * Aspect 10: start points are mostly integer multiples; not measured, and slow sweep not considered
 * 
 * 
 * Characteristics: large data volume, many cases, complex cases such as virtual points, boundaries and 2.5, and much switching logic
 * 
 * 
 * 
 * 
 * CompressInfoUnit algorithm notes (updated version):
 * 
 * Abstract out a virtual pixel cluster, managing actual pixel point offset + actual pixel point compression + actual pixel point drawing
 * 
 * Use scpi (the data point index corresponding to a point relative to the screen center), and the auxiliary cpoff (offset of scpi from the screen center point), passed as parameters to different virtual pixel clusters on timebase switch
 * 
 * When switching between compress and stretch, always convert internally and pass in the scpi of the screen center point for the 1:1 case
 * 
 * 
 * 
 * Later considered it can be improved like this: at one go, get at this scale the number of virtual points that can be fully compressed on the left and right, and the corresponding actual point boundaries
 * 
 * @author Matt
 * 
 */

public class CompressInfoUnit implements InfoUnit {
	private IntBuffer pixbuf;
	private ByteBuffer b_adcbuf;

	private LocInfo li;

	private Range ap;
	private VirtualPointHandler vph;
	private VirtualPoint vp;
	/**
	 * Screen Center Point Index, the index of the real data point in memory depth; this data point is very close to the screen center
	 */
	private int scpi;
	/**
	 * cpoff is the pixel difference between scpi and the center, -...+ (-1, 0, 1), nonzero only during panning
	 */
	private int cpoff;
	/**
	 * x coordinate of the first drawing point from the left of the screen
	 */
	private int xoffset;

	private int pixs, halfpixs, datalen, filePointer, initPos, screendatalen,
			databeg;

	public CompressInfoUnit() {
	}

	private void prepare(LocInfo li) {
		this.li = li;
		pixs = li.pixs;
		halfpixs = li.halfpixs;
		datalen = li.datalen;
		filePointer = li.filePointer;
		// position of the data point at the screen start pixel
		initPos = li.initPos;
		screendatalen = li.screendatalen;

		if (li.wasSlowMove) {
			// still usable when transfer is optimized
			databeg = datalen - li.slowmove;
		} else {
			databeg = 0;
		}

		ap = new Range();
		vph = new VirtualPointHandler(li.getDeepMemoryStorage(), filePointer, datalen);

		prepareBuffer();

		VirtualPoint.prepare(li.getMachineInfo().DEEPValue);
	}

	private void prepareBuffer() {
		if (b_adcbuf == null) {
			b_adcbuf = ByteBuffer.allocate((GDefine.AREA_WIDTH << 2) + 100)
					.order(ByteOrder.BIG_ENDIAN);

			pixbuf = IntBuffer.allocate((GDefine.AREA_WIDTH << 2) + 100);
			pixbuf.position(0);
			pixbuf.limit(0);
		}
	}

	private void logln(String txt) {
		System.err.println(txt);
	}

	public void initLoad(LocInfo lci, DMDataInfo di) {
		prepare(lci);

		// In the 1k case, use the trigger pull offset parameter
		initPos += di.pi.pluggedTrgOffset;

		/**
		 * 
		 * Split the full-screen count in half to determine the center point; if it cannot be split evenly, split it anyway, so the timebase can zoom
		 * 
		 * In slow sweep, if the data is full dm data, only scrbeg needs to change; otherwise when only valid data is transferred,
		 * 
		 * initPos may not be the data point count corresponding to the screen start, so screendatalen-slowmove must be considered,
		 * 
		 * or adjust initPos, screendatalen
		 * 
		 * In slow sweep, transfer the full data
		 * 
		 * scpi initially points at the exact screen center; during panning the data point position closest to the screen center is still used
		 */
		scpi = initPos + (screendatalen >> 1);
		vp = VirtualPoint.getInstance(screendatalen, pixs);
		if (vp.sample <= 0)
			logln("VirtualPoint.getInstance(screendatalen, pixs).sample == 0!!!");
		cpoff = 0;
		bufcpoff = 0;
		// Handle the case of compressing into one screen separately
		computeXOffset();
	}

	protected void computeXOffset() {
		xoffset = vph.compressExecute(vp, 0, pixs, halfpixs + cpoff, scpi,
				b_adcbuf, databeg, datalen, ap, isCompress1Screen(),
				li.shouldReverse, li.loadPos0);

		confirmADCBuffer(b_adcbuf);
	}

	/**
	 * Position of the horizontal trigger
	 * 
	 * @param hortrgidx
	 * @return +...-
	 */
	private int getHorTrgPos(int hortrgidx) {
		// here, because of panning under compression to one screen, cpoff was changed, so errors occur
		int v = -cpoff - vp.getOffsetBetween(scpi - hortrgidx) + bufcpoff;
		// System.out.println(String.format("%d,%d,%d,%d", scpi, cpoff,
		// hortrgidx,
		// v));
		return v;
	}

	public VirtualPointHandler getVirtualPointHandler() {
		return vph;
	}

	public void release() {
		VirtualPoint.release();
		b_adcbuf = null;
	}

	public void save2RefIntBuffer(IntBuffer adcbuf, int delpos0) {
		ByteBuffer buf = this.b_adcbuf;
		byte[] src = buf.array();
		int limit = buf.limit();
		int pos = buf.position();
		for (int i = pos; i < limit; i++) {
			adcbuf.put(src[i] - delpos0);
		}
		adcbuf.position(0);
		adcbuf.limit(limit - pos);
	}

	private boolean isCompress1Screen() {
		return vp.isCompress1Screen(li.fsrate);
	}

	/**
	 * Algorithm specific: maps one-to-one to pixels, no points to draw off-screen, note that lines are drawn at the start point rather than the end point, incremental compression  -...+
	 * 
	 * @param m
	 */
	public void computeMove(int m) {
		if (isCompress1Screen()) {
			xoffset += m;
			bufcpoff += m;
			return;
		}

		// System.out.println(String.format("%d, %d, %d", m, scpi, cpoff));
		if (vp.pixel == 1) {
			scpi -= m * vp.sample;
		} else {
			int pn = vp.pixel;
			int spl = vp.sample;

			// -...+, when positive scpi gets smaller
			scpi = scpi - m / pn * spl;
			int rem = m % pn;
			cpoff += rem;

			// System.out.println(String.format("%d, %d", scpi, cpoff));
			// trim the compressible points in cpoff again
			int num, tc = cpoff;
			if (cpoff > 0) {
				num = tc / pn;
				scpi -= num * spl;
				cpoff -= num * pn;
			} else {
				tc = -tc;
				num = tc / pn;
				scpi += num * spl;
				cpoff = -(tc - num * pn);
			}
		}

		// System.out.println(String.format("%d, %d\n", scpi, cpoff));
		if (b_adcbuf == null)
			logln("CompressInfoUnit.b_adcbuf==null");

		computeXOffset();
	}

	public void resetDMIntBuf(BigDecimal vbmulti, int yb, boolean screenMode_3) {
		boolean b = PrimaryTypeUtil.canHoldAsInt(vbmulti);

		if (b) {
			VirtualPointHandler.transcriptionInt(b_adcbuf, pixbuf, vbmulti, yb, screenMode_3);
		} else {
			VirtualPointHandler
					.transcriptionDouble(b_adcbuf, pixbuf, vbmulti, yb,
							screenMode_3);
		}
		// System.err.println("resetDM:"+b_adcbuf.remaining());
	}

	/**
	 * Buffered cpoff margin produced by panning when compressed to one screen
	 */
	private int bufcpoff = 0;

	/**
	 * Precondition of the algorithm: drawing compressed pixels always starts from a position at an integer multiple of the compression ratio
	 * 
	 * Since this may be an intermediate step, it contains no recompression or filling process
	 * 
	 * @param pre
	 * @param nex
	 * @return
	 */
	private void tbTranslate_(BigDecimal pre, BigDecimal nex) {
		BigDecimal bi = nex.divide(pre);
		BigDecimal nexrate = bi.multiply(vp.getRate());
		BigDecimal dbnexfsl = nexrate.multiply(BigDecimal.valueOf(pixs));

		VirtualPoint ov = vp;
		// System.out.println(String.format("%d, %d", scpi, cpoff));
		scpi -= ov.getDilute2End(cpoff + bufcpoff);
		bufcpoff = 0;
		// System.out.println(String.format("%d, %d\n", scpi, cpoff));
		cpoff = 0;

		if (!PrimaryTypeUtil.canHoldAsInt(dbnexfsl)) {
			/**
			 * The full-screen count is too large to fit in an int; it must be 1:4 compression then, so use another way to directly construct vp
			 */
			vp = VirtualPoint.getInstance(nexrate);
		} else {
			int nexfsl = dbnexfsl.intValue();
			vp = VirtualPoint.getInstance(nexfsl, pixs);
		}

		int rate = vp.sample;
		if (rate == 0)
			logln("v = getMultiply(screendatalen, pixs) == 0!!!");
	}

	public void tbTranslate(BigDecimal pre, BigDecimal nex, int tbidx) {
		tbTranslate_(pre, nex);
		computeXOffset();
	}

	public void tbTranslate2D(BigDecimal pre, BigDecimal nex,
			IDiluteInfoUnit diu, int tbidx) {
		BigDecimal nex1_1 = null;
		nex1_1 = pre.divide(vp.getRate());
		/** First switch to the C1:1 compression case, then switch to D */
		tbTranslate_(pre, nex1_1);
		// TODO even via this relay, 1:1 is still compressed; needs optimization

		release();

		diu.fakeIn(scpi, tbidx);
		diu.tbTranslate(nex1_1, nex, tbidx);
	}

	@Override
	public void confirmADCBuffer(ByteBuffer bbuf) {
	}

	@Override
	public ByteBuffer getb_adcbuf() {
		return b_adcbuf;
	}

	/**
	 * 1:1
	 * 
	 * @param cpidx
	 * @param rate
	 */
	public void fakeIn(int cpidx, int rate, LocInfo li, int cpoff) {
		prepare(li);
		scpi = cpidx;

		vp = VirtualPoint.getInstance(pixs, pixs);
		this.cpoff = cpoff;
	}

	public void drawView(Graphics g, int xoff, int hh, int wlen, int hortrgidx) {
		Graphics2D g2d = (Graphics2D) g;
		int et = StorageView.edgetall, et2 = et << 1;
		int dl = datalen;// hp = halfpixs,
		/** All points of the memory depth are drawn onto ratelen pixels */
		if (vp == null)
			logln("ERR:CompressInfoUnit,VirtualPoint==null");
		int ratelen = (dl / vp.sample);
		int x0, x1, v, wratelen, wx0, y0, y1;

		g2d.setColor(Color.LIGHT_GRAY);
		/** The display mode is changed to window movement instead of T position movement */
		if (isCompress1Screen()) {
			/** The bracket is stretched to full, and the horizontal line shrinks accordingly */
			/** Length of the overview area horizontal line */
			wratelen = (int) ((double) ratelen * wlen / pixs);
			/** wx0: start position of the horizontal line; wlen: length of the bracket; */
			wx0 = xoff + ((wlen - wratelen) >> 1);
			g2d.drawLine(wx0, hh, wratelen + wx0, hh);

			v = (int) ((double) hortrgidx * wratelen / dl + wx0);

			if (v <= xoff)
				v = xoff;
			else if (v >= xoff + wlen)
				v = xoff + wlen;

			int aw = GDefine.AREA_WIDTH;
			/** Bracket */
			x0 = v
					- (int) (((aw >> 1) - Platform.getDataHouse().controlManager
							.getTimeControl().getHorizontalTriggerPosition())
							/ (double) aw * wlen);
			x1 = x0 + wlen;

			g2d.drawLine(x0, hh - et, x0, hh + et);
			g2d.drawLine(x1, hh - et, x1, hh + et);

			/** T */
			g2d.setColor(Color.MAGENTA);

			y0 = hh - et;
			y1 = hh - et2;
			g2d.drawLine(v, y0, v, y1);
			g2d.drawLine(v - 1, y1 + 2, v - 1, y1);
			g2d.drawLine(v + 1, y1 + 2, v + 1, y1);
		} else {
			/** The horizontal line is stretched to full, and the bracket shrinks accordingly */
			int xw = (int) ((double) (pixs) * wlen / ratelen);
			x0 = (int) ((double) scpi * wlen / dl - (xw >> 1) + xoff);
			x1 = x0 + xw;

			// System.err.println(String.format("%d,%d,%d,%d,%d,%d", scpi, wlen,
			// dl,
			// ratelen, pixs, xoff));

			/** Bracket */
			g2d.drawLine(x0, hh - et, x0, hh + et);
			g2d.drawLine(x1, hh - et, x1, hh + et);

			/** Horizontal line */
			x0 = xoff;
			x1 = xoff + wlen;
			g2d.drawLine(x0, hh, x1, hh);

			/** T */
			g2d.setColor(Color.MAGENTA);
			v = (int) ((double) hortrgidx * wlen / dl + xoff);

			// System.err.println((double) hortrgidx * wlen / dl);

			if (v <= xoff)
				v = xoff;
			else if (v >= xoff + wlen)
				v = xoff + wlen;

			y0 = hh - et;
			y1 = hh - et2;
			g2d.drawLine(v, y0, v, y1);
			g2d.drawLine(v - 1, y1 + 2, v - 1, y1);
			g2d.drawLine(v + 1, y1 + 2, v + 1, y1);
		}

	}

	public double getGap() {
		return vp.getRFGap();
	}

	public int getXoffset() {
		return xoffset;
	}

	@Override
	public int getDrawMode() {
		return vp.getDrawMode();
	}

}