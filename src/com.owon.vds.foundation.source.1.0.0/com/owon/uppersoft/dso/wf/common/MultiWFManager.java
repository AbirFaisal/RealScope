package com.owon.uppersoft.dso.wf.common;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.machine.aspect.IMultiWFManager;
import com.owon.uppersoft.dso.model.WFTimeScopeControl;
import com.owon.uppersoft.dso.model.WaveFormInfoControl;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.dso.wf.ChannelInverseTranslator;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.data.OfflineInfo;
import com.owon.uppersoft.vds.core.machine.MachineType;
import com.owon.uppersoft.vds.core.machine.VDS_Portable;
import com.owon.uppersoft.vds.core.paint.ScreenContext;
import com.owon.uppersoft.vds.core.wf.WaveFormInfo;
import com.owon.uppersoft.vds.core.wf.dm.DMDataInfo;
import com.owon.uppersoft.vds.core.wf.dm.DMInfo;
import com.owon.uppersoft.vds.core.wf.rt.ChannelDataInfo;
import com.owon.uppersoft.vds.core.wf.rt.ChannelsTransportInfo;
import com.owon.uppersoft.vds.core.wf.rt.LoadMedia;

/**
 * MultiWFManager，为不同场合提供数据灌入WaveForm的处理
 * 
 */
public class MultiWFManager implements IMultiWFManager {
	private ControlManager cm;
	private ChannelInverseTranslator cit;

	public MultiWFManager(ControlManager cm) {
		this.cm = cm;
		cit = new ChannelInverseTranslator();
	}

	protected void doReceiveRTData(LoadMedia cti, ScreenContext pc,
			WaveFormManager wfm, boolean asReload) {
		/** TODO for check: 混画，效率的瓶颈在画图，而不是传输，RT传输时短，多幅一次画提高刷新率 */
		Iterator<? extends ChannelDataInfo> it = cti.iterator_ChannelDataInfo();
		while (it.hasNext()) {
			ChannelDataInfo cdi = it.next();
			int chl = cdi.chl;
			WaveForm wf = wfm.getWaveForm(chl);
			// TODO 更新频率值

			wf.setDm_xoffset(cdi.xoffset);
			wf.prepareRTNormalPaint(cdi, cti,
					asReload ? pc.getHcenter() : wf.getYb(pc));
			cti.retireInstance(cdi);
		}
		if (cti.getChannelCount() > 0) {
			// 在wf之后计算
			wfm.afterWaveFormsFeed(pc);
		}
	}

	public void receiveRTData(LoadMedia cti, ScreenContext pc,
			WaveFormManager wfm, boolean freshFreq) {
		BigDecimal bd = cm.getCurrentSampleRateBD_kHz();
		MachineType mi = cm.getMachine();
		Iterator<? extends ChannelDataInfo> it = cti.iterator_ChannelDataInfo();
		while (it.hasNext()) {
			ChannelDataInfo cdi = it.next();
			int chl = cdi.chl;
			WaveForm wf = wfm.getWaveForm(chl);
			ChannelInfo ci = wf.wfi.ci;

			cdi.computeFreq(bd, mi);
			wf.saveFirstLoadPos0();

			if (freshFreq)
				ci.updateFreqLabel(cdi.getFrequencyFloat());
		}
		doReceiveRTData(cti, pc, wfm, true);
	}

	public void receiveOfflineData(LoadMedia cti, ScreenContext pc,
			WaveFormManager wfm) {
		Iterator<? extends ChannelDataInfo> it = cti.iterator_ChannelDataInfo();
		while (it.hasNext()) {
			ChannelDataInfo cdi = it.next();
			int chl = cdi.chl;
			WaveForm wf = wfm.getWaveForm(chl);

			ChannelInfo ci = wf.wfi.ci;
			ci.setOn(true);

			OfflineInfo oi = cdi.oi;
			// 离线数据载入时的剩余参数设置
			ci.setVoltbaseIndex(oi.vbIdx, false, false);
			ci.setPos0(oi.pos0);
			wf.saveFirstLoadPos0();
			ci.setProbeMultiIdx(oi.probeMultiIdx);

			/** 录制波形无需进行反相 */
			ci.c_setInverse(cit.setOfflineInverseType_Normal(cdi
					.getInverseType()));

			ci.updateFreqLabel(cdi.getFrequencyFloat());
		}
		doReceiveRTData(cti, pc, wfm, true);
	}

	public void receiveOfflineDMData(DMInfo cti, ScreenContext pc,
			BigDecimal tbbd, int tbIdx, WaveFormManager wfm) {
		List<DMDataInfo> list = cti.channels;
		wfm.offAllWaveForms();
		for (DMDataInfo cdi : list) {
			int chl = cdi.chl;
			WaveForm wf = wfm.getWaveForm(chl);

			ChannelInfo ci = wf.wfi.ci;
			ci.setOn(true);

			// 离线数据载入时的剩余参数设置
			ci.setVoltbaseIndex(cdi.vbIdx, false, false);
			ci.setPos0(cdi.pos0);

			/** 预先保存初始零点 */
			wf.saveFirstLoadPos0();

			boolean inverse = cit.setOfflineInverseType_DM(cdi.inverseType);
			cdi.shouldInverse = inverse;
			ci.c_setInverse(inverse);

			ci.setProbeMultiIdx(cdi.probeMultiIdx);

			ci.updateFreqLabel(cdi.getFrequencyFloat());
			wf.wfi.prepareDM(pc, cdi, tbbd, tbIdx, ci.getVoltbaseIndex(),
					cm.getMachine(), cti);
		}

		simulateInitAsDM(cti, pc, wfm, true, tbIdx);
	}

