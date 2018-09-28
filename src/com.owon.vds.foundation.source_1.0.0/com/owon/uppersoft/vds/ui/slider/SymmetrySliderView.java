package com.owon.uppersoft.vds.ui.slider;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Stroke;

public class SymmetrySliderView implements ISliderView {
	public Stroke Stroke3 = new BasicStroke(3);
	
	private final Dimension sz;

	private int vRange;
	private int vPos, middle0;
	private boolean vertical;
	private Color vco;
	private int defaultValue;
	private SliderDelegate delegate;

	public SymmetrySliderView(Dimension sz, int max, int defaultValue, int v,
			boolean vertical, Color co, SliderDelegate delegate) {
		this.sz = new Dimension(sz);

		this.delegate = delegate;

		this.vertical = vertical;
		this.vco = co;

		this.defaultValue = defaultValue;

		/** KNOW 这里可能需要对max, defaultValue, v三者进行关系限界判断 */

		this.max = max;
		vRange = vertical ? sz.height : sz.width;
		middle0 = vRange >> 1;
		value = v;
		vPos = v * vRange / max;

		if (vertical) {
			restrict(vPos, sz.height - tail);
		} else {
			restrict(vPos, sz.width - tail);
		}
	}

	private int max;
	private int value;

	/**
	 * 供内部改变值时调用，可以fire监听事件
	 * 
	 * @param v
	 */
	private void setValue(int v) {
		int newv = v, old = this.value;
		if (newv > max - 1) {
			newv = max - 1;
		} else if (newv < 0) {
			newv = 0;
		}

		if (newv == old)
			return;

		value = newv;
		delegate.valueChanged(old, newv);

		int sp = newv * vRange / max;
		vPos = sp;

		if (vertical) {
			restrict(vPos, sz.height - tail);
		} else {
			restrict(vPos, sz.width - tail);
		}
	}

	public static final int critical1 = 100;

	@Override
	public void adjustAdd(int del) {
		int tmp = del >> 2;// 降低对称(垂直)Slider的滑动速度到1/4
		if (Math.abs(tmp) < critical1)
			tmp = tmp / 2;
		if (tmp == 0)
			tmp = (del > 0) ? 1 : -1;

		add(tmp);
	}

	private void add(int del) {
		setValue(value + del);
	}

	public static final int Gap = 2, tail = 8;
	private int ga;

	private void restrict(int v, int len) {
		int x = 0;
		if (v < tail) {
			x = tail - v + 1;
		} else if (v > len) {
			x = v - len + 1;
		} else {
		}
		ga = x + Gap;
	}

	@Override
	public void actionOff() {
		delegate.actionOff();
	}

	@Override
	public void setDefault() {
		setValue(defaultValue);
		delegate.actionOff();
	}

	public void paintSelf(Graphics2D g) {
		g.setColor(Color.DARK_GRAY);
		int w = sz.width, h = sz.height;
		g.setStroke(Stroke3);
		if (vertical) {
			g.drawLine(Gap, middle0, w - Gap, middle0);
		} else {
			g.drawLine(middle0, Gap, middle0, h - Gap);
		}

		g.setColor(vco);
		if (vertical) {
			g.drawLine(ga, vPos, w - ga, vPos);
		} else {
			g.drawLine(vPos, ga, vPos, h - ga);
		}
	}
}