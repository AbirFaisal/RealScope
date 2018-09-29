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
		// 通过slice创建ByteBuffer共享数组的子视图
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
	 * 这里需要考虑虚拟点存储是否要反转的问题，因为在从adc到pix的时候针对存储点进行简单的反转
	 * 
	 * 压缩的过程中不会对由scpi和scrctr确定的波形屏幕位置造成移动
	 * 
	 * scpi未必指向屏幕正中，scrctr则指向scpi所在的起始像素位置
	 * 
	 * 针对压缩到一屏甚至是一点的情况
	 * 
	 * @param vp
	 *            x:左边数据短的位置，倒装; y:右边数据短的位置，正装
	 * @param scrbeg
	 *            画图起始像素
	 * @param scrend
	 *            画图结束像素
	 * @param scr4scpi
	 *            scpi对应的像素点
	 * @param scpi
	 *            Screen Center Point Index
	 * @param array
	 *            保存数组
	 * @param databeg
	 * @param dataend
	 * @param ap
	 *            x:左边数据个数；y:右边数据个数
	 * @return x和y部分界点对应到屏幕上的像素位置，是x部分的最后一个点的位置
	 */
	private int doCompressExecute_fs(VirtualPoint vp, int scrbeg, int scrend,
			int scrctr, int scpi, byte[] array, int databeg, int dataend,
			Range ap) {
		// 一开始设置为无效的值，返回后加到cpoff
		/** x和y部分界点对应到屏幕上的像素位置，是x部分的最后一个点的位置 */
		int xyrangeoffset = scrbeg - (scrend - scrbeg);

		ap.left = ap.right = -1;
		int spl = vp.sample, pn = vp.pixel;
		int si = scpi, j, pi = scrctr;
		int len = dataend - databeg;

		int off = si - databeg;
		int num = off / spl;
		pi = scrctr - num * pn - pn;

		int del = Math.abs(off) % spl;

		// 兼容si往左往右到达databeg
		if (off > 0) {
			if (del > 0)
				pi -= pn;
		} else if (off < 0) {
			del = spl - del;
		}

		xyrangeoffset = pi;

		// 兼容压缩在小于一个像素点上
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
				// 发现越界，改为dataend - 1 - si
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
	 * 这里需要考虑虚拟点存储是否要反转的问题，因为在从adc到pix的时候针对存储点进行简单的反转
	 * 
	 * scpi未必指向屏幕正中，scrctr则指向scpi所在的起始像素位置
	 * 
	 * @param vp
	 *            x:左边数据短的位置，倒装; y:右边数据短的位置，正装
	 * @param scrbeg
	 *            画图起始像素
	 * @param scrend
	 *            画图结束像素
	 * @param scr4scpi
	 *            scpi对应的像素点
	 * @param scpi
	 *            Screen Center Point Index
	 * @param array
	 *            保存数组
	 * @param databeg
	 * @param dataend
	 * @param ap
	 *            x:左边数据个数；y:右边数据个数
	 * @return x和y部分界点对应到屏幕上的像素位置，是x部分的最后一个点的位置
	 */
	private int doCompressExecute(VirtualPoint vp, int scrbeg, int scrend,
			int scrctr, int scpi, byte[] array, int databeg, int dataend,
			Range ap) {
		// 一开始设置为无效的值，返回后加到cpoff
		/** x和y部分界点对应到屏幕上的像素位置，是x部分的最后一个点的位置 */
		int xyrangeoffset = scrbeg - (scrend - scrbeg);

		ap.left = ap.right = -1;
		int spl = vp.sample, pn = vp.pixel;
		int si = scpi, j, pi = scrctr;

		// 最先判断的两种情况是scpi上不存在满足范围的数据点
		// TODO 包含慢扫
		if (si < databeg) {
			// 数据点在scpi右侧
			// 虽然scpi不存在满足范围的数据点，但是scpi所对应的scrctr上，可能是可以压缩得到可画像素点的
			j = 0;
			while (pi <= scrend) {
				si += spl;
				pi += pn;
				// 碰到可用数据点时处理并跳出
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
			// 数据点在scpi左侧
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

		// scpi上存在满足范围的数据点
		si = scpi;
		j = 0;
		pi = scrctr;
		// 虽然scpi存在满足范围的数据点，但是scpi所对应的scrctr上，可能达不到用于压缩所需的数据点个数
		int del = dataend - si;
		if (del < spl) {
			j += for1_tail(array, si, j, vp, del);
		} else {
			j += for1(array, si, j, vp);
		}
		pi -= pn;
		si -= spl;
		// 先往scpi左侧，在数组x部，存放的数据从xyrangeoffset像素点向左画
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
		// 往scpi右侧，在数组y部，存放的数据从xyrangeoffset像素点向右画
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
	 * scpi未必指向屏幕正中，scrctr则指向scpi所在的起始像素位置
	 * 
	 * @param vp
	 *            x:左边数据短的位置，倒装; y:右边数据短的位置，正装
	 * @param scrbeg
	 *            画图起始像素
	 * @param scrend
	 *            画图结束像素
	 * @param scr4scpi
	 *            scpi对应的像素点
	 * @param scpi
	 *            Screen Center Point Index
	 * @param adcbuf
	 *            保存缓冲区
	 * @param databeg
	 * @param dataend
	 * @param ap
	 *            x:左边数据个数；y:右边数据个数；经过处理，ap被保存了新的分段信息
	 * @param isCompress1Screen
	 * @param inverseType
	 *            反相类型
	 * @param loadPos0
	 *            零点位置
	 * @return x轴左侧起始画图点的像素位置，(为ap的x和y两部分的分界点对应到屏幕上的像素位置，因为x部分是从右向左对应了索引从小到大的，
	 *         所以是x部分的最后一个点的屏幕位置)
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
		 * KNOW 由于已经是压缩到屏幕上的像素，数据量小，反序消耗的时间可以接受
		 * 
		 * 在这里处理过后，xy两段就融合为一了
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
	 * 为反相作调整，同时限制屏幕点的范围
	 * 
	 * @param position
	 * @param limit
	 * @param inverseType
	 * @param loadPos0
	 * @param array
	 */
	private void restrictForReverse(int position, int limit,
			boolean shouldReverse, int loadPos0, byte[] array) {
		/** 交给画图屏幕，处理反相问题 */
		shouldReverse = false;
		final byte max = (byte) getMax(), min = (byte) getMin();
		/** 在adc数据取出绘图前进行必要的反相处理 */
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
	 * @return 数组被填充的字节数
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
	 * @return 数组被填充的字节数
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
	 * @return 数组被填充的字节数
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

	public void transcriptionInt(ByteBuffer adcbuf, IntBuffer pixbuf,
			BigDecimal vbmulti, int yb, boolean screenMode_3) {
		doTranscriptionInt(adcbuf, pixbuf, vbmulti, yb, screenMode_3);
	}

	public void transcriptionDouble(ByteBuffer adcbuf, IntBuffer pixbuf,
			BigDecimal vbmulti, int yb, boolean screenMode_3) {
		doTranscriptionDouble(adcbuf, pixbuf, vbmulti, yb, screenMode_3);
	}

	protected void reverseX_transcriptionInt(byte[] dma, int[] ina,
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

	protected void reverseX_transcriptionDouble(byte[] dma, int[] ina,
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