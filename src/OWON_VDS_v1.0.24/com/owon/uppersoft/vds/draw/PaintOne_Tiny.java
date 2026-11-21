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
				 * multiple frames occur only here
				 * 
				 * keep the ordinary drawing method for 2500 during peak detect, verify the effect, then consider whether to research a drawing method for peak detect that follows the sampling method more closely
				 * 
				 * corresponds to the case where the real adc acquires 2500 points for the full screen, with 5 pairs of peaks placed on 4 pixels
				 * 
				 * for the case of 1250 points, i.e. 250 points acquired for the full screen, it does not meet the requirements of peak detect, so it can be ignored
				 */
				WFDrawRTUtil.paintDrawMode(g2d, pixbuf, drawMode, xb, linkline);
			} else {
				PKDetectDrawUtil.paint_pkdetect(g2d, pixbuf, xb, yb, height,
						pk_detect_type, linkline);
			}

		} else if (statusType == 'M') {
			WFDrawRTUtil.paintByGap(g2d, pixbuf, xb, linkline, gap);
		} else {
			/** multiple frames occur only here */
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
			/** peak detect combined with normal trigger has a bug: the waveform is wrong after expanding in a fake stop */
			// System.err.println("2.pkdetect");
			if (pk_detect_type == PKDetectDrawUtil.PK_DETECT_TYPE_NO
			// || pk_detect_type == PKDetectDrawUtil.PK_DETECT_TYPE_2500_2_5p
			// || pk_detect_type == PKDetectDrawUtil.PK_DETECT_TYPE_1250_4_5p
			) {
				/**
				 * multiple frames occur only here
				 * 
				 * keep the ordinary drawing method for 2500 during peak detect, verify the effect, then consider whether to research a drawing method for peak detect that follows the sampling method more closely
				 * 
				 * corresponds to the case where the real adc acquires 2500 points for the full screen, with 5 pairs of peaks placed on 4 pixels
				 * 
				 * for the case of 1250 points, i.e. 250 points acquired for the full screen, it does not meet the requirements of peak detect, so it can be ignored
				 */
				WFDrawRTUtil.paintDrawMode(g2d, pixbuf, drawMode, xb, linkline);
			} else {
				PKDetectDrawUtil.paint_pkdetect(g2d, pixbuf, xb, yb, height,
						pk_detect_type, linkline);
			}
		} else if (wftsc.isZoom()) {
			// drawing for normal and single expansion
			// System.err.println("3.isZoom");
			WFDrawRTUtil.paintByGap(g2d, pixbuf, xb, linkline, wftsc
					.getZoomGap());
		} else {
			// System.err.println("4.drawMode: " + drawMode);
			// vl.logByteBuffer(pixbuf);
			/** multiple frames occur only here */
			WFDrawRTUtil.paintDrawMode(g2d, pixbuf, drawMode, xb, linkline);
		}

	}
}
