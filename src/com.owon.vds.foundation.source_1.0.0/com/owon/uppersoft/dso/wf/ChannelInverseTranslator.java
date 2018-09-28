package com.owon.uppersoft.dso.wf;

import com.owon.uppersoft.vds.core.machine.VDS_Portable;

public class ChannelInverseTranslator {
	public ChannelInverseTranslator() {
	}

	/** RT & DM 是否进行各自的反相处理 */
	public boolean setOfflineInverseType_Normal(int inverseType) {
		switch (inverseType) {
		default:
		case VDS_Portable.INVERSE_TYPE_RAW_FINE:
			return false;
		case VDS_Portable.INVERSE_TYPE_REVERSED:
			return true;
		}
		/** 录制波形无需进行反相 */
	}

	public boolean setOfflineInverseType_DM(int inverseType) {
		switch (inverseType) {
		default:
		case VDS_Portable.INVERSE_TYPE_RAW_FINE:
			return false;
		case VDS_Portable.INVERSE_TYPE_RAW_REVERSE:
			/** 离线载入DM，需要反相 */
			return true;
		}
	}

	public boolean setRTInverseType_DM(boolean inverse) {
		if (inverse) {
			/** 载入DM，需要反相 */
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 保存深存储文件
	 * 
	 * @return
	 */
	public int getInverseType_DM(boolean inverse) {
		return inverse ? VDS_Portable.INVERSE_TYPE_RAW_REVERSE
				: VDS_Portable.INVERSE_TYPE_RAW_FINE;
	}

	/**
	 * 保存录制文件
	 * 
	 * @return
	 */
	public int getInverseType_Record(boolean inverse) {
		return inverse ? VDS_Portable.INVERSE_TYPE_REVERSED
				: VDS_Portable.INVERSE_TYPE_RAW_FINE;
	}
}
