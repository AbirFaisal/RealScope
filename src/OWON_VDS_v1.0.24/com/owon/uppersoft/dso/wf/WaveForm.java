package com.owon.uppersoft.dso.wf;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.owon.uppersoft.dso.function.ref.ReferenceFile;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.model.WFTimeScopeControl;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.model.trigger.TriggerSet;
import com.owon.uppersoft.dso.model.trigger.VoltsensableTrigger;
import com.owon.uppersoft.dso.ref.IRefSource;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.util.ui.LineUtil;
import com.owon.uppersoft.dso.wf.rt.RTLocInfoManager;
import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.aspect.IView;
import com.owon.uppersoft.vds.core.aspect.help.WF;
import com.owon.uppersoft.vds.core.data.CByteArrayInputStream;
import com.owon.uppersoft.vds.core.measure.MeasureADC;
import com.owon.uppersoft.vds.core.paint.ScreenContext;
import com.owon.uppersoft.vds.core.wf.WaveFormInfo;
import com.owon.uppersoft.vds.core.wf.rt.ChannelDataInfo;
import com.owon.uppersoft.vds.core.wf.rt.LoadMedia;

/**
 * WaveForm, waveform, draws from the points provided
 * 
 * RT: gets the interpolated data points 1k~4k; DM: gets the original sample points 25~10M
 * 
 */
public class WaveForm implements IView, WF, IRefSource {

	@Override
	public int getPos0() {
		return wfi.ci.getPos0();
	}

	@Override
	public int getWaveType() {
		return ReferenceFile.RefFile_Normal;
	}

	public boolean isGround() {
		return wfi.ci.isGround();
	}

	public boolean isOn() {
		return wfi.ci.isOn();
	}

	public Color getColor() {
		return wfi.ci.getColor();
	}

	@Override
	public MeasureADC getMeasureADC() {
		return madc;
	}

