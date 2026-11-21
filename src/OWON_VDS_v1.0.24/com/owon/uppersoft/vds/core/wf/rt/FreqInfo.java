package com.owon.uppersoft.vds.core.wf.rt;

import java.math.BigDecimal;

import com.owon.uppersoft.vds.core.machine.MachineType;

public class FreqInfo {
	/** Could be abstracted */
	private int freq_base, freq_ref;
	private float freq;

	public float getFrequencyFloat() {
		return freq;
	}

	public void setFreqBaseNRef(int b, int rf) {
		freq_base = b;
		freq_ref = rf;
	}

	/**
	 * KNOW Received frequency algorithm: frequency = frequency value/reference value*125M
	 * 
	 * @param base
	 * @param ref
	 * @return unit Hz
	 */
	public void computeFreq(BigDecimal currentSampleRateBD_kHz,
			MachineType machine) {
		if (freq_ref <= 0) {
			freq = freq_base;
			return;
		}
		if (currentSampleRateBD_kHz == null) {
			freq = -1;
			return;
		}

		// DBG.configln(String.format("base_c: %f, ref_c: %f, bd: %s",
		// freq_base,
		// freq_ref, currentSampleRateBD_kHz));
		freq = (float) machine.doHandleFrequencyCompute(freq_base, freq_ref,
				currentSampleRateBD_kHz);
	}

	public void setFreq(float b) {
		freq = b;
		// DBG.configln(String.format("chl: %d, b: %d, rf: %d", chl, b, rf));
	}

}
