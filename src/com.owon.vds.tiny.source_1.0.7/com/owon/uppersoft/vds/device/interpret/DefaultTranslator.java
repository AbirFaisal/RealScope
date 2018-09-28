package com.owon.uppersoft.vds.device.interpret;

import com.owon.vds.calibration.stuff.CalArgTypeProvider;
import com.owon.vds.tiny.firm.pref.model.ArgType;

public class DefaultTranslator extends AbsTranslator {

	public DefaultTranslator(CalArgTypeProvider catp) {
		super(catp);
	}

	@Override
	public int translate2PosValue(int chl, int pos0, int vb) {
		int[][] stepping = catp.getSimpleAdjustCMDType(ArgType.Step).getArgs();
		int[][] compensation = catp
				.getSimpleAdjustCMDType(ArgType.Compensation).getArgs();

		/**
		 * VDS1022:
		 * 
		 * offset(des) - step(as) = (600~800)
		 * 
		 */
		int v = (compensation[chl][vb] - pos0 * stepping[chl][vb] / 100);
		return v;
	}

}