	@Override
	public int getProbeMultiIdx() {
		return wfi.ci.getProbeMultiIdx();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof WaveForm) {
			WaveForm wfo = (WaveForm) obj;
			return wfi.ci.getNumber() == wfo.wfi.ci.getNumber();
		}
		return false;
	}

	public int getChannelNumber() {
		return wfi.ci.getNumber();
	}

	public boolean isInverted() {
		return wfi.ci.isInverse();
	}

	@Override
	public String toString() {
		return wfi.ci.getName();
	}

	public int getVoltbaseIndex() {
		return wfi.ci.getVoltbaseIndex();
	}

	/**
	 * Data: adc - pos0
	 * 
	 * @return
	 */
	public int getPos0ForADC() {
		return wfi.ci.getPos0();
	}

	public int getPos0onChart(ScreenContext pc) {
		int hc = pc.getHcenter();
		if (pc.isScreenMode_3())
			return hc - wfi.ci.getPos0();
		else
			return hc - (wfi.ci.getPos0() << 1);
	}

	private MeasureADC madc;
	public WaveFormInfo wfi;
	public DataHouse dh;
	private ControlManager cm;
	public boolean onShowPos0 = false;

	private RTLocInfoManager rtlim;

	public WaveForm(DataHouse dh, WaveFormInfo wfi) {
		this.dh = dh;
		this.wfi = wfi;
		cm = dh.controlManager;
		rtlim = new RTLocInfoManager(this);
		madc = new MeasureADC();
	}

	/**
	 * The reference waveform needs to provide extra info on which xb to start drawing from
	 * 
	 * @param adcbuf
	 */
	public IntBuffer save2RefIntBuffer() {
		return rtlim.save2RefIntBuffer();
	}

	/**
	 * Currently the final pixel array for drawing; needs to be modified at real run time and for deep memory
	 * 
	 * for use by recording and PF
	 * 
	 * @return
	 */
	public ByteBuffer getADC_Buffer() {
		return rtlim.rtliADC_Buffer();
	}

	public ByteBuffer getFFT_Buffer() {
		return getADC_Buffer();
	}

	public ByteBuffer getNextFrameADCBuffer(int i) {
		return rtlim.getNextFrameADCBuffer(i);
	}

	public boolean isADCBeyondMax() {
		return rtlim.isRtADC_MaxBeyond();
	}

	public boolean isADCBeyondMin() {
		return rtlim.isRtADC_MinBeyond();
	}

	public int computeMiddle() {
		return rtlim.computeMiddle();
	}

	public void reduceFrame() {
		rtlim.restrictFrames(1, getChannelNumber());
	}

	/** KNOW this variable saves the zero point position at load time; load time means reading DM in, and the initial state when playing back a recorded waveform */
	private int loadPos0;

	public void prepareRTNormalPaint(ChannelDataInfo cdi, LoadMedia cti, int yb) {
		ScreenContext pc = getScreenContext();
		ChannelInfo ci = wfi.ci;
		this.basevbidx = ci.getVoltbaseIndex();

		/** bBuf holds the original data, intBuf holds the actual pixel points that include the zero point info */
		/** yb is passed in with dmli.loadPos0 and the current pos0 change, which gives the effect of loading a recorded normal waveform */
		/** since inversion is handled, this method is called only once when loading data */

		rtlim.resetRTIntBuf(cdi, pc, dh.getWaveFormManager().getSkipPoints(),
				yb, cti, vbmulti);
	}

	public boolean setVoltBaseIndex(final int vbidx, final boolean tranWithVB) {
		ChannelInfo ci = wfi.ci;

		int change = ci.getValidVotageChangeIndex(vbidx);
		if (change < 0)
			return false;

		ci.c_setVoltage(change, cm.getResetPersistenceRunnable());
		if (tranWithVB) {
			update_Voltbase(change);
		}

		return true;
	}

	private void update_Voltbase(int vbidx) {
		updateVBMulti(vbidx);
		resetNormalIntBuf(getScreenContext());
		cm.pcs.firePropertyChange(PropertiesItem.REPAINT_CHARTSCREEN, null,
				null);
	}

	private ScreenContext getScreenContext() {
		return cm.paintContext;
	}

	public void setZeroYLoc(int yl, boolean commit, boolean moveWithPos0) {
		ScreenContext pc = getScreenContext();
		ChannelInfo ci = wfi.ci;
		ci.c_setZero(yl, commit);

		// System.err.println(p0+","+p1);
		if (moveWithPos0) {
			resetNormalIntBuf(pc);
		}
	}

	public void updateVBMulti(int vbidx) {
		vbmulti = cm.getMachineInfo().getVoltagesBDRatioBetween(basevbidx,
				vbidx);
	}

	// TODO check that basevbidx may be problematic
	public int basevbidx = 0;
	private BigDecimal vbmulti = BigDecimal.valueOf(1);

	// RT<->STOP, play
	public void resetVbmulti() {
		this.vbmulti = BigDecimal.valueOf(1);
	}

	// RT->STOP, RT->DM, OFFDM, OFFPLAY
	/**
	 * as the zero point position corresponding to the current adc data acquisition
	 */
	public void saveFirstLoadPos0() {
		loadPos0 = wfi.ci.getPos0();
	}

	public BigDecimal getVbmulti() {
		return vbmulti;
	}

	public int getYb(ScreenContext pc) {
		int hcenter = pc.getHcenter();
		int pos0 = getPos0ForADC();
		boolean screenMode_3 = pc.isScreenMode_3();
		int yb;
		// if(getChannelNumber() == 0)System.err.println(pos0+", "+ loadPos0);
		if (screenMode_3) {
			yb = hcenter - pos0 + (int) (loadPos0 * vbmulti.doubleValue());
		} else {
			yb = hcenter - (pos0 << 1)
					+ (int) ((loadPos0 << 1) * vbmulti.doubleValue());
		}
		return yb;
	}

	private void resetNormalIntBuf(ScreenContext pc) {
		// System.out.println(getFirstLoadPos0() + ", " + getVbmulti());
		/** use the difference between the zero point position at load time and the changed zero point position to tell that the waveform was moved */
		rtlim.resetIntBuf(vbmulti, getYb(pc), pc.isScreenMode_3());
	}

	@Override
	public void adjustView(ScreenContext pc, Rectangle bound) {
		/** RT: just wait for the next frame; various kinds of lock-based support can be added later */
		if (!pc.allowLazyRepaint())
			resetNormalIntBuf(pc);
	}

	/**
	 * @return default zero point position when deep memory is loaded
	 */
	public int getFirstLoadPos0() {
		return loadPos0;
	}

	public void paintItem(Graphics2D g2d, ScreenContext pc, Rectangle r,
			ControlManager cm, boolean onFront) {
		int yb = getPos0onChart(pc);
		paintItem(g2d, pc, r, cm, onFront, yb);
	}

	private void paintItem(Graphics2D g2d, ScreenContext pc, Rectangle r,
			ControlManager cm, boolean onFront, int yb) {
		/** Below is the case of being connected to the device: get real-time data for drawing, fast refresh */
		if (!isOn())
			return;
		ChannelInfo ci = wfi.ci;
		g2d.setColor(getColor());

		int y = r.y;
		int bottom = r.y + r.height;
		/** Draw the left ruler, drawing top, bottom, middle in turn */
		LineUtil.paintChannelLabel(yb, y, bottom, g2d,
				String.valueOf(ci.getNumber() + 1), 2, onFront);

		/** Draw the zero point display on the right side of the left ruler */
		if (onShowPos0) {
			LineUtil.paintOnShowPos0(g2d, pc, dh.divUnits, ci.getPos0(), yb, r);
		}
	}

	@Override
	public void paintView(Graphics2D g2d, ScreenContext pc, Rectangle r) {
		/** Below is the case of being connected to the device: get real-time data for drawing, fast refresh */
		if (!isOn())
			return;

		boolean linkline = dh.isLineLink();

		// System.err.println("pv pkdet"+pc.pkdetect);

		g2d.setColor(getColor());

		/**
		 * For persistence drawing a fixed Rectangle would be easier, but the inheritance structure cannot be broken conveniently for now,
		 * so a fixed Rectangle is used directly inside WaveForm, which makes the smallest change
		 */
		int xb = r.x;
		// if (xb != 12)
		// System.err.println(xb);
		Shape tmp = g2d.getClip();
		g2d.setClip(r);
		/**
		 * The drawing methods are divided by scale and memory depth, handled when importing data and on shift/zoom operations; the drawing site just maps to a specific drawing mode
		 * 
		 * M case: slow sweep is left for later handling
		 */
		WFTimeScopeControl wftsc = dh.getWaveFormManager()
				.getWFTimeScopeControl();
		boolean pkdetect = wftsc.isPK_Detect();

		int drawMode = wftsc.getDrawMode();

		if (wftsc.useCommonXboff())
			xb = wftsc.computeXoffset(xb, drawMode);
		else
			xb += dm_xoffset;

		rtlim.paintKinds(g2d, drawMode, xb, linkline, r, pkdetect, wftsc, pc);
		g2d.setClip(tmp);
	}

	private int dm_xoffset = 0;

	public void setDm_xoffset(int dm_xoffset) {
		this.dm_xoffset = dm_xoffset;
	}

	/**
	 * Operation triggered when double-clicking on the trigger level etc.
	 */
	public void doubleClickOnLevel() {
		setTrg50Percent(cm.getTriggerControl());
	}

	/**
	 * Set the trigger level to the 50% position of the waveform peak-to-peak value; here it only sets the calculation to be done when the next data comes in
	 */
	public void setTrg50Percent(TriggerControl trgc) {
		if (!cm.is50percentAvailable())
			return;
		TriggerSet ts = trgc.getTriggerSetOrNull(wfi.ci.getNumber());
		if (ts == null || !ts.isVoltsenseSupport())
			return;

		/** The Trg50% calculation was done; if a valid value is obtained it is set, and either way the next Trg50% calculation is turned off */
		int v = rtlim.getTrg50percentValue();
		if (v == Integer.MAX_VALUE)
			return;

		VoltsensableTrigger vt = (VoltsensableTrigger) ts.getTrigger();
		boolean changed = vt.c_setVoltsense(v);
		if (changed)
			trgc.getVTPatchable().submiteVoltsense(getChannelNumber(), vt);
		cm.pcs.firePropertyChange(PropertiesItem.REPAINT_CHARTSCREEN, null,
				null);
		cm.pcs.firePropertyChange(PropertiesItem.UPDATE_VOLTSENSE, -1,
				getChannelNumber());
		/** KNOW set first, then finish, a send is always executed once */
	}

	/**
	 * Set the trigger level to the 50% position of the waveform peak-to-peak value; here it only sets the calculation to be done when the next data comes in
	 */
	public void setTrgEdgeMiddle(TriggerControl trgctl) {
		TriggerSet ts = trgctl.getTriggerSetOrNull(wfi.ci.getNumber());
		if (ts == null || !ts.isCurrentTrigger_Edge())
			return;

		/** The Trg50% calculation was done; if a valid value is obtained it is set, and either way the next Trg50% calculation is turned off */
		int v = rtlim.getTrg50percentValue();
		if (v == Integer.MAX_VALUE)
			return;

		VoltsensableTrigger vt = (VoltsensableTrigger) ts.getTrigger();
		int last = vt.c_getVoltsense();
		// System.err.println(last+" | "+v);

		/** deviation within 1 small division */
		if (Math.abs(last - v) < 5)
			return;

		boolean changed = vt.c_setVoltsense(v);
		if (changed)
			trgctl.getVTPatchable().submiteVoltsense(getChannelNumber(), vt);
		cm.pcs.firePropertyChange(PropertiesItem.REPAINT_CHARTSCREEN, null,
				null);
		cm.pcs.firePropertyChange(PropertiesItem.UPDATE_VOLTSENSE, -1,
				getChannelNumber());
		/** KNOW set first, then finish, a send is always executed once */
	}

	/**
	 * Set the trigger level to the 50% position of the waveform peak-to-peak value; here it only sets the calculation to be done when the next data comes in
	 */
	public int getTrg50Percent() {
		if (!cm.is50percentAvailable())
			return Integer.MAX_VALUE;

		/** The Trg50% calculation was done; if a valid value is obtained it is set, and either way the next Trg50% calculation is turned off */
		int v = rtlim.getTrg50percentValue();
		return v;
	}

}