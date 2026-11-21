package com.owon.uppersoft.dso.wf.common;

import static com.owon.uppersoft.dso.global.DataHouse.Offline_DM;
import static com.owon.uppersoft.dso.global.DataHouse.RT_DM;
import static com.owon.uppersoft.dso.global.DataHouse.RT_Normal;

import java.math.BigDecimal;

import com.owon.uppersoft.dso.function.PersistentDisplay;
import com.owon.uppersoft.dso.function.RecordControl;
import com.owon.uppersoft.dso.function.record.OfflineChannelsInfo;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.CoreControl;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.machine.aspect.IMultiReceiver;
import com.owon.uppersoft.dso.mode.control.TimeControl;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.source.comm.TrgStatus;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.dso.view.ToolPane;
import com.owon.uppersoft.vds.core.aspect.control.ITimeControl;
import com.owon.uppersoft.vds.core.machine.MachineType;
import com.owon.uppersoft.vds.core.paint.ScreenContext;
import com.owon.uppersoft.vds.core.wf.dm.DMInfo;
import com.owon.uppersoft.vds.core.wf.rt.ChannelsTransportInfo;
import com.owon.uppersoft.vds.print.PrinterPreviewFrame;

/**
 * MultiReceiver, provides the processing for reading data into the DataHouse for different situations, and also updates the state of the corresponding settings in controlManager
 * 
 */
public class MultiReceiver implements IMultiReceiver {
	private ControlManager cm;
	private WaveFormManager wfm;
	private ScreenContext pc;
	private TimeControl tc;

	public MultiReceiver(ControlManager cm, WaveFormManager wfm) {
		this.cm = cm;
		this.wfm = wfm;
		pc = cm.paintContext;
		tc = cm.getTimeControl();
	}

	protected void dataCompleteNIncrease(DataHouse dh) {
		dh.dataComplete();
		drawCount++;
	}

	private int drawCount = 0;

	protected void resetDrawCount() {
		drawCount = 0;
	}

	@Override
	public void receiveOfflineData(OfflineChannelsInfo oci, DataHouse dh,
			int status) {
		if (oci.getChannelCount() == 0)
			return;
		/** 0 frame case returns, so that empty frames are not counted during average sampling */
		if (oci.getFrameCount() == 0)
			return;
		dh.setStatus(status);
		dh.dataComplete();

		// timebase
		MainWindow mw = Platform.getMainWindow();
		ToolPane tp = mw.getToolPane();

		boolean slow = cm.getCoreControl().isRunMode_slowMove();

		// System.err.println("receiveRTData: "+pc.pkdetect);

		// Preparation for deep memory drawing: point LocInfo.cba at dh.cba etc., so that it does not crash when the timebase changes and is set.
		int dmidx = cm.getMachineInfo().getDMIndexFromLength(oci.DMlen);
		if (dmidx >= 0)
			cm.getDeepMemoryControl().setDeepIdx(dmidx);
		// Setting and updating the horizontal trigger position; the depth must be set first
		tc.setHorTrgPos(oci.horTrgPos);
		cm.getCoreControl().updateHorTrgIdx4View();
		cm.getCoreControl().updateHorTrgPosRange();

		wfm.offAllWaveForms();
		wfm.receiveOfflineData(oci, pc, slow, oci.timebase);
		tc.offline_setTimeBaseIdx(oci.timebase);

		cm.measure(wfm);

		/** Computation depends on the selected waveform info; must be done after the waveform is imported */
		tc.recomputeHorizontalTriggerLabel();
		mw.updateShow();
		tp.updateChannels();
		mw.getITitleStatus().updateView();
		cm.pcs.firePropertyChange(ITimeControl.onTimebaseUpdated, null, null);

		cm.pcs.firePropertyChange(PropertiesItem.APPLY_CHANNELS, null, null);

		PrinterPreviewFrame.selfRepaint();
	}

