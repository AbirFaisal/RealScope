package com.owon.uppersoft.vds.auto.jobs;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.owon.uppersoft.dso.global.CoreControl;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.vds.auto.WFAutoRoutine;

public class RecognizeJob implements Runnable {
	public RecognizeJob() {
		// recognizeChannelsChecks = 0;
	}

	// private int recognizeChannelsChecks = 0;

	@Override
	public void run() {
		recognizeChannels();
	}

	private List<WFAutoRoutine> wfrlist;
	private List<WFAutoRoutine> next_wfrlist;

	private void recognizeChannels() {
		if (wfrlist.size() > 0) {

			// if (recognizeChannelsChecks < 0) {
			// recognizeChannelsChecks++;
			// return;
			// }
			// recognizeChannelsChecks = 0;

			Iterator<WFAutoRoutine> wfri = wfrlist.iterator();

			while (wfri.hasNext()) {
				/** Channel where calibration has finished */
				WFAutoRoutine wfr = wfri.next();

				if (wfr.routOut()) {
					wfri.remove();

					if (wfr.getAutoStatus() == WFAutoRoutine.AutoStatus.NoInput) {
						/** Keep the original channel-off mode, otherwise channels with no signal cannot be turned off */
						wfr.getWaveForm().wfi.ci.c_setOn(false);
					} else {
						next_wfrlist.add(wfr);
					}
				}
			}

			/** Prevent all channels being off, no more data being fetched, and the autoset task flow being unable to continue */
			if (wfrlist.size() == 0 && next_wfrlist.size() == 0) {
				cc.getWaveFormInfoControl().preSetChannelsForFFT(0);

				WaveFormManager wfm = Platform.getDataHouse()
						.getWaveFormManager();
				wfm.getWaveForm(0).setTrg50Percent(cc.getTriggerControl());
				jr.tail();
			}
		} else {
			jr.replaceRunnable(new ArrangeTimeJob(next_wfrlist, jr));
		}
	}

	private JobRunner jr;
	private CoreControl cc;

	public RecognizeJob(List<WFAutoRoutine> wfrlist, JobRunner jr,
			CoreControl cc) {
		this.wfrlist = wfrlist;
		this.jr = jr;
		this.cc = cc;
		next_wfrlist = new LinkedList<WFAutoRoutine>();
	}
}
