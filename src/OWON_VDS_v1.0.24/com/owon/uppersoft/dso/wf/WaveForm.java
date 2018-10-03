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
 * WaveForm，波形，就提供的点进行绘制
 * 
 * RT时获取插值后的数据点1k~4k，DM时获取原始的采样点25~10M
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
	 * 数据：adc - pos0
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
	 * 参考波形需要提供额外的从xb多少开始画的信息
	 * 
	 * @param adcbuf
	 */
	public IntBuffer save2RefIntBuffer() {
		return rtlim.save2RefIntBuffer();
	}

	/**
	 * 目前是最终画点的像素数组，需要真的运行时和深存储进行修改
	 * 
	 * 供录制和PF使用
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

	/** KNOW 通过这一变量保存载入时的零点位置，所谓载入时，有读入DM的情况，以及回放录制波形时最初的状态 */
	private int loadPos0;

	public void prepareRTNormalPaint(ChannelDataInfo cdi, LoadMedia cti, int yb) {
		ScreenContext pc = getScreenContext();
		ChannelInfo ci = wfi.ci;
		this.basevbidx = ci.getVoltbaseIndex();

		/** bBuf存放原始数据，intBuf存放包含了零点信息的实际像素点 */
		/** yb用dmli.loadPos0同当前pos0的变化传入，即为载入录制的普通波形时的效果 */
		/** 由于处理了反相，该方法只在载入数据时调用一次 */

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

	// TODO 检查basevbidx可能有问题
	public int basevbidx = 0;
	private BigDecimal vbmulti = BigDecimal.valueOf(1);

	// RT<->STOP, play
	public void resetVbmulti() {
		this.vbmulti = BigDecimal.valueOf(1);
	}

	// RT->STOP, RT->DM, OFFDM, OFFPLAY
	/**
	 * 作为当前adc数据采集时对应的零点位置
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
		/** 使用载入时的零点位置和变化后的零点位置之差以告知波形被移动了 */
		rtlim.resetIntBuf(vbmulti, getYb(pc), pc.isScreenMode_3());
	}

	@Override
	public void adjustView(ScreenContext pc, Rectangle bound) {
		/** RT时直接等下一幅，后续可以再作加锁方式的各种支持 */
		if (!pc.allowLazyRepaint())
			resetNormalIntBuf(pc);
	}

	/**
	 * @return 深存储载入时的默认零点位置
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
		/** 以下为同下位机连接的情况下，获取实时数据绘图，快速刷新 */
		if (!isOn())
			return;
		ChannelInfo ci = wfi.ci;
		g2d.setColor(getColor());

		int y = r.y;
		int bottom = r.y + r.height;
		/** 画左边的标尺 ,依次画顶 底 中 */
		LineUtil.paintChannelLabel(yb, y, bottom, g2d,
				String.valueOf(ci.getNumber() + 1), 2, onFront);

		/** 画左边的标尺的右侧零点位置显示 */
		if (onShowPos0) {
			LineUtil.paintOnShowPos0(g2d, pc, dh.divUnits, ci.getPos0(), yb, r);
		}
	}

	@Override
	public void paintView(Graphics2D g2d, ScreenContext pc, Rectangle r) {
		/** 以下为同下位机连接的情况下，获取实时数据绘图，快速刷新 */
		if (!isOn())
			return;

		boolean linkline = dh.isLineLink();

		// System.err.println("pv pkdet"+pc.pkdetect);

		g2d.setColor(getColor());

		/**
		 * 余辉画图时使用固定的Rectangle会更容易解决，但是继承结构暂不方便打破，
		 * 所以改为直接在WaveForm内部使用固定的Rectangle，这样改动最小
		 */
		int xb = r.x;
		// if (xb != 12)
		// System.err.println(xb);
		Shape tmp = g2d.getClip();
		g2d.setClip(r);
		/**
		 * 画图方式的区分基于档位和存储深度分治，在导入数据及位移缩放操作的时候处理，绘图处只是对应到特定的画图模式上
		 * 
		 * M情况下的慢扫留待后续处理
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
	 * 在触发电平等上双击的情况下触发的操作
	 */
	public void doubleClickOnLevel() {
		setTrg50Percent(cm.getTriggerControl());
	}

	/**
	 * 设置触发电平为波形数据峰峰值的50%位置，这里只是在设置下一次数据进入时进行计算
	 */
	public void setTrg50Percent(TriggerControl trgc) {
		if (!cm.is50percentAvailable())
			return;
		TriggerSet ts = trgc.getTriggerSetOrNull(wfi.ci.getNumber());
		if (ts == null || !ts.isVoltsenseSupport())
			return;

		/** 进行了Trg50%的计算，如果获得有效值则设置，无论与否，关闭下次Trg50%的计算 */
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
		/** KNOW 先设置，再结束，总会执行一次发送 */
	}

	/**
	 * 设置触发电平为波形数据峰峰值的50%位置，这里只是在设置下一次数据进入时进行计算
	 */
	public void setTrgEdgeMiddle(TriggerControl trgctl) {
		TriggerSet ts = trgctl.getTriggerSetOrNull(wfi.ci.getNumber());
		if (ts == null || !ts.isCurrentTrigger_Edge())
			return;

		/** 进行了Trg50%的计算，如果获得有效值则设置，无论与否，关闭下次Trg50%的计算 */
		int v = rtlim.getTrg50percentValue();
		if (v == Integer.MAX_VALUE)
			return;

		VoltsensableTrigger vt = (VoltsensableTrigger) ts.getTrigger();
		int last = vt.c_getVoltsense();
		// System.err.println(last+" | "+v);

		/** 偏差在1小格 */
		if (Math.abs(last - v) < 5)
			return;

		boolean changed = vt.c_setVoltsense(v);
		if (changed)
			trgctl.getVTPatchable().submiteVoltsense(getChannelNumber(), vt);
		cm.pcs.firePropertyChange(PropertiesItem.REPAINT_CHARTSCREEN, null,
				null);
		cm.pcs.firePropertyChange(PropertiesItem.UPDATE_VOLTSENSE, -1,
				getChannelNumber());
		/** KNOW 先设置，再结束，总会执行一次发送 */
	}

	/**
	 * 设置触发电平为波形数据峰峰值的50%位置，这里只是在设置下一次数据进入时进行计算
	 */
	public int getTrg50Percent() {
		if (!cm.is50percentAvailable())
			return Integer.MAX_VALUE;

		/** 进行了Trg50%的计算，如果获得有效值则设置，无论与否，关闭下次Trg50%的计算 */
		int v = rtlim.getTrg50percentValue();
		return v;
	}

}