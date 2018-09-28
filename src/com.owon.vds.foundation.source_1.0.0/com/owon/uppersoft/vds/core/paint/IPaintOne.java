package com.owon.uppersoft.vds.core.paint;

import java.awt.Graphics2D;
import java.nio.IntBuffer;

import com.owon.uppersoft.dso.wf.WFTimeScopeContext;

public interface IPaintOne {
	void paintONE(Graphics2D g2d, int drawMode, int xb, boolean linkline,
			int yb, int height, boolean pkdetect, WFTimeScopeContext wftsc,
			IntBuffer pixbuf, int pk_detect_type);

	void paintRef(Graphics2D g2d, int drawMode, int xb, boolean linkline, int yb,
			int height, boolean pk_detect, IntBuffer pixbuf,
			int pk_detect_type, int statusType, double gap);
}