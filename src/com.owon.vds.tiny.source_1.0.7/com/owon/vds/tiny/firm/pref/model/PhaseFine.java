package com.owon.vds.tiny.firm.pref.model;

public class PhaseFine {
	public static final int PHASE_FINE_MAX = 255;

	private boolean select;
	private short abs_value;

	public void setRdValue(short phase_fine_v) {
		// 相位细调参数的符号位，为1代表递增，在界面上是勾选的状态，为0代表递减，在界面上是未勾选状态
		select = phase_fine_v < 0;

		// 相位细调参数的绝对值，去除第15位上的符号
		abs_value = (short) (phase_fine_v & (~(1 << 15)));
		validate();
	}

	void validate() {
		if (abs_value > PHASE_FINE_MAX)
			abs_value = PHASE_FINE_MAX;
	}

	public short getWrValue() {
		// 设置相位细调参数，使用勾选状态设置第15位上的符号位
		int v = select ? (abs_value | (1 << 15)) : abs_value;
		return (short) v;
	}

	public void setPhaseFineValue(boolean b, short value) {
		select = b;
		abs_value = value;
		validate();
	}

	public boolean isSelect() {
		return select;
	}

	public short getAbsValue() {
		return abs_value;
	}

}