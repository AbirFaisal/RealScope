package com.owon.uppersoft.dso.function.ref;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.File;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.vds.core.aspect.control.VoltageProvider;
import com.owon.uppersoft.vds.core.control.MathControl;
import com.owon.uppersoft.vds.core.paint.ScreenContext;

public interface IReferenceWaveForm {
	void setObjIndex(int idx);

	int getObjIndex();

	int getVBIndex();

	int getTbIdx();

	void adjustView(ScreenContext pc, Rectangle bound);

	void paintView(Graphics2D g2d, ScreenContext pc, Rectangle r);

	void paintItem(Graphics2D g2d, ScreenContext pc, Rectangle r, boolean b);

	String getIntVoltageLabel_mV(VoltageProvider vp, MathControl mc);

	void persistRefFile(ControlManager cm, File refwavfile, int rtscreendatalen);

	void resetRTIntBuf(int hcenter, boolean screenMode_3);
}