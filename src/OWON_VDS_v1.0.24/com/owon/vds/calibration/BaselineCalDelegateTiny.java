package com.owon.vds.calibration;

import java.awt.Window;

import com.owon.uppersoft.dso.global.ControlApps;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.source.comm.AutoCalDelegate;
import com.owon.uppersoft.vds.machine.PrincipleTiny;
import com.owon.uppersoft.vds.machine.TinyMachine;
import com.owon.uppersoft.vds.source.comm.InterCommTiny;
import com.owon.uppersoft.vds.ui.dialog.ProgressObserver;
import com.owon.vds.calibration.stuff.ArgCreator;
import com.owon.vds.tiny.firm.pref.PrefControl;

public class BaselineCalDelegateTiny extends AutoCalDelegate {

	private AutomaticWorkManager acm;
	private TinyMachine tm;
	private PrincipleTiny pt;

	public BaselineCalDelegateTiny(Window wnd, ControlApps ca, TinyMachine tm,
			PrincipleTiny pt) {
		super(wnd, ca);
		this.tm = tm;
		this.pt = pt;
	}

	public Runnable nextJob;

	/** 自校正能够重新校准部分参数，但仍有如增益在出厂后不方便重新校对，故而该部分不去改动 */
	@Override
	public void autoCal(ProgressObserver po) {
		PrefControl pc = pt.getPrefControl();
		ArgCreator ac = tm.getArgCreator(pt.getCalArgTypeProvider());
		acm = new BaselineCalManager(cm, (InterCommTiny) ca.interComm,
				Platform.getDataHouse()) {
			@Override
			public void onFinished(ProgressObserver sd) {
				super.onFinished(sd);

				if (nextJob != null)
					nextJob.run();
			}
		};
		acm.getReady(po, ac, pc);
	}

	@Override
	public void cancel(Runnable afterCancel) {
		if (acm != null) {
			acm.cancel(afterCancel);
			acm = null;
		}
	}
}