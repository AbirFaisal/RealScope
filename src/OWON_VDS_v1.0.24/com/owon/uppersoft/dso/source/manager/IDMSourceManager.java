package com.owon.uppersoft.dso.source.manager;

import com.owon.uppersoft.vds.core.wf.dm.DMInfo;

public interface IDMSourceManager {
	/**
	 * 发送可能的多次指令，接收复合数据，深存储用
	 * 
	 * @return 数据中的通道信息
	 */
	void acceptDMData(DMInfo ci);
}
