package com.owon.uppersoft.dso.source.comm.effect;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Principle;
import com.owon.uppersoft.vds.core.comm.JobQueueDispatcher;
import com.owon.uppersoft.vds.core.comm.effect.Submitable;

public class SubmitorFactory {
	private static Submitable sb;

	public static void setSourceManager(JobQueueDispatcher df, ControlManager cm) {
		Principle pp = cm.getPrinciple();
		sb = pp.createSubmitor(df, cm);
	}

	public static Submitable getSubmitable() {
		return sb;
	}

	public static Submitable reInit() {
		sb.prepare();
		return sb;
	}

}
