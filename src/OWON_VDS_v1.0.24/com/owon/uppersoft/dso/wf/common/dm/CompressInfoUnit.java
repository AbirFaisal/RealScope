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
 * CompressInfoUnit算法思路(已遗弃)：
 * 
 * 对结果集的分析4种情况：2.5(特殊)；压缩不在一屏需要分段压缩；压缩率在一屏一次压缩；压缩在一点
 * 
 * 
 * 
 * 方面1：为支持中心点缩放，需要知道中心点在存储深度中的位置信息，
 * 
 * 这里使用直接持有，只在压缩时计算屏幕左右边界及实际画图像素
 * 
 * 
 * 方面2：总会出现到数据头或是数据尾而造成的屏边空白，需要限位，往往先计算后限位
 * 
 * 但是先计算在大存储深度可能由于数值过大int溢出
 * 
 * 
 * 方面3：始终需要在采样点和像素点之间切换，以保证画图和压缩取点
 * 
 * 
 * 方面4：由方面2，方面3，考虑使用两个假设，
 * 
 * 一是不限数组边界，也就是数组索引看作可以为负，可以超过存储深度，只在压缩的最后一步限制，
 * 
 * 这样节省了缩放和平移中需要考虑的内容
 * 
 * 二是在实际数组索引点基础上建立虚点(理想的虚拟像素点)，即压缩后的虚数组的索引点，同样是不限数组边界的，
 * 
 * 压缩后的数值变小了不会溢出，而且压缩后的虚点索引可以对应到像素点(或虚拟的)上，方便计算缩进即水平触发位置
 * 
 * 
 * 方面5：2.5的情况采用了方面5的压缩率，每两个像素对应一个虚点索引，是对方面4的变通，
 * 
 * 这样产生了可能的平移偏差，无法始终对应到中心点，需要处理
 * 
 * 
 * 方面6：起始点不一定是压缩率的整数倍，中心点也就不一定是。而画图效果希望达到在进入该时基后，
 * 
 * 既和运行时一致(也就是从起始点开始压缩)，
 * 
 * 又在水平移动后保持一致(也就是从哪一点开始按倍压缩这个点就始终对应得上)，
 * 
 * 又基于方面5的设计，考虑截断头尾多余数据，在压缩环节补进数组，在画图环节补进缩进和余点画图处理
 * 
 * (或者对头尾多余数据，进行不足情况下的压缩)
 * 
 * 
 * 方面7：载入衔接，就是初始读入这种状态预先设定了压缩的内容，
 * 
 * 初始读入考虑头尾余点，画图缩进，设置所有状态为有效值，顺利进入平移和缩放
 * 
 * 
 * 方面8：时基切换衔接，考虑到可能从伸缩状态的另一者切换而来，需要类似载入衔接那样设置好所有状态的有效值
 * 
 * 
 * 方面9：每次压缩直接从文件读入，在平移和缩放后确定中心点对应的像素值，扩充到左右边界，重新压缩即可，
 * 
 * 没有实现增量压缩和临时文件缓存整个压缩点集的优化，性能尚可接收
 * 
 * 
 * 方面10：起始点大多为整数倍，未实测也未考虑慢扫
 * 
 * 
 * 特点：数据量大，情况多，虚点、边界、2.5等情况复杂，切换逻辑多
 * 
 * 
 * 
 * 
 * CompressInfoUnit算法思路(更新版)：
 * 
 * 抽象出虚拟像素簇，管理 实际像素点偏移 + 实际像素点压缩 + 实际像素点画图
 * 
 * 使用scpi(即屏幕相对中心的某点对应的数据点索引值)，及辅助的cpoff(scpi同屏幕中心点的偏移)，在时基切换的时候作为参数传递给不同的虚拟像素簇
 * 
 * 当压缩拉伸间切换时，总是将其在内部转换至1:1的情况下的屏幕中心点的scpi传入即可
 * 
 * 
 * 
 * 后来考虑可以这样改进：一次就在该档位下得到左右可完整压缩的虚拟点个数及对应的实际点边界
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
	 * Screen Center Point Index，存储深度中真实数据点索引值，这个数据点是离屏幕中心很接近的
	 */
	private int scpi;
	/**
	 * cpoff为scpi与中心的像素差，-...+ (-1, 0, 1)，只在平移过程中会出现非零情况
	 */
	private int cpoff;
	/**
	 * 屏左起第一个画图点的x坐标
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
		// 屏幕起始像素所在的数据点位置
		initPos = li.initPos;
		screendatalen = li.screendatalen;

		if (li.wasSlowMove) {
			// 优化传输的情况下仍可用
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

		// 在1k的情况下，使用拉触发偏移参数
		initPos += di.pi.pluggedTrgOffset;

		/**
		 * 
		 * 满屏数对半分，确定中心点，没法对半分的，也得对半分，这样才能时基缩放
		 * 
		 * 在慢扫时，如果是完整dm数据，则只需要更改scrbeg；否则在仅传有效数据的情况下，
		 * 
		 * initPos可能不是屏幕起始点对应的数据点数，需要考虑screendatalen-slowmove，
		 * 
		 * 或者调整initPos, screendatalen
		 * 
		 * 慢扫时，传完整数据
		 * 
		 * scpi一开始指向屏幕正中，平移过程中仍然使用最接近屏幕中心的数据点位置
		 */
		scpi = initPos + (screendatalen >> 1);
		vp = VirtualPoint.getInstance(screendatalen, pixs);
		if (vp.sample <= 0)
			logln("VirtualPoint.getInstance(screendatalen, pixs).sample == 0!!!");
		cpoff = 0;
		bufcpoff = 0;
		// 对压缩到一屏的情况另外处理
		computeXOffset();
	}

	protected void computeXOffset() {
		xoffset = vph.compressExecute(vp, 0, pixs, halfpixs + cpoff, scpi,
				b_adcbuf, databeg, datalen, ap, isCompress1Screen(),
				li.shouldReverse, li.loadPos0);

		confirmADCBuffer(b_adcbuf);
	}

	/**
	 * 水平触发所在位置
	 * 
	 * @param hortrgidx
	 * @return +...-
	 */
	private int getHorTrgPos(int hortrgidx) {
		// 这里由于压缩到一屏下的平移，cpoff被改变了，所以会出错
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
	 * 算法特定： 和像素一一对应，没有屏幕外需画的点，注意在起始点而非结束点画线，增量压缩 -...+
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

			// -...+，正的情况下scpi在变小
			scpi = scpi - m / pn * spl;
			int rem = m % pn;
			cpoff += rem;

			// System.out.println(String.format("%d, %d", scpi, cpoff));
			// 再次裁剪cpoff中可压缩的点
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
	 * 压缩到一屏情况下平移产生的缓冲cpoff余量
	 */
	private int bufcpoff = 0;

	/**
	 * 算法的前提：画压缩像素始终是从整数倍压缩率的位置开始画起
	 * 
	 * 因为可能是中间过程，所以不包含任何重新压缩或填充的过程
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
			 * 满屏数太大，无法用int容纳，此时必然是1:4压缩了，则直接使用其它方式构造vp
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
		/** 先切换到C1:1的压缩情况，然后再切换到D */
		tbTranslate_(pre, nex1_1);
		// TODO 这里即使中转还是会对1:1进行压缩，需要优化

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
		/** 存储深度所有点画到了ratelen个像素上 */
		if (vp == null)
			logln("ERR:CompressInfoUnit,VirtualPoint==null");
		int ratelen = (dl / vp.sample);
		int x0, x1, v, wratelen, wx0, y0, y1;

		g2d.setColor(Color.LIGHT_GRAY);
		/** 显示方式改为窗口移动而不是T位置移动 */
		if (isCompress1Screen()) {
			/** 括弧拉到满，横线相应缩小 */
			/** 总览区横线长 */
			wratelen = (int) ((double) ratelen * wlen / pixs);
			/** wx0:横线的起始位置; wlen:括弧的长; */
			wx0 = xoff + ((wlen - wratelen) >> 1);
			g2d.drawLine(wx0, hh, wratelen + wx0, hh);

			v = (int) ((double) hortrgidx * wratelen / dl + wx0);

			if (v <= xoff)
				v = xoff;
			else if (v >= xoff + wlen)
				v = xoff + wlen;

			int aw = GDefine.AREA_WIDTH;
			/** 括弧 */
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
			/** 横线拉到满，括弧相应缩小 */
			int xw = (int) ((double) (pixs) * wlen / ratelen);
			x0 = (int) ((double) scpi * wlen / dl - (xw >> 1) + xoff);
			x1 = x0 + xw;

			// System.err.println(String.format("%d,%d,%d,%d,%d,%d", scpi, wlen,
			// dl,
			// ratelen, pixs, xoff));

			/** 括弧 */
			g2d.drawLine(x0, hh - et, x0, hh + et);
			g2d.drawLine(x1, hh - et, x1, hh + et);

			/** 横线 */
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