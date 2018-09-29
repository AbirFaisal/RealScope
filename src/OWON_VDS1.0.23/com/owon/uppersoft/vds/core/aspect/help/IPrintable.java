package com.owon.uppersoft.vds.core.aspect.help;

import java.awt.Graphics2D;

public interface IPrintable {

	void printView(Graphics2D g2, int width, int height);

	int getWidth();

	int getHeight();
}