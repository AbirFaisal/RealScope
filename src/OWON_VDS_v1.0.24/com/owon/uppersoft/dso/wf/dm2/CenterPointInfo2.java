package com.owon.uppersoft.dso.wf.dm2;

import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.aspect.base.VLog;
import com.owon.uppersoft.vds.core.paint.PaintContext;
import com.owon.uppersoft.vds.data.MultiplyorAndDivisor;
import com.owon.uppersoft.vds.data.Point;

public class CenterPointInfo2 {
	public static final int DEFINE_AREA_WIDTH = GDefine.AREA_WIDTH;

	/** Index value of the reference point, x coordinate of cpidx, starting from the leftmost, -...+ */
	private int cpidx;
	/** Position of the reference point when the center point has value 0 -...+ */
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

		/** Approach the point closest to the screen center */
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

	/** Distance of the starting drawing point from the left boundary */
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
	 * Determine cpidx, rate, cpoff; compute xbloc
	 */
	public Point reDilute2(int databeg, int datalen,
			MultiplyorAndDivisor diluteR) {
		vl.logln("reDilute2");
		final int mul = diluteR.getMultiplyor();
		final int div = diluteR.getDivisor();

		logcp();
		/** Start and midpoint of the screen; if floating-point values occur here, digits after the decimal point may be dropped */
		double int_cpoff = cpoff;
		double x;
		int i = cpidx;

		/** i0, i1 are unbounded point indexes, not limited by the actual 0 value or the memory depth */
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
			x -= mul;
		}
		int v = (int) (x - -wc);
		xbloc = v;

		Point p = new Point(i0, i1);
		// Then limit back to the data boundary
		return p;
	}

	int databeg;

	public void setDatabegin(int databeg) {
		this.databeg = databeg;
	}

	public int getDataBegin() {
		return databeg;
	}
}