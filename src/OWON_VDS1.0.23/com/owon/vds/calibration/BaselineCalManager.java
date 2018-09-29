package com.owon.vds.calibration;

import java.beans.PropertyChangeSupport;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.source.comm.InterCommTiny;
import com.owon.uppersoft.vds.ui.dialog.ProgressObserver;
import com.owon.vds.calibration.stuff.ArgCreator;

/**
 * 基线校正管理器
 * 
 */
public class BaselineCalManager extends AutomaticWorkManager {
	private CalibrationRunner baselineCalRunner;

	public BaselineCalManager(ControlManager cm, InterCommTiny ict, DataHouse dh) {
		super(cm, ict, dh);
	}

	public void cancel(Runnable afterCancel) {
		if (baselineCalRunner != null) {
			baselineCalRunner.cancel(afterCancel);
			baselineCalRunner = null;
		}
	}

	@Override
	public void onFinished(ProgressObserver sd) {
		super.onFinished(sd);
		sd.shutdown();
	}

	@Override
	protected void getReadyForCalibration(ProgressObserver sd, ArgCreator ac,
			PropertyChangeSupport pcs, Runnable finishedJob) {
		baselineCalRunner = new CalibrationRunner(ac, sd, finishedJob, pcs) {
			@Override
			protected int computeMaximum(int chlnum, int vbnum) {
				return vbnum * chlnum * 2;
			}

			protected IWFCalRoutine createWFCalRoutine(int vbnum, WaveForm wf,
					ArgCreator ac) {
				return new BaselineCal(wf, vbnum, ac);
			}
		};
		baselineCalRunner.getReady();
	}
}