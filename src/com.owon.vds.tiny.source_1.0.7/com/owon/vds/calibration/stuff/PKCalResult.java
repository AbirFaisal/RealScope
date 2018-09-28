package com.owon.vds.calibration.stuff;

public class PKCalResult {
	public int min, max, pk;

	public PKCalResult(int min, int max) {
		this.min = min;
		this.max = max;
		pk = max - min;
	}

	public void withoutPos0(int pos0) {
		min -= pos0;
		max -= pos0;
	}

	@Override
	public String toString() {
		return "min: " + min + " , max: " + max + " , pk: " + pk;
	}

}
