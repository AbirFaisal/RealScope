package com.owon.uppersoft.dso.wf.common.dm;

import java.awt.Graphics;
import java.math.BigDecimal;
import java.nio.ByteBuffer;

import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.data.CByteArrayInputStream;
import com.owon.uppersoft.vds.core.machine.MachineInfo;
import com.owon.uppersoft.vds.core.machine.MachineType;
import com.owon.uppersoft.vds.core.paint.ScreenContext;
import com.owon.uppersoft.vds.core.wf.dm.DMDataInfo;
import com.owon.uppersoft.vds.core.wf.dm.DMInfo;

/**
 * LocInfo, premise of the algorithm and of the design approach:
 * 
 * Purpose: supports the stopped case and the static loaded waveform case, reading in deep memory (no slow sweep) data,
 * 
 * needs to implement the zoom brought by horizontal movement and timebase switching, needs to mark the horizontal trigger position, needs a good drawing effect.
 * 
 * 
 * 
 * 
 * Step 1: clarify all possible cases, below;
 * 
 * Channel on/off, memory depth and timebase together determine the sampling rate, and thus the full-screen count
 * 
 * Stretch or compress, whether the zoom ratio is an integer
 * 
 * Read in the parameters of the various known states, compute the sampling rate and zoom ratio tables, and build the result set, so that an algorithm of suitable efficiency can be identified
 * 
 * 
 * 
 * Step 2: consider stretch and compress separately, choose suitable internal structures and memory arrays
 * 
 * Then consider whether they can be merged, so that externally required voltage scaling, zero point panning, 3in1 zoom and drawing coordinate switching apply automatically
 * 
 * 
 * 
 * Step 3: implement timebase switching across the stretch and compress states; first finish the timebase switching within each of them,
 * 
 * then find the critical state common to both, i.e. compression ratio == stretch ratio == 1, and link up the whole switching chain
 * 
 * 
 * 
 * Step 4: implement stretch/compress, initial test
 * 
 * 
 * 
 * Step 5: implement the horizontal trigger position and the top view
 * 
 */
public abstract class LocInfo {

	public int initPos;
	public int screendatalen;
	public int slowmove;
	public int filePointer;
	public int datalen;

	/** Holds the previous timebase state when switching timebase */
	public BigDecimal bdtb;
	public BigDecimal bdfsl;// fullScreenLen

	public final int pixs, halfpixs;

	/** Full-screen compression ratio */
	public int fsrate;

	public LocInfo() {
		pixs = GDefine.AREA_WIDTH;
		halfpixs = pixs >> 1;

		datalen = -1;
	}

	public boolean wasSlowMove;

	public void release() {
		if (diu != null)
			diu.release();
		if (ciu != null)
			ciu.release();
	}

	public boolean shouldReverse;
	public int loadPos0;

	public void prepareDM(ScreenContext pc, DMDataInfo cdi, BigDecimal tbbd,
			int basevbidx, int tbidx, boolean shouldReverse, MachineType mt,
			DMInfo cti) {
		datalen = cdi.datalen;
		slowmove = cdi.slowMove;
		loadPos0 = cdi.pos0;
		this.shouldReverse = shouldReverse;

		/** In deep memory, compute the new filePointer from the slow move count, locking the valid waveform area */
		filePointer = cdi.filePointer;

		screendatalen = cdi.screendatalen;
		initPos = cdi.initPos;
		// System.out.println("initPos: " + initPos);
		// System.out.println("datalen: " + datalen);
		// System.out.println("screendatalen: " + screendatalen);
		fsrate = datalen / pixs;

		bdtb = tbbd;
		bdfsl = BigDecimal.valueOf(screendatalen);

		wasSlowMove = getMachineInfo().isSlowMove(tbidx);
		if (wasSlowMove) {
			/** When entering compress mode, initialize databeg to the value under the same algorithm as at run time; this holds in all compress cases */
			int r = datalen / pixs;
			if (r > 1) {
				slowmove -= slowmove % r;
				if (slowmove < 0)
					slowmove = 0;
			}
		}

		/** Try treating the 1k full-screen case as compress, which reduces the Dilute cases and avoids handling slow sweep */
		compress = becomeCompress(screendatalen);

		diu = createDiluteInfoUnit(this);
		ciu = new CompressInfoUnit();

		diu.setPlug(!compress);

		prepareBeforeInitLoadInfoUnit(cti);
		// System.err.println(screendatalen+", "+compress);

		diu.reinit();
		/** 1k points are handed to diu by default, but interpolation is only for fewer than 1k points */
		if (compress) {
			ciu.initLoad(this, cdi);
		} else {
			diu.initLoad(cdi, tbidx, mt);
		}
	}

