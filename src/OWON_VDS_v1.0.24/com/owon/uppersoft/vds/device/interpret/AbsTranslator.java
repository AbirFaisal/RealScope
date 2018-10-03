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

		/** 下限=上限-10, level在上升沿时用作上限，在下降沿时用作下限 */
		return (up & 0xff) | ((low & 0xff) << 8);
	}
}