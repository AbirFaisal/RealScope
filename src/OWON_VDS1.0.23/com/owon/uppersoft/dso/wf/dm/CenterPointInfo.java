package com.owon.uppersoft.dso.wf.dm;

import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.aspect.base.VLog;
import com.owon.uppersoft.vds.core.paint.PaintContext;
import com.owon.uppersoft.vds.data.MultiplyorAndDivisor;
import com.owon.uppersoft.vds.data.Point;

public class CenterPointInfo {
	public static final int DEFINE_AREA_WIDTH = GDefine.AREA_WIDTH;

	/** 参照点的索引值，cpidx的x坐标，起始于最左边，-...+ */
	private int cpidx;
	/** 中心点为0值时参照点的位置-...+ */
	private double cpoff;

	public int getCenterPointIndex() {
		return cpidx;
	}

	public void setCenterPointIndex(int v) {
		cpidx = v;
	}

	public double getCenterPointOffset() {
		return cpoff;
	}

	public void setCenterPointOffset(double v) {
		cpoff = v;
	}

	public void addCenterPointOffset(int del) {
		cpoff += del;
	}

	public void setCenterPoint(int cpidx, int cpoff) {
		this.cpidx = cpidx;
		this.cpoff = cpoff;
	}

	protected void computeCenterPointStuff(MultiplyorAndDivisor diluteR) {
		vl.logln("computeCenterPointStuff");
		final int mul = diluteR.getMultiplyor();
		final int div = diluteR.getDivisor();

		double cpoff = this.cpoff;
		int cpidx = this.cpidx;

		int int_cpoff = (int) cpoff;

		/** 逼近到离屏幕最中心的点 */
		if (cpoff >= 0) {
			int num = int_cpoff / mul;
			cpoff -= num * mul;
			cpidx -= num * div;
		} else {
			int num = -int_cpoff / mul;
			cpoff += num * mul;
			cpidx += num * div;
		}

		this.cpidx = cpidx;
		this.cpoff = cpoff;
		logcp();
	}

	private void logcp() {
		vl.logln("cpidx: " + cpidx + ", cpoff: " + cpoff);
	}

	VLog vl = new VLog();

	/** 起始画图点离左边界的距离 */
	private int xbloc;

	public int getXBloc() {
		return xbloc;
	}

	public void setXBloc(int v) {
		xbloc = v;
	}

	public static final int pw = DEFINE_AREA_WIDTH, p0 = 0,
			wc = PaintContext.hwidth;

	/**
	 * 确定cpidx, rate, cpoff；计算xbloc
	 */
	public Point reDilute2(int databeg, int datalen,
			MultiplyorAndDivisor diluteR) {
		vl.logln("reDilute2");
		final int mul = diluteR.getMultiplyor();
		final int div = diluteR.getDivisor();

		logcp();
		/** 屏幕的起始和中点，这里如果遇上浮点数，可能被省略小数点后信息 */
		double int_cpoff = cpoff;
		double x;
		int i = cpidx;

		/** i0，i1为无边界的点索引，不受具体0值和存储深度限制 */
		int i0 = i, i1 = i + 5;

		x = int_cpoff;
		while (x < wc) {
			i1 += div;
			x += mul;
		}
		if (i1 > datalen)
			i1 = datalen;

		x = int_cpoff;
		while (x > -wc) {
			i0 -= div;
			x -= mul;
		}
		while (i0 < databeg) {
			i0 += div;
			// x -= mul; 这里修复了从慢扫未半屏停止下来展开到dilute情况下存在的bug
			x += mul;
		}
		setXBloc((int) (x - -wc));

		Point p = new Point(i0, i1);
		// 再限制回数据边界
		return p;
	}
}