	protected abstract void prepareBeforeInitLoadInfoUnit(DMInfo cti);

	protected abstract IDiluteInfoUnit createDiluteInfoUnit(LocInfo li);

	protected InfoUnit getInfoUnit() {
		if (compress) {
			return ciu;
		} else {
			return diu;
		}
	}

	private boolean compress;

	public boolean isCompress() {
		return compress;
	}

	private boolean becomeCompress(int nexfsl) {
		return nexfsl >= pixs;
	}

	private boolean becomeCompress(double nexfsl) {
		return nexfsl >= pixs;
	}

	public DataHouse getDataHouse() {
		return Platform.getDataHouse();
	}

	public void setTimebaseIndex(int idx) {
		BigDecimal nexbdtb = getMachineInfo().bdTIMEBASE[idx];
		if (bdtb == null)
			System.err.println("bdtb==null");
		// Switching from run to pause and scrolling the mouse wheel quickly, bdtb may be empty after deep memory finishes, perhaps caused by adjusting the timebase before it fully loaded.
		BigDecimal bi = nexbdtb.divide(bdtb);// BigDecimal.divide(UnknownSource)
		BigDecimal nexfsl = bdfsl.multiply(bi);
		double dblnexfsl = nexfsl.doubleValue();
		boolean bC = becomeCompress(dblnexfsl);
		if (compress) {
			if (bC) {
				ciu.tbTranslate(bdtb, nexbdtb, idx);
			} else {
				// c->d
				ciu.tbTranslate2D(bdtb, nexbdtb, diu, idx);
				compress = false;
			}
		} else {
			if (bC) {
				// d->c
				diu.tbTranslate2C(bdtb, nexbdtb, ciu, idx);
				compress = true;
			} else {
				diu.tbTranslate(bdtb, nexbdtb, idx);
			}
		}
		// System.err.println(isCompress() + " : " + bC);
		bdtb = nexbdtb;
		bdfsl = nexfsl;
	}

	public CByteArrayInputStream getDeepMemoryStorage() {
		return getDataHouse().getDeepMemoryStorage();
	}

	public MachineInfo getMachineInfo() {
		return getDataHouse().controlManager.getMachineInfo();
	}

	protected IDiluteInfoUnit diu;
	protected CompressInfoUnit ciu;

	/**
	 * +...-
	 * 
	 * @param del
	 */
	public void addWaveFormsXloc(int del) {
		getInfoUnit().computeMove(-del);
	}

	/**
	 * Get the pixel difference of the trigger point relative to the screen center in different cases, as the source of the algorithms for drawing and getting time values
	 * 
	 * -...+
	 * 
	 * @param hortrgidx
	 * @return +...-
	 */
//	private int getHorTrgPos(int hortrgidx) {
//		return getInfoUnit().getHorTrgPos(hortrgidx);
//	}

	public void drawView(Graphics g, int xoff, int hh, int wlen, int hortrgidx) {
		getInfoUnit().drawView(g, xoff, hh, wlen, hortrgidx);
	}

	public void resetIntBuf(BigDecimal vbmulti, int yb, boolean screenMode_3) {
		getInfoUnit().resetDMIntBuf(vbmulti, yb, screenMode_3);
	}

	public int getXoffset() {
		return getInfoUnit().getXoffset();
	}

	public ByteBuffer getADC_Buf() {
		if (datalen <= 0)
			return null;

		if (compress) {
			return ciu.getb_adcbuf();
		} else {
			return diu.getb_adcbuf();
		}
	}

	public double getGap() {
		if (compress) {
			return ciu.getGap();
		} else {
			return (double) diu.getGap();
		}
	}

	public int getDrawMode() {
		if (compress) {
			return ciu.getDrawMode();
		} else {
			return diu.getDrawMode();
		}
	}

}