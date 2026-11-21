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
 * MultiWFManager, provides the processing for feeding data into WaveForm for different situations
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
		/** TODO for check: mixed drawing; the efficiency bottleneck is drawing, not transfer; RT transfer is short, drawing several frames at once improves the refresh rate */
		Iterator<? extends ChannelDataInfo> it = cti.iterator_ChannelDataInfo();
		while (it.hasNext()) {
			ChannelDataInfo cdi = it.next();
			int chl = cdi.chl;
			WaveForm wf = wfm.getWaveForm(chl);
			// TODO update the frequency value

			wf.setDm_xoffset(cdi.xoffset);
			wf.prepareRTNormalPaint(cdi, cti,
					asReload ? pc.getHcenter() : wf.getYb(pc));
			cti.retireInstance(cdi);
		}
		if (cti.getChannelCount() > 0) {
			// computed after wf
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
			// remaining parameter settings when loading offline data
			ci.setVoltbaseIndex(oi.vbIdx, false, false);
			ci.setPos0(oi.pos0);
			wf.saveFirstLoadPos0();
			ci.setProbeMultiIdx(oi.probeMultiIdx);

			/** Recorded waveforms need no inversion */
			ci.c_setInverse(ChannelInverseTranslator.setOfflineInverseType_Normal(cdi
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

			// remaining parameter settings when loading offline data
			ci.setVoltbaseIndex(cdi.vbIdx, false, false);
			ci.setPos0(cdi.pos0);

			/** Save the initial zero point beforehand */
			wf.saveFirstLoadPos0();

			boolean inverse = ChannelInverseTranslator.setOfflineInverseType_DM(cdi.inverseType);
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
		/** TODO for check: mixed drawing; the efficiency bottleneck is drawing, not transfer; RT transfer is short, drawing several frames at once improves the refresh rate */
		List<DMDataInfo> list = ci.channels;
		for (DMDataInfo cdi : list) {
			int chl = cdi.chl;
			WaveForm wf = wfm.getWaveForm(chl);
			cdi.shouldInverse = ChannelInverseTranslator.setRTInverseType_DM(wf.wfi.ci.isInverse());

			ChannelInfo chi = wf.wfi.ci;
			chi.updateFreqLabel(cdi.getFrequencyFloat());
			wf.wfi.prepareDM(pc, cdi, tbbd, tbIdx, chi.getVoltbaseIndex(),
					cm.getMachine(), ci);
		}

		ci.setPKDetect(cm.isPeakDetectWork());
		/** Using the last zero point position before fetching deep memory handles zero point moves that happen after stopping */
		simulateInitAsDM(ci, pc, wfm, false, cm.getTimeControl()
				.getTimebaseIdx());
	}

	/** Feed deep memory data into memory */
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

		/** do not set wftsc.setPk_1kpix_250(pk_1kpix_250); */
		wftsc.loadDM(cti, wftsc.isPK_Detect(), retrieveDrawModeFromDM(wfic),
				getDMGap(wfic));
		doReceiveRTData(cti, pc, wfm, false);
	}

	/**
	 * Convert deep memory to the run-time screen point count
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
	 * DM case: obtained through LocInfo
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
