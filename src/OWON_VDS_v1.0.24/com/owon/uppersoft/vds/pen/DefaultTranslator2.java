package com.owon.uppersoft.vds.pen;

import com.owon.uppersoft.vds.device.interpret.AbsTranslator;
import com.owon.vds.calibration.stuff.CalArgTypeProvider;
import com.owon.vds.tiny.firm.pref.model.ArgType;

public class DefaultTranslator2 extends AbsTranslator {

	private int complementCode;

	public DefaultTranslator2(CalArgTypeProvider catp, int complementCode) {
		super(catp);
		this.complementCode = complementCode;
	}

	@Override
	public int translate2PosValue(int chl, int pos0, int vb) {
		int[][] stepping = catp.getSimpleAdjustCMDType(ArgType.Step).getArgs();
		int[][] compensation = catp
				.getSimpleAdjustCMDType(ArgType.Compensation).getArgs();

		int comp = compensation[chl][vb];
		int step = stepping[chl][vb];

		// System.out.println("comp: " + comp);
		// System.out.println("step: " + step);
		// System.out.println("pos0: " + pos0);
		/**
		 * the final sent parameter value is inversely related to the acquisition point; the sent value is in (0~Unsign_Max_ADC), so the outermost part of the formula takes the complement of Unsign_Max_ADC,
		 * each term keeps a proportional relationship
		 * 
		 * the step value is originally a floating-point decimal; here it is an integer and must be divided by 100 when used;
		 * 
		 * when adjusting the zero point compensation, the step is multiplied by 0, so the unit compensation value corresponds one to one with the sent value;
		 * 
		 * when adjusting the zero point step, the zero point position is 100 and the compensation is fixed, so the unit step value corresponds one to one with the sent value
		 * 
		 * VDS1021:
		 * 
		 * -(offset(des) + step(as))
		 * 
		 */
		int v = complementCode - (comp + pos0 * step / 100);

		// System.out.println("v: (append a '-')" + v);
		return v;
	}

}