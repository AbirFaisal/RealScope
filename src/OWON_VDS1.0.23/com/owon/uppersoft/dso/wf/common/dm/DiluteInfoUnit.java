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
 * KNOW 参考CompressInfoUnit的算法思路，补充：
 * 
 * frct(a:b)用于保存在不同时基下的(中心点和前一采样点间距:拉伸率)，切换的过程保证这一比率不变来定中心点
 * 
 * 
 * 特点：数据量少，取点快，切换时基的时候定中心点是关键
 * 
 * 先用虚索引点确定数据边界及x偏移，再插值
 * 
 * 插值：
 * 
 * 插值只在接受深存储文件的时候根据满屏个数<=200的情况插正弦值一次，
 * 
 * 可能的满屏数为200、100、50、40、25、20、10，
 * 
 * 拉伸率即5、10、20、25、40、50、100，
 * 
 * 插值后点像素间距为1/2、1、2、2.5、4、5、10，
 * 
 * 画图为压缩、1比1、拉伸画、拉伸画，
 * 
 * 在此基础上对拉伸画再线性插值到1比1
 * 
 * 
 * 
 * 
 * 处理可以透明地看待Pluger，只在最后获得画图数据点的时候使用其插值后的点
 * 
 * 慢扫的时候没有拉触发和插值
 * 
 */
