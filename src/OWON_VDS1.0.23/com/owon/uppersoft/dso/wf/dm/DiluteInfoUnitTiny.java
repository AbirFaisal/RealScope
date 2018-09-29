package com.owon.uppersoft.dso.wf.dm;

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
import com.owon.uppersoft.vds.util.PrimaryTypeUtil;

public class DiluteInfoUnitTiny implements InfoUnit, IDataMaxMin,
		IDiluteInfoUnit {
	public void setCenterPoint(int cpidx, int cpoff) {
		cpi.setCenterPoint(cpidx, cpoff);
	}

	private LocInfo li;

	private ByteBuffer b_adcbuf;
	private IntBuffer pixbuf;
	private IntBuffer i_adcbuf;

	public int[] array;

	public DiluteInfoUnitTiny(LocInfo li) {
		this.li = li;

		pg = new Pluger(this);
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

	/**
	 * 水平触发所在位置
	 * 
	 * @param hortrgidx
	 * @return
	 */
	// private int getHorTrgPos(int hortrgidx) {
	// int del = hortrgidx - cpi.getCenterPointIndex();
	// final int mul = diluteR.getMultiplyor();
	// final int div = diluteR.getDivisor();
	// int v = cpi.getCenterPointOffset();
	// if (del < 0) {
	// del = -del;
	// v -= (del / div * mul);// + (((del % div) != 0) ? 1 : 0);
	// } else {
	// v += (del / div * mul);// + (((del % div) != 0) ? 1 : 0);
	// }
	// return v;
	// }
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
//		resetSPIOFF();
	}

	/**
	 * 用于记忆每一个时基档位的cpoff，在没有作平移的情况下不会修改，如果未被设置才去填充
	 */
//	private void resetSPIOFF() {
		// spi_offs = new int[li.getMachineInfo().TIMEBASE.length];
		// Arrays.fill(spi_offs, 0, spi_offs.length, invalidOff);
//	}

	private void prepare() {
		if (i_adcbuf == null) {
			int capacity = 15000;// 拉触发和插值的情况下最多(20+50*2)*5*10 = 6000

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
	}

	private final static int invalidOff = Integer.MAX_VALUE;

	// private int[] spi_offs;

	public void initLoad(DMDataInfo cdi, int tbidx, MachineType mt) {
		prepare();
		this.tbidx = tbidx;

		/** 载入深存储时为无需插值的时基，则后续也不进行插值 */
		int pixs = li.pixs;
		screendatalen = li.screendatalen;
		pg.initPlugInfo(cdi, mt);
		/** 理论满屏数 <= 屏幕像素宽度，拉伸 */
		/** 拉伸率 */
		// System.out.println("cpidx: " + cpidx);
		// reDilute();
		if (!pg.isPlug()) {
			System.err.println("err DiluteInfoUnitTiny initLoad !pg.isPlug()");
			return;
		}
		/** 走initLoad的只会是要插值拉触发的情况 */
		IntBuffer handlebuf = IntBuffer.wrap(i_adcbuf.array());
		int filePointer = li.filePointer;
		CByteArrayInputStream cba = getDeepMemoryStorage();
		boolean shouldReverse = li.shouldReverse;
		int loadPos0 = li.loadPos0;
		int datalen = li.datalen;

		pg.processPlugForInit(filePointer, shouldReverse, loadPos0, handlebuf,
				cba, li.halfpixs);

		/** 这里使用满屏点数，假定插值对Dilute不可见 */
		diluteR.set(pixs, screendatalen);
		cpi.computeCenterPointStuff(diluteR);
		// spi_offs[tbidx] = getCenterPointOffset();
		// pg.setPlug(false);

		/** 可以不用此方法而用下面注释的方法，但是此方法可以看到拉触发信息转换到内部的坐标体系是否正确 */
		reDilute();
		// int pos = handlebuf.position();
		// int limit = handlebuf.limit();
		// i_adcbuf.position(pos);
		// i_adcbuf.limit(limit);
		// lastHandle(i_adcbuf);
	}

	private CByteArrayInputStream getDeepMemoryStorage() {
		return li.getDeepMemoryStorage();
	}

	public boolean shouldPlugAsOriginal() {
		return pg.shouldPlugAsOriginal(screendatalen);
	}

	/**
	 * 算法特点：采用虚点和像素一一对应 -...+
	 * 
	 * @param m
	 */
	public void computeMove(int m) {
//		resetSPIOFF();
		/**
		 * 参照点距离中心点的新位置
		 */
		cpi.addCenterPointOffset(m);
		cpi.computeCenterPointStuff(diluteR);
		reDilute();
	}

	private MultiplyorAndDivisor diluteR = new MultiplyorAndDivisor();
	private Pluger pg;

	public Pluger getPluger() {
		return pg;
	}

	private void rebuf(int iPos, int len, IntBuffer i_adcbuf) {
		IntBuffer handlebuf = IntBuffer.wrap(i_adcbuf.array());

		int pos, limit;
		if (pg.isPlug()) {// && screendatalen <= pg.getScreendatalen()
			int filePointer = li.filePointer;
			CByteArrayInputStream cba = getDeepMemoryStorage();
			boolean shouldReverse = li.shouldReverse;
			int loadPos0 = li.loadPos0;
			int datalen = li.datalen;

			pg.processPlug(iPos, len, datalen, screendatalen, filePointer,
					shouldReverse, loadPos0, handlebuf, cba);
			if (screendatalen == 200 && screendatalen > pg.getScreendatalen()) {
				/** 手动修改 100ns 200(100MS/s) 的偏差 */
				cpi.setXBloc(cpi.getXBloc() - 7);
			} else if (screendatalen == 400
					&& screendatalen > pg.getScreendatalen()) {
				/** 手动修改 100ns 200(100MS/s) 的偏差 */
				cpi.setXBloc(cpi.getXBloc() - 3);
			}
		} else {
			pg.setGap(li.pixs / (double) screendatalen);
			int filePointer = li.filePointer;
			CByteArrayInputStream cba = getDeepMemoryStorage();
//			boolean shouldReverse = li.shouldReverse;
//			int loadPos0 = li.loadPos0;

			// 不进行插值，当初始档位不是拉伸的情况下
			retrieveBuf(iPos, 0, len, handlebuf, filePointer, cba);
//			restrictForReverse(handlebuf, shouldReverse, loadPos0);
		}
		pos = handlebuf.position();
		limit = handlebuf.limit();

		/** pos limit先设置limit，防止pos超过原来的limit报错 */
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
		/** len:真实adc点数加左右拉触发点100; beg:深存储数据中htp对应位置 */
		CByteArrayInputStream cba = getDeepMemoryStorage();
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
	 * 确定cpidx, rate, cpoff；计算xbloc
	 */
	private void reDilute() {
		MultiplyorAndDivisor md = diluteR;
		if (shouldPlugAsOriginal()) {
			// md = new MultiplyorAndDivisor(md.getMultiplyor() * screendatalen
			// / pg.getScreendatalen(), md.getDivisor());
		}
		Point p = cpi.reDilute2(databeg, li.datalen, md);
		i0 = p.x;
		i1 = p.y;
		vl.logln("xbloc: " + cpi.getXBloc());
		/**
		 * 可以给initLoad使用，替代reDilute，支持慢扫和拉触发的插值
		 */
		int datalen = li.datalen;
		int len = i1 - i0;
		vl.logln("i0: " + i0 + ", i1:" + i1 + ", len:" + len);
		/** i0的底限和i1的顶限，合起来就限制了完全超出屏幕外的情况 */
		if (len > 0) {
			if (i0 + len < datalen)
				len++;

			rebuf(i0, len, i_adcbuf);
		} else {
			i_adcbuf.limit(0);
			i_adcbuf.position(0);
			lastHandle(i_adcbuf);
		}
	}

//	/**
//	 * 为反相作调整
//	 * 
//	 * @param pos
//	 * @param limit
//	 */
//	public static final void restrictForReverse(IntBuffer i_adcbuf,
//			boolean reverse, int loadPos0) {
//		/** 交给画图屏幕，处理反相问题 */
//		reverse = false;
//
//		/** 在adc数据取出绘图前进行必要的反相处理 */
//		/** 内部也在这里把intArray转成byteArray，可同时进行两项处理 */
//		if (reverse) {
//			int pos = i_adcbuf.position();
//			int limit = i_adcbuf.limit();
//
//			int[] array = i_adcbuf.array();
//			final int dpos0 = loadPos0 << 1;
//			for (int i = pos; i < limit; i++) {
//				array[i] = dpos0 - array[i];
//			}
//		}
//	}

	VLog vl = new VLog();
	public boolean BeyondMax = false, BeyondMin = false;

	/** KNOW 限制adc数据点不越边界，从i_adcbuf存入b_adcbuf */
	private void lastHandle(IntBuffer i_adcbuf) {
		int pos = i_adcbuf.position();
		int limit = i_adcbuf.limit();
		// ArrayLogger.outArray2Logable(vl, i_adcbuf.array(), pos +
		// cpi.getXBloc()+450, 100);

		if (limit <= pos) {
			b_adcbuf.position(0);
			b_adcbuf.limit(0);
			return;
		}

		int[] array = i_adcbuf.array();

		BeyondMax = BeyondMin = false;
		byte[] barr = b_adcbuf.array();
		int j = 0, v;
		final byte max = (byte) getMax(), min = (byte) getMin();
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

		/** 反比关系，但仍会出现小数值丢失 */
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

	/** 拉伸到拉伸 */
	public void tbTranslate(BigDecimal pre, BigDecimal nex, int tbidx) {
		tbTranslate_(pre, nex, tbidx);

		/** 在这里关闭插值可在其它的插值时基缩放看到不插值的效果 */
		// pg.setPlug(false);
		reDilute();
	}

	/** 拉伸到压缩 */
	public void tbTranslate2C(BigDecimal pre, BigDecimal nex,
			CompressInfoUnit ciu, int tbidx) {
		// rate / nex1_1rate= nex1_1/ pre
		// nex1_1rate == 1

		BigDecimal nex1_1 = diluteR.multiplyByBD(pre);
		/** 先切换到D1:1的压缩情况，然后再切换到C */
		tbTranslate_(pre, nex1_1, tbidx);

		release();

		// cpoff无须传递出去
		ciu.fakeIn(cpi.getCenterPointIndex(), 1, li, 0);
		ciu.tbTranslate(nex1_1, nex, tbidx);
	}

	@Override
	public ByteBuffer getb_adcbuf() {
		return b_adcbuf;
	}

	public void fakeIn(int cpidx, int tbidx) {
		/** fake in的时候要看一开始载入的是否的Dilute */
		prepare();

		screendatalen = li.pixs;
		cpi.setCenterPointIndex(cpidx);
		diluteR = new MultiplyorAndDivisor(1, 1);

		/** 若要切换的那个档位未设置，则赋值0 */
		// if (spi_offs[tbidx] == invalidOff)
		// cpi.setCenterPointOffset(0);// setCenterPointOffset(spi_offs[tbidx]);
		cpi.setXBloc(0);
	}

	private CenterPointInfo cpi = new CenterPointInfo();

	public void drawView(Graphics g, int xoff, int hh, int wlen, int hortrgidx) {
		Graphics2D g2d = (Graphics2D) g;
		int et = StorageView.edgetall;// 括弧高
		int x0, x1, y0, y1;
		double r = diluteR.getDoubleValue();

		/** 横线拉到满，括弧相应缩小 */
		/** ?? (i0 / (double) li.datalen) - xbloc / (li.datalen * r) */
		x0 = (int) (wlen
				* ((i0 / (double) li.datalen) - getXoffset() / (li.datalen * r)) + xoff);
		x1 = x0 + (int) (wlen * li.pixs / (li.datalen * r));

		g2d.setColor(Color.LIGHT_GRAY);

		/** 括弧 */
		g2d.drawLine(x0, hh - et, x0, hh + et);// 左括弧
		if (x1 != x0) {
			g2d.drawLine(x1, hh - et, x1, hh + et);// 右括弧
		}

		/** 横线 */
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

	public int getScreendatalen() {
		return screendatalen;
	}

	@Override
	public int getDrawMode() {
		if (pg.isPlug()) {
			if (getScreendatalen() == pg.getScreendatalen())
				return WFDrawRTUtil.DrawMode1p;
			else if (getScreendatalen() < pg.getScreendatalen()) {
				/** 这里按初始载入的插值倍数插值，借用非插值的方式画图 */
				return WFDrawRTUtil.DrawModeDilute;
			} else {
				return WFDrawRTUtil.DrawModeDilute;
			}
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