package com.owon.uppersoft.dso.wf.common.dm;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.owon.uppersoft.vds.core.data.CByteArrayInputStream;
import com.owon.uppersoft.vds.core.rt.IDataMaxMin;
import com.owon.uppersoft.vds.data.Range;

/**
 * @author Matt
 * 
 */
public class VirtualPointHandler implements IDataMaxMin {

	private ByteBuffer buf;

	public VirtualPointHandler(CByteArrayInputStream aa, int filePointer,
			int dataend) {
		// System.out.println(filePointer + " | " + dataend);
		ByteBuffer org = aa.buf();
		org.limit(filePointer + dataend);
		org.position(filePointer);
		// Create a sub-view of the shared array of the ByteBuffer through slice
		this.buf = org.slice();
	}

	@Override
	public int getMax() {
		return Max_8bit;
	}

	@Override
	public int getMin() {
		return Min_8bit;
	}

	/**
	 * Here we need to consider whether virtual point storage should be reversed, because a simple reversal is applied to the stored points when going from adc to pix
	 * 
	 * The compression process does not shift the waveform screen position determined by scpi and scrctr
	 * 
	 * scpi does not necessarily point to the screen center; scrctr points to the starting pixel position where scpi is located
	 * 
	 * For the case of compressing to one screen or even one point
	 * 
	 * @param vp
	 *            x:left side, short data position, reversed order; y: right side, short data position, forward order
	 * @param scrbeg
	 *            pixel where drawing starts
	 * @param scrend
	 *            pixel where drawing ends
	 * @param scr4scpi
	 *            scpi corresponding pixel point
	 * @param scpi
	 *            Screen Center Point Index
	 * @param array
	 *            save array
	 * @param databeg
	 * @param dataend
	 * @param ap
	 *            x:left data count; y: right data count
	 * @return x and y part boundary point's pixel position on the screen, which is the position of the last point of the x part
	 */
	private int doCompressExecute_fs(VirtualPoint vp, int scrbeg, int scrend,
			int scrctr, int scpi, byte[] array, int databeg, int dataend,
			Range ap) {
		// initially set to an invalid value; after returning, it is added to cpoff
		/** x and y part boundary point's pixel position on the screen, which is the position of the last point of the x part */
		int xyrangeoffset = scrbeg - (scrend - scrbeg);

		ap.left = ap.right = -1;
		int spl = vp.sample, pn = vp.pixel;
		int si = scpi, j, pi = scrctr;
		int len = dataend - databeg;

		int off = si - databeg;
		int num = off / spl;
		pi = scrctr - num * pn - pn;

		int del = Math.abs(off) % spl;

		// compatible with si going left or right to reach databeg
		if (off > 0) {
			if (del > 0)
				pi -= pn;
		} else if (off < 0) {
			del = spl - del;
		}

		xyrangeoffset = pi;

		// compatible with compression into less than one pixel
		if (del > len)
			del = len;

		j = 0;
		if (del > 0) {
			j += for1_head(array, databeg, j, vp, del);
			si = databeg + del;
		} else {
			si = databeg;
		}

		while (si < dataend) {
			if (si + spl > dataend) {
				// out of bounds found, change to dataend - 1 - si
				j += for1_tail(array, si, j, vp, dataend - 1 - si);
				break;
			} else if (si + spl == dataend) {
				break;
			}
			j += for1(array, si, j, vp);
			pi += pn;
			si += spl;
		}
		ap.left = 0;
		ap.right = j;
		return xyrangeoffset;
	}

