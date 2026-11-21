package com.owon.uppersoft.dso.model;

import java.math.BigDecimal;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.wf.WFTimeScopeContext;
import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.rt.WFDrawRTUtil;
import com.owon.uppersoft.vds.core.wf.peak.PKDetectDrawUtil;
import com.owon.uppersoft.vds.core.wf.rt.ChannelsTransportInfo;

/**
 * WFTimeScopeControl, control for changing the timebase of all channels uniformly
 * 
 */
public abstract class WFTimeScopeControl implements WFTimeScopeContext {
	private boolean hasZoom;
	private double orggap, nowgap;

	protected ControlManager cm;

	public WFTimeScopeControl(ControlManager cm) {
		this.cm = cm;
	}

	public void setTimebaseIndex(int idx, int lastidx) {
		hasZoom = true;

		BigDecimal[] bdtbs = cm.getMachineInfo().bdTIMEBASE;

		BigDecimal ovb = bdtbs[orgtbidx];
		BigDecimal nvb = bdtbs[idx];
		BigDecimal lvb = bdtbs[lastidx];

		BigDecimal rate = nvb.divide(ovb);
		nowgap = BigDecimal.valueOf(orggap).divide(rate).doubleValue();

		// take the distance from the center point to the data start point as the value changed by the timebase switch, then convert it to the position of the start point
		int hp = GDefine.AREA_WIDTH >> 1;
		int oxbeg = hp - xboff;
		BigDecimal nxbeg = BigDecimal.valueOf(oxbeg).divide(nvb.divide(lvb));
		this.xboff = hp - (int) nxbeg.doubleValue();

		// note slow sweep
	}

	/**
	 * -...+, distance from the left boundary of the grid
	 */
	private int xboff = 0;

	/**
	 * +...-
	 * 
	 * @param del
	 */
	public void addWaveFormsXloc(int del) {
		this.xboff = getXboff() - del;
	}

	/**
	 * Here, offset the drawing start position accordingly for the pixels that need to be skipped during slow sweep
	 * 
	 * @param xb
	 * @param dm
	 * @return
	 */
	public int computeXoffset(int xb, int dm) {
		if (hasZoom) {
			xb += xboff + (int) (skipPoints * nowgap);
			return xb;
		}

		xb += xboff;

		xb += getSlowMoveOffset(dm);
		return xb;
	}

	/**
	 * Convert the pixels that need to be skipped during slow sweep into an offset of the drawing start position
	 */
	public int getSlowMoveOffset(int drawMode) {
		int offset = 0;

		if (skipPoints > 0) {
			switch (drawMode) {
			case WFDrawRTUtil.DrawMode4in1:
				offset = (skipPoints >> 2);
				break;
			case WFDrawRTUtil.DrawMode1p:
				offset = skipPoints;
				break;
			case WFDrawRTUtil.DrawMode2p:
				/** vds1022 */
				offset = (skipPoints >> 1);
				break;
			}
		}
		return offset;
	}

	private int orgtbidx, orgfsl;

	private void saveOrginalTimebaseStatus(int screendatalen) {
		orgtbidx = cm.getTimeControl().getTimebaseIdx();
		orgfsl = screendatalen;

		this.xboff = 0;
		hasZoom = false;
		orggap = GDefine.AREA_WIDTH / (double) orgfsl;
	}

	private int skipPoints;

	private void computeSkipPoints(int screendatalen, int slowMove, boolean slow) {
		if (slow) {
			skipPoints = screendatalen - slowMove;
		} else {
			skipPoints = 0;
		}
		// System.err.println("skipPoints: "+skipPoints);
	}

	/**
	 * pk information when various deep memories are loaded
	 */
	private boolean PK_Detect;

	private void setPKDetect(boolean pk) {
		PK_Detect = pk;
	}

	public boolean isPK_Detect() {
		return PK_Detect;
	}

	private int pk_detect_type = PKDetectDrawUtil.PK_DETECT_TYPE_NO;

	public int getPK_detect_type() {
		return pk_detect_type;
	}

	protected void setPK_detect_type(int type) {
		if (PK_Detect)
			pk_detect_type = type;
	}

	protected void setPK_detect_typeByDrawMode(boolean dm) {
		if (PK_Detect) {
			pk_detect_type = getPKType(dm, drawMode);
		}
	}

	protected abstract int getPKType(boolean dm, int drawMode);

	public int getSkipPoints() {
		return skipPoints;
	}

	private void setSkipPoints(int skipPoints) {
		this.skipPoints = skipPoints;
	}

	@Override
	public double getZoomGap() {
		return nowgap;
	}

	@Override
	public boolean isZoom() {
		return hasZoom;
	}

	private void setHasZoom(boolean b) {
		hasZoom = b;
	}

	/**
	 * Drawing mode used only in RT, distinguished by waveform point count
	 */
	private int drawMode = -1;

	public int getDrawMode() {
		return drawMode;
	}

	/**
	 * RTIn RT it is determined by the full-screen count; the full-screen count here can also be obtained by calculation
	 * 
	 * @param screendatalen
	 */
	private void setDrawModeByScreendatalen(int screendatalen) {
		this.drawMode = WFDrawRTUtil.getDrawModeFromLength(screendatalen);
	}

	private void setDrawMode(int dm) {
		this.drawMode = dm;
	}

	private double gap;

	public void loadRT(int screendatalen, int slowMove, boolean slow,
			boolean pkdetect, boolean fft) {
		if (fft) {
			drawMode = WFDrawRTUtil.DrawModeDouble;
		} else {
			setDrawModeByScreendatalen(screendatalen);
		}
		saveOrginalTimebaseStatus(screendatalen);
		computeSkipPoints(screendatalen, slowMove, slow);
		PK_Detect = pkdetect;
		setPK_detect_typeByDrawMode(false);

		commonXboff = true;
	}

	public void loadDM(ChannelsTransportInfo cpi, boolean pkdetect,
			int drawMode, double gap) {
		this.skipPoints = 0;
		PK_Detect = pkdetect;
		// setPK_detect_type(cpi.pk_detect_type);
		this.drawMode = drawMode;
		setPK_detect_typeByDrawMode(true);
		this.gap = gap;

		// System.err.println(drawMode);
		// System.err.println(gap);

		commonXboff = false;
	}

	private boolean commonXboff = true;

	public boolean useCommonXboff() {
		return commonXboff;
	}

	private void setXboff(int xboff) {
		this.xboff = xboff;
	}

	private int getXboff() {
		return xboff;
	}

	@Override
	public double getDiluteGap() {
		return gap;
	}

}
