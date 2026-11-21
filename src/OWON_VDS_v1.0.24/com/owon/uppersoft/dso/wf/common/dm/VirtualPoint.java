package com.owon.uppersoft.dso.wf.common.dm;

import java.math.BigDecimal;
import java.nio.ByteBuffer;

import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.data.Range;
import com.owon.uppersoft.vds.util.format.SFormatter;

/**
 * VirtualPointMode, virtual point mode
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
	 * Factory method to create an instance
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
			/** compression ratio is an integer */
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
	 * Used when the compression ratio is so large that the full-screen count cannot be computed; factory method to create an instance
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
	 * @return compression ratio
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
	 * @return compression ratio
	 */
	public double getCompressRate() {
		return rate.doubleValue();
	}

	/**
	 * Whether it is full-screen compression
	 * 
	 * @param li
	 * @return
	 */
	public boolean isCompress1Screen(double fsrate) {
		return rate.doubleValue() >= fsrate;
	}

	/**
	 * Compression that exactly satisfies the compression point count
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
	 * Compress into the saved data points, at the head and tail, where the compression point count is not satisfied
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
	 * Fill in the leftover data points corresponding to the remaining pixels, according to the positive or negative direction
	 * 
	 * -...+: neg points to -
	 * 
	 * @param neg
	 * @param re
	 * @return
	 */
	protected abstract int getDilute2EndRemaining(boolean neg, int re);

	public abstract int getDrawMode();

	/**
	 * @param offset
	 * @return virtual point offset
	 */
	public int getOffsetBetween(int offset) {
		return offset / sample * pixel;
	}

	/**
	 * 
	 * @param cpoff
	 *            pixel offset
	 * @return actual data point offset corresponding to the pixel offset
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
	 * Based on the x-segment point count provided by ap.x, reverse the data area in array into forward order
	 * 
	 * @param ap
	 * @param array
	 */
	public void back2NormalXOrder(Range ap, byte[] array) {
		int n = point;
		int x1 = ap.left / n;

		// restore forward order
		if (x1 > 1) {
			int i = 0, x0 = 0, j = ap.left - n;
			x1--;
			byte[] tmp = new byte[10];
			// either odd or even works
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
	 * Compute the leftmost pixel position of the x segment from the pixel position of the x/y segment split point located by xyrangeoffset
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