public abstract class DiluteInfoUnit implements InfoUnit, IDataMaxMin,
		IDiluteInfoUnit {

	public static final int DEFINE_AREA_WIDTH = GDefine.AREA_WIDTH;

	/** re为0时先画3，否则画2 */
	public final int re = 0;

	/** 起始画图点离左边界的距离 */
	private int xbloc;

	/** 参照点的索引值 */
	private int cpidx;
	/** 中心点为0值时参照点的位置-...+ */
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
		setCenterPointIndex(cpidx);
		setCenterPointOffset(cpoff);
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
		return getXBloc();
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
	 * 水平触发所在位置
	 * 
	 * @param hortrgidx
	 * @return
	 */
	private int getHorTrgPos(int hortrgidx) {
		int del = hortrgidx - getCenterPointIndex();
		int v = diluteR.multiplyByInt(del) + getCenterPointOffset();

		// 2.5 MISS 这里可能出现浮点数丢失
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
	 * 用于记忆每一个时基档位的cpoff，在没有作平移的情况下不会修改，如果未被设置才去填充
	 */
	private void resetSPIOFF() {
		spi_offs = new int[li.getMachineInfo().TIMEBASE.length];
		Arrays.fill(spi_offs, 0, spi_offs.length, invalidOff);
	}

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

		/** 载入深存储时为无需插值的时基，则后续也不进行插值 */
		int pixs = li.pixs;
		screendatalen = li.screendatalen;
		pg.initPlugInfo(cdi, mt);
		/** 理论满屏数 <= 屏幕像素宽度，拉伸 */
		/** 拉伸率 */
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

			/** 这里使用满屏点数，假定插值对Dilute不可见 */
			diluteR.set(pixs, screendatalen);
			computeCenterPointStuff(getCenterPointOffset());
			// spi_offs[tbidx] = getCenterPointOffset();
			setXBloc(0);

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
		int cpidx = getCenterPointIndex();
		if (right) {
			cpidx -= num;
			cpoff = re;
		} else {
			cpidx += num;
			cpoff = -re;
		}
		setCenterPointIndex(cpidx);
		setCenterPointOffset(cpoff);
		// System.err.println("cpidx: " + cpidx);
	}

	/**
	 * 算法特点：采用虚点和像素一一对应 -...+
	 * 
	 * @param m
	 */
	public void computeMove(int m) {
		resetSPIOFF();
		/**
		 * 参照点距离中心点的新位置
		 */
		int nxo = getCenterPointOffset() + m;
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

			// 不进行插值，当初始档位不是拉伸的情况下
			retrieveBuf(iPos, 0, len, handlebuf, filePointer, cba);
			restrictForReverse(handlebuf, shouldReverse, loadPos0);
		}

		int pos = handlebuf.position();
		int limit = handlebuf.limit();

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
	 * 确定cpidx, rate, cpoff；计算xbloc
	 */
	private void reDilute() {
		// System.err.println("reDilute");
		// PaintContext pc = Platform.getMainWindow().getChartScreen()
		// .getPaintContext();
		/** 屏幕的起始和中点 */
		final int pw = DEFINE_AREA_WIDTH, p0 = 0, wc = PaintContext.hwidth;

		/** 数据的起始索引 */
		/** i0，i1为无边界的点索引，不受具体0值和存储深度限制，x1为不可画的右边界 */
		final int cpidx = getCenterPointIndex();
		// System.err.println("cpidx: " + cpidx);
		i1 = i0 = cpidx;
		loadcp();
		/** cpidx的x坐标，起始于最左边，-...+ */
		final int cpx = wc + getCenterPointOffset();
		// ...

		if (cpx < pw) {
			// i1扩展到屏幕右边界
			int range = pw - cpx;
			int num = diluteR.divideDirectByInt(range);
			i1 = cpidx + num;

			// if (range % diluteR.getMultiplyor() != 0)
			i1 += diluteR.getDivisor();
		}
		if (cpx > p0) {
			// i0扩展到屏幕左边界，初始载入时，也因此xbloc得到设置
			int range = cpx - p0;
			int num = diluteR.divideDirectByInt(range);
			int rem = diluteR.modeByIntOffset(range);
			i0 = cpidx - num;
			int xb = p0;
			// if (rem != 0) {
			i0 -= diluteR.getDivisor();
			xb += rem - diluteR.getMultiplyor();
			// }
			setXBloc(xb);
		}

		loadcp();
		restrictAfterDilute();
	}

	/**
	 * 可以给initLoad使用，替代reDilute，支持慢扫和拉触发的插值
	 */
	private void restrictAfterDilute() {
		// System.err.println("restrictAfterDilute");
		// System.err.println("i(0,1):" + i0 + ", " + i1);
		// i1 += 10;// 屏尾多补一些

		int datalen = li.datalen;
		int data0 = databeg;

		// 再限制回数据边界
		if (i1 > datalen)
			i1 = datalen;

		// 再限制回数据边界
		if (i0 < data0) {
			/** MISS 这里可能出现浮点数丢失 */
			addXBloc(diluteR.multiplyByInt(data0 - i0));
			i0 = data0;
		}
		// System.err.println("xbloc: " + xbloc);

		int len = i1 - i0;
		// System.out.println("i0: " + i0 + ", i1:" + i1 + ", len:" + len);
		/** i0的底限和i1的顶限，合起来就限制了完全超出屏幕外的情况 */
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
	 * 为反相作调整
	 * 
	 * @param pos
	 * @param limit
	 */
	public static final void restrictForReverse(IntBuffer i_adcbuf,
			boolean reverse, int loadPos0) {
		/** 交给画图屏幕，处理反相问题 */
		reverse = false;

		/** 在adc数据取出绘图前进行必要的反相处理 */
		/** 内部也在这里把intArray转成byteArray，可同时进行两项处理 */
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

	/** KNOW 限制adc数据点不越边界，从i_adcbuf存入b_adcbuf */
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
		// DBG.configArray(barr, 0, j);
		confirmADCBuffer(b_adcbuf);
	}

	private void tbTranslate_(BigDecimal pre, BigDecimal nex, int tb) {
		// rate, cpoff两个变化
		MultiplyorAndDivisor md = MultiplyorAndDivisor.getFrom1_2_5TypeDivison(
				nex, pre);

		/** 反比关系，但仍会出现小数值丢失 */
		/** * preRate:nextRate=nextTb:preTb */
		MultiplyorAndDivisor nextRate = diluteR.divide_MD(md);
		/** * nextSdlen:preSdlen=nextTb:preTb */
		screendatalen = md.multiplyByInt(screendatalen);
		int cpoff;

		cpoff = getCenterPointOffset();

		/** 保存当前档位的cpoff */
		spi_offs[tbidx] = cpoff;

		// System.err.println("cur:" + tbidx + " tb:" + tb);
		if (spi_offs[tb] == invalidOff) {
			/** 若要切换的那个档位未设置，则根据本档位计算得出 */
			// System.err.println("invalid: " + cpoff);
			/** MISS 这里可能出现浮点数丢失 */
			cpoff = (int) nextRate.multiplyByDouble(diluteR
					.divideByDouble(cpoff));

			// System.err.println("to: " + cpoff);
		} else {
			/** 若要切换的那个档位已设置，则直接使用 */
			cpoff = spi_offs[tb];
			// System.err.println("valid: " + cpoff);
		}
		tbidx = tb;
		setCenterPointOffset(cpoff);

		diluteR = nextRate;
	}

	/** 拉伸到拉伸 */
	public void tbTranslate(BigDecimal pre, BigDecimal nex, int tbidx) {
		tbTranslate_(pre, nex, tbidx);

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
		ciu.fakeIn(getCenterPointIndex(), 1, li, 0);
		ciu.tbTranslate(nex1_1, nex, tbidx);
	}

	public void fakeIn(int cpidx, int tbidx) {
		/** fake in的时候要看一开始载入的是否的Dilute */
		prepare();

		screendatalen = li.pixs;
		setCenterPointIndex(cpidx);
		diluteR = new MultiplyorAndDivisor(1, 1);

		/** 若要切换的那个档位未设置，则赋值0 */
		if (spi_offs[tbidx] == invalidOff)
			setCenterPointOffset(0);// setCenterPointOffset(spi_offs[tbidx]);
		setXBloc(0);
	}

	public void drawView(Graphics g, int xoff, int hh, int wlen, int hortrgidx) {
		Graphics2D g2d = (Graphics2D) g;
		int et = StorageView.edgetall;// 括弧高
		int x0, x1, y0, y1;
		double r = diluteR.getDoubleValue();

		/** 横线拉到满，括弧相应缩小 */
		/** ?? (i0 / (double) li.datalen) - xbloc / (li.datalen * r) */
		x0 = (int) (wlen
				* ((i0 / (double) li.datalen) - getXBloc() / (li.datalen * r)) + xoff);
		x1 = x0 + (int) (wlen * li.pixs / (li.datalen * r));

		g2d.setColor(Color.LIGHT_GRAY);

		// System.err.println(x0 + ", " + x1);
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
		DBG.outprintln(",xbloc: " + getXBloc());
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
		int screendatalen = getScreendatalen();
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
		int screendatalen = getScreendatalen();

		if (pg.canPaintSinePlug(screendatalen)) {
			return pg.getGap();
		} else
			return diluteR.getDoubleValue();
	}

}