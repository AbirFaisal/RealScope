package com.owon.uppersoft.dso.source.comm;

import java.nio.ByteBuffer;
import java.util.Iterator;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.mode.control.SampleControl;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.vds.core.comm.ICommunicateManager;
import com.owon.uppersoft.vds.core.wf.WaveFormInfo;
import com.owon.uppersoft.vds.core.wf.rt.ChannelDataInfo;
import com.owon.uppersoft.vds.core.wf.rt.ChannelsTransportInfo;
import com.owon.uppersoft.vds.core.wf.rt.LoadMedia;

public abstract class AbsGetDataRunner {
	public static final String UPDATE_FPS = "UPDATE_FPS";

	protected ICommunicateManager ism;
	protected DataHouse dh;
	protected ControlManager cm;

	public AbsGetDataRunner(DataHouse dh, ICommunicateManager ism) {
		this.dh = dh;
		this.ism = ism;
		cm = dh.controlManager;
	}

	public ChannelsTransportInfo getDataT() {
		long t0 = System.currentTimeMillis();

		ChannelsTransportInfo cti = getData();

		long t1 = System.currentTimeMillis();
		dbgln("getDataT: " + (t1 - t0) + " ms");
		return cti;
	}

	protected void updateFrame(int fc) {
		/** 记录实际接收帧数 */
		// System.err.println(receiveFrameCount);
		// t2 = System.currentTimeMillis();
		// del = t2 - t1;
		// DBG.dbg(ct + " WaveForms on Rate: " + del / ct + " ms/f\n");
		computeFramesPerSec(fc);
	}

	private void dbgln(String string) {
	}

	protected abstract ChannelsTransportInfo getData();

	/**
	 * 采集后进行平均值和接地的处理，以及反相
	 * 
	 * @param cti
	 */
	protected final void handleGroundNInverse(ChannelsTransportInfo cti) {
		WaveFormManager wfm = dh.getWaveFormManager();
		Iterator<? extends ChannelDataInfo> it = cti.iterator_ChannelDataInfo();
		while (it.hasNext()) {
			ChannelDataInfo cdi = it.next();
			WaveFormInfo wfi = wfm.getWaveForm(cdi.chl).wfi;
			ChannelInfo ci = wfi.ci;
			/**
			 * 可控的软件接地实现，仅针对屏幕的adc点，
			 * 
			 * 保存为文件时须存储耦合方式以在dm文件载入时设置正确的耦合方式
			 * 
			 * 同时也处理了adc限界，所以条件转入后即可continue
			 */
			if (ci.isGround()) {
				cdi.forceGround(ci.getPos0());
			} else {
				cdi.handleADCBoundNInverse(ci.getPos0(), ci.isInverse());
			}
		}
	}

	/**
	 * 采集后进行平均值和接地的处理，以及反相
	 * 
	 * 只对屏幕点做平均值，这样速度快，而且在拉触发和插值后，此时深存储停下只有最后一帧
	 * 
	 * fft开启时不处理
	 * 
	 * 接地或限界后，仍可以进入平均值的处理
	 * 
	 * @param cti
	 */
	protected void handleAverage(ChannelsTransportInfo cti) {
		WaveFormManager wfm = dh.getWaveFormManager();
		SampleControl sc = dh.controlManager.getSampleControl();
		boolean avgon = sc.avgon;
		int avgtimes = sc.getAvgTimes();
		Iterator<? extends ChannelDataInfo> it = cti.iterator_ChannelDataInfo();
		while (it.hasNext()) {
			ChannelDataInfo cdi = it.next();
			WaveFormInfo wfi = wfm.getWaveForm(cdi.chl).wfi;
			ByteBuffer adcbuf = cdi.getUniqueAdcbuf();
			wfi.ac.updateAverage(adcbuf, avgtimes, avgon);
		}

	}

	private int framesCount;
	private long tbeg;

	public void resetCumulateFrames() {
		framesCount = 0;
	}

	protected void computeFramesPerSec(int fs) {
		if (framesCount == 0)
			tbeg = System.currentTimeMillis();
		framesCount += fs;
		long timeCount = System.currentTimeMillis() - tbeg;
		if (timeCount > 1000) {
			float tmp = framesCount / (float) timeCount;// frame/ms
			float fps = tmp * 1000;// ms转s
			// BigDecimal tmp = BigDecimal.valueOf(framesCount).divide(
			// BigDecimal.valueOf(timeCount));
			// fps = tmp.multiply(BigDecimal.valueOf(1000000000)).intValue();
			// DBG.outprintln(framesCount + "," + timeCount);
			cm.pcs.firePropertyChange(UPDATE_FPS, -1, fps);
			resetCumulateFrames();
		}
	}

	protected void checkTrgEdgeMiddle() {
		dh.getWaveFormManager().setTrgEdgeMiddle();
	}

	public void release() {
		getChannelsTransportInfo().release();
	}

	public LoadMedia getLoadMedia() {
		return getChannelsTransportInfo();
	}

	public abstract ChannelsTransportInfo getChannelsTransportInfo();

	public void prepareToRun() {
		ChannelsTransportInfo ci = getChannelsTransportInfo();
		ci.frameAllNum = 0;
		ci.resetStatusArr();
		resetCumulateFrames();
	}
}