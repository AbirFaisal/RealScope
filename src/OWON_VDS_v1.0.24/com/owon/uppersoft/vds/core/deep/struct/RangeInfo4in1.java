package com.owon.uppersoft.vds.core.deep.struct;

public class RangeInfo4in1 {
	private int min, max, first, last;

	public int fillArray(byte[] arr, int p) {
		arr[p++] = (byte) first;
		arr[p++] = (byte) min;
		arr[p++] = (byte) max;
		arr[p++] = (byte) last;
		return p;
	}

	public int fillArray(int[] arr, int p) {
		arr[p++] = first;
		arr[p++] = min;
		arr[p++] = max;
		arr[p++] = last;
		return p;
	}

	public void setAllAs1(int v) {
		min = max = first = last = v;
	}

	public void setFirst(int first) {
		this.first = first;
	}

	public int getMax() {
		return max;
	}

	public int getMin() {
		return min;
	}

	public RangeInfo4in1() {
	}

	public void setAll(int min, int max, int first, int last) {
		this.min = min;
		this.max = max;
		this.first = first;
		this.last = last;
	}

	@Override
	public String toString() {
		return String.format("%1$d, %2$d; %3$d, %4$d", min, max, first, last);
	}

	public static void main(String[] args) {
		RangeInfo4in1 ri = new RangeInfo4in1();
		ri.setAll(1, 2, 3, 4);
		System.out.println(ri);
	}
}