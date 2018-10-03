package com.owon.vds.tiny.circle;
public class TopBaseCalResult {
	public int top, base, amp;

	public TopBaseCalResult(int top, int base) {
		this.top = top;
		this.base = base;
		amp = top - base;
	}

	public void withoutPos0(int pos0) {
		top -= pos0;
		base -= pos0;
	}

	@Override
	public String toString() {
		return "top: " + top + " , base: " + base + " , amp: " + amp;
	}

}