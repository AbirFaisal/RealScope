package com.owon.uppersoft.vds.core.zoom;

import java.awt.Graphics2D;

import com.owon.uppersoft.vds.ui.slider.ISliderView;

public class ZBordersSliderView implements ISliderView {
//	private SliderDelegate2 delegate;

//	public ZBordersSliderView(SliderDelegate2 delegate) {
//		this.delegate = delegate;
//	}

	@Override
	public void adjustAdd(int delta) {
//		delegate.valueIncret(delta);
	}

	@Override
	public void paintSelf(Graphics2D g) {
	}

	@Override
	public void setDefault() {
//		delegate.onReset0();
	}

	@Override
	public void actionOff() {
	}
}
