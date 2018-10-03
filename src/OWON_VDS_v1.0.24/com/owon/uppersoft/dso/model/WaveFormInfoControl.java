package com.owon.uppersoft.dso.model;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.dso.wf.ON_WF_Iterator;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.calibration.ChannelSets;
import com.owon.uppersoft.vds.core.aspect.base.IOrgan;
import com.owon.uppersoft.vds.core.aspect.control.ISupportChannelsNumber;
import com.owon.uppersoft.vds.core.aspect.control.Pos0_VBChangeInfluence;
import com.owon.uppersoft.vds.core.aspect.control.VoltageProvider;
import com.owon.uppersoft.vds.core.comm.effect.IPatchable;
import com.owon.uppersoft.vds.core.comm.effect.Submitable;
import com.owon.uppersoft.vds.core.machine.VDS_Portable;
import com.owon.uppersoft.vds.core.wf.WaveFormInfo;
import com.owon.uppersoft.vds.core.wf.rt.ChannelDataInfo;
import com.owon.uppersoft.vds.core.wf.rt.ChannelsTransportInfo;
import com.owon.uppersoft.vds.util.Pref;

public abstract class WaveFormInfoControl implements IPatchable, IOrgan,
		ISupportChannelsNumber {
	private int lowMachineChannels;

	public int getMaxSupportChannelsNumber() {
		return wfinfo.length;
	}

	public WaveFormInfoControl(VoltageProvider vp, int channelsNumber,
			Pos0_VBChangeInfluence pvi) {
		wfinfo = new WaveFormInfo[channelsNumber];
		wfDisplay = new boolean[channelsNumber];

		for (int i = 0; i < channelsNumber; i++) {
			wfinfo[i] = createWaveFormInfo(i, vp, pvi);
		}

		lowMachineChannels = channelsNumber;
	}

	protected abstract WaveFormInfo createWaveFormInfo(int i,
			VoltageProvider vp, Pos0_VBChangeInfluence pvi);

	public boolean isSupportDelay1_2() {
		return wfinfo[0].ci.isOn() && wfinfo[1].ci.isOn();
	}

	public boolean isSupportDelay3_4() {
		return wfinfo[2].ci.isOn() && wfinfo[3].ci.isOn();
	}

	public void releaseWFIDMLocInfos() {
		int len = wfinfo.length;
		for (int i = 0; i < len; i++) {
			wfinfo[i].releaseDM();
		}
	}

	public List<WaveForm> getWFs() {
		return wfs.subList(0, lowMachineChannels);
	}

	public Iterator<WaveForm> getLowMachineWFIterator() {
		return getWFs().iterator();
	}

	@Override
	public int getSupportChannelsNumber() {
		return lowMachineChannels;
	}

	public int getLowMachineChannels() {
		return lowMachineChannels;
	}

	public void selectOneChannelOn(int chl) {
		for (int i = 0; i < lowMachineChannels; i++) {
			wfinfo[i].ci.c_setOn(i == chl);
		}
	}

	public void storeChannelsDisplaybeforeFFT() {
		for (int i = 0; i < lowMachineChannels; i++) {
			wfDisplay[i] = wfinfo[i].ci.isOn();
		}
	}

	public void recoverChannelsDisplayfromNonFFT() {
		for (int i = 0; i < lowMachineChannels; i++) {
			wfinfo[i].ci.setOnWithoutSync(wfDisplay[i]);
		}
	}

	public void preSetChannelsForFFT(int chl) {
		for (int i = 0; i < lowMachineChannels; i++) {
			wfinfo[i].ci.setOnWithoutSync(i == chl);
		}
	}

	public void turnOnAllChannelsWitouSync() {
		for (int i = 0; i < lowMachineChannels; i++) {
			wfinfo[i].ci.setOnWithoutSync(true);
		}
	}

	public void turnOnAllChannelsACNotInverse() {
		turnOnAllChannelsNotInverse(ChannelInfo.CouplingACIndex);
	}

	public void turnOnAllChannelsNotInverse(int coupling) {
		for (int i = 0; i < lowMachineChannels; i++) {
			ChannelInfo ci = wfinfo[i].ci;
			ci.c_setCoupling(coupling);
			ci.c_setInverse(false);
			ci.c_setOn(true);
			// ci.initbandlimit_forCorrectOSerr();
		}
	}
	
	public void turnOnAllChannelsCouplingNotInverse() {
		for (int i = 0; i < lowMachineChannels; i++) {
			ChannelInfo ci = wfinfo[i].ci;
			// ci.c_setCoupling(ChannelInfo.CouplingACIndex);
			ci.c_setInverse(false);
			ci.c_setOn(true);
			// ci.initbandlimit_forCorrectOSerr();
		}
	}

	public void turnChannelsDC(ChannelSets[] css) {
		int len = css.length;
		for (int i = 0; i < len; i++) {
			ChannelSets cs = css[i];

			WaveFormInfo wfi = wfinfo[cs.channel];

			if (!wfi.ci.isOn())
				continue;
			wfi.ci.c_setCoupling(cs.coupling);
		}
	}

	public boolean isWFOn(int idx) {
		if (idx < 0 || idx >= lowMachineChannels)
			return false;
		return wfinfo[idx].ci.isOn();
	}

	/**
	 * @return 开启的通道的耦合
	 */
	public ChannelSets[] getAllChannelSets() {
		ChannelSets[] css = new ChannelSets[lowMachineChannels];
		for (int i = 0; i < lowMachineChannels; i++) {
			WaveFormInfo wfi = wfinfo[i];
			ChannelSets cs = new ChannelSets();
			css[i] = cs;
			cs.channel = i;
			cs.on = wfi.ci.isOn();
			if (!cs.on)
				continue;

			cs.coupling = wfi.ci.getCouplingIdx();
			cs.inverse = wfi.ci.isInverse();
			cs.vbidx = wfi.ci.getVoltbaseIndex();
			cs.pos0 = wfi.ci.getPos0();
		}
		return css;
	}

	/**
	 * 通过参数定制通道信息
	 * 
	 * @param channels
	 * @param coupling
	 * @param inverses
	 */
	public void customizeChannelCouplingNInverse(ChannelSets[] css) {
		int len = css.length;
		for (int i = 0; i < len; i++) {
			ChannelSets cs = css[i];

			WaveFormInfo wfi = wfinfo[cs.channel];

			wfi.ci.c_setOn(cs.on);
			if (!cs.on)
				continue;
			wfi.ci.c_setCoupling(cs.coupling);
			wfi.ci.c_setInverse(cs.inverse);
			wfi.ci.c_setZero(cs.pos0, true);
			wfi.ci.c_setVoltage(cs.vbidx, null);
		}
	}

	public byte getWaveFormFlag() {
		return (byte) getWaveFormFlagTailing(1);
	}

	/**
	 * 通道状态标志在最低位. j的各位都为零,表示通道全关
	 * 
	 * @param bits
	 * @return j 从低到高位顺序记录1到N通道开关情况
	 */
	public int getWaveFormFlagTailing(int bits) {
		int j = 0;
		int len = lowMachineChannels;
		for (int i = len - 1; i >= 0; i--) {
			j = (j << bits) | (wfinfo[i].ci.isOn() ? 1 : 0);
		}
		return j;
	}

	/**
	 * 通道状态标志在最高位
	 * 
	 */
	public int getWaveFormFlagHeading16bitsL(byte[] arr, final int begin,
			boolean needTrgAround, int databeg, int datalen) {
		final int bits = 7;
		int p = begin;

		int j;
		int len = lowMachineChannels;
		for (int i = len - 1; i >= 0; i--) {
			j = wfinfo[i].ci.isOn() ? (1 << bits) : 0;
			arr[p] = (byte) (j | databeg);
			p++;

			j = needTrgAround ? (1 << bits) : 0;
			arr[p] = (byte) (j | datalen);
			p++;
		}
		return p;
	}

	public ChannelInfo getWaveFormChannelInfo(int idx) {
		return wfinfo[idx].ci;
	}

	public int getOnWaveFormNumber() {
		int channelCount = 0;
		int len = lowMachineChannels;
		for (int i = 0; i < len; i++) {
			if (wfinfo[i].ci.isOn())
				channelCount++;
		}
		return channelCount;
	}

	public ArrayList<WaveForm> getClosedWaveForms() {
		ArrayList<WaveForm> offWfs = new ArrayList<WaveForm>();
		for (int i = 0; i < lowMachineChannels; i++) {
			if (!isWFOn(i)) {
				offWfs.add(getWaveForm(i));
			}
		}
		return offWfs;
	}

	public WaveFormInfo getWaveFormInfoForDM() {
		WaveFormInfo wfii = null;
		for (WaveFormInfo wfi : wfinfo) {
			if (wfi.ci.isOn()) {
				wfii = wfi;
				break;
			}
		}

		return wfii;
	}

	private WaveFormInfo[] wfinfo;
	private boolean[] wfDisplay;

	public WaveFormInfo getWaveFormInfo(int idx) {
		return wfinfo[idx];
	}

	public WaveFormInfo[] getWaveFormInfos() {
		return wfinfo;
	}

	public void load(Pref p) {
		int len = wfinfo.length;
		for (int i = 0; i < len; i++) {
			wfinfo[i].load(p);
		}
		selectedwfIdx = p.loadInt("selectedwfIdx");
		if (selectedwfIdx >= lowMachineChannels)
			selectedwfIdx = lowMachineChannels - 1;
		// lowMachineChannels = p.loadInt( "lowMachineChannels");
		// if (lowMachineChannels <= 0)
		// lowMachineChannels = 2;
	}

	public void persist(Pref p) {
		int len = wfinfo.length;
		for (int i = 0; i < len; i++) {
			wfinfo[i].persist(p);
		}
		p.persistInt("selectedwfIdx", selectedwfIdx);
		p.persistInt("lowMachineChannels", lowMachineChannels);
	}

	@Override
	public void selfSubmit(Submitable sbm) {
		int i = 0;
		for (; i < lowMachineChannels; i++) {
			wfinfo[i].ci.c_SyncChannel(sbm);
		}
	}

	private int selectedwfIdx = 0;

	public int getSelectedwfIdx() {
		return selectedwfIdx;
	}

	public WaveForm getSelectedWF() {
		return getWaveForm(selectedwfIdx);
	}

	public void setSelectedwfIdx(int selectedwfIdx) {
		this.selectedwfIdx = selectedwfIdx;
	}

	private ArrayList<WaveForm> wfs;

	public ON_WF_Iterator on_wf_Iterator() {
		return new ON_WF_Iterator(getWFs());
	}

	public WaveForm getWaveForm(int idx) {
		if (idx < 0 || idx >= lowMachineChannels)
			return null;
		return wfs.get(idx);
	}

	public void setDataHouse(DataHouse dh) {
		int len = wfinfo.length;
		// 内部多数去掉直接引用
		wfs = new ArrayList<WaveForm>(len);
		// 初始化时就固定的通道数，将改掉

		for (int i = 0; i < len; i++) {
			WaveForm wf = createWaveForm(dh, wfinfo[i]);
			wfs.add(wf);
		}
	}

	protected abstract WaveForm createWaveForm(DataHouse dh,
			WaveFormInfo waveFormInfo);
}
