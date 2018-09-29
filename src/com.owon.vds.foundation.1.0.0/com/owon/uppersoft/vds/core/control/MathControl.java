package com.owon.uppersoft.vds.core.control;

import com.owon.uppersoft.vds.util.Pref;
import com.owon.uppersoft.vds.util.format.ParseUtil;

public class MathControl {

	public static final String MATH_MATHON = "Math.mathon";
	public static final String MATH_OPERATION = "Math.operation";
	public static final String MATH_M2 = "Math.m2";
	public static final String MATH_M1 = "Math.m1";

	public static final String[] operations = { "+", "-", "*", "/" };

	public boolean mathon;
	public int m1, m2, operation;

	public boolean isMathChannelUse(int idx) {
		return mathon && (idx == m1 || idx == m2);
	}

	public boolean isInclude(int chl) {
		return chl == m1 || chl == m2;
	}

	public int probeIndex = 0;

	/**
	 * KNOW 在重新打开或是重启打开后，自动判断运算波形的电压档位为两个波形中较大档位的小一档，此后随用户调节
	 */
	private int mathvbidx = -1;

	int judgeVBidx(int vb1, int vb2) {
		int vb;
		if (vb1 == vb2) {
			vb = vb1;
		} else {
			vb = Math.max(vb1, vb2) - 1;
			if (vb < 0)
				vb++;
		}
		return vb;
	}

	public void updateMathVbIdx(int vb1, int vb2) {
		if (getMathvbidx() < 0 || getMathvbidx() >= dbMathVoltage.length)
			setMathvbidx(judgeVBidx(vb1, vb2));
	}

	public void refreshMathVoltBase(int vb1, int vb2) {
		// 8;//当要自动调整CHM档位前统一切换到此档位
		setMathvbidx(judgeVBidx(vb1, vb2));
		// System.out.println(mathvbidx);
	}

	public MathControl(Pref p) {
		initVOLTAGEMath();
		load(p);
	}

	public void load(Pref p) {
		m1 = p.loadInt(MATH_M1);
		m2 = p.loadInt(MATH_M2);
		operation = p.loadInt(MATH_OPERATION);

		mathon = p.loadBoolean(MATH_MATHON);
	}

	public void persist(Pref p) {
		p.persistInt(MATH_M1, m1);
		p.persistInt(MATH_M2, m2);
		p.persistInt(MATH_OPERATION, operation);

		p.persistBoolean(MATH_MATHON, mathon);
	}

	private String[][] MathVolts;

	public String[] MathVolt = { "1n", "2n", "5n", "10n", "20n", "50n", "100n",
			"200n", "500n",// nV
			"1u", "2u", "5u", "10u", "20u", "50u", "100u", "200u", "500u",// uV
			"1m", "2m", "5m", "10m", "20m", "50m", "100m", "200m", "500m",// mV
			"1", "2", "5", "10", "20", "50", "100", "200", "500",// V
			"1K", "2K", "5K", "10K", "20K", "50K", "100K", "200K", "500K",// KV
			"1M", "2M", "5M", "10M", "20M", "50M", "100M", "200M", "500M",// MV
	};

	public String[] getMathVBs() {
		return MathVolts[operation];
	}

	public double[] dbMathVoltage;

	private void initVOLTAGEMath() {
		int vlen = MathVolt.length;
		int olen = MathControl.operations.length;
		dbMathVoltage = new double[vlen];
		for (int i = 0; i < vlen; i++) {
			dbMathVoltage[i] = getDouble_mV(MathVolt[i]);
			// System.out.println(MathVolt[i]+"v:("+dbMathVoltage[i] + ")mv,");
		}
		MathVolts = new String[olen][vlen];
		for (int i = 0; i < olen; i++) {
			for (int j = 0; j < vlen; j++) {
				if (i == 2)
					MathVolts[i][j] = MathVolt[j] + "VV";
				else if (i == 3)
					MathVolts[i][j] = MathVolt[j] + "V/V";
				else
					MathVolts[i][j] = MathVolt[j] + "V";
			}
		}
	}

	private double getDouble_mV(String sv) {
		sv = sv.trim();
		double v;
		String val = sv.substring(0, sv.length() - 1);
		if (sv.endsWith("n")) {
			v = ParseUtil.translateInt(val) / (double) 1000000;// nV
		} else if (sv.endsWith("u")) {
			v = ParseUtil.translateInt(val) / (double) 1000;// uV
		} else if (sv.endsWith("m")) {
			v = ParseUtil.translateInt(val);// mV
		} else if (sv.endsWith("K")) {
			v = ParseUtil.translateInt(val) * (double) 1000000;// KV
		} else if (sv.endsWith("M")) {
			v = ParseUtil.translateInt(val) * (double) 1000000000;// MV
		} else {
			v = ParseUtil.translateInt(sv) * (double) 1000;// V
		}
		return v;
	}

	public int getMathvbidx() {
		return mathvbidx;
	}

	public void setMathvbidx(int mathvbidx) {
		this.mathvbidx = mathvbidx + 24;//电压档位最小值是50mv，数学计算的50mv档位的位置需要加24
	}

}