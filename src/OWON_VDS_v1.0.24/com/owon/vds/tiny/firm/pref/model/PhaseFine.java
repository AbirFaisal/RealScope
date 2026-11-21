package com.owon.vds.tiny.firm.pref.model;

public class PhaseFine {
	public static final int PHASE_FINE_MAX = 255;

	private boolean select;
	private short abs_value;

	public void setRdValue(short phase_fine_v) {
		// Sign bit of the phase fine-tune parameter: 1 means increasing, shown as checked in the UI; 0 means decreasing, shown as unchecked
		select = phase_fine_v < 0;

		// Absolute value of the phase fine-tune parameter, with the sign on bit 15 removed
		abs_value = (short) (phase_fine_v & (~(1 << 15)));
		validate();
	}

	void validate() {
		if (abs_value > PHASE_FINE_MAX)
			abs_value = PHASE_FINE_MAX;
	}

	public short getWrValue() {
		// Set the phase fine-tune parameter, using the checked state to set the sign bit on bit 15
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