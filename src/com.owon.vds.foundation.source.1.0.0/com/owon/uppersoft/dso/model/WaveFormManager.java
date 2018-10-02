package com.owon.uppersoft.dso.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.ResourceBundle;

import com.owon.uppersoft.dso.function.FFTView;
import com.owon.uppersoft.dso.function.PFRuleManager;
import com.owon.uppersoft.dso.function.ReferenceWaveControl;
import com.owon.uppersoft.dso.function.measure.MeasureWFSupport;
import com.owon.uppersoft.dso.function.perspective.CompositeWaveForm;
import com.owon.uppersoft.dso.function.perspective.XYView;
import com.owon.uppersoft.dso.function.record.OfflineChannelsInfo;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.machine.aspect.IMultiWFManager;
import com.owon.uppersoft.dso.mode.control.FFTControl;
import com.owon.uppersoft.dso.mode.control.TimeControl;
import com.owon.uppersoft.dso.model.trigger.helper.TriggerLevelDelegate;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.dso.wf.ChartScreenSelectModel;
import com.owon.uppersoft.dso.wf.ON_WF_Iterator;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.aspect.Decorate;
import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.core.aspect.control.ITimeControl;
import com.owon.uppersoft.vds.core.paint.ScreenContext;
import com.owon.uppersoft.vds.core.wf.WaveFormInfo;
import com.owon.uppersoft.vds.core.wf.dm.DMInfo;
import com.owon.uppersoft.vds.core.wf.rt.ChannelsTransportInfo;
import com.owon.uppersoft.vds.data.LocRectangle;
import com.owon.uppersoft.vds.util.LocalizeCenter;

/**
 * WaveFormManager，Management waveform
 */
