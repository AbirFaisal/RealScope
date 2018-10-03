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
 * LowControlManger，用于从后台系统中获取对应的参数，这些参数是已有保存的设置，不直接通过Submitor调用传递，需要自行获取
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
	 * 之前机型的做法是在达不到更高采样率的档位下，使用更少的满屏点进行插值，达到符合的频谱档位下的点数；
	 * 
	 * 而在tiny中，仅简单地从存储深度中取用满足fft所需的2048个点，不插值做fft，故而频谱档位没有提高；
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

		// [7] 通道开关
		bs.set1(ci.isOn(), 7);

		// [56] 耦合方式
		int cplidx = Tiny_Coupling[ci.getCouplingIdx()];
		// System.err.println("cplidx: "+cplidx);
		bs.setN(cplidx, 5, 6);

		// [4] 无
		bs.set1(0, 4);

		// [23] 带宽限制
		bs.setN(0, 2, 3);

		// 1 输入衰减

		int vbIdx = vb;
		if (vbIdx >= 0 && vbIdx <= DelayAttenuationVBIndex) {
			// vb[0]~vb[DelayAttenuationVBIndex] 1:25
			bs.set1(0, 1);
		} else {
			// vb[DelayAttenuationVBIndex+1]~vb[vb.length-1] 1:25
			bs.set1(1, 1);
		}

		// [0] 无
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
	 * 在时基需要插值拉触发的档位，在插值和拉触发之后，由于htp移动对应的采样点相对屏幕还是有偏差，这里需要增加信息辅助拉触发
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
			// 各种情况下各通道都发1，解决在单通道开启的情况下，正常模式的强制触发无效的问题
			v = v | (1 << i);
		}
		return v;
		// }
	}

	public boolean alsoSupportPeakDetect() {
		return cm.isPeakDetectWork();
		// 可不加后续的限制，因为principle / coreContorl.isPeakDetectWork()的实现已经更改为只根据采样率判断
		// int tb = getTimeControl().getTimebaseIdx();
		// && (tb >= PreHandler.TB_1_4kFullScreen && tb <
		// PreHandler.TB_10kFullScreen);
	}

}