package com.owon.uppersoft.vds.device.interpret.util;

import java.util.BitSet;

import com.owon.uppersoft.vds.util.PrimaryTypeUtil;

public class CBitSet extends BitSet {

	public CBitSet(int nbits) {
		super(nbits);
	}

	/**
	 * @param bs
	 * @param v
	 * @param idx
	 *            从要设置的低位到高位
	 */
	public void setN(int v, int... idx) {
		int l = idx.length;

		for (int i = 0; i < l; i++) {
			int bv = (v >> i) & 1;
			int id = idx[i];
			if (bv == 0)
				clear(id);
			else
				set(id);
		}
	}

	public void set1(boolean b, int idx) {
		if (b)
			set(idx);
		else
			clear(idx);
	}

	public void set1(int v, int idx) {
		set1(v != 0, idx);
	}

	public int getValue() {
		int v = 0;
		int num = length();
		for (int i = 0; i < num; i++) {
			int d = get(i) ? 1 : 0;
			v |= d << i;
		}
		return v;
	}

	public static void main(String[] args) {
		CBitSet bs = new CBitSet(16);

		bs.set1(0, 15);
		bs.setN(2, 8, 14);
		bs.set1(1, 13);
		bs.setN(6, 5, 6, 7);

		String t = PrimaryTypeUtil.toBytesString_2(bs.getValue());
		System.err.println(t);
	}
}