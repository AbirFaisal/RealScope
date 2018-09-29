package com.owon.uppersoft.vds.core.tune;

/**
 * 键名中的"_"分隔部分字符提取器
 * 
 */
public class StringRoller {
	private int ptr;
	private String current, base;

	public StringRoller(String base, int ptr) {
		this.base = base;
		this.ptr = ptr;
	}

	protected void setBase(String base) {
		this.base = base;
	}

	protected void setPtr(int ptr) {
		this.ptr = ptr;
	}

	public void nextWord() {
		if (ptr < 0)
			return;

		int beg = ptr;
		ptr = base.indexOf('_', beg);

		if (ptr < 0) {
			current = base.substring(beg);
			ptr = -1;
			return;
		}
		current = base.substring(beg, ptr);
		ptr++;
	}

	/**
	 * 也可调用2次nextWord()来实现
	 */
	public void next2Words() {
		if (ptr < 0)
			return;

		int beg = ptr;
		ptr = base.indexOf('_', beg);
		if (ptr < 0) {
			current = base.substring(beg);
			ptr = -1;
			return;
		}
		ptr = base.indexOf('_', ptr + 1);
		if (ptr < 0) {
			current = base.substring(beg);
			ptr = -1;
			return;
		}

		current = base.substring(beg, ptr);
		ptr++;
	}

	/**
	 * 调用os[i]的equals方法，可扩展以实现对current的等价判断
	 * 
	 * @param os
	 * @return
	 */
	public int fineWord(Object[] os) {
		String s = current;
		int len = os.length;
		for (int n = 0; n < len; n++) {
			if (s.equals(os[n].toString())) {
				return n;
			}
		}
		return -1;
	}
}