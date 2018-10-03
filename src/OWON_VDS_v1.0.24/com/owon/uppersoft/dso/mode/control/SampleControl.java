package com.owon.uppersoft.dso.mode.control;

import com.owon.uppersoft.dso.data.LObject;
import com.owon.uppersoft.dso.source.comm.effect.SubmitorFactory;
import com.owon.uppersoft.vds.core.aspect.base.IOrgan;
import com.owon.uppersoft.vds.core.comm.effect.IPatchable;
import com.owon.uppersoft.vds.core.comm.effect.Submitable;
import com.owon.uppersoft.vds.core.data.MinMax;
import com.owon.uppersoft.vds.util.Pref;

public class SampleControl implements IPatchable, IOrgan, ISampleControl {

	public static final LObject[] SamplingMode = {
			new LObject("M.Sample.Sampling"), new LObject("M.Sample.PKdetect"),
			new LObject("M.Sample.Average") };
	public final String[] SAMPleMode = { "SAMPle", "PEAK", "AVERage" };

	// 平均值采集，开关，限值，存值
	public static int MinAverageSampleTimes = 1, MaxAverageSampleTimes = 128;

	@Override
	public MinMax getAvgTimesRange() {
		return new MinMax(MinAverageSampleTimes, MaxAverageSampleTimes);
	}

	public boolean avgon = false;
	private int avgTimes = 1;

	private int modelIdx = 0;

	public static int Sample_Sampling = 0;
	public static int Sample_PKDetect = 1;
	public static int Sample_Average = 2;

	public SampleControl() {
	}

	public void load(Pref p) {
		modelIdx = p.loadInt("Sample.mode");
		avgon = (modelIdx == 2);
		c_setAvgTimes(p.loadInt("Sample.avgTimes"));
	}

	public void persist(Pref p) {
		p.persistInt("Sample.avgTimes", avgTimes);
		p.persistInt("Sample.mode", modelIdx);
	}

	public boolean isPeakDetect() {
		return modelIdx == 1;
	}

	public int getModelIdx() {
		return modelIdx;
	}

	public void setModelIdx(int v) {
		this.modelIdx = v;
		avgon = (v == 2);
	}

	public void c_setModelIdx(int v) {
		this.modelIdx = v;
		avgon = (v == 2);

		Submitable sbm = SubmitorFactory.reInit();
		sbm.c_sample(modelIdx);
		sbm.apply();
	}

	public int getAvgTimes() {
		return avgTimes;
	}

	public void c_setAvgTimes(int v) {
		this.avgTimes = v;
		// PackUtil.submit(this);

		if (avgTimes > MaxAverageSampleTimes)
			avgTimes = MaxAverageSampleTimes;
		else if (avgTimes < MinAverageSampleTimes)
			avgTimes = MinAverageSampleTimes;
	}

	@Override
	public void selfSubmit(Submitable sbm) {
		sbm.c_sample(modelIdx);
	}

	public int getSAMPleModeIdx(String args) {
		for (int i = 0; i < SAMPleMode.length; i++) {
			if (args.equalsIgnoreCase(SAMPleMode[i]))
				return i;
		}
		return -1;
	}
}
