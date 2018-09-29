package com.owon.uppersoft.vds.core.measure;

import com.owon.uppersoft.vds.util.format.UnitConversionUtil;

public class MeasureElem {
	public boolean on = false;
	public int idx;
	public String label,name;
	public double Value;
	public String vu;// Value with Unit

	public MeasureElem(int idx) {
		this.idx = idx;
	}

	public void setDelayValue(double v) {
		Value = v;
		if (v < 0) {
			vu = "?";
		} else {
			vu = UnitConversionUtil.getSimplifiedTimebaseLabel_mS(Value * 1000);
		}
	}
}
