package com.owon.uppersoft.vds.core.machine;

/**
 * VDS_Portable，针对便携式类型，即一部分的工作量仍放在下位机cpu进行的VDS产品
 * 
 * 目前仅在保存文件中加入了反相戳，需要扩展到录制和参考
 * 
 * 新版本的处理代码不断加入，
 * 
 * 版本内部发行，软件版本号开始使用0.xxx
 */
public class VDS_Portable {
	static {
		// Platform.codeName_();
	}
	public static final int INVERSE_TYPE_RAW_REVERSE = 2;// 处在反相采集状态+数据载入后需要进行反相
	public static final int INVERSE_TYPE_REVERSED = 1;// 处在反相采集状态+数据载入后不需要进行反相
	public static final int INVERSE_TYPE_RAW_FINE = 0;// 不处在反相采集状态
	
	public static final String FileHeader = "SPBVDS";

	/** 用于EXTEND字段的第一字节判断 */
	/** 文件类型判断 */
	public static final int FILEFORMAT_PREUSE = 0;
	public static final int FILEFORMAT_DM = 1;
	public static final int FILEFORMAT_NORMAL = 2;
	public static final int FILEFORMAT_RECORD = 3;
	public static final int FILEFORMAT_REF = 4;
}