package com.owon.uppersoft.vds.ui.slider;

import java.awt.Component;

import com.owon.uppersoft.vds.data.Point;

public interface SliderBarLocation {
	public static final int sliderheight = SymmetrySliderBar.sliderheight;
	public static final int sliderwidth = SymmetrySliderBar.sliderwidth;

	Point getSliderBarLocation(int x, int y, int xs, int ys, Component cp);
}