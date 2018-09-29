package com.owon.uppersoft.dso.model;

import com.owon.uppersoft.vds.core.aspect.control.DeepProvider;
import com.owon.uppersoft.vds.core.wf.dm.DMInfo;

public class DMInfoTiny extends DMInfo {
	public static final int FILEPROTOCOL_VDS_INVERSE = 1;// 从1开始，0表示原型版本
	public static final int FILEPROTOCOL_VDS_EXTEND = 2;// 从2开始，加入扩展标识
	public static final int FILEPROTOCOL_VDS_PK_PULLTRG = 3;// 从3开始，加入峰值检测和拉触发信息
	public static final int FILEPROTOCOL_VDS_PLUG10 = 4;
	public static final int FILEPROTOCOL_VDS_CURRENT = FILEPROTOCOL_VDS_PLUG10;// 始终使用软件当前的版本

	public DMInfoTiny(DeepProvider dp) {
		super(dp);
	}
}