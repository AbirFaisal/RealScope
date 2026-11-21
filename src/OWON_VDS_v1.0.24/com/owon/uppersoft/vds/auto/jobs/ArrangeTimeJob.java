package com.owon.uppersoft.vds.auto.jobs;

import java.util.List;

import com.owon.uppersoft.dso.global.CoreControl;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.model.trigger.TriggerDefine;
import com.owon.uppersoft.dso.model.trigger.TriggerSet;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.auto.WFAutoRoutine;
import com.owon.uppersoft.vds.core.aspect.base.Logable;
import com.owon.uppersoft.vds.core.machine.MachineInfo;
import com.owon.uppersoft.vds.device.interpret.util.DefaultCMDResponser;
import com.owon.uppersoft.vds.source.comm.ext.IntObject;

public class ArrangeTimeJob implements Runnable, Logable {

	@Override
	public void run() {
		arrangeTime();
	}

	public void log(Object o) {
		System.out.print(o);
	}

	public void logln(Object o) {
		System.out.println(o);
	}

	private List<WFAutoRoutine> next_wfrlist;
	private JobRunner jr;
	private CoreControl cc;

	public ArrangeTimeJob(List<WFAutoRoutine> next_wfrlist, JobRunner jr) {
		this.jr = jr;
		this.cc = Platform.getCoreControl();
		this.next_wfrlist = next_wfrlist;
	}

	private void arrangeTime() {
		final IntObject io = new IntObject();
		io.value = -1;

		if (!cc.getMachine().isVideoTrgSupport()) {
			toArrange(0);
			return;
		}

		jr.queryVideoTrgd(new DefaultCMDResponser() {
			@Override
			protected void handleResponse(int res) {
				/** Only set the state here, do not execute the task here */
				io.value = res;
			}
		});
		jr.replaceRunnable(new Runnable() {
			private void sleepThread0(int ms) {
				try {
					Thread.sleep(ms);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void run() {
				while (io.value < 0) {
					System.err.println("while");
					sleepThread0(50);
				}
				// System.err.println("video flag: " + v);
				/** Currently only chl1 can be video */
				final int trg_chl_flag = io.value & 1;

				toArrange(trg_chl_flag);
			}
		});
	}

	private void toArrange(final int trg_chl_flag) {
		jr.replaceRunnable(new Runnable() {

			@Override
			public void run() {
				final int singleTrg = TriggerDefine.TrgModeSingleIndex;
				if (trg_chl_flag != 0) {
					/** Given that only single trigger and channel 1 support video */
					int chl = 0;
					TriggerControl tc = cc.getTriggerControl();
					tc.c_setChannelMode(singleTrg);
					tc.setSingleChannel(chl);
					// tc.resetSingleTrgset();

					TriggerSet ts = tc.getSingleTriggerSet();
					ts.setTrigger(ts.video);

					tc.selfSubmit();

					/** Set the timebase according to a channel being video */
					MachineInfo mi = cc.getMachineInfo();

					int tbi = mi.getTimebaseIndex("50us");
					cc.getTimeControl().c_setTimebaseIdx(tbi, true);

					jr.doArrangeChannels(next_wfrlist, tbi);
				} else {

					int num = next_wfrlist.size();

					if (num == 1) {
						WaveForm wf = next_wfrlist.get(0).getWaveForm();
						int chl = wf.getChannelNumber();
						TriggerControl tc = cc.getTriggerControl();
						tc.c_setChannelMode(singleTrg);
						tc.setSingleChannel(chl);
						tc.selfSubmit();
					}

					jr.replaceRunnable(new ArrangeTimeBaseJob(next_wfrlist, jr,
							cc));
				}

			}
		});
	}

}