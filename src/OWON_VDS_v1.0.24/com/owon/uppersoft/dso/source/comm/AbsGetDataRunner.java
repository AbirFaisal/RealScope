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
		/** Records the number of frames actually received */
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
	 * After acquisition, perform averaging, grounding and inversion
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
			 * Controllable software grounding implementation, only for the adc points on the screen,
			 * 
			 * When saving to a file, the coupling mode must be stored so that the correct coupling mode is set when the dm file is loaded
			 * 
			 * It also handles the adc limits, so continue once the condition is enteredcontinue
			 */
			if (ci.isGround()) {
				cdi.forceGround(ci.getPos0());
			} else {
				cdi.handleADCBoundNInverse(ci.getPos0(), ci.isInverse());
			}
		}
	}

	/**
	 * After acquisition, perform averaging, grounding and inversion
	 * 
	 * Average only the on-screen points, which is fast; also, after trigger pull and interpolating, deep memory stopped at that moment has only the last frame
	 * 
	 * fftNot processed when fft is on
	 * 
	 * After grounding or limiting, averaging can still be applied
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
			float fps = tmp * 1000;// ms to s
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