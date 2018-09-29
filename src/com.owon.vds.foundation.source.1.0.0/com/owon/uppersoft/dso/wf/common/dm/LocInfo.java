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
 * LocInfo，算法前提和设计思路前提：
 * 
 * 用途：是支持停止情况和静态载入波形的情况，读入深存储(无慢扫)的数据，
 * 
 * 需要实现水平移动和时基切换带来的缩放，需要标记水平触发的位置，需要较好的画图效果。
 * 
 * 
 * 
 * 
 * 第一步：明确可能出现的所有情况，以下；
 * 
 * 通道开关、存储深度、时基和共同决定了采样率，从而也决定了满屏数
 * 
 * 拉伸还是压缩，伸缩率整数与否
 * 
 * 读入各种已知状态的参数，计算采样率和伸缩率表，建立结果集，才能明确适当效率的算法
 * 
 * 
 * 
 * 第二步：分别考虑拉伸和压缩，选用合适的内部结构和内存数组
 * 
 * 然后考虑是否可合并，这样对于外部需要进行的电压缩放、零点平移、3in1缩放、画图坐标切换自动适用
 * 
 * 
 * 
 * 第三步：实现跨越伸缩两种状态的时基切换，先完成伸缩各自的时基切换，
 * 
 * 再找到两种共有的临界状态，即压缩率==拉伸率==1的情况，把整个切换链路连起来
 * 
 * 
 * 
 * 第四步：实现伸缩，初测
 * 
 * 
 * 
 * 第五步：实现水平触发位置和顶端视图
 * 
 */
public abstract class LocInfo {

	public int initPos;
	public int screendatalen;
	public int slowmove;
	public int filePointer;
	public int datalen;

	/** 切换时基时存放前次的时基情况 */
	public BigDecimal bdtb;
	public BigDecimal bdfsl;// fullScreenLen

	public final int pixs, halfpixs;

	/** 满屏压缩率 */
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

		/** 在深存储时通过慢速移动数，计算新的filePointer，锁定有效波形区 */
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
			/** 在进入compress模式的时候，初始化设置databeg为和运行时同样算法下的值，然后在compress的任何情况下都是如此 */
			int r = datalen / pixs;
			if (r > 1) {
				slowmove -= slowmove % r;
				if (slowmove < 0)
					slowmove = 0;
			}
		}

		/** 尝试把1k满屏的情况视为压缩，这样Dilute的情况就少了，不用处理慢扫 */
		compress = becomeCompress(screendatalen);

		diu = createDiluteInfoUnit(this);
		ciu = new CompressInfoUnit();

		diu.setPlug(!compress);

		prepareBeforeInitLoadInfoUnit(cti);
		// System.err.println(screendatalen+", "+compress);

		diu.reinit();
		/** 1k个点默认交给diu来处理，但是插值只在<1k个点的情况下 */
		if (compress) {
			ciu.initLoad(this, cdi);
		} else {
			diu.initLoad(cdi, tbidx, mt);
		}
	}

	protected abstract void prepareBeforeInitLoadInfoUnit(DMInfo cti);

	protected abstract IDiluteInfoUnit createDiluteInfoUnit(LocInfo li);

	protected InfoUnit getInfoUnit() {
		if (isCompress()) {
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
		// 运行切到暂停，快速滚动鼠标轮，深存储完后有可能bdtb为空，也许是没完全载入就去调时基造成。
		BigDecimal bi = nexbdtb.divide(bdtb);// BigDecimal.divide(UnknownSource)
		BigDecimal nexfsl = bdfsl.multiply(bi);
		double dblnexfsl = nexfsl.doubleValue();
		boolean bC = becomeCompress(dblnexfsl);
		if (isCompress()) {
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
	 * 取不同情况下触发点相对屏幕中心的像素差，作为画图和获取时间值的算法源头
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

		if (isCompress()) {
			return ciu.getb_adcbuf();
		} else {
			return diu.getb_adcbuf();
		}
	}

	public double getGap() {
		if (isCompress()) {
			return ciu.getGap();
		} else {
			return (double) diu.getGap();
		}
	}

	public int getDrawMode() {
		if (isCompress()) {
			return ciu.getDrawMode();
		} else {
			return diu.getDrawMode();
		}
	}

}