	public void receiveRTDMData(DMInfo ci, ScreenContext pc, BigDecimal tbbd,
			int tbIdx, WaveFormManager wfm) {
		/** TODO for check: 混画，效率的瓶颈在画图，而不是传输，RT传输时短，多幅一次画提高刷新率 */
		List<DMDataInfo> list = ci.channels;
		for (DMDataInfo cdi : list) {
			int chl = cdi.chl;
			WaveForm wf = wfm.getWaveForm(chl);
			cdi.shouldInverse = cit.setRTInverseType_DM(wf.wfi.ci.isInverse());

			ChannelInfo chi = wf.wfi.ci;
			chi.updateFreqLabel(cdi.getFrequencyFloat());
			wf.wfi.prepareDM(pc, cdi, tbbd, tbIdx, chi.getVoltbaseIndex(),
					cm.getMachine(), ci);
		}

		ci.setPKDetect(cm.isPeakDetectWork());
		/** 使用拿深存储前最后一次的零点位置，就可以处理停止后发生零点移动的情况 */
		simulateInitAsDM(ci, pc, wfm, false, cm.getTimeControl()
				.getTimebaseIdx());
	}

	/** 深存储数据 灌入内存 */
	protected void simulateInitAsDM(DMInfo ci, ScreenContext pc,
			WaveFormManager wfm, boolean asReload, int tbidx) {
		WFTimeScopeControl wftsc = wfm.getWFTimeScopeControl();
		WaveFormInfoControl wfic = cm.getWaveFormInfoControl();
		ChannelsTransportInfo cpi = retrieveChannelsTransportInfoFromDM(false,
				asReload, wfic);
		wftsc.loadDM(cpi, ci.isPKDetect(), retrieveDrawModeFromDM(wfic),
				getDMGap(wfic));
		doReceiveRTData(cpi, pc, wfm, asReload);
	}

	public void simulateReloadAsDM(ScreenContext pc, WaveFormInfoControl wfic,
			WFTimeScopeControl wftsc, WaveFormManager wfm) {
		ChannelsTransportInfo cti = retrieveChannelsTransportInfoFromDM(false,
				false, wfic);

		/** 不设置wftsc.setPk_1kpix_250(pk_1kpix_250); */
		wftsc.loadDM(cti, wftsc.isPK_Detect(), retrieveDrawModeFromDM(wfic),
				getDMGap(wfic));
		doReceiveRTData(cti, pc, wfm, false);
	}

	/**
	 * 由深存储转化出运行时的屏幕点数
	 * 
	 * @return
	 */
	public ChannelsTransportInfo retrieveChannelsTransportInfoFromDM(
			boolean avgon, boolean asReLoad, WaveFormInfoControl wfic) {
		ChannelsTransportInfo cti = Platform.getControlApps().getDaemon()
				.getChannelsTransportInfo();
		cti.reset('T');

		int len = wfic.getLowMachineChannels();
		for (int i = 0; i < len; i++) {
			WaveFormInfo wfi = wfic.getWaveFormInfo(i);
			ChannelInfo ci = wfi.ci;

			if (!ci.isOn())
				continue;

			int wnb = ci.getNumber();
			ChannelDataInfo cdi = cti.getInstance();
			cdi.chl = wnb;

			if (ci.isInverse()) {
				cdi.setInverseType(VDS_Portable.INVERSE_TYPE_REVERSED);
			} else {
				cdi.setInverseType(VDS_Portable.INVERSE_TYPE_RAW_FINE);
			}

			ByteBuffer bb = wfi.getADC_Buf();
			cdi.initPos = bb.position();

			cdi.screendatalen = bb.remaining();
			cdi.datalen = cdi.screendatalen;
			cdi.slowMove = 0;

			cdi.xoffset = wfi.getXOffset_DM();

			cdi.setUniqueAdcbuf(bb);

			cti.addChannelDataInfo(cdi);

		}
		cti.setDataComplete(1);
		return cti;
	}

	public double getDMGap(WaveFormInfoControl wfic) {
		WaveFormInfo wfii = wfic.getWaveFormInfoForDM();
		if (wfii == null)
			return -1;
		return wfii.getDMGap();
	}

	/**
	 * DM时通过LocInfo得到
	 * 
	 * @return
	 */
	public int retrieveDrawModeFromDM(WaveFormInfoControl wfic) {
		WaveFormInfo wfii = wfic.getWaveFormInfoForDM();
		if (wfii == null)
			return -1;

		return wfii.getDrawMode();
	}
}