public abstract class WaveFormManager implements Decorate, MeasureWFSupport,
		Localizable {

	public static final int RollOverCheckGap = 10;
	public int freqFreshCount;
	protected WFTimeScopeControl wfTimeScopeControl;
	protected ControlManager controlManager;
	private DataHouse dataHouse;
	private XYView xyView;
	private FFTView fftView;
	private CompositeWaveForm compositeWaveForm;
	private ReferenceWaveControl referenceWaveControl;
	private IMultiWFManager iMultiWFManager;
	private ArrayList<WaveForm> offWfs;
	private PFRuleManager ruleManager;
	private boolean hideDraw = false;
	/**
	 * Can be used to force refresh hardware frequency meter
	 */
	private boolean forcefreshFreq = false;
	private WaveFormInfoControl wfic;

	public WaveFormManager(DataHouse dataHouse) {
		this.dataHouse = dataHouse;
		controlManager = dataHouse.controlManager;

		wfic = controlManager.getWaveFormInfoControl();
		wfic.setDataHouse(dataHouse);
		retainClosedWaveForms();
		xyView = new XYView(this, controlManager.displayControl);
		fftView = new FFTView(this, controlManager);

		LocalizeCenter lc = controlManager.getLocalizeCenter();
		lc.addPrimeTextLocalizable(this);

		compositeWaveForm = new CompositeWaveForm(
				this,
				controlManager.mathControl,
				controlManager.getCoreControl()
				.getVoltageProvider(),
				controlManager);

		referenceWaveControl = controlManager.rwc;
		ruleManager = controlManager.ruleManager;
		iMultiWFManager = createMultiWFManager(controlManager);
		wfTimeScopeControl = createWFTimeScopeControl();
		freqFreshCount = controlManager.computeFreqTimes;

		controlManager.pcs.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				String pn = evt.getPropertyName();
				if (pn.equals(ITimeControl.onTimebaseEffect)) {
					int newidx = (Integer) evt.getNewValue();
					int oldidx = (Integer) evt.getOldValue();
					setWaveFormTimebaseIndex(newidx, oldidx);
				}
			}
		});
	}

	public WaveFormInfoControl getWaveFormInfoControl() {
		return wfic;
	}

	public boolean isMath(int chl) {
		return !(chl >= 0 && chl < controlManager.getSupportChannelsNumber());
	}

	public boolean isMathSupport() {
		return controlManager.getSupportChannelsNumber() > 1;
	}

	@Override
	public void localize(ResourceBundle rb) {
		xyView.localize(rb);
		fftView.localize(rb);
	}

	protected abstract IMultiWFManager createMultiWFManager(ControlManager cm);

	protected abstract WFTimeScopeControl createWFTimeScopeControl();

	public boolean is3in1On() {
		return xyView.isOn() || controlManager.getFFTControl().isFFTon();
	}

	public void setZeroYLoc(WaveForm wf, int yl, boolean commit) {
		boolean moveWithPos0 = false;
		if (dataHouse.isDMLoad()) {
			moveWithPos0 = true;
		} else if ((controlManager.allowTransformScreenWaveForm_Ready())
				|| dataHouse.allowTransformScreenWaveFormVertical()) {
			moveWithPos0 = true;
		}

		wf.setZeroYLoc(yl, commit, moveWithPos0);
	}

	/**
	 * Under normal, single-shot, the voltage changes until all instructions are sent and then acknowledged.
	 * <p>
	 * And scaling is done regardless of whether there is a trigger or not
	 * <p>
	 * Trg'd->Ready is longer when the trigger is normal. In order to ensure that the zoom can
	 * be supported during the period, only the conditions can be scaled without judgment.
	 * <p>
	 * Zooming during this time
	 */
	public boolean setVoltBaseIndex(WaveForm wf, int vb) {
		boolean tranWithVB = false;
		if (dataHouse.isDMLoad()) {
			tranWithVB = true;
		} else if ((controlManager.allowTransformScreenWaveForm_Ready())
				|| dataHouse.allowTransformScreenWaveFormVertical()) {
			tranWithVB = true;
		}

		return wf.setVoltBaseIndex(vb, tranWithVB);
	}

	@Override
	public WaveForm getWaveForm(int idx) {
		return wfic.getWaveForm(idx);
	}

	public String getClosedChannelName(int c1, int c2) {
		String chName = "";
		WaveForm wf1 = getWaveForm(c1);
		WaveForm wf2 = getWaveForm(c2);

		if (!wf1.isOn()) {
			chName += wf1.toString();
		}
		if (c1 != c2 && !wf2.isOn()) {
			if (chName.length() != 0) {
				chName += ", ";
			}
			chName += wf2.toString();
		}
		if (chName.length() != 0)
			chName = " " + chName + " ";
		return chName;
	}

	public void retainClosedWaveForms() {
		// offWfs = null;
		WaveFormInfoControl wfic = controlManager.getWaveFormInfoControl();
		offWfs = wfic.getClosedWaveForms();
	}

	public boolean isNoWFDataFilled(WaveForm wf) {
		if (controlManager.isRuntime())
			return false;
		return offWfs.contains(wf);
	}

	/**
	 * KNOW Use the index value to get the channel in real time,
	 * you can guarantee that the data that is currently acquired is used.
	 */
	public WaveForm getCHX() {
		return getWaveForm(controlManager.displayControl.wfx);
	}

	public WaveForm getCHY() {
		return getWaveForm(controlManager.displayControl.wfy);
	}

	public WaveForm getM1() {
		return getWaveForm(controlManager.mathControl.m1);
	}

	public WaveForm getM2() {
		return getWaveForm(controlManager.mathControl.m2);
	}

	public int getMathOperation() {
		return controlManager.mathControl.operation;
	}

	public void setMathOperation(int idx) {
		controlManager.mathControl.operation = idx;
	}

	public boolean isCHXYSupport() {
		WaveForm chx = getCHX();
		WaveForm chy = getCHY();
		return chx != null && chy != null && chx.isOn() && chy.isOn();
	}

	public boolean isM1M2Support() {
		WaveForm m1 = getM1();
		WaveForm m2 = getM2();
		return m1 != null && m2 != null && m1.isOn() && m2.isOn();
	}

	/**
	 * @return Selected waveform
	 */
	public WaveForm getSelectedWaveForm() {
		return wfic.getSelectedWF();
	}

	public boolean setSelectedWaveForm(int idx) {
		int tmp = wfic.getSelectedwfIdx();

		if (tmp == idx)
			return false;

		wfic.setSelectedwfIdx(idx);

		controlManager.pcs.firePropertyChange(PropertiesItem.SELECT_W_F, tmp, idx);
		return true;
	}

	private boolean checkChannelRoll(int pos, int loc, int y, int bottom) {
		int up, low;
		if (pos < y) {
			pos = y;

			up = y;
			low = y + (RollOverCheckGap << 1);
		} else if (pos > bottom) {
			pos = bottom;

			up = bottom - (RollOverCheckGap << 1);
			low = bottom;
		} else {
			up = pos - RollOverCheckGap;
			low = pos + RollOverCheckGap;
		}

		boolean b = false;
		if (loc > up && loc < low) {
			b = true;
		}
		return b;
	}

	public int isWaveFormRollOver(ScreenContext pc, Rectangle r, int loc) {
		int y = r.y;
		int bottom = r.y + r.height;

		ON_WF_Iterator owi = on_wf_Iterator();
		while (owi.hasNext()) {
			WaveForm wf = owi.next();
			ChannelInfo ci = wf.wfi.ci;
			int p = wf.getPos0onChart(pc);
			if (checkChannelRoll(p, loc, y, bottom)) {
				return ci.getNumber();
			}
		}

		if (compositeWaveForm.isOn()) {
			int p = compositeWaveForm.getPos0onChart(pc);
			if (checkChannelRoll(p, loc, y, bottom)) {
				return CompositeWaveForm.CompositeWaveFormIndex;
			}
		}

		return -1;
	}

	/**
	 * The two transformations on the x-axis need to be passed to each WaveForm and ChannelInfo here.
	 * <p>
	 * +...-
	 *
	 * @param del
	 */
	public void addWaveFormsRTXloc(int del, ScreenContext pc) {
		if (controlManager.allowTransformScreenWaveForm_Ready()) {
			wfTimeScopeControl.addWaveFormsXloc(del);
			compositeWaveForm.receiveNewData(pc);
		}
	}

	/**
	 * The two transformations on the x-axis need to be passed to each WaveForm and ChannelInfo here.
	 * <p>
	 * +...-
	 *
	 * @param del
	 */
	public void addWaveFormsDMXloc(int del, ScreenContext pc) {
		//After rough measurement within 100 range System.out.println("del: " + del);
		WaveFormInfo[] wfis = wfic.getWaveFormInfos();
		for (WaveFormInfo wfi : wfis) {
			if (wfi.ci.isOn()) {
				wfi.addWaveFormsXloc(del, pc);
			}
		}

		iMultiWFManager.simulateReloadAsDM(pc, wfic, wfTimeScopeControl, this);
	}

	private void setWaveFormTimebaseRTIndex(int idx, int lastidx) {
		ScreenContext pc = getScreenContext();
		if (controlManager.allowTransformScreenWaveForm_Ready()) {
			wfTimeScopeControl.setTimebaseIndex(idx, lastidx);
			compositeWaveForm.receiveNewData(pc);
		}
	}

	private ScreenContext getScreenContext() {
		return controlManager.paintContext;
	}

	private void setWaveFormTimebaseDMIndex(int idx, int lastidx) {
		ScreenContext pc = getScreenContext();
		WaveFormInfo[] wfis = wfic.getWaveFormInfos();
		for (WaveFormInfo wfi : wfis) {
			if (wfi.ci.isOn()) {
				wfi.setTimebaseIndex(idx, lastidx, pc);
			}
		}

		iMultiWFManager.simulateReloadAsDM(pc, wfic, wfTimeScopeControl, this);
	}

	public void offAllWaveForms() {
		ON_WF_Iterator owi = on_wf_Iterator();
		while (owi.hasNext()) {
			WaveForm wf = owi.next();
			wf.wfi.ci.setOn(false);
		}
	}

	public void afterWaveFormsFeed(ScreenContext pc) {
		compositeWaveForm.receiveNewData(pc);
		fftView.receiveNewData();
	}

	public void receiveOfflineData(OfflineChannelsInfo cti, ScreenContext pc,
	                               boolean slow, int tbIdx) {
		int screendatalen = cti.screendatalen;
		int slowMove = cti.slowMove;
		boolean pkdetect = cti.isPKDetect;

		wfTimeScopeControl.loadRT(screendatalen, slowMove, slow, pkdetect, false);

		// cti.prepareLoad(true);
		iMultiWFManager.receiveOfflineData(cti, pc, this);
	}

	public void receiveOfflineDMData(DMInfo cti, ScreenContext pc,
	                                 BigDecimal tbbd, int tbIdx) {
		iMultiWFManager.receiveOfflineDMData(cti, pc, tbbd, tbIdx, this);
	}

	public void receiveRTDMData(DMInfo cti, ScreenContext pc, BigDecimal tbbd,
	                            int tbIdx) {
		iMultiWFManager.receiveRTDMData(cti, pc, tbbd, tbIdx, this);
	}

	/**
	 * In the case of fft, use equal division of drawing
	 *
	 * @param cti
	 * @param pc
	 * @param slow
	 */
	public void receiveRTData(ChannelsTransportInfo cti, ScreenContext pc,
	                          boolean slow) {
		boolean pkdetect = controlManager.isPeakDetectWork();
		FFTControl fftc = controlManager.getFFTControl();
		boolean forFFT = fftc.isFFTon();

		int screendatalen = cti.screendatalen;
		// System.out.println("cti.screendatalen:"+cti.screendatalen);
		int slowMove = cti.slowMove;

		wfTimeScopeControl.loadRT(screendatalen, slowMove, slow, pkdetect, forFFT);

		boolean freshFreq = (freqFreshCount == controlManager.computeFreqTimes) || forcefreshFreq;

		// Prevent some new frames from skipping the reset state when the trigger is normal
		// if (controlManager.allowTransformScreenWaveForm())
		resetVbmulti();

		iMultiWFManager.receiveRTData(cti, pc, this, freshFreq);
		ruleManager.receiveData(this, screendatalen);
		releaseWFIDMLocInfos();

		/** Use a segment refresh rate meter only at runtime */
		if (freqFreshCount <= 0) {
			freqFreshCount = controlManager.computeFreqTimes;
		} else {
			freqFreshCount--;
		}
	}

	public void releaseWFIDMLocInfos() {
		wfic.releaseWFIDMLocInfos();
	}

	public WFTimeScopeControl getWFTimeScopeControl() {
		return wfTimeScopeControl;
	}

	public void paintRulePoints(Graphics2D g2d, ScreenContext pc, Rectangle r) {
		ruleManager.paintRulePoints(g2d, pc.isScreenMode_3(), r,
				dataHouse.getGlobalDecorater());
	}

	public void paintPFLabel(Graphics2D g2d, ScreenContext pc) {
		ruleManager.paintPFLabel(g2d, pc);
	}

	@Override
	public void adjustView(ScreenContext pc, Rectangle bound) {
		referenceWaveControl.adjustReferenceView(pc, bound);
		ON_WF_Iterator owi = on_wf_Iterator();
		while (owi.hasNext()) {
			WaveForm wf = owi.next();
			wf.adjustView(pc, bound);
		}
		// Calculated after wf
		compositeWaveForm.adjustView(pc, bound);

		ruleManager.adjustView();
	}

	public void hideDraw() {
		hideDraw = true;
	}

	public void resumeDraw() {
		hideDraw = false;
	}

	public void forceFreshFreq() {
		forcefreshFreq = true;
	}

	public void resumeForceFreshFreq() {
		forcefreshFreq = false;
	}

	public void setTrgEdgeMiddle() {
		if (!controlManager.getTriggerControl().getTriggerUIInfo().isAuto_trglevel_middle()
				|| !controlManager.getMachine().isTrgEdgeMiddleSupport())
			return;

		ON_WF_Iterator owi = on_wf_Iterator();
		while (owi.hasNext()) {
			WaveForm wf = owi.next();
			wf.setTrgEdgeMiddle(controlManager.getTriggerControl());
		}
	}

	@Override
	public void paintView(Graphics2D g2d, ScreenContext pc, Rectangle r) {
		if (hideDraw)
			return;

		referenceWaveControl.paitReferenceWaves(g2d, pc, r);

		FFTControl fftc = controlManager.getFFTControl();
		if (fftc.isFFTon()) {
			getWaveForm(fftc.getFFTchl()).paintView(g2d, pc, r);
		} else {
			ON_WF_Iterator owi = on_wf_Iterator();
			while (owi.hasNext()) {
				WaveForm wf = owi.next();
				wf.paintView(g2d, pc, r);
			}
		}

		compositeWaveForm.paintView(g2d, pc, r);
	}

	public void paintViewWithoutWaveForms(Graphics2D g2d, ScreenContext pc,
	                                      Rectangle r) {
		referenceWaveControl.paitReferenceWaves(g2d, pc, r);
		compositeWaveForm.paintView(g2d, pc, r);
	}

	public int getOnWaveForms() {
		int n = 0;
		ON_WF_Iterator owi = on_wf_Iterator();
		while (owi.hasNext()) {
			owi.next();
			n++;
		}
		return n;
	}

	protected void paintFreq(Graphics2D g2d, ScreenContext pc) {
		int h = 20;
		int y = 520 - getOnWaveForms() * 20;
		ON_WF_Iterator owi = on_wf_Iterator();
		while (owi.hasNext()) {
			WaveForm wf = owi.next();
			Color c = wf.getColor();
			g2d.setColor(c);
			// g2d.drawString(String.format("frq: %d, %d", wf.fr_b, wf.fr_rf),
			// 20, y);
			y += h;
		}
	}

	public void paintWaveFormInfo(Graphics2D g2d, ScreenContext pc,
	                              Rectangle r, ControlManager cm, LocRectangle lr,
	                              ChartScreenSelectModel cssm) {
		if (hideDraw)
			return;

		referenceWaveControl.paitReferenceItems(g2d, pc, r);

		TriggerLevelDelegate tld = cm.getCoreControl()
				.getTriggerLevelDelegate();
		tld.preparePaintChannelTrgLabelContext(g2d, pc, lr, cssm);
		tld.paintTrgLabel(cm, this);

		int sidx = cssm.getScreenSelectWFidx();

		/** Topping drawing logic only appears in this method body */
		FFTControl fftc = cm.getFFTControl();
		if (fftc.isFFTon()) {
			// int yb = pc.hcenter;
			getWaveForm(fftc.getFFTchl()).paintItem(g2d, pc, r, cm, false);
		} else {
			// System.err.println("sidx: "+sidx);
			if (sidx >= 0 && sidx < wfic.getLowMachineChannels()) {
				WaveForm tmp = null;
				ON_WF_Iterator owi = on_wf_Iterator();
				while (owi.hasNext()) {
					WaveForm wf = owi.next();
					// System.err.println("wf.getChannelNumber():
					// "+wf.getChannelNumber());
					// Determine if the current channel is topped
					if (wf.getChannelNumber() == sidx) {
						tmp = wf;
						continue;
					}
					wf.paintItem(g2d, pc, r, cm, false);
				}

				compositeWaveForm.paintItem(g2d, pc, r, cm, false);
				// Replenishing the top channel
				if (tmp != null)
					tmp.paintItem(g2d, pc, r, cm, true);
				return;
			}

			ON_WF_Iterator owi = on_wf_Iterator();
			while (owi.hasNext()) {
				WaveForm wf = owi.next();
				wf.paintItem(g2d, pc, r, cm, false);
			}
		}
		// Compatible with each channel without pinning
		compositeWaveForm.paintItem(g2d, pc, r, cm,
				(sidx == CompositeWaveForm.CompositeWaveFormIndex));
	}

	public CompositeWaveForm getCompositeWaveForm() {
		return compositeWaveForm;
	}

	public FFTView getFFTView() {
		return fftView;
	}

	public DataHouse getDataHouse() {
		return dataHouse;
	}

	public int getHorTrgPos(TimeControl tc) {
		return controlManager.getTimeControl().getHorizontalTriggerPosition();
	}

	public int getSkipPoints() {
		return wfTimeScopeControl.getSkipPoints();
	}

	public void reduceFrame() {
		ON_WF_Iterator owi = on_wf_Iterator();
		while (owi.hasNext()) {
			WaveForm wf = owi.next();
			wf.reduceFrame();
		}
	}

	// // When the recording playback setting loadRtPos0 is returned to the RT state,
	// public void setRtPos0backRT() {
	// ON_WF_Iterator owi = on_wf_Iterator();
	// while (owi.hasNext()) {
	// WaveForm wf = owi.next();
	// wf.loadPos0backRT();
	// }
	// }

	public void resetVbmulti() {
		ON_WF_Iterator owi = on_wf_Iterator();
		while (owi.hasNext()) {
			WaveForm wf = owi.next();
			wf.resetVbmulti();
		}
	}

	public void saveFirstLoadPos0() {
		ON_WF_Iterator owi = on_wf_Iterator();
		while (owi.hasNext()) {
			WaveForm wf = owi.next();
			wf.saveFirstLoadPos0();
		}
	}

	@Override
	public ON_WF_Iterator on_wf_Iterator() {
		return wfic.on_wf_Iterator();
	}

	public XYView getXYView() {
		return xyView;
	}

	public ReferenceWaveControl getReferenceWaveControl() {
		return referenceWaveControl;
	}

	public void setWaveFormTimebaseIndex(int newidx, int oldidx) {
		if (dataHouse.isDMLoad()) {
			setWaveFormTimebaseDMIndex(newidx, oldidx);
		} else {
			setWaveFormTimebaseRTIndex(newidx, oldidx);
		}
	}

}