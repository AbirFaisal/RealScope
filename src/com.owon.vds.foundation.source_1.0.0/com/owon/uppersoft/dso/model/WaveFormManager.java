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
 * WaveFormManager，管理波形
 * 
 */
public abstract class WaveFormManager implements Decorate, MeasureWFSupport,
		Localizable {

	private DataHouse dh;

	private XYView xyv1;
	private FFTView fftv;
	private CompositeWaveForm cwf;
	private ReferenceWaveControl rwc;

	private IMultiWFManager mwfm;
	protected WFTimeScopeControl wftsc;
	protected ControlManager cm;
	private ArrayList<WaveForm> offWfs;

	public WaveFormInfoControl getWaveFormInfoControl() {
		return wfic;
	}

	public boolean isMath(int chl) {
		return !(chl >= 0 && chl < cm.getSupportChannelsNumber());
	}

	public boolean isMathSupport() {
		return cm.getSupportChannelsNumber() > 1;
	}

	@Override
	public void localize(ResourceBundle rb) {
		xyv1.localize(rb);
		fftv.localize(rb);
	}

	public WaveFormManager(DataHouse dh) {
		this.dh = dh;
		cm = dh.controlManager;

		wfic = cm.getWaveFormInfoControl();
		wfic.setDataHouse(dh);
		retainClosedWaveForms();
		xyv1 = new XYView(this, cm.displayControl);
		fftv = new FFTView(this, cm);

		LocalizeCenter lc = cm.getLocalizeCenter();
		lc.addPrimeTextLocalizable(this);

		cwf = new CompositeWaveForm(this, cm.mathControl, cm.getCoreControl()
				.getVoltageProvider(), cm);
		rwc = cm.rwc;

		ruleManager = cm.ruleManager;
		mwfm = createMultiWFManager(cm);

		wftsc = createWFTimeScopeControl();

		freqFreshCount = cm.computeFreqTimes;

		cm.pcs.addPropertyChangeListener(new PropertyChangeListener() {
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

	protected abstract IMultiWFManager createMultiWFManager(ControlManager cm);

	protected abstract WFTimeScopeControl createWFTimeScopeControl();

	public boolean is3in1On() {
		return xyv1.isOn() || cm.getFFTControl().isFFTon();
	}

	public void setZeroYLoc(WaveForm wf, int yl, boolean commit) {
		boolean moveWithPos0 = false;
		if (dh.isDMLoad()) {
			moveWithPos0 = true;
		} else if ((cm.allowTransformScreenWaveForm_Ready())
				|| dh.allowTransformScreenWaveFormVertical()) {
			moveWithPos0 = true;
		}

		wf.setZeroYLoc(yl, commit, moveWithPos0);
	}

	/**
	 * 正常、单次触发下，电压改变等到所有指令都发下得到应答后再进行缩放，
	 * 
	 * 而且缩放是无论是否有触发的情况都进行的
	 * 
	 * 正常触发时，Trg'd->Ready比较久，为了保证期间能够支持缩放，只有不判断条件皆可缩放
	 * 
	 * 这期间也会进行缩放
	 */
	public boolean setVoltBaseIndex(WaveForm wf, int vb) {
		boolean tranWithVB = false;
		if (dh.isDMLoad()) {
			tranWithVB = true;
		} else if ((cm.allowTransformScreenWaveForm_Ready())
				|| dh.allowTransformScreenWaveFormVertical()) {
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
		WaveFormInfoControl wfic = cm.getWaveFormInfoControl();
		offWfs = wfic.getClosedWaveForms();
	}

	public boolean isNoWFDataFilled(WaveForm wf) {
		if (cm.isRuntime())
			return false;
		return offWfs.contains(wf);
	}

	/**
	 * KNOW 都用索引值即时去取通道，可以保证使用的都是当前获取的那次数据
	 */
	public WaveForm getCHX() {
		return getWaveForm(cm.displayControl.wfx);
	}

	public WaveForm getCHY() {
		return getWaveForm(cm.displayControl.wfy);
	}

	public WaveForm getM1() {
		return getWaveForm(cm.mathControl.m1);
	}

	public WaveForm getM2() {
		return getWaveForm(cm.mathControl.m2);
	}

	public void setMathOperation(int idx) {
		cm.mathControl.operation = idx;
	}

	public int getMathOperation() {
		return cm.mathControl.operation;
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
	 * @return 选择的波形
	 */
	public WaveForm getSelectedWaveForm() {
		return wfic.getSelectedWF();
	}

	public boolean setSelectedWaveForm(int idx) {
		int tmp = wfic.getSelectedwfIdx();

		if (tmp == idx)
			return false;

		wfic.setSelectedwfIdx(idx);

		cm.pcs.firePropertyChange(PropertiesItem.SELECT_W_F, tmp, idx);
		return true;
	}

	public static final int RollOverCheckGap = 10;

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

		if (cwf.isOn()) {
			int p = cwf.getPos0onChart(pc);
			if (checkChannelRoll(p, loc, y, bottom)) {
				return CompositeWaveForm.CompositeWaveFormIndex;
			}
		}

		return -1;
	}

	/**
	 * 对x轴上的两种变换需在这里传递到每个WaveForm及ChannelInfo中去
	 * 
	 * +...-
	 * 
	 * @param del
	 */
	public void addWaveFormsRTXloc(int del, ScreenContext pc) {
		if (cm.allowTransformScreenWaveForm_Ready()) {
			wftsc.addWaveFormsXloc(del);
			cwf.receiveNewData(pc);
		}
	}

	/**
	 * 对x轴上的两种变换需在这里传递到每个WaveForm及ChannelInfo中去
	 * 
	 * +...-
	 * 
	 * @param del
	 */
	public void addWaveFormsDMXloc(int del, ScreenContext pc) {
		// 经粗测在100范围以内System.out.println("del: " + del);
		WaveFormInfo[] wfis = wfic.getWaveFormInfos();
		for (WaveFormInfo wfi : wfis) {
			if (wfi.ci.isOn()) {
				wfi.addWaveFormsXloc(del, pc);
			}
		}

		mwfm.simulateReloadAsDM(pc, wfic, wftsc, this);
	}

	private void setWaveFormTimebaseRTIndex(int idx, int lastidx) {
		ScreenContext pc = getScreenContext();
		if (cm.allowTransformScreenWaveForm_Ready()) {
			wftsc.setTimebaseIndex(idx, lastidx);
			cwf.receiveNewData(pc);
		}
	}

	private ScreenContext getScreenContext() {
		return cm.paintContext;
	}

	private void setWaveFormTimebaseDMIndex(int idx, int lastidx) {
		ScreenContext pc = getScreenContext();
		WaveFormInfo[] wfis = wfic.getWaveFormInfos();
		for (WaveFormInfo wfi : wfis) {
			if (wfi.ci.isOn()) {
				wfi.setTimebaseIndex(idx, lastidx, pc);
			}
		}

		mwfm.simulateReloadAsDM(pc, wfic, wftsc, this);
	}

	public void offAllWaveForms() {
		ON_WF_Iterator owi = on_wf_Iterator();
		while (owi.hasNext()) {
			WaveForm wf = owi.next();
			wf.wfi.ci.setOn(false);
		}
	}

	public void afterWaveFormsFeed(ScreenContext pc) {
		cwf.receiveNewData(pc);
		fftv.receiveNewData();
	}

	public void receiveOfflineData(OfflineChannelsInfo cti, ScreenContext pc,
			boolean slow, int tbIdx) {
		int screendatalen = cti.screendatalen;
		int slowMove = cti.slowMove;
		boolean pkdetect = cti.isPKDetect;

		wftsc.loadRT(screendatalen, slowMove, slow, pkdetect, false);

		// cti.prepareLoad(true);
		mwfm.receiveOfflineData(cti, pc, this);
	}

	public void receiveOfflineDMData(DMInfo cti, ScreenContext pc,
			BigDecimal tbbd, int tbIdx) {
		mwfm.receiveOfflineDMData(cti, pc, tbbd, tbIdx, this);
	}

	public void receiveRTDMData(DMInfo cti, ScreenContext pc, BigDecimal tbbd,
			int tbIdx) {
		mwfm.receiveRTDMData(cti, pc, tbbd, tbIdx, this);
	}

	/**
	 * 在fft的情况下，使用均等分的画图方式
	 * 
	 * @param cti
	 * @param pc
	 * @param slow
	 */
	public void receiveRTData(ChannelsTransportInfo cti, ScreenContext pc,
			boolean slow) {
		boolean pkdetect = cm.isPeakDetectWork();
		FFTControl fftc = cm.getFFTControl();
		boolean forFFT = fftc.isFFTon();

		int screendatalen = cti.screendatalen;
		// System.out.println("cti.screendatalen:"+cti.screendatalen);
		int slowMove = cti.slowMove;

		wftsc.loadRT(screendatalen, slowMove, slow, pkdetect, forFFT);

		boolean freshFreq = (freqFreshCount == cm.computeFreqTimes)
				|| forcefreshFreq;

		// 防止在触发正常的情况下，一些新帧略过了重置的状态
		// if (cm.allowTransformScreenWaveForm())
		resetVbmulti();

		mwfm.receiveRTData(cti, pc, this, freshFreq);
		ruleManager.receiveData(this, screendatalen);
		releaseWFIDMLocInfos();

		/** 只在运行时使用隔段刷新频率计 */
		if (freqFreshCount <= 0) {
			freqFreshCount = cm.computeFreqTimes;
		} else {
			freqFreshCount--;
		}
	}

	public int freqFreshCount;

	public void releaseWFIDMLocInfos() {
		wfic.releaseWFIDMLocInfos();
	}

	public WFTimeScopeControl getWFTimeScopeControl() {
		return wftsc;
	}

	private PFRuleManager ruleManager;

	public void paintRulePoints(Graphics2D g2d, ScreenContext pc, Rectangle r) {
		ruleManager.paintRulePoints(g2d, pc.isScreenMode_3(), r,
				dh.getGlobalDecorater());
	}

	public void paintPFLabel(Graphics2D g2d, ScreenContext pc) {
		ruleManager.paintPFLabel(g2d, pc);
	}

	@Override
	public void adjustView(ScreenContext pc, Rectangle bound) {
		rwc.adjustReferenceView(pc, bound);
		ON_WF_Iterator owi = on_wf_Iterator();
		while (owi.hasNext()) {
			WaveForm wf = owi.next();
			wf.adjustView(pc, bound);
		}
		// 在wf之后计算
		cwf.adjustView(pc, bound);

		ruleManager.adjustView();
	}

	private boolean hideDraw = false;

	public void hideDraw() {
		hideDraw = true;
	}

	public void resumeDraw() {
		hideDraw = false;
	}

	/** 可用来强制刷新硬件频率计 */
	private boolean forcefreshFreq = false;

	public void forceFreshFreq() {
		forcefreshFreq = true;
	}

	public void resumeForceFreshFreq() {
		forcefreshFreq = false;
	}

	public void setTrgEdgeMiddle() {
		if (!cm.getTriggerControl().getTriggerUIInfo().isAuto_trglevel_middle()
				|| !cm.getMachine().isTrgEdgeMiddleSupport())
			return;

		ON_WF_Iterator owi = on_wf_Iterator();
		while (owi.hasNext()) {
			WaveForm wf = owi.next();
			wf.setTrgEdgeMiddle(cm.getTriggerControl());
		}
	}

	@Override
	public void paintView(Graphics2D g2d, ScreenContext pc, Rectangle r) {
		if (hideDraw)
			return;

		rwc.paitReferenceWaves(g2d, pc, r);

		FFTControl fftc = cm.getFFTControl();
		if (fftc.isFFTon()) {
			getWaveForm(fftc.getFFTchl()).paintView(g2d, pc, r);
		} else {
			ON_WF_Iterator owi = on_wf_Iterator();
			while (owi.hasNext()) {
				WaveForm wf = owi.next();
				wf.paintView(g2d, pc, r);
			}
		}

		cwf.paintView(g2d, pc, r);
	}

	public void paintViewWithoutWaveForms(Graphics2D g2d, ScreenContext pc,
			Rectangle r) {
		rwc.paitReferenceWaves(g2d, pc, r);
		cwf.paintView(g2d, pc, r);
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

	private WaveFormInfoControl wfic;

	public void paintWaveFormInfo(Graphics2D g2d, ScreenContext pc,
			Rectangle r, ControlManager cm, LocRectangle lr,
			ChartScreenSelectModel cssm) {
		if (hideDraw)
			return;

		rwc.paitReferenceItems(g2d, pc, r);

		TriggerLevelDelegate tld = cm.getCoreControl()
				.getTriggerLevelDelegate();
		tld.preparePaintChannelTrgLabelContext(g2d, pc, lr, cssm);
		tld.paintTrgLabel(cm, this);

		int sidx = cssm.getScreenSelectWFidx();

		/** 置顶的绘图逻辑只出现在这一个方法体内 */
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
					// 判断当前通道是否置顶
					if (wf.getChannelNumber() == sidx) {
						tmp = wf;
						continue;
					}
					wf.paintItem(g2d, pc, r, cm, false);
				}

				cwf.paintItem(g2d, pc, r, cm, false);
				// 补画置顶的通道
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
		// 兼容各通道都不需要置顶的情况
		cwf.paintItem(g2d, pc, r, cm,
				(sidx == CompositeWaveForm.CompositeWaveFormIndex));
	}

	public CompositeWaveForm getCompositeWaveForm() {
		return cwf;
	}

	public FFTView getFFTView() {
		return fftv;
	}

	public DataHouse getDataHouse() {
		return dh;
	}

	public int getHorTrgPos(TimeControl tc) {
		return cm.getTimeControl().getHorizontalTriggerPosition();
	}

	public int getSkipPoints() {
		return wftsc.getSkipPoints();
	}

	public void reduceFrame() {
		ON_WF_Iterator owi = on_wf_Iterator();
		while (owi.hasNext()) {
			WaveForm wf = owi.next();
			wf.reduceFrame();
		}
	}

	// // 当录制回放设置loadRtPos0返回给RT状态使用
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
		return xyv1;
	}

	public ReferenceWaveControl getReferenceWaveControl() {
		return rwc;
	}

	public void setWaveFormTimebaseIndex(int newidx, int oldidx) {
		if (dh.isDMLoad()) {
			setWaveFormTimebaseDMIndex(newidx, oldidx);
		} else {
			setWaveFormTimebaseRTIndex(newidx, oldidx);
		}
	}

}