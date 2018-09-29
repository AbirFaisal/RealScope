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
		 * 先还原为原始数值，然后除以8ns，得到的数量转换为科学计数法
		 * 
		 * 这样上层仍然可保留原来的累加方式和步进档位，而下位机接收的是8ns的个数，忽略余数偏差
		 * 
		 * 这边传入的数值计数最小单位是根据对应枚举类来定的，查表可知，需要在10ns的步进档作调整
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
