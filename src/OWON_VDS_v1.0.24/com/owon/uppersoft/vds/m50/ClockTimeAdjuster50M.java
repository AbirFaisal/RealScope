package com.owon.uppersoft.vds.m50;

import java.math.BigDecimal;

import com.owon.uppersoft.vds.core.aspect.base.EchoLogger;
import com.owon.uppersoft.vds.machine.LowControlManger;
import com.owon.uppersoft.vds.source.comm.Submitor2;
import com.owon.uppersoft.vds.source.comm.ext.Arg_En;
import com.owon.uppersoft.vds.source.comm.ext.ClockTimeAdjuster;

public class ClockTimeAdjuster50M extends ClockTimeAdjuster {

	public ClockTimeAdjuster50M(LowControlManger lcm, Submitor2 sbm) {
		super(lcm, sbm);
	}

	private EchoLogger el = new EchoLogger();

	@Override
	protected Arg_En getArg_En(int arg, int en) {
		el.logln(arg + " : " + en);
		Arg_En ae = new Arg_En(arg, en);

		/**
		 * first restore the original value, then divide by 8ns, and convert the resulting count to scientific notation
		 * 
		 * this way the upper layer can still keep the original accumulation method and step scales, while the device receives the number of 8ns units, ignoring the remainder deviation
		 * 
		 * the minimum counting unit of the value passed in here is determined by the corresponding enum class; per the table, adjustment is needed at the 10ns step scale
		 * */
		if (en == 0)
			en = 1;

		BigDecimal v = BigDecimal.valueOf(arg)
				.multiply(BigDecimal.valueOf(10).pow(en))
				.divide(BigDecimal.valueOf(8));

		double lv = v.doubleValue();

		int e = 0;
		while (lv > 1024) {
			lv = lv / 10;
			e += 1;
		}

		el.logln(lv);
		ae.arg = Math.round((float) lv);
		ae.en = e;
		el.logln(ae);
		return ae;
	}

}
