package com.owon.uppersoft.vds.auto.jobs;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import com.owon.uppersoft.dso.global.CoreControl;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.auto.WFAutoRoutine;

public class ArrangeTimeBaseJob implements Runnable {
	private int num;// , arrangeTimeChecks;
	private List<WFAutoRoutine> next_wfrlist;
	private JobRunner jr;
	private CoreControl cc;

	public ArrangeTimeBaseJob(List<WFAutoRoutine> next_wfrlist, JobRunner jr,
			CoreControl cc) {
		num = next_wfrlist.size();
		this.next_wfrlist = next_wfrlist;
		this.jr = jr;
		this.cc = cc;
		// arrangeTimeChecks = 0;
	}

	@Override
	public void run() {

		/** 从设置触发电平到硬件频率计计算出正确的数值，需要的时间接近信号的周期 */
		// if (arrangeTimeChecks < 0) {
		// arrangeTimeChecks++;
		// return;
		// }
		/** 从设置触发电平到硬件频率计计算出正确的数值，需要的时间接近信号的周期 */
		// TriggerControl tc = cc.getTriggerControl();
		switch (num) {
		case 1: {
			WaveForm wf = next_wfrlist.get(0).getWaveForm();
			int chl = wf.getChannelNumber();

			double msperiod = wf.wfi.ci.getPeriod();
			logln(wf.toString() + " peroid " + msperiod);

			int tbi = timebase(msperiod, chl);
			boolean success = tbi >= 0;
			if (success) {
				jr.doArrangeChannels(next_wfrlist, tbi);
			} else {
				jr.tail();
			}
		}
			break;
		default: {
			double max_msperiod = -1;
			int chl = -1;

			// 可作多次获取看看是否硬件频率计计算异常
			// int i = 0;
			// while (i < 5) {
			Iterator<WFAutoRoutine> wfri = next_wfrlist.iterator();
			while (wfri.hasNext()) {
				WFAutoRoutine wfar = wfri.next();
				WaveForm wf = wfar.getWaveForm();
				int ch = wf.getChannelNumber();

				double msp = wf.wfi.ci.getPeriod();
				logln(wf.toString() + " peroid " + msp);
				if (!Double.isInfinite(msp) && msp > 0 && msp > max_msperiod) {
					chl = ch;
					max_msperiod = msp;
				}
			}
			// sleepThread(250);
			// i++;
			// }
			if (max_msperiod <= 0)
				break;

			int tbi = timebase(max_msperiod, chl);
			boolean success = tbi >= 0;
			if (success) {
				jr.doArrangeChannels(next_wfrlist, tbi);
			} else {
				jr.tail();
			}
		}
			break;
		}
	}

	private void logln(Object o) {
		System.out.println(o);
	}

	private int timebase(double msperiod, int chl) {
		double sp = msperiod / 1000000 * 3 / 20;
		logln("defaultSystemPrefrences: " + sp);
		BigDecimal[] bdTIMEBASE = cc.getMachineInfo().bdTIMEBASE;
		int tbnum = bdTIMEBASE.length;
		int lasttb = tbnum - 1;
		int init_tb = tbnum >> 1;

		int tbi = init_tb;
		logln("init_tb: " + init_tb);
		double tbbd = bdTIMEBASE[tbi].doubleValue();
		while (tbbd > sp && tbi - 1 >= 0) {
			tbi--;
			logln("tbi: " + tbi);
			tbbd = bdTIMEBASE[tbi].doubleValue();
		}
		logln("again: ");
		while (tbbd < sp && tbi + 1 <= lasttb) {
			tbi++;
			logln("tbi: " + tbi);
			tbbd = bdTIMEBASE[tbi].doubleValue();
		}
		cc.getTimeControl().c_setTimebaseIdx(tbi, true);

		return tbi;
	}
}