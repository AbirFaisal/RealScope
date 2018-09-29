package com.owon.vds.tiny.ui.tune.control;

import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.source.comm.effect.SubmitorFactory;
import com.owon.uppersoft.vds.core.comm.effect.JobUnitDealer;
import com.owon.uppersoft.vds.source.comm.InterCommTiny;
import com.owon.uppersoft.vds.source.comm.Submitor2;
import com.owon.vds.tiny.firm.pref.PrefControl;
import com.owon.vds.tiny.firm.pref.PrefSync;
import com.owon.vds.tiny.firm.pref.model.ArgType;
import com.owon.vds.tiny.tune.TinyTuneDelegate;

public class DefaultTinyTuneDelegate implements TinyTuneDelegate {
	private PrefControl dpc;

	public DefaultTinyTuneDelegate(PrefControl dpc) {
		this.dpc = dpc;
	}

	public JobUnitDealer getJobUnitDealer() {
		return (Submitor2) SubmitorFactory.reInit();
	}

	@Override
	public void vbChangeForChannel(int chl, int vbidx) {
		Platform.getMainWindow().getToolPane().getInfoPane()
				.vbChangeForChannel(chl, vbidx);
	}

	@Override
	public void selfCalibration() {
		getInterCommTiny().selfCalibration();
	}

	private InterCommTiny getInterCommTiny() {
		return (InterCommTiny) Platform.getControlApps().interComm;
	}

	@Override
	public void writeDeviceNSync() {
		PrefControl pc = createPrefControl();
		JobUnitDealer sbm = getJobUnitDealer();
		pc.save2DevicePart();
		syncFlash(pc, sbm);
	}

	private PrefControl createPrefControl() {
		return dpc;
	}

	private void syncFlash(PrefControl pc, JobUnitDealer sbm) {
		new PrefSync().syncFlash(pc, sbm);
	}

	@Override
	public void writeFactoryNSync() {
		PrefControl pc = createPrefControl();
		JobUnitDealer sbm = getJobUnitDealer();
		pc.save2FactoryPart();
		syncFlash(pc, sbm);
	}

	@Override
	public void writeRegistryNSync() {
		PrefControl pc = createPrefControl();
		JobUnitDealer sbm = getJobUnitDealer();
		pc.saveRegistry();
		syncFlash(pc, sbm);
	}

	@Override
	public void resumeFactoryNSync() {
		getInterCommTiny().doSyncFactorySet2Machine();
	}

	@Override
	public void argChange(int type, int chl, int vb) {
		Submitor2 sb = (Submitor2) SubmitorFactory.reInit();
		// dblogln(type + ", " + chl + ", " + vb);
		ArgType at = ArgType.VALUES[type];

		int v = 0;
		switch (at) {
		case Step:
			v = sb.wrPos0(chl);
			break;
		case Gain:
			sb.wrVoltbase(chl, vb);
			break;
		case Compensation:
			v = sb.wrPos0(chl);
			break;
		default:
			break;
		}
	}

}