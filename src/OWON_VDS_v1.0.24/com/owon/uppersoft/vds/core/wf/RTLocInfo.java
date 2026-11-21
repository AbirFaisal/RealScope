package com.owon.uppersoft.vds.core.wf;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.data.MinMax;
import com.owon.uppersoft.vds.core.rt.IDataMaxMin;
import com.owon.uppersoft.vds.util.PrimaryTypeUtil;

/**
 * RT single-shot waveform drawing; grounding is not supported internally but set externally, keep the supported logic minimal, strictly control additions, applicable to different models, performance optimization will be considered later
 * 
 * @author Matt
 * 
 */
public class RTLocInfo implements IDataMaxMin {
	public static int AREA_WIDTH = GDefine.AREA_WIDTH;
	/**
	 * This array is fixed adc byte data, actually a shared array used by usb; do not modify its values directly
	 */
	public ByteBuffer adcbuf;

	public IntBuffer pixbuf;

	private int chln;

	public RTLocInfo(int n) {
		this.chln = n;
	}

	private void prepare() {
		if (pixbuf == null) {
			/** Extend the pixbuf range so that it supports drawing starting before the screen, which extends to more data points */
			pixbuf = IntBuffer.allocate((AREA_WIDTH << 2) + 1000);// 4008
			pixbuf.position(0);
			pixbuf.limit(0);
		}
	}

	@Override
	public int getMax() {
		return Max_8bit;
	}

	@Override
	public int getMin() {
		return Min_8bit;
	}

	protected void release() {
		adcbuf = null;
		pixbuf = null;
	}

	/**
	 * The handling here is just a simple shift and scale of the adc data; it is also used for the max_min data in peak detect, with separate handling only when drawing peaks
	 * 
	 * 
	 * @param vbmulti
	 * @param yb
	 * @param screenMode_3
	 */
	public void resetIntBuf(BigDecimal vbmulti, int yb, boolean screenMode_3) {
		ByteBuffer buf = adcbuf;
		int p = buf.position();
		int l = buf.limit();
		byte[] dma = buf.array();

		int[] ina = pixbuf.array();
		int i = 0;

		pixbuf.position(i);
		if (PrimaryTypeUtil.canHoldAsInt(vbmulti)) {
			int m = vbmulti.intValue();

			if (screenMode_3) {
				if (m == 1) {
					while (p < l) {
						ina[i] = yb - (dma[p]);
						p++;
						i++;
					}
				} else {
					while (p < l) {
						ina[i] = yb - (dma[p]) * m;
						p++;
						i++;
					}
				}
			} else {
				if (m == 1) {
					while (p < l) {
						ina[i] = yb - (((int) dma[p]) << 1);
						p++;
						i++;
					}
				} else {
					while (p < l) {
						ina[i] = yb - ((((int) dma[p]) << 1) * m);
						p++;
						i++;
					}
				}
			}
		} else {
			double m = vbmulti.doubleValue();

			if (screenMode_3) {
				while (p < l) {
					ina[i] = yb - (int) ((dma[p]) * m);
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
		pixbuf.limit(i);
	}

	public MinMax collect_max_min() {
		if (adcbuf == null)
			return null;

		int p = adcbuf.position();
		int l = adcbuf.limit();
		byte[] bb = adcbuf.array();

		if (p >= l)
			return null;

		int bmax, bmin, bv;
		bmin = bmax = bb[p];
		for (int i = p + 1; i < l; i++) {
			bv = bb[i];
			if (bv > bmax) {
				bmax = bv;
			} else if (bv < bmin) {
				bmin = bv;
			}
		}

		return new MinMax(bmin, bmax);
	}

	/**
	 * RT: fill the drawing buffer from the data buffer; called only once at load time
	 * 
	 * Because inversion is handled, this method is called only once when loading data
	 * 
	 * @param buf
	 * @param yb
	 * @param ScreenMode_3
	 */
	public void resetRTIntBuf(ByteBuffer buf, int yb, boolean ScreenMode_3,
			BigDecimal vbmulti) {
		prepare();

		/**
		 * KNOW The temporary buffer is used directly and reused over multiple transfers; all content shares one large buffer for easy allocation, and a small amount of configuration info is also kept in it for convenience
		 * 
		 * KNOW The array contains all the data uploaded from the device, but only the portion cut out with initPos and screendatalen is drawn on screen
		 */
		adcbuf = buf;
		// vl.logByteBuffer(pixbuf);
		// DBG.configArray(adcbuf.array(), adcbuf.position(),
		// adcbuf.remaining());
		resetIntBuf(vbmulti, yb, ScreenMode_3);
	}

	public int getChln() {
		return chln;
	}

}