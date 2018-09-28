package com.owon.uppersoft.dso.mode.control;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.source.comm.effect.SubmitorFactory;
import com.owon.uppersoft.vds.core.aspect.base.IOrgan;
import com.owon.uppersoft.vds.core.aspect.control.DeepProvider;
import com.owon.uppersoft.vds.core.comm.effect.IPatchable;
import com.owon.uppersoft.vds.core.comm.effect.Submitable;
import com.owon.uppersoft.vds.util.Pref;

public class DeepMemoryControl implements IPatchable, IOrgan {

	private int deepIdx = 0;
	private int fpga_deepIdx = 0;
	private DeepProvider dp;

	public DeepMemoryControl(DeepProvider dp) {
		this.dp = dp;
		// DBG.dbgln("load deepIdx = " + deepIdx);
	}

	public void load(Pref p) {
		int idx = p.loadInt("DeepMemory");
		if (idx < 0 || idx >= dp.getDeepNumber())
			idx = 0;
		deepIdx = idx;
	}

	public void persist(Pref p) {
		p.persistInt("DeepMemory", deepIdx);
	}

	public int getDeepIdx() {
		return deepIdx;
	}

	public int restrictDMidx(int idx) {
		int deeplen = dp.getDeepNumber();
		if (idx < 0)
			idx = 0;
		else if (idx >= deeplen)
			idx = deeplen - 1;
		return idx;
	}

	public int getDeepDataLen() {
		return dp.getLength(deepIdx);
	}

	public String getDeepLabel() {
		return dp.getLabel(deepIdx);
	}

	public int getC_DeepIdx() {
		return fpga_deepIdx;
	}

	public void applyDeepIdx(int deepIdx) {
		this.deepIdx = deepIdx;
	}

	public void applyFPGADeepIdx() {
		fpga_deepIdx = deepIdx;
	}

	public void setDeepIdx(int deepIdx) {
		if (this.deepIdx == deepIdx || deepIdx < 0)
			return;
		this.deepIdx = deepIdx;
		ControlManager cm = Platform.getDataHouse().controlManager;

		cm.getCoreControl().updateCurrentSampleRate();
		cm.getCoreControl().updateHorTrgPosRange();
		cm.getTimeControl().restrictHorTrgPos();
	}

	public void c_setDeepIdx(int deepIdx) {
		setDeepIdx(deepIdx);
		Submitable sbm = SubmitorFactory.reInit();
		sbm.c_dm(deepIdx);
		sbm.apply();
	}

	@Override
	public void selfSubmit(Submitable sbm) {
		sbm.c_dm(deepIdx);
	}

	public boolean restrictDeepMemory() {
		boolean b = restrictShouldChange(deepIdx);
		if (b) {
			c_setDeepIdx(XYModeSupportOnly1DeepMemoryIndex);
		}
		return b;
	}

	/**
	 * 以后可能不只一档支持
	 */
	public static final int XYModeSupportOnly1DeepMemoryIndex = 0;

	/**
	 * 目前1k的情况不需要，其它情况都要
	 * 
	 * @return
	 */
	public static final boolean restrictShouldChange(int idx) {
		return idx == XYModeSupportOnly1DeepMemoryIndex ? false : true;
	}
}
