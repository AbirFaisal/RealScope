package com.owon.uppersoft.dso.wf.common.dm;

import java.math.BigDecimal;
import java.nio.ByteBuffer;

import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.data.Range;
import com.owon.uppersoft.vds.util.format.SFormatter;

/**
 * VirtualPointMode，虚拟点的模式
 * 
 */
public abstract class VirtualPoint {

	public static int MaxCompressTempBufferSize;
	public static byte[] tmparray;

	public static final void prepare(int[] DEEPValue) {
		if (!(tmparray == null || MaxCompressTempBufferSize == 0))
			return;

		int len = DEEPValue.length;

		MaxCompressTempBufferSize = DEEPValue[len - 1] / GDefine.AREA_WIDTH;
		tmparray = new byte[MaxCompressTempBufferSize + 50];
	}

	public static final void release() {
		tmparray = null;
		MaxCompressTempBufferSize = 0;
	}

	/**
	 * 工厂方法创建实例
	 * 
	 * @param scr
	 * @param pix
	 * @return
	 */
	public static final VirtualPoint getInstance(int scr, int pix) {
		if (scr == 0 || pix == 0) {
			errprintln("computeSavePoints can not handle!");
			return null;
		}
		if (scr % pix == 0) {
			/** 压缩率为整数 */
			return new VP1_4(scr / pix);
		}

		int x, y;
		x = pix;
		y = scr;
		while (x % 5 == 0 && y % 5 == 0) {
			x /= 5;
			y /= 5;
		}
		while (x % 2 == 0 && y % 2 == 0) {
			x /= 2;
			y /= 2;
		}

		// System.out.println(toString());System.out.println(rate);

		if (x == 2) {
			if (y == 5) {
				return new VP2_5();
			} else if (y == 25) {
				return new VP2_25();
			} else {
				errprintln("computeSavePoints can not handle!");
				return null;
			}
		}
		if (x == 4 && y == 5) {
			return new VP4_5();
		}
		errprintln("computeSavePoints can not handle!");
		return null;
	}

	protected static void errprintln(String string) {
		System.err.println(string);
	}

	/**
	 * 用于压缩率很大算不出满屏数的情况下，工厂方法创建实例
	 * 
	 * @param rate
	 * @return
	 */
	public static final VirtualPoint getInstance(BigDecimal rate) {
		VP1_4 p = new VP1_4(rate);
		return p;
	}

	/**
	 * Virtual Point collect how many samples
	 */
	public int sample;
	/**
	 * Virtual Point base on how many pixels
	 */
	public int pixel;
	/**
	 * Virtual Point use how many bytes to draw this pixels
	 */
	protected int point;

	@Override
	public String toString() {
		return SFormatter.UIformat("sample: %d, pixel: %d, point: %d", sample,
				pixel, point);
	}

	private BigDecimal rate;

	public double getRFGap() {
		return pixel / (double) point;
	}

	/**
	 * @return 压缩率
	 */
	public BigDecimal getRate() {
		return rate;
	}

	protected void setRate(BigDecimal rate) {
		this.rate = rate;
	}

	protected void computeRate() {
		rate = BigDecimal.valueOf(sample).divide(BigDecimal.valueOf(pixel));
	}

	/**
	 * @return 压缩率
	 */
	public double getCompressRate() {
		return rate.doubleValue();
	}

	/**
	 * 是否满屏压缩
	 * 
	 * @param li
	 * @return
	 */
	public boolean isCompress1Screen(double fsrate) {
		return rate.doubleValue() >= fsrate;
	}

	/**
	 * 正好满足压缩点数的压缩
	 * 
	 * @param cba
	 * @param arr
	 * @param abeg
	 * @param tmp
	 * @param tbeg
	 */
	public abstract void compressBySavePoints_Enough(ByteBuffer cba,
			byte[] arr, int abeg, int tbeg);

	/**
	 * 压缩成保存的数据点，在头尾，不满足压缩点个数的情况
	 * 
	 * @param cba
	 * @param arr
	 * @param abeg
	 * @param tmp
	 * @param tbeg
	 * @param num
	 * @param head
	 */
	public abstract void compressBySavePoints(ByteBuffer cba,
			byte[] arr, int abeg, int tbeg, int num, boolean head);

	/**
	 * 根据正负方向补上剩余像素对应的数据点余量
	 * 
	 * -...+: neg指向-
	 * 
	 * @param neg
	 * @param re
	 * @return
	 */
	protected abstract int getDilute2EndRemaining(boolean neg, int re);

	public abstract int getDrawMode();

	/**
	 * @param offset
	 * @return 虚拟点偏移
	 */
	public int getOffsetBetween(int offset) {
		return offset / sample * pixel;
	}

	/**
	 * 
	 * @param cpoff
	 *            像素偏移
	 * @return 像素偏移对应到的实际数据点偏移
	 */
	public int getDilute2End(int cpoff) {
		boolean neg = cpoff < 0;
		if (neg)
			cpoff = -cpoff;

		int v = 0;
		v += cpoff / pixel * sample;
		int re = cpoff % pixel;
		if (re == 0)
			return (neg) ? -v : v;

		v += getDilute2EndRemaining(neg, re);
		return (neg) ? -v : v;
	}

	/**
	 * 根据ap.x提供的x段点数，倒转array中的数据区为正序
	 * 
	 * @param ap
	 * @param array
	 */
	public void back2NormalXOrder(Range ap, byte[] array) {
		int n = point;
		int x1 = ap.left / n;

		// 改回正序
		if (x1 > 1) {
			int i = 0, x0 = 0, j = ap.left - n;
			x1--;
			byte[] tmp = new byte[10];
			// 奇偶皆可
			while (x0 < x1) {
				System.arraycopy(array, i, tmp, 0, n);
				System.arraycopy(array, j, array, i, n);
				System.arraycopy(tmp, 0, array, j, n);
				i += n;
				j -= n;
				x0++;
				x1--;
			}
		}
	}

	/**
	 * 由xyrangeoffset定位的x、y段分隔点的像素位置计算得到x段最左边的像素位置
	 * 
	 * @param xyrangeoffset
	 * @param ap
	 * @return
	 */
	public int xyrangeoffset2xoffset(int xyrangeoffset, Range ap) {
		int v = xyrangeoffset + 1 - (ap.left / point * pixel);
		return v;
	}

	public static final int FirstValueIdx = 0, MinValueIdx = 1,
			MaxValueIdx = 2, LastValueIdx = 3;

	public int getPoint() {
		return point;
	}

	public void setPoint(int point) {
		this.point = point;
	}

}