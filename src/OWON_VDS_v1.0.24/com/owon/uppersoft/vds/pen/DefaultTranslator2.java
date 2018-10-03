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
		 * 最终发的参数值和采集点的对应是反的，发送的值在(0~Unsign_Max_ADC)，所以公式最外对Unsign_Max_ADC取补，
		 * 各个项维持正比关系
		 * 
		 * 步进的数值本是浮点小数，这里用整数要除以100来使用；
		 * 
		 * 在调零点补偿时，步进乘0，所以单位补偿值和发送值一一对应；
		 * 
		 * 在调零点步进时，零点位置为100，补偿固定，所以单位步进值和发送值一一对应
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