	/**
	 * Here we need to consider whether virtual point storage should be reversed, because a simple reversal is applied to the stored points when going from adc to pix
	 * 
	 * scpi does not necessarily point to the screen center; scrctr points to the starting pixel position where scpi is located
	 * 
	 * @param vp
	 *            x:left side, short data position, reversed order; y: right side, short data position, forward order
	 * @param scrbeg
	 *            pixel where drawing starts
	 * @param scrend
	 *            pixel where drawing ends
	 * @param scr4scpi
	 *            scpi corresponding pixel point
	 * @param scpi
	 *            Screen Center Point Index
	 * @param array
	 *            save array
	 * @param databeg
	 * @param dataend
	 * @param ap
	 *            x:left data count; y: right data count
	 * @return x and y part boundary point's pixel position on the screen, which is the position of the last point of the x part
	 */
	private int doCompressExecute(VirtualPoint vp, int scrbeg, int scrend,
			int scrctr, int scpi, byte[] array, int databeg, int dataend,
			Range ap) {
		// initially set to an invalid value; after returning, it is added to cpoff
		/** x and y part boundary point's pixel position on the screen, which is the position of the last point of the x part */
		int xyrangeoffset = scrbeg - (scrend - scrbeg);

		ap.left = ap.right = -1;
		int spl = vp.sample, pn = vp.pixel;
		int si = scpi, j, pi = scrctr;

		// The first two cases checked are those where scpi has no data points within range
		// TODO include slow sweep
		if (si < databeg) {
			// data point is to the right of scpi
			// Although scpi has no data points within range, the scrctr corresponding to scpi may still yield drawable pixel points by compression
			j = 0;
			while (pi <= scrend) {
				si += spl;
				pi += pn;
				// when a usable data point is encountered, process it and break out
				if (si > databeg) {
					xyrangeoffset = pi - pn;
					// if (pi <= pixs)
					j += for1_head(array, databeg, j, vp, si - databeg);
					break;
				} else if (si == databeg) {
					xyrangeoffset = pi;
					break;
				}
			}
			while (pi <= scrend) {
				j += for1(array, si, j, vp);
				pi += pn;
				si += spl;
			}
			ap.left = 0;
			ap.right = j;
			return xyrangeoffset;
		} else if (si >= dataend) {
			// data point is to the left of scpi
			j = 0;
			while (pi >= scrbeg) {
				pi -= pn;
				si -= spl;

				if (si >= dataend) {
					continue;
				}

				int del = dataend - si;
				if (del < spl) {
					xyrangeoffset = pi;
					// if (pi >= scrbeg)
					j += for1_tail(array, si, j, vp, del);
					pi -= pn;
					si -= spl;
					break;
				} else {
					xyrangeoffset = pi;
					break;
				}
			}
			while (pi >= scrbeg) {
				j += for1(array, si, j, vp);
				pi -= pn;
				si -= spl;
			}
			ap.left = j;
			ap.right = 0;
			return xyrangeoffset;
		}

		// scpi has data points that satisfy the range
		si = scpi;
		j = 0;
		pi = scrctr;
		// Although scpi has data points within range, the scrctr corresponding to scpi may not reach the data point count needed for compression
		int del = dataend - si;
		if (del < spl) {
			j += for1_tail(array, si, j, vp, del);
		} else {
			j += for1(array, si, j, vp);
		}
		pi -= pn;
		si -= spl;
		// First go to the left of scpi, in the x part of the array; the stored data is drawn leftward from the xyrangeoffset pixel
		while (pi >= scrbeg) {
			if (si < databeg) {
				del = databeg - si;
				if (del < spl) {
					j += for1_head(array, databeg, j, vp, spl - del);
				} else {
					pi += pn;
				}
				break;
			}
			j += for1(array, si, j, vp);
			pi -= pn;
			si -= spl;
		}
		ap.left = j;
		xyrangeoffset = scrctr;

		// System.out.println(vp);

		si = scpi + spl;
		pi = scrctr + pn;
		// Go to the right of scpi, in the y part of the array; the stored data is drawn rightward from the xyrangeoffset pixel
		while (pi <= scrend) {
			if (si == dataend) {
				break;
			} else if (si > dataend) {
				break;
			} else if (si + spl > dataend) {
				del = dataend - si;
				j += for1_tail(array, si, j, vp, del);
				pi += pn;
				si = dataend;
				break;
			}
			j += for1(array, si, j, vp);
			pi += pn;
			si += spl;
		}
		ap.right = j - ap.left;

		return xyrangeoffset;
	}

