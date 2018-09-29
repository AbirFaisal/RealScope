package com.owon.uppersoft.vds.core.aspect.help;

import java.awt.image.BufferedImage;

import com.owon.uppersoft.vds.data.Point;

public interface AreaImageHelper {

	Point getDrawSize();

	void resetARGBBufferImage(BufferedImage pesistbi);

	BufferedImage createARGBScreenBufferedImage();

}