package com.owon.vds.calibration;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.CoreControl;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.wf.ON_WF_Iterator;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.aspect.base.Logable;
import com.owon.uppersoft.vds.core.aspect.control.VoltageProvider;
import com.owon.uppersoft.vds.source.comm.InterCommTiny;
import com.owon.uppersoft.vds.ui.dialog.ProgressObserver;
import com.owon.vds.calibration.stuff.ArgCreator;

public abstract class CalibrationRunner implements Logable {
	public static final int ZEROSTEPCAL_LIMIT = 5;
	public static final int ZEROSTEPCAL_BASE = 100;

	private WaveFormManager waveFormManager;
	private ControlManager controlManager;
	private ProgressObserver progressObserver;
	private VoltageProvider voltageProvider;

	private Runnable finishedJob;
	private CoreControl coreControl;
	private int maximum;
	private int progress;
	private PropertyChangeSupport ict;

	private ArgCreator ac;

	public CalibrationRunner(ArgCreator ac, ProgressObserver progressObserver,
			Runnable finishedJob, PropertyChangeSupport ict) {
		this.ac = ac;
		this.progressObserver = progressObserver;
		this.ict = ict;
		this.finishedJob = finishedJob;

		DataHouse dh = Platform.getDataHouse();
		controlManager = dh.controlManager;
		coreControl = dh.controlManager.getCoreControl();
		waveFormManager = dh.getWaveFormManager();
		voltageProvider = coreControl.getVoltageProvider();
	}

	protected abstract int computeMaximum(int chlnum, int vbnum);

	protected abstract IWFCalRoutine createWFCalRoutine(int vbnum, WaveForm wf,
			ArgCreator ac);

	public void getReady() {
		logln("parallel");

		int vbnum = voltageProvider.getVoltageNumber();
		int wfnum = coreControl.getWaveFormInfoControl().getLowMachineChannels();//
		maximum = computeMaximum(wfnum, vbnum);

		progressObserver.setMaximum(maximum);

		coreControl.getTimeControl().c_setTimebase_HorTrgPos(
				coreControl.getMachineInfo().getTimebaseIndex("1ms"), 0);

		wfrlist = new ArrayList<IWFCalRoutine>(wfnum);
		ON_WF_Iterator owi = waveFormManager.on_wf_Iterator();
		while (owi.hasNext()) {
			WaveForm wf = owi.next();
			// if(wf.getChannelNumber() == 1)continue;
			IWFCalRoutine wfr = createWFCalRoutine(vbnum, wf, ac);
			wfr.getReady();
			wfrlist.add(wfr);
		}
		progress = 0;
		isOver = false;
		isCancel = false;
		afterCancelJob = null;
		ict.addPropertyChangeListener(pcl);
	}

	private PropertyChangeListener pcl = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			String n = evt.getPropertyName();
			if (n.equalsIgnoreCase(InterCommTiny.NEW_DATA_RECEIVE)) {
				call();
			}
		}
	};

	private boolean isOver;
	private List<IWFCalRoutine> wfrlist;

	private boolean isExit() {
		return controlManager.isExit();
	}

	private boolean isCancel;

	public boolean isCancel() {
		return isCancel;
	}

	private Runnable afterCancelJob;

	public void cancel(Runnable afterJob) {
		this.isCancel = true;
		this.afterCancelJob = afterJob;
	}

	private void call() {
		if (isExit() || isOver) {
			return;
		}

		if (progress < maximum && !isCancel) {
			Iterator<IWFCalRoutine> wfri = wfrlist.iterator();
			while (wfri.hasNext()) {
				if (isExit())
					return;

				/** 校正终结的通道 */
				IWFCalRoutine wfr = wfri.next();
				if (wfr.getRoutCalType() < 0)
					continue;

				if (isCancel) {
					jobOnFinished();
					break;
				}

				/** 校正 */
				int v = wfr.routOut();
				if (v > 0) {
					progress += v;
					progressObserver.increaseValue(v);
				}
			}
			logln("...roll...");
		} else {
			jobOnFinished();
		}

		if (isCancel) {
			if (afterCancelJob != null)
				afterCancelJob.run();
		}
	}

	private void jobOnFinished() {
		isOver = true;
		ict.removePropertyChangeListener(pcl);
		if (finishedJob != null)
			finishedJob.run();
	}

	@Override
	public void log(Object o) {
		System.err.print(o);
	}

	@Override
	public void logln(Object o) {
		System.err.println(o);
	}

}