package com.owon.uppersoft.dso.source.manager;

import com.owon.uppersoft.vds.core.wf.dm.DMInfo;

public interface IDMSourceManager {
	/**
	 * Send possibly multiple commands and receive composite data, used for deep memory
	 * 
	 * @return Channel info in the data
	 */
	void acceptDMData(DMInfo ci);
}