	/**
	 * scpi does not necessarily point to the screen center; scrctr points to the starting pixel position where scpi is located
	 * 
	 * @param vp
	 *            x:left side, short data position, reversed order; y: right side, short data position, forward order
	 * @param scrbeg
	 *            pixel where drawing starts
	 * @param scrend
	 *            pixel where drawing ends
	 * @param scr4scpi
	 *            scpi corresponding pixel point
	 * @param scpi
	 *            Screen Center Point Index
	 * @param adcbuf
	 *            save buffer
	 * @param databeg
	 * @param dataend
	 * @param ap
	 *            x:left data count; y: right data count; after processing, ap has new segmentation info saved
	 * @param isCompress1Screen
	 * @param inverseType
	 *            invert type
	 * @param loadPos0
	 *            zero position
	 * @return x-axis left start drawing point pixel position, (the pixel position on screen of the boundary between the x and y parts of ap; since the x part maps indexes increasing from right to left,
	 *         so it is the screen position of the last point of the x part)
	 */
	public int compressExecute(VirtualPoint vp, int scrbeg, int scrend,
			int scr4scpi, int scpi, ByteBuffer adcbuf, int databeg,
			int dataend, Range ap, boolean isCompress1Screen,
			boolean shouldReverse, int loadPos0) {
		byte[] array = adcbuf.array();
		int v;
		if (isCompress1Screen) {
			v = doCompressExecute_fs(vp, scrbeg, scrend, scr4scpi, scpi, array,
					databeg, dataend, ap);
		} else {
			v = doCompressExecute(vp, scrbeg, scrend, scr4scpi, scpi, array,
					databeg, dataend, ap);
		}

		/**
		 * KNOW Since these are already pixels compressed onto the screen, the data volume is small, so the time cost of reversing is acceptable
		 * 
		 * After processing here, the x and y segments merge into one
		 */
		vp.back2NormalXOrder(ap, array);

		int limit = ap.left + ap.right, position = 0;

		restrictForReverse(position, limit, shouldReverse, loadPos0, array);

		adcbuf.position(position);
		adcbuf.limit(limit);
		// System.err.println("com: "+adcbuf.remaining());

		return vp.xyrangeoffset2xoffset(v, ap);
	}

	public boolean BeyondMax = false, BeyondMin = false;

	/**
	 * Adjust for invert, and also limit the range of screen points
	 * 
	 * @param position
	 * @param limit
	 * @param inverseType
	 * @param loadPos0
	 * @param array
	 */
	private void restrictForReverse(int position, int limit,
			boolean shouldReverse, int loadPos0, byte[] array) {
		/** Hand over to the drawing screen to handle the invert issue */
		shouldReverse = false;
		final byte max = (byte) Max_8bit, min = (byte) Min_8bit;
		/** Perform the necessary invert processing before adc data is taken out for drawing */
		if (shouldReverse) {
			int elm;
			BeyondMax = BeyondMin = false;
			final int dpos0 = loadPos0 << 1;
			for (int i = position; i < limit; i++) {
				elm = dpos0 - array[i];

				if (elm > max) {
					array[i] = max;
					BeyondMax = true;
				} else if (elm < min) {
					array[i] = min;
					BeyondMin = true;
				} else {
					array[i] = (byte) elm;
				}
			}
		} else {
			int elm;
			BeyondMax = BeyondMin = false;
			for (int i = position; i < limit; i++) {
				elm = array[i];

				if (elm > max) {
					array[i] = max;
					BeyondMax = true;
				} else if (elm < min) {
					array[i] = min;
					BeyondMin = true;
				} else {
					array[i] = (byte) elm;
				}
			}

		}
	}

	/**
	 * 
	 * @param array
	 * @param si
	 * @param j
	 * @param v
	 * @return number of bytes the array was filled with
	 */
	private int for1(byte[] array, int si, int j, VirtualPoint v) {
		buf.position(si);

		v.compressBySavePoints_Enough(buf, array, j, 0);
		return v.getPoint();
	}

	/**
	 * 
	 * @param array
	 * @param si
	 * @param j
	 * @param v
	 * @return number of bytes the array was filled with
	 */
	private int for1_tail(byte[] array, int si, int j, VirtualPoint v, int del) {
		buf.position(si);

		v.compressBySavePoints(buf, array, j, 0, del, false);
		return v.getPoint();
	}

	/**
	 * 
	 * @param array
	 * @param si
	 * @param j
	 * @param v
	 * @return number of bytes the array was filled with
	 */
	private int for1_head(byte[] array, int si, int j, VirtualPoint v, int del) {
		buf.position(si);

		v.compressBySavePoints(buf, array, j, 0, del, true);
		return v.getPoint();
	}