	public void receiveOfflineDMData(DMInfo ci, DataHouse dh) {
		if (ci.channelNum == 0)
			return;
		dh.dataNotComplete();
		dh.setStatus(Offline_DM);

		MainWindow mw = Platform.getMainWindow();
		ToolPane tp = mw.getToolPane();
		CoreControl cc = cm.getCoreControl();

		// pc.slow = tc.isSlowMove();
		// System.err.println(ci.datalen);
		int dmidx = cm.getMachineInfo().getDMIndexFromLength(ci.datalen);
		dh.setupDeepMemoryStorage(ci.file);

		int tbIdx = ci.timebase;
		BigDecimal tbd = cc.getTimeConfProvider().getBDTimebase(tbIdx);

		// memory depth TODO temporary for testing; the pixel difference from the screen center point is converted to the trigger point position at read time, applicable in the center-symmetric run-time case
		cm.getDeepMemoryControl().setDeepIdx(dmidx);

		// Setting and updating the horizontal trigger position; the depth must be set first
		tc.setHorTrgPos(ci.horTrgPos);

		int initPos = ci.initPos;
		int screenlen = ci.getScreendatalen();
		cc.updateDMHorTrgIdx4View(initPos, screenlen);

		cc.updateHorTrgPosRange();

		tc.setTimebaseIndexValue(tbIdx);
		wfm.receiveOfflineDMData(ci, pc, tbd, tbIdx);
		dh.dataComplete();

		// point LocInfo.cba at dh.cba etc., so that it does not crash when the timebase changes and is set.
		tc.offline_setTimeBaseIdx(tbIdx);
		// On a timebase change, LocInfo->CompressInfoUnit.tbTranslate() needs the buf data, so the cba of LocInfo cannot be empty; set it first.

		cm.measure(wfm);

		/** Computation depends on the selected waveform info; must be done after the waveform is imported */
		tc.recomputeHorizontalTriggerLabel();

		cm.pcs.firePropertyChange(PropertiesItem.APPLY_CHANNELS, null, null);

		mw.updateShow();
		tp.updateChannels();
		mw.getITitleStatus().updateView();
		cm.pcs.firePropertyChange(ITimeControl.onTimebaseUpdated, null, null);

		PrinterPreviewFrame.selfRepaint();
	}

	public void receiveRTData(ChannelsTransportInfo cti, DataHouse dh) {
		dh.dataNotComplete();
		// DBG.dbgln(ci.channels.size() + ", " + ci.dataComplete);
		if (cti.getChannelCount() == 0 || cti.getDataComplete() == 0)
			return;

		/** 0 frame case returns, so that empty frames are not counted during average sampling */
		if (cti.getFrameCount() == 0)
			return;

		dh.setStatus(RT_Normal);// also dm
		dataCompleteNIncrease(dh);

		MainWindow mw = Platform.getMainWindow();
		ToolPane tp = mw.getToolPane();

		boolean slow = cm.getCoreControl().isRunMode_slowMove();
		// System.err.println("slow: "+slow);
		/**
		 * Extra check, to prevent getting a few slow sweep frames when switching from slow sweep to non-slow sweep
		 * 
		 * In non-slow sweep, cti.slowMove is not used and waveforms are drawn as usual, but these few frames still contain slow sweep data and need to be filtered out
		 */

		if (!slow) {
			if (cti.slowMove > 0) {
				MachineType mt = cm.getMachine();
				// LATER
				boolean pass = mt.passSlowOnNoneSlowTimebase();
				if (pass) {
					cti.slowMove = 0;
				} else {
					return;
				}
			}
		}
		// System.err.println("receiveRTData: "+pc.pkdetect);

		cm.getCoreControl().updateHorTrgIdx4View();

		/** When doing a single trigger in slow sweep, the frame where it stops is a special case, because it cannot be drawn in the slow sweep manner */
		boolean sweepOnceStop = cm.getTriggerControl().isSweepOnce()
				&& cti.triggerStatus == TrgStatus.Stop.ordinal();
		slow = slow && !sweepOnceStop;
		wfm.receiveRTData(cti, pc, slow);

		cm.updateMeasure(wfm);

		/** Computation depends on the selected waveform info; must be done after the waveform is imported */

		PersistentDisplay pd = dh.getPersistentDisplay();
		// RT persistence
		if (pd.isUseCanvasBuffer()) {
			pd.bufferWaveForms(pc, wfm, mw);
		} else {
			mw.updateShow();
		}

		tp.updateAfterData();

		RecordControl rc = cm.rc;
		rc.recordOnce(wfm, cti);
	}

	public void receiveRTDMData(DMInfo ci, DataHouse dh) {
		// DBG.dbgln(ci.channels.size() + ", " + ci.dataComplete);
		if (ci.channelNum == 0 || ci.dataComplete == 0)
			return;
		dh.setStatus(RT_DM);
		dh.dataComplete();

		MainWindow mw = Platform.getMainWindow();
		ToolPane tp = mw.getToolPane();

		dh.setupDeepMemoryStorage(ci.file);

		int initPos = ci.initPos;
		int screenlen = ci.getScreendatalen();
		cm.getCoreControl().updateDMHorTrgIdx4View(initPos, screenlen);

		wfm.receiveRTDMData(ci, pc, tc.getBDTimebase(), tc.getTimebaseIdx());

		// When stopped, loading deep memory does not compute the measurement values again
		/** Computation depends on the selected waveform info; must be done after the waveform is imported */
		mw.updateShow();
		tp.updateAfterData();
	}

}
