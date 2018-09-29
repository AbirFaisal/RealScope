package com.owon.uppersoft.vds.auto.jobs;

import java.util.Iterator;
import java.util.List;

import com.owon.uppersoft.dso.global.CoreControl;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.auto.WFAutoArrange;
import com.owon.uppersoft.vds.auto.WFAutoRoutine;
import com.owon.uppersoft.vds.core.aspect.base.Logable;
import com.owon.uppersoft.vds.core.aspect.control.VoltageProvider;

public class ArrangeChannelsJob implements Runnable, Logable {
	private final int tbi;
	private List<WFAutoRoutine> next_wfrlist;
	private JobRunner jr;
	private CoreControl cc;

	public ArrangeChannelsJob(int tbi, List<WFAutoRoutine> next_wfrlist,
			JobRunner jr, CoreControl cc) {
		this.tbi = tbi;
		this.jr = jr;
		this.cc = cc;
		this.next_wfrlist = next_wfrlist;
	}

	@Override
	public void run() {
		arrangeChannels(tbi);
	}

	/** 支持单通道到4通道 */
	private int[][] pos0Array = { { 0 }, { 50, -50 }, { 50, 0, -50 },
			{ 75, 25, -25, -75 } };
	private double[] ampsArray = { 0.8, 0.4, 0.4, 0.25 };

	private void arrangeChannels(int tbi) {
		/** 将wf升序排列，但是后面的迭代可能不能保证 */
		Iterator<WFAutoRoutine> wfri = next_wfrlist.iterator();

		/** 开启的通道数 */
		int chlnum = next_wfrlist.size();

		if (chlnum < 0 || chlnum > 4) {
			System.err.println("arrangeChannels  chlnum < 0 || chlnum > 4");
			jr.tail();
			return;
		}
		VoltageProvider vp = cc.getVoltageProvider();
		int vbnum = vp.getVoltageNumber();
		int chlidx = chlnum - 1;
		int chlamp = (int) (ampsArray[chlidx] * WFAutoRoutine.ADC_RANGE);
		int index = 0;
		while (wfri.hasNext()) {
			WFAutoRoutine wfr = wfri.next();
			WaveForm wf = wfr.getWaveForm();
			WFAutoArrange wfa = new WFAutoArrange(wf, vbnum, cc);
			wfa.getReady2(pos0Array[chlidx][index++], chlamp, computeVBIndex(
					chlamp, wfr.getAMP(), wf));
		}

		jr.replaceRunnable(new Runnable() {
			int i = 0;

			@Override
			public void run() {
				if (i < 1) {
					i++;
					return;
				}
				Iterator<WFAutoRoutine> wfri = next_wfrlist.iterator();
				while (wfri.hasNext()) {
					WFAutoRoutine wfr = wfri.next();
					WaveForm wf = wfr.getWaveForm();
					wf.setTrg50Percent(cc.getTriggerControl());
				}
				jr.tail();
			}
		});
	}

	private int computeVBIndex(int chlamp, double amp, WaveForm wf) {
		VoltageProvider vp = cc.getVoltageProvider();
		int vbnum = vp.getVoltageNumber();

		int chlampmin = (int) (chlamp * 0.8);

		int vbnow = wf.getVoltbaseIndex();
		logln("wf: " + wf.getChannelNumber() + ", amp: " + amp + ", vb:"
				+ vp.getVoltageLabel(vbnow));
		int i = vbnow;

		while (amp <= chlampmin && i - 1 >= 0) {
			double r = vp.getVoltage(0, i - 1) / (double) vp.getVoltage(0, i);
			amp /= r;
			i--;
			logln(amp + ", " + i);
		}
		logln("again");
		while (amp > chlamp && i + 1 < vbnum) {
			double r = vp.getVoltage(0, i + 1) / (double) vp.getVoltage(0, i);
			amp /= r;
			i++;
			logln(amp + ", " + i);
		}

		return i;
	}

	public void log(Object o) {
		System.out.print(o);
	}

	public void logln(Object o) {
		System.out.println(o);
	}
}