	public static void doTranscriptionInt(ByteBuffer adcbuf, IntBuffer pixbuf,
			BigDecimal vbmulti, int yb, boolean screenMode_3) {
		byte[] dma = adcbuf.array();
		int[] ina = pixbuf.array();
		int m = vbmulti.intValue();
		int p = adcbuf.position(), l = adcbuf.limit(), j = 0;
		if (screenMode_3) {
			if (m == 1) {
				while (p < l) {
					ina[j] = yb - dma[p];
					p++;
					j++;
				}
			} else {
				while (p < l) {
					ina[j] = yb - dma[p] * m;
					p++;
					j++;
				}
			}
		} else {
			if (m == 1) {
				while (p < l) {
					ina[j] = yb - (dma[p] << 1);
					p++;
					j++;
				}
			} else {
				while (p < l) {
					ina[j] = yb - (dma[p] << 1) * m;
					p++;
					j++;
				}
			}
		}
		pixbuf.position(0);
		pixbuf.limit(j);
	}

	public static void doTranscriptionDouble(ByteBuffer adcbuf,
			IntBuffer pixbuf, BigDecimal vbmulti, int yb, boolean screenMode_3) {
		byte[] dma = adcbuf.array();
		int[] ina = pixbuf.array();
		double m = vbmulti.doubleValue();
		int p = adcbuf.position(), l = adcbuf.limit(), j = 0;
		if (screenMode_3) {
			while (p < l) {
				ina[j] = yb - (int) (dma[p] * m);
				p++;
				j++;
			}
		} else {
			while (p < l) {
				ina[j] = yb - (int) ((((int) dma[p]) << 1) * m);
				p++;
				j++;
			}
		}
		pixbuf.position(0);
		pixbuf.limit(j);
	}

	public static void transcriptionInt(ByteBuffer adcbuf, IntBuffer pixbuf,
	                                    BigDecimal vbmulti, int yb, boolean screenMode_3) {
		doTranscriptionInt(adcbuf, pixbuf, vbmulti, yb, screenMode_3);
	}

	public static void transcriptionDouble(ByteBuffer adcbuf, IntBuffer pixbuf,
	                                       BigDecimal vbmulti, int yb, boolean screenMode_3) {
		doTranscriptionDouble(adcbuf, pixbuf, vbmulti, yb, screenMode_3);
	}

	protected static void reverseX_transcriptionInt(byte[] dma, int[] ina,
	                                                BigDecimal vbmulti, int yb, boolean screenMode_3, Range ap) {
		int m = vbmulti.intValue();
		int i = 0;
		int p = ap.left - 1, y = ap.left + ap.right;
		if (screenMode_3) {
			if (m == 1) {
				while (p >= 0) {
					ina[i] = yb - dma[p];
					p--;
					i++;
				}
				p = ap.left;
				while (p < y) {
					ina[i] = yb - dma[p];
					p++;
					i++;
				}
			} else {
				while (p >= 0) {
					ina[i] = yb - dma[p] * m;
					p--;
					i++;
				}
				p = ap.left;
				while (p < y) {
					ina[i] = yb - dma[p] * m;
					p++;
					i++;
				}
			}
		} else {
			if (m == 1) {
				while (p >= 0) {
					ina[i] = yb - (dma[p] << 1);
					p--;
					i++;
				}
				p = ap.left;
				while (p < y) {
					ina[i] = yb - (dma[p] << 1);
					p++;
					i++;
				}
			} else {
				while (p >= 0) {
					ina[i] = yb - (dma[p] << 1) * m;
					p--;
					i++;
				}
				p = ap.left;
				while (p < y) {
					ina[i] = yb - (dma[p] << 1) * m;
					p++;
					i++;
				}
			}
		}
	}

	protected static void reverseX_transcriptionDouble(byte[] dma, int[] ina,
	                                                   BigDecimal vbmulti, int yb, boolean screenMode_3, Range ap) {
		double m = vbmulti.doubleValue();
		int i = 0;
		int p = ap.left - 1, y = ap.left + ap.right;
		if (screenMode_3) {
			while (p >= 0) {
				ina[i] = yb - (int) (dma[p] * m);
				p--;
				i++;
			}
			p = ap.left;
			while (p < y) {
				ina[i] = yb - (int) (dma[p] * m);
				p++;
				i++;
			}
		} else {
			while (p >= 0) {
				ina[i] = yb - (int) ((((int) dma[p]) << 1) * m);
				p--;
				i++;
			}
			p = ap.left;
			while (p < y) {
				ina[i] = yb - (int) ((((int) dma[p]) << 1) * m);
				p++;
				i++;
			}
		}
	}
}