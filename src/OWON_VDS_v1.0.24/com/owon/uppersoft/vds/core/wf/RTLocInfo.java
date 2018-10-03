package com.owon.uppersoft.vds.core.wf;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.data.MinMax;
import com.owon.uppersoft.vds.core.rt.IDataMaxMin;
import com.owon.uppersoft.vds.util.PrimaryTypeUtil;

/**
 * RT单次波形绘图，不在内部支持接地，由外部设置，尽量精简支持的逻辑，严格控制增加，适用不同机型，性能优化后期再考虑
 * 
 * @author Matt
 * 
 */
public class RTLocInfo implements IDataMaxMin {
	public static int AREA_WIDTH = GDefine.AREA_WIDTH;
	/**
	 * 这个数组为固定adc字节数据，实为usb使用的共享数组，不在上面直接修改值
	 */
	public ByteBuffer adcbuf;

	public IntBuffer pixbuf;

	private int chln;

	public RTLocInfo(int n) {
		this.chln = n;
	}

	private void prepare() {
		if (pixbuf == null) {
			/** 扩大pixbuf范围，使其支持在屏幕之前开始画图而延伸出更多数据点的情况 */
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
	 * 这里的处理方式仅是对adc数据简单地做平移和缩放，在峰值检测时的max_min数据也同样使用，仅在峰值画图时，分开处理
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
	 * RT时通过数据buffer填充画图buffer的内容，只在载入时调用一次
	 * 
	 * 由于处理了反相，该方法只在载入数据时调用一次
	 * 
	 * @param buf
	 * @param yb
	 * @param ScreenMode_3
	 */
	public void resetRTIntBuf(ByteBuffer buf, int yb, boolean ScreenMode_3,
			BigDecimal vbmulti) {
		prepare();

		/**
		 * KNOW 临时缓冲区被直接拿来使用，在多次传输中反复使用，并且所有内容共用一个大的缓冲区方便分配，少量配置信息出于方便也存在其中
		 * 
		 * KNOW 数组包含了从下位机传上来的所有数据，但只有用initPos和screendatalen截取的才是画在屏幕上的
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