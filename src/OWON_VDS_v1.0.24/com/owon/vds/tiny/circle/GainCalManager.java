package com.owon.vds.tiny.circle;

import java.beans.PropertyChangeSupport;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.source.comm.InterCommTiny;
import com.owon.uppersoft.vds.ui.dialog.ProgressObserver;
import com.owon.vds.calibration.AutomaticWorkManager;
import com.owon.vds.calibration.stuff.ArgCreator;

/**
 * tiny-specific AutoCalManager
 * 
 */
public class GainCalManager extends AutomaticWorkManager {
	private AGPControl agp;

	public GainCalManager(ControlManager cm, InterCommTiny ict, DataHouse dh,
			AGPControl agp) {
		super(cm, ict, dh);
		this.agp = agp;
	}

	private CalRunner2 gainCalRunner;

	protected void presetChannels() {
		cm.getWaveFormInfoControl().turnOnAllChannelsNotInverse(ChannelInfo.CouplingDCIndex);
	}

	/**
	 * <code>
	 * 1. Self-calibration + closed loop + self-calibration, note to toggle agp
	 * 2. Automatically save the txt and write it into the factory settings
	 * 
	 * </code>
	 * 
	 * */
	@Override
	protected void getReadyForCalibration(final ProgressObserver sd,
			ArgCreator ac, PropertyChangeSupport pcs, Runnable finishedJob) {
		// Gain calibration
		gainCalRunner = new CalRunner2(ac, sd, finishedJob, pcs, agp) {
			@Override
			protected int computeMaximum(int chlnum, int vbnum) {
				return vbnum * chlnum;
			}

			protected IWFCalRoutine2 createWFCalRoutine(int vbnum, WaveForm wf,
					ArgCreator ac) {
				return new GainCal(wf, vbnum, ac, agp);// 1
			}
		};

		gainCalRunner.getReady();
	}

	public void cancel(Runnable afterCancel) {
		if (gainCalRunner != null) {
			// Closed-loop calibration can be canceled
			gainCalRunner.cancel(afterCancel);
			gainCalRunner = null;
		}
	}

	public void onFinished(ProgressObserver sd) {
		// Before the wrap-up work, add the other tasks that follow self-calibration
		super.onFinished(sd);

		sd.shutdown();
	}

}