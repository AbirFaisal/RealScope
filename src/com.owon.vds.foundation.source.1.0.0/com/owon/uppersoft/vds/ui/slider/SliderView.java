package com.owon.uppersoft.vds.ui.slider;

import java.awt.Graphics2D;

public class SliderView implements ISliderView {

	private SliderDelegate delegate;

	public SliderView(int min, int max, int v, SliderDelegate delegate) {
		this.min = min;
		this.max = max;
		this.delegate = delegate;

		/** KNOW 这里可能需要对min,max,v三者进行关系限界判断 */
		value = v;
	}

	private int min, max;
	private int value;
	private int RightBorder = 500, LeftBorder = -500;

	/**
	 * 供内部改变值时调用，可以fire监听事件
	 * 
	 * @param v
	 */
	private void setValue(int v) {
		int newv = v, old = value;
		if (newv > max) {
			newv = max;
		} else if (newv < min) {
			newv = min;
		}

		if (newv == old)
			return;
		value = newv;
		// System.out.println("newv:"+newv);
		delegate.valueChanged(old, newv);
	}

	public static final int critical20 = 20, critical30 = 30, critical40 = 40,
			critical50 = 50, critical60 = 60, critical70 = 70, critical80 = 80;

	@Override
	public void adjustAdd(int del) {
		int tmp = Math.abs(del);
		// System.out.println(String.format("del:%d,tmp:%d", del, tmp));
		/** 这里往左为负 */
		if (value > RightBorder) {
			if (tmp < critical40) {
			} else if (tmp >= critical40) {
				tmp = tmp * 2;
			}
		} else if (value >= LeftBorder && value <= RightBorder) {
			if (tmp < critical20) {
				tmp = tmp >> 1;
				if (tmp == 0)
					tmp = 1;
			} else if (tmp >= critical20 && tmp < critical40) {
				tmp = 10;
			} else if (tmp >= critical40 && tmp < critical60) {
				tmp = 15;
			} else if (tmp >= critical60 && tmp < critical80) {
				tmp = 20;
			}
		} else {
			if (tmp < critical20) {
			} else if (tmp >= critical20 && tmp < critical30) {
			} else if (tmp >= critical30 && tmp < critical40) {
				tmp = tmp * 2;
			} else if (tmp >= critical40 && tmp < critical50) {
				tmp = tmp * 5;
			} else if (tmp >= critical50 && tmp < critical60) {
				tmp = tmp * 10;
			} else if (tmp >= critical60 && tmp < critical70) {
				tmp = tmp * 100;
			} else if (tmp >= critical70 && tmp < critical80) {
				tmp = tmp * 1000;
			}
			// System.out.println(value + "," + tmp);
			int v = value + (del > 0 ? tmp : -tmp);
			if ((v > LeftBorder && value < LeftBorder)) {// v>RightBorder
				setValue(LeftBorder);
				return;
			} else if (v < RightBorder && value > RightBorder) {// v<LeftBorder
				setValue(RightBorder);
				return;
			}
		}
		// System.out.println(String.format("value:%d,tmp:%d", value, tmp));
		add(del > 0 ? tmp : -tmp);
	}

	private void add(int del) {
		setValue(value + del);
	}

	@Override
	public void actionOff() {
		delegate.actionOff();
	}

	@Override
	public void setDefault() {
		setValue(0);
		delegate.actionOff();
	}

	public void paintSelf(Graphics2D g) {
	}
}