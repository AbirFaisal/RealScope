package com.owon.uppersoft.vds.draw;

import java.awt.Graphics2D;
import java.nio.IntBuffer;

import com.owon.uppersoft.dso.wf.WFTimeScopeContext;
import com.owon.uppersoft.vds.core.paint.IPaintOne;
import com.owon.uppersoft.vds.core.rt.WFDrawRTUtil;
import com.owon.uppersoft.vds.core.wf.peak.PKDetectDrawUtil;

public class PaintOne_Tiny implements IPaintOne {

	public PaintOne_Tiny() {
	}

	@Override
	public void paintRef(Graphics2D g2d, int drawMode, int xb,
			boolean linkline, int yb, int height, boolean pk_detect,
			IntBuffer pixbuf, int pk_detect_type, int statusType, double gap) {
		if (pk_detect) {
			if (pk_detect_type == PKDetectDrawUtil.PK_DETECT_TYPE_NO
					|| pk_detect_type == PKDetectDrawUtil.PK_DETECT_TYPE_2500_2_5p
					|| pk_detect_type == PKDetectDrawUtil.PK_DETECT_TYPE_1250_4_5p) {
				/**
				 * 只在这里出现多帧的情况
				 * 
				 * 保持2500在峰值检测时的普通画法，验证效果，再考虑是否针对峰值检测研究更遵循采样方式的画法
				 * 
				 * 对应真实adc采集满屏2500个点的情况，5对峰值放在4个像素点上
				 * 
				 * 对1250个点即采集满屏250个点的情况，因其达不到峰值检测的要求，故可不考虑
				 */
				WFDrawRTUtil.paintDrawMode(g2d, pixbuf, drawMode, xb, linkline);
			} else {
				PKDetectDrawUtil.paint_pkdetect(g2d, pixbuf, xb, yb, height,
						pk_detect_type, linkline);
			}

		} else if (statusType == 'M') {
			WFDrawRTUtil.paintByGap(g2d, pixbuf, xb, linkline, gap);
		} else {
			/** 只在这里出现多帧的情况 */
			WFDrawRTUtil.paintDrawMode(g2d, pixbuf, drawMode, xb, linkline);
		}
	}

	@Override
	public void paintONE(Graphics2D g2d, int drawMode, int xb,
			boolean linkline, int yb, int height, boolean pkdetect,
			WFTimeScopeContext wftsc, IntBuffer pixbuf, int pk_detect_type) {

		if (pixbuf == null || !pixbuf.hasRemaining())
			return;

		if (drawMode == WFDrawRTUtil.DrawModeDilute) {
			// System.err.println("1.DrawModeDilute");
			WFDrawRTUtil.paintByGap(g2d, pixbuf, xb, linkline, wftsc
					.getDiluteGap());
		} else if (pkdetect) {
			/** 峰值检测，遇上正常触发，存在bug，假停展开后波形错误 */
			// System.err.println("2.pkdetect");
			if (pk_detect_type == PKDetectDrawUtil.PK_DETECT_TYPE_NO
			// || pk_detect_type == PKDetectDrawUtil.PK_DETECT_TYPE_2500_2_5p
			// || pk_detect_type == PKDetectDrawUtil.PK_DETECT_TYPE_1250_4_5p
			) {
				/**
				 * 只在这里出现多帧的情况
				 * 
				 * 保持2500在峰值检测时的普通画法，验证效果，再考虑是否针对峰值检测研究更遵循采样方式的画法
				 * 
				 * 对应真实adc采集满屏2500个点的情况，5对峰值放在4个像素点上
				 * 
				 * 对1250个点即采集满屏250个点的情况，因其达不到峰值检测的要求，故可不考虑
				 */
				WFDrawRTUtil.paintDrawMode(g2d, pixbuf, drawMode, xb, linkline);
			} else {
				PKDetectDrawUtil.paint_pkdetect(g2d, pixbuf, xb, yb, height,
						pk_detect_type, linkline);
			}
		} else if (wftsc.isZoom()) {
			// 正常、单次展开的画图
			// System.err.println("3.isZoom");
			WFDrawRTUtil.paintByGap(g2d, pixbuf, xb, linkline, wftsc
					.getZoomGap());
		} else {
			// System.err.println("4.drawMode: " + drawMode);
			// vl.logByteBuffer(pixbuf);
			/** 只在这里出现多帧的情况 */
			WFDrawRTUtil.paintDrawMode(g2d, pixbuf, drawMode, xb, linkline);
		}

	}
}
