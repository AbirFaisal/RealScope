package com.owon.uppersoft.vds.data;

import com.owon.uppersoft.vds.util.format.SFormatter;

public class Range {
	public int left, right;

	public Range() {
	}

	public Range(int l, int r) {
		set(l, r);
	}

	public void set(int l, int r) {
		this.left = l;
		this.right = r;
	}

	@Override
	public String toString() {
		return SFormatter.UIformat("(%d, %d)", left, right);
	}

}
