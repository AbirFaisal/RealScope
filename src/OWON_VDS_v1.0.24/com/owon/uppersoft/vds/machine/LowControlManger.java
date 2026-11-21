package com.owon.uppersoft.vds.machine;

import com.owon.uppersoft.dso.global.ControlApps;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.mode.control.DeepMemoryControl;
import com.owon.uppersoft.dso.mode.control.TimeControl;
import com.owon.uppersoft.dso.model.WaveFormInfoControl;
import com.owon.uppersoft.dso.model.trigger.PulseTrigger;
import com.owon.uppersoft.dso.model.trigger.SlopeTrigger;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.model.trigger.TriggerSet;
import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.vds.core.comm.BufferredSourceManager;
import com.owon.uppersoft.vds.device.interpret.util.CBitSet;
import com.owon.uppersoft.vds.source.comm.ext.IntObject;
import com.owon.uppersoft.vds.source.front.PreHandler;

/**
 * LowControlManger: used to obtain the corresponding parameters from the backend system; these parameters are already-saved settings that are not passed directly through Submitor calls and must be obtained separately
 * 
 */
public abstract class LowControlManger {
	protected ControlManager cm;

	protected WaveFormInfoControl wfic;

	public LowControlManger(ControlManager cm) {
		this.cm = cm;
		wfic = cm.getWaveFormInfoControl();
	}

	/**
	 * earlier models, at scales that cannot reach a higher sampling rate, interpolated using fewer full-screen points to reach the point count required for the corresponding spectrum scale;
	 * 
	 * whereas in tiny, the 2048 points required for fft are simply taken from the memory depth and fft is done without interpolation, so the spectrum scale is not improved;
	 * 
	 * @param tb
	 * @return
	 */
	public int getFFTSeperateFreq(int tb) {
		return getMachineInfo().getFFTSeperateFreq(tb);
	}

	public abstract int getHTPWithoutCompress(int htp, IntObject io);

	public int getSeperateFreq(int tb) {
		return getMachineInfo().getSeperateFreq(tb);
	}

	public MachineInfo_Tiny getMachineInfo() {
		return (MachineInfo_Tiny) cm.getMachineInfo();
	}

	public TinyMachine getMachineType() {
		return (TinyMachine) cm.getMachine();
	}

	public int getDeepMemoryIndex() {
		return cm.getDeepMemoryControl().getDeepIdx();
	}

	public int getDeepMemoryDataLen() {
		return cm.getDeepMemoryControl().getDeepDataLen()
				+ PreHandler.TRANBUFLEN_PREDM + PreHandler.TRANBUFLEN_SUFDM;
	}

	public boolean isFFTOn() {
		return cm.getFFTControl().isFFTon();
	}

	protected TimeControl getTimeControl() {
		return cm.getTimeControl();
	}

	protected DeepMemoryControl getDeepMemoryControl() {
		return cm.getDeepMemoryControl();
	}

	public int getLowMachineChannels() {
		return cm.getWaveFormInfoControl().getLowMachineChannels();
	}

	public int getChannelPos0(int chl) {
		ChannelInfo ci = wfic.getWaveFormInfo(chl).ci;
		return ci.getPos0();
	}

	public int getChannelVBIndex(int chl) {
		ChannelInfo ci = wfic.getWaveFormInfo(chl).ci;
		return ci.getVoltbaseIndex();
	}

	public int getChannelsFlag() {
		return wfic.getWaveFormFlag();
	}

	public static final int[] Tiny_Coupling = { 1, 0, 2 };

	public int getChannelsSetFlag(int chl, int vb) {
		int DelayAttenuationVBIndex = getMachineType()
				.getDelayAttenuationVBIndex();
		ChannelInfo ci = wfic.getWaveFormInfo(chl).ci;
		return getWaveFormSetFlag(ci, DelayAttenuationVBIndex, vb);
	}

	public static final byte getWaveFormSetFlag(ChannelInfo ci,
			int DelayAttenuationVBIndex, int vb) {
		CBitSet bs = new CBitSet(8);
		bs.clear();

		// [7] channel on/off
		bs.set1(ci.isOn(), 7);

		// [56] coupling mode
		int cplidx = Tiny_Coupling[ci.getCouplingIdx()];
		// System.err.println("cplidx: "+cplidx);
		bs.setN(cplidx, 5, 6);

		// [4] none
		bs.set1(0, 4);

		// [23] bandwidth limit
		bs.setN(0, 2, 3);

		// 1 input attenuation

		int vbIdx = vb;
		if (vbIdx >= 0 && vbIdx <= DelayAttenuationVBIndex) {
			// vb[0]~vb[DelayAttenuationVBIndex] 1:25
			bs.set1(0, 1);
		} else {
			// vb[DelayAttenuationVBIndex+1]~vb[vb.length-1] 1:25
			bs.set1(1, 1);
		}

		// [0] none
		bs.set1(0, 0);
		return (byte) bs.getValue();
	}

	public boolean isSlowMoveTimebase(int tbidx) {
		return cm.getCoreControl().getTimeConfProvider()
				.isOnSlowMoveTimebase(tbidx);
	}

	public boolean isPulse_Equal(int idx) {
		return PulseTrigger.isCondition_Equal(idx);
	}

	public boolean isSlope_Equal(int idx) {
		return SlopeTrigger.isCondition_Equal(idx);
	}

	public int getThredsholdPairArg(int chl) {
		TriggerSet ts = cm.getTriggerControl().getTriggerSetOrNull(chl);
		if (ts == null)
			return -1;
		int low = ts.slope.c_getLowest() & 0xff;
		int up = ts.slope.c_getUppest() & 0xff;

		return low << 8 | up;
	}

	public static final int ChannelSampleConfig = 0;

	private int remainHtp = 0;

	/**
	 * at timebase scales that need interpolation and trigger locating, after interpolation and trigger locating the sampling points corresponding to the htp movement are still offset relative to the screen, so extra information is needed here to assist trigger locating
	 * 
	 * @return
	 */
	public synchronized int getRemainHtp() {
		return remainHtp;
	}

	public synchronized void commitRemainHtp(int v) {
		remainHtp = v;
	}

	public ControlApps getControlApps() {
		return Platform.getControlApps();
	}

	public BufferredSourceManager getSourceManager() {
		return cm.sourceManager;
	}

	public int getTrgChannles() {
		TriggerControl tc = cm.getTriggerControl();
		// if (tc.isSingleTrg()) {
		// int chl = tc.getSingleTrgChannel();
		// return 1 << chl;
		// } else {
		int chls = tc.getChannelsNumber();
		int v = 0;
		WaveFormInfoControl wfic = cm.getWaveFormInfoControl();
		for (int i = 0; i < chls; i++) {
			// if (wfic.isWFOn(i))
			// send 1 for every channel in all cases, to fix forced trigger being ineffective in normal mode when only a single channel is on
			v = v | (1 << i);
		}
		return v;
		// }
	}

	public boolean alsoSupportPeakDetect() {
		return cm.isPeakDetectWork();
		// the subsequent restriction can be omitted, because the implementation of principle / coreContorl.isPeakDetectWork() has been changed to judge by sampling rate only
		// int tb = getTimeControl().getTimebaseIdx();
		// && (tb >= PreHandler.TB_1_4kFullScreen && tb <
		// PreHandler.TB_10kFullScreen);
	}

}