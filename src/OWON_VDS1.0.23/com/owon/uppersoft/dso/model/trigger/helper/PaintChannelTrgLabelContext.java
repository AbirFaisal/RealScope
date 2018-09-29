package com.owon.uppersoft.dso.model.trigger.helper;

import java.awt.Graphics2D;

import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.dso.wf.ChartScreenSelectModel;
import com.owon.uppersoft.vds.core.paint.ScreenContext;
import com.owon.uppersoft.vds.data.LocRectangle;

public class PaintChannelTrgLabelContext {
	public ScreenContext pc;
	public ChannelInfo ci;
	public LocRectangle lr;
	public Graphics2D g2d;
	public boolean lineLevel;

	public ChartScreenSelectModel cssm;

	public PaintChannelTrgLabelContext() {
	}

	/**
	 * 必须先设置，然后才能调用set方法
	 * 
	 * @param cssm
	 */
	public void prepare(Graphics2D g2d, ScreenContext pc, LocRectangle lr,
			ChartScreenSelectModel cssm) {
		this.g2d = g2d;
		this.pc = pc;
		this.lr = lr;
		this.cssm = cssm;
	}

	/**
	 * @param ci
	 */
	public void setChannelInfo(ChannelInfo ci) {
		this.ci = ci;
		lineLevel = (ci.getNumber() == cssm.getScreenSelectWFidx())
				&& cssm.shouldLine();
	}

}