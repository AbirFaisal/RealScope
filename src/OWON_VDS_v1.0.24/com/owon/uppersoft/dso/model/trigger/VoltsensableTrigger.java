package com.owon.uppersoft.dso.model.trigger;

import java.awt.Color;
import java.awt.Graphics2D;

import com.owon.uppersoft.dso.control.TrgLevelCheckHandler;
import com.owon.uppersoft.dso.model.trigger.common.Voltsensor;
import com.owon.uppersoft.dso.model.trigger.helper.PaintChannelTrgLabelContext;
import com.owon.uppersoft.dso.util.ui.LineUtil;
import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.comm.effect.Submitable;
import com.owon.uppersoft.vds.core.paint.ScreenContext;
import com.owon.uppersoft.vds.data.LocRectangle;
import com.owon.uppersoft.vds.util.Pref;
import com.owon.uppersoft.vds.util.format.UnitConversionUtil;

public abstract class VoltsensableTrigger extends AbsTrigger {

	public VoltsensableTrigger(TrgTypeDefine idx, int halfRange) {
		super(idx);
		vs = new Voltsensor(halfRange);
	}

	public int coupling = 0;
	private int sweep = 0;

	private Voltsensor vs;

	public boolean c_setVoltsense(int v) {
		return vs.c_setVoltsense(v);
	}

	public int c_getVoltsense() {
		return vs.c_getVoltsense();
	}

	public Voltsensor getVoltsensor() {
		return vs;
	}

	public boolean c_setVoltsenseWithoutSync(int volt) {
		return vs.c_setVoltsense(volt);
	}

	@Override
	public void loadProperties(String prefix, Pref p) {
		String txt = getName();
		coupling = p.loadInt(prefix + txt + ".coupling");
		setSweep(p.loadInt(prefix + txt + ".sweep"));
		c_setVoltsenseWithoutSync(p.loadInt(prefix + txt + ".voltsense"));
	}

	@Override
	public void persistProperties(String prefix, Pref p) {
		String txt = getName();
		p.persistInt(prefix + txt + ".coupling", coupling);
		p.persistInt(prefix + txt + ".sweep", sweep);
		p.persistInt(prefix + txt + ".voltsense", vs.c_getVoltsense());
	}

	@Override
	public void paintIcon(Graphics2D g2d) {
	}

	@Override
	public String getLabelText(boolean inverse, int voltbase, int pos0) {
		// used as the trigger level text
		double volt = c_getVoltsense() - pos0;
		volt = volt * voltbase / GDefine.PIXELS_PER_DIV;
		return UnitConversionUtil.getSimplifiedVoltLabel_mV(inverse ? -volt
				: volt);
		// voltsense + "pix";
	}

	/**
	 * Increment or decrement the specified parameter
	 * 
	 * @param del
	 *            Send a command to change only if non-zero
	 * @param type
	 * @return class name of the trigger event
	 */
	public boolean handelIncr(int del, TrgCheckType type) {
		if (del != 0) {
			int v = vs.c_getVoltsense() + del;
			// System.out.println(getVoltsense());
			// System.out.println(del);
			return c_setVoltsense(v);
			// System.out.println();
		} else
			return false;
	}

	public boolean doCheckTrgLevel(TrgLevelCheckHandler checkHandler) {
		int v = c_getVoltsense();
		return checkHandler.checkAroundTrgAndHandle(v,
				TrgCheckType.VoltsenseOver, TrgCheckType.NotOver);
	}

	/**
	 * Draw the trigger level marker
	 * 
	 */
	protected void paintChannelVoltsenseLabel(PaintChannelTrgLabelContext pctlc) {
		Graphics2D g2d = pctlc.g2d;
		ScreenContext pc = pctlc.pc;

		ChannelInfo ci = pctlc.ci;
		LocRectangle lr = pctlc.lr;
		boolean lineLevel = pctlc.lineLevel;
		boolean arrow = pctlc.cssm.isDrawArrow();
		boolean inverted = ci.isInverse();
		int pos0 = ci.getPos0();
		Color c = ci.getColor();

		int vs = c_getVoltsense();

		/** For the trigger level, the one that draws the line is the one that draws the box */
		boolean showDetail = lineLevel && !pctlc.cssm.isTrgInfoControlActive();

		int y = 0;

		// draw the box value information
		int voltbase = ci.getVoltageLabel().getValue();
		int p0 = ci.getPos0();
		boolean inverse = ci.isInverse();

		String detailInfo = getLabelText(inverse, voltbase, p0);

		// for inverted drawing just flip it, placed before 3in1
		if (inverted) {
			vs = ChannelInfo.getLevelFromPos0(vs, pos0);
		}

		if (pc.isScreenMode_3()) {
		} else {
			vs = vs << 1;
		}

		y = pc.getHcenter() - vs;
		g2d.setColor(c);
		LineUtil.paintTrgLevel(y, lr, g2d, c, lineLevel, arrow, "T");

		/**
		 * Draw the info box of the trigger level marker
		 */
		if (detailInfo != null && showDetail)
			LineUtil.paintTrgLevelDetail(g2d, lr, y, detailInfo, c);
	}

	/**
	 * Draw the trigger level marker
	 * 
	 */
	public void paintChannelTrgLabel(PaintChannelTrgLabelContext pctlc) {
		paintChannelVoltsenseLabel(pctlc);
	}

	public abstract void submitVoltsense(int mode, int chl, Submitable sbm);

	public void c_setSweep(int sweep, TriggerControl tc) {
		this.sweep = sweep;
		tc.doSumbitTrgSweep(sweep);
	}

	public int setSweep(int sweep) {
		return this.sweep = sweep;
	}

	public int getSweep() {
		return sweep;
	}
}
