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
				/** 仅在这里设置状态，不在这里执行任务 */
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
				/** 目前只有chl1可为视频 */
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
					/** 针对单触且通道1才支持视频的前提下 */
					int chl = 0;
					TriggerControl tc = cc.getTriggerControl();
					tc.c_setChannelMode(singleTrg);
					tc.setSingleChannel(chl);
					// tc.resetSingleTrgset();

					TriggerSet ts = tc.getSingleTriggerSet();
					ts.setTrigger(ts.video);

					tc.selfSubmit();

					/** 根据一个通道为视频来设置时基 */
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