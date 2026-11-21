package com.owon.uppersoft.dso.wf;

import com.owon.uppersoft.vds.core.machine.VDS_Portable;

public class ChannelInverseTranslator {
	public ChannelInverseTranslator() {
	}

	/** RT & DM each do their own inversion processing or not */
	public static boolean setOfflineInverseType_Normal(int inverseType) {
		switch (inverseType) {
		default:
		case VDS_Portable.INVERSE_TYPE_RAW_FINE:
			return false;
		case VDS_Portable.INVERSE_TYPE_REVERSED:
			return true;
		}
		/** Recorded waveforms need no inversion */
	}

	public static boolean setOfflineInverseType_DM(int inverseType) {
		switch (inverseType) {
		default:
		case VDS_Portable.INVERSE_TYPE_RAW_FINE:
			return false;
		case VDS_Portable.INVERSE_TYPE_RAW_REVERSE:
			/** Loading DM offline needs inversion */
			return true;
		}
	}

	public static boolean setRTInverseType_DM(boolean inverse) {
		if (inverse) {
			/** Loading DM needs inversion */
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Save deep memory file
	 * 
	 * @return
	 */
	public static int getInverseType_DM(boolean inverse) {
		return inverse ? VDS_Portable.INVERSE_TYPE_RAW_REVERSE
				: VDS_Portable.INVERSE_TYPE_RAW_FINE;
	}

	/**
	 * Save recording file
	 * 
	 * @return
	 */
	public static int getInverseType_Record(boolean inverse) {
		return inverse ? VDS_Portable.INVERSE_TYPE_REVERSED
				: VDS_Portable.INVERSE_TYPE_RAW_FINE;
	}
}
