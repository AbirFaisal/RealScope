package com.owon.vds.calibration;

import java.beans.PropertyChangeSupport;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.mode.control.SampleControl;
import com.owon.uppersoft.vds.core.machine.MachineInfo;
import com.owon.uppersoft.vds.source.comm.InterCommTiny;
import com.owon.uppersoft.vds.source.comm.Submitor2;
import com.owon.uppersoft.vds.ui.dialog.ProgressObserver;
import com.owon.vds.calibration.stuff.ArgCreator;
import com.owon.vds.tiny.firm.pref.PrefControl;

/**
 * Auto calibration manager for Tiny
 * 
 */
public abstract class AutomaticWorkManager {
	protected ControlManager cm;
	private InterCommTiny ict;
	protected DataHouse dh;

	public AutomaticWorkManager(ControlManager cm, InterCommTiny ict,
			DataHouse dh) {
		this.cm = cm;
		this.ict = ict;
		this.dh = dh;
	}

	public void getReady(final ProgressObserver sd, ArgCreator ac,
			PrefControl pc) {
		this.pc = pc;
		scs = preSelCalSetting();

		getReadyForCalibration(sd, ac, ict.getDataReceiveBroadcast(),
				new Runnable() {
					@Override
					public void run() {
						onFinished(sd);
					}
				});
	}

	protected abstract void getReadyForCalibration(ProgressObserver sd,
			ArgCreator ac, PropertyChangeSupport pcs, Runnable finishedJob);

	public abstract void cancel(Runnable afterCancel);

	private SelCalSetting scs;
	private PrefControl pc;

	// Subclasses should call sd.shutdown() after calling the parent's method.
	public void onFinished(ProgressObserver sd) {
		sd.shutdown();
		// System.err.println("Calibration done, runner.run");
		if (cm.isExit())
			return;
		Submitor2 sbm = ict.reinitSubmitor2();
		if (1 == 1) {
			pc.save2DevicePart();
			ict.syncFlash();
		}

		resumeSelCalSetting(scs);

		// Apply the new parameters to the current oscilloscope settings once.
		cm.initDetail(sbm);
		cm.initDetail(new Submitor2()); // Create a new instance of Submitor2
		dh.getWaveFormManager().resumeDraw();
		dh.getControlApps().getOperateBlocker().kickThrough();
	}

	private void resumeSelCalSetting(SelCalSetting scs) {
		// if (true) return;

		dh.getPersistentDisplay().fadeThdOn_Off_UI(scs.persistenceIndex);

		cm.getWaveFormInfoControl().customizeChannelCouplingNInverse(scs.css);

		cm.displayControl.setXYMode(scs.xymode);
		cm.getMeasureManager().setMeasureOn(scs.measureon);
		cm.getSampleControl().c_setModelIdx(scs.sampleIndex);

		cm.getTimeControl().c_setTimebase_HorTrgPos(scs.timebase, scs.htp);

		cm.getDeepMemoryControl().c_setDeepIdx(scs.deepIndex);
	}

	protected SelCalSetting preSelCalSetting() {
		SelCalSetting scs = new SelCalSetting(cm);

		dh.getPersistentDisplay().fadeThdOn_Off_UI(0);
		presetChannels();
		cm.displayControl.setXYMode(false);
		cm.getMeasureManager().setMeasureOn(false);
		cm.getSampleControl().c_setModelIdx(SampleControl.Sample_Sampling);

		MachineInfo mi = cm.getCoreControl().getMachineInfo();
		cm.getTimeControl().c_setTimebase_HorTrgPos(mi.getTimebaseIndex("1us"),
				0);

		cm.getDeepMemoryControl().c_setDeepIdx(0);
		return scs;
	}

	protected void presetChannels() {
		cm.getWaveFormInfoControl().turnOnAllChannelsACNotInverse();
	}
	
	
}