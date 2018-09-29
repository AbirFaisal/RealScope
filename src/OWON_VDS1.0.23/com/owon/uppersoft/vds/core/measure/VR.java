package com.owon.uppersoft.vds.core.measure;

import com.owon.uppersoft.vds.util.format.UnitConversionUtil;

/**
 * VR，ValueResult
 * 
 */
public class VR {
	public boolean on = false;
	public double v;
	public Object mt;
	public String vu = " ";// Value with Unit

	public VR(Object mt) {
		this.mt = mt;
	}

	@Override
	public String toString() {
		return mt.toString();
	}

	public static VR[] createVRs() {
		MeasureT[] vs = MeasureT.VALUES;
		int len = vs.length;
		VR[] vrs = new VR[len];
		for (int i = 0; i < len; i++) {
			vrs[i] = new VR(vs[i]);
		}
		return vrs;
	}

	public static void setHorizontalValue(VR[] vrs, MeasureT mt, double value,
			boolean invalid, boolean isvideo, double freLimit) {
		int idx = mt.idx;
		vrs[idx].v = value;

		if (isvideo) {
			switch (mt) {
			case PDUTy:
			case NDUTy:
			case FREQuency:
			case PERiod:
			case RTime:
			case FTime:
			case PWIDth:
			case NWIDth:
				vrs[idx].vu = "?";
				return;
			}
		}

		switch (mt) {
		case FREQuency:
			if (freLimit > 0 && value > freLimit) {
				vrs[idx].vu = "?";
				break;
			}
			if (value <= 0 || invalid)
				vrs[idx].vu = "?";
			else
				vrs[idx].vu = UnitConversionUtil
						.getSimplifiedFrequencyLabel_Hz(value);
			break;
		case PERiod:
			double periodLimit = 1 / (double) freLimit;
			// System.out.println("periodLimit:" + (periodLimit > 0)
			// + ",value < periodLimit? " + (value < periodLimit)
			// + ",value <= 0?" + (value <= 0));
			if (periodLimit > 0 && value < periodLimit) {
				vrs[idx].vu = "?";
				break;
			}
			// case RiseTime:
			// case FallTime:
		case PWIDth:
		case NWIDth:
			if (value <= 0 || invalid) {
				vrs[idx].vu = "?";
			} else
				vrs[idx].vu = UnitConversionUtil
						.getSimplifiedTimebaseLabel_mS(value * 1000);
			break;
		case PDUTy:
		case NDUTy:
			if (value < 0 || invalid)
				vrs[idx].vu = "?";
			else
				vrs[idx].vu = UnitConversionUtil.getPercent(value);
			break;
		default:
			errprintln("unknow MeasureT: " + mt);
			break;
		}

	}

	public static void setVerticalValue(VR[] vrs, MeasureT mt, double value,
			boolean beyond, boolean invalid, boolean isvideo) {
		int idx = mt.idx;
		vrs[idx].v = value;
		if (isvideo) {
			switch (mt) {
			case OVERshoot:
			case PREShoot:
				vrs[idx].vu = "?";
				return;
			}
		}
		switch (mt) {
		case OVERshoot:
		case PREShoot:
			if (value < 0)
				vrs[idx].vu = "?";
			else
				vrs[idx].vu = UnitConversionUtil.getPercent(value);
			break;
		case PKPK:
		case MAX:
		case MIN:
		case AVERage:
		case CYCRms:
		case VAMP:
		case VTOP:
		case VBASe:
			if (invalid)
				vrs[idx].vu = "?";
			else {
				vrs[idx].vu = UnitConversionUtil
						.getSimplifiedVoltLabel_mV(value);
				if (beyond)
					vrs[idx].vu = vrs[idx].vu + '?';
			}
			break;
		default:
			errprintln("unknow MeasureT: " + mt);
			break;
		}
	}

	public static void setRise_FallTimeValue(VR[] vrs, MeasureT mt,
			double value, boolean beyond, int flag, boolean isvideo) {
		int idx = mt.idx;
		vrs[idx].v = value;

		if (isvideo) {
			/** 如果视频触发,不论上升下降时间都不显示 */
			vrs[idx].vu = "?";
			return;
		}

		switch (mt) {
		case RTime:
		case FTime:
			if (value <= 0 || beyond)
				vrs[idx].vu = "?";
			else {
				vrs[idx].vu = UnitConversionUtil
						.getSimplifiedTimebaseLabel_mS(value * 1000);
				if (flag == 1)
					vrs[idx].vu = '<' + vrs[idx].vu;
			}

			break;
		default:
			errprintln("unknow MeasureT: " + mt);
			break;
		}
	}

	private static void errprintln(String txt) {
		System.err.println(txt);
	}
}
