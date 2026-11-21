package com.owon.uppersoft.vds.device.interpret;

import com.owon.vds.calibration.stuff.CalArgTypeProvider;
import com.owon.vds.tiny.firm.pref.model.ArgType;

public abstract class AbsTranslator implements LowerTranslator {
	private static final int LevelMax = 128;

	protected CalArgTypeProvider catp;

	public AbsTranslator(CalArgTypeProvider catp) {
		this.catp = catp;
	}

	@Override
	public int translate2VBValue(int chl, int vb) {
		return catp.getSimpleAdjustCMDType(ArgType.Gain).getArgs()[chl][vb];
	}

	@Override
	public int getLevelArg(int v, int rnf) {
		int up, low;

		if (rnf == 0) {
			up = v;
			low = up - 10;
		} else {
			low = v;
			up = low + 10;
		}

		if (up > LevelMax) {
			up = LevelMax;
			low = up - 10;
		}

		if (low < -LevelMax) {
			low = -LevelMax;
			up = low + 10;
		}

		/** lower limit = upper limit - 10; level is used as the upper limit on a rising edge and as the lower limit on a falling edge */
		return (up & 0xff) | ((low & 0xff) << 8);
	}
}