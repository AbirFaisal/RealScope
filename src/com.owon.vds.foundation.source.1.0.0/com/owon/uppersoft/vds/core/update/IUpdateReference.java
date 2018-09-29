package com.owon.uppersoft.vds.core.update;

public interface IUpdateReference {
//	public static final String MESSAGELIB_BUNDLE = "com.owon.uppersoft.common.i18n.UpdateMessage";
	public static final int BUFFER_SIZE = (1 << 10) * 10;// 缓冲区大小
	public static final int TaskDelayTime = 0;// 大于0说明调试
	public static final int HttpURLConnectionTimeout = 3000;
	public static final String TempDir = "downloadTemp";
}
