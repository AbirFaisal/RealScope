package com.owon.vds.tiny.firm.pref;

import com.owon.uppersoft.vds.core.comm.BufferredSourceManager;
import com.owon.uppersoft.vds.core.comm.effect.JobUnitDealer;
import com.owon.uppersoft.vds.core.comm.job.JobUnit;
import com.owon.vds.tiny.firm.DeviceFlashCommunicator;

public class PrefSync {
	public void syncFlash(PrefControl pc, JobUnitDealer sb) {
		final byte[] devPref = pc.outputAsSyncImage();
		sendPrefernce_Write(devPref, sb);
	}

	public void sendPrefernce_Write(final byte[] devPref, JobUnitDealer sb) {
		sb.addJobUnit(new JobUnit() {
			@Override
			public void doJob(BufferredSourceManager sm) {
				DeviceFlashCommunicator dflash = new DeviceFlashCommunicator();
				dflash.sendPrefernce(sm, devPref);
			}

			@Override
			public String getName() {
				return "send flash";
			}

			@Override
			public boolean merge(JobUnit ju) {
				return false;
			}

		});
	}
}
