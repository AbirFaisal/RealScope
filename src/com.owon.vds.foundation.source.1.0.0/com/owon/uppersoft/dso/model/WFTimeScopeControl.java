package com.owon.uppersoft.dso.model;

import java.math.BigDecimal;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.wf.WFTimeScopeContext;
import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.rt.WFDrawRTUtil;
import com.owon.uppersoft.vds.core.wf.peak.PKDetectDrawUtil;
import com.owon.uppersoft.vds.core.wf.rt.ChannelsTransportInfo;

/**
 * WFTimeScopeControl，通道时基统一改变的控制
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
		setHasZoom(true);

		BigDecimal[] bdtbs = cm.getMachineInfo().bdTIMEBASE;

		BigDecimal ovb = bdtbs[orgtbidx];
		BigDecimal nvb = bdtbs[idx];
		BigDecimal lvb = bdtbs[lastidx];

		BigDecimal rate = nvb.divide(ovb);
		nowgap = BigDecimal.valueOf(orggap).divide(rate).doubleValue();

		// 将中心点到数据起始点的距离，作为时基切换改变的值，然后转换为起始点的位置
		int hp = GDefine.AREA_WIDTH >> 1;
		int oxbeg = hp - getXboff();
		BigDecimal nxbeg = BigDecimal.valueOf(oxbeg).divide(nvb.divide(lvb));
		setXboff(hp - (int) nxbeg.doubleValue());

		// 注意慢扫
	}

	/**
	 * -...+，与网格左边界的距离
	 */
	private int xboff = 0;

	/**
	 * +...-
	 * 
	 * @param del
	 */
	public void addWaveFormsXloc(int del) {
		setXboff(getXboff() - del);
	}

	/**
	 * 在这里对慢扫需要略过的像素点相应地偏移画图起始位置
	 * 
	 * @param xb
	 * @param dm
	 * @return
	 */
	public int computeXoffset(int xb, int dm) {
		if (isZoom()) {
			xb += getXboff() + (int) (skipPoints * nowgap);
			return xb;
		}

		xb += getXboff();

		xb += getSlowMoveOffset(dm);
		return xb;
	}

	/**
	 * 由慢扫时需要略过的像素点，换算偏移的画图起始位置
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

		setXboff(0);
		setHasZoom(false);
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
	 * 各种深存储载入时的pk信息
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
	 * 仅用于RT的按波形点数进行区分的画图模式
	 */
	private int drawMode = -1;

	public int getDrawMode() {
		return drawMode;
	}

	/**
	 * RT时通过满屏数确定，这里的满屏数还可以通过计算得出
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
		setPKDetect(pkdetect);
		setPK_detect_typeByDrawMode(false);

		commonXboff = true;
	}

	public void loadDM(ChannelsTransportInfo cpi, boolean pkdetect,
			int drawMode, double gap) {
		setSkipPoints(0);
		setPKDetect(pkdetect);
		// setPK_detect_type(cpi.pk_detect_type);
		setDrawMode(drawMode);
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
