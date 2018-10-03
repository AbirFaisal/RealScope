package com.owon.uppersoft.vds.core.data;

import com.owon.uppersoft.vds.util.format.SFormatter;

public class MinMax {

	private int min;
	private int max;

	public MinMax() {
		this(0, 0);
	}

	public MinMax(int min, int max) {
		set(min, max);
	}

	public void set(int min, int max) {
		this.min = min;
		this.max = max;
	}

	public void set(MinMax mm) {
		set(mm.min, mm.max);
	}

	public int getMiddle() {
		return (min + max) >> 1;
	}

	public void mergeMinMax(MinMax mm) {
		mergeMinMax(mm.min, mm.max);
	}

	public void mergeMinMax(int min, int max) {
		if (this.min > min)
			this.min = min;
		if (this.max < max)
			this.max = max;
	}

	public int getMax() {
		return max;
	}

	public int getMin() {
		return min;
	}

	public int computeMiddle() {
		return (min + max) >> 1;
	}

	public int fillArray(byte[] arr, int p) {
		arr[p++] = (byte) min;
		arr[p++] = (byte) max;
		return p;
	}

	@Override
	public String toString() {
		return SFormatter.UIformat("min_max(%d, %d)", min, max);
	}
}
