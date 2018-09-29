package com.owon.uppersoft.vds.core.wf;

import java.awt.Graphics;
import java.math.BigDecimal;
import java.nio.ByteBuffer;

import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.dso.wf.common.dm.LocInfo;
import com.owon.uppersoft.vds.core.aspect.control.Pos0_VBChangeInfluence;
import com.owon.uppersoft.vds.core.aspect.control.VoltageProvider;
import com.owon.uppersoft.vds.core.machine.MachineType;
import com.owon.uppersoft.vds.core.paint.ScreenContext;
import com.owon.uppersoft.vds.core.rt.AverageContainer;
import com.owon.uppersoft.vds.core.wf.dm.DMDataInfo;
import com.owon.uppersoft.vds.core.wf.dm.DMInfo;
import com.owon.uppersoft.vds.util.Pref;

/**
 * WaveFormInfo，通道相关信息，无论通道是否打开都保存着唯一的一份，可用于波形判断是否相同
 */
public abstract class WaveFormInfo {
	public AverageContainer ac = new AverageContainer();

	public ChannelInfo ci;
	private LocInfo dmli;

	public WaveFormInfo(int number, VoltageProvider vp,
			Pos0_VBChangeInfluence pvi) {
		ci = new ChannelInfo(number, vp, pvi);

		dmli = createLocInfo();
	}

	protected abstract LocInfo createLocInfo();

	public void load(Pref p) {
		ci.load(p);
	}

	public void persist(Pref p) {
		ci.persist(p);
	}

	@Override
	public String toString() {
		return ci.getName();
	}

	/**
	 * DM时通过数据buffer填充画图buffer的内容 // int hcenter, boolean screenMode_3
	 * 
	 * @param wf
	 * @param pc
	 */
	protected void resetDMIntBuf(ScreenContext pc, int delpos0,
			BigDecimal vbmulti) {
		int hcenter = pc.getHcenter();
		boolean screenMode_3 = pc.isScreenMode_3();

		/** 使用载入时的零点位置和变化后的零点位置之差以告知波形被移动了 */
		int yb;
		if (screenMode_3) {
			yb = hcenter + delpos0;
		} else {
			yb = hcenter + (delpos0 << 1);
		}

		resetIntBuf_dm(vbmulti, yb, screenMode_3);
	}

	public void prepareDM(ScreenContext pc, DMDataInfo cdi, BigDecimal tbbd,
			int tbidx, int vbidx, MachineType mt, DMInfo cti) {
		dmli.prepareDM(pc, cdi, tbbd, vbidx, tbidx, cdi.shouldInverse, mt, cti);
		resetDMIntBufDefaultY(pc);
	}

	public ByteBuffer getADC_Buf() {
		return dmli.getADC_Buf();
	}

	public void drawView_dm(Graphics g, int gap, int hh, int wlen, int horTrgIdx) {
		dmli.drawView(g, gap, hh, wlen, horTrgIdx);
	}

	public void releaseDM() {
		dmli.release();
	}

	public void addWaveFormsXloc(int del, ScreenContext pc) {
		dmli.addWaveFormsXloc(del);
		resetDMIntBufDefaultY(pc);
	}

	public void setTimebaseIndex(int idx, int lastidx, ScreenContext pc) {
		dmli.setTimebaseIndex(idx);
		resetDMIntBufDefaultY(pc);
	}

	protected void resetDMIntBufDefaultY(ScreenContext pc) {
		dmli.resetIntBuf(BigDecimal.valueOf(1), pc.getHcenter(),
				pc.isScreenMode_3());
	}

	public int getDrawMode() {
		return dmli.getDrawMode();
	}

	public double getDMGap() {
		return dmli.getGap();
	}

	public int getXOffset_DM() {
		return dmli.getXoffset();
	}

	public int getDataLen() {
		return dmli.datalen;
	}

	public int getDMFilePointer() {
		return dmli.filePointer;
	}

	protected void resetIntBuf_dm(BigDecimal vbmulti, int yb,
			boolean screenMode_3) {
		dmli.resetIntBuf(vbmulti, yb, screenMode_3);
	}

}