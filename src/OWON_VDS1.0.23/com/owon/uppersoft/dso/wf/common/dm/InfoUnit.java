package com.owon.uppersoft.dso.wf.common.dm;

import java.awt.Graphics;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public interface InfoUnit {
	void resetDMIntBuf(BigDecimal vbmulti, int yb, boolean screenMode_3);

	void save2RefIntBuffer(IntBuffer adcbuf, int delpos0);

	void tbTranslate(BigDecimal bdtb, BigDecimal nexbdtb, int tbidx);

//	int getHorTrgPos(int hortrgidx);

	void drawView(Graphics g, int xoff, int hh, int wlen, int hortrgidx);

	void release();

	/** -..htp..+ */
	void computeMove(int m);

	int getXoffset();

	void confirmADCBuffer(ByteBuffer bbuf);

	/**
	 * 转化为对应到RT时的画图模式
	 * 
	 * @return
	 */
	int getDrawMode();

	double getGap();

	ByteBuffer getb_adcbuf();
}