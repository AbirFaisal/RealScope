package com.owon.uppersoft.vds.core.machine;

/**
 * VDS_Portable, for the portable type, i.e. VDS products where part of the workload is still done on the device cpu
 * 
 * Currently only an inverse stamp has been added to saved files; it needs to be extended to recording and reference
 * 
 * Processing code for new versions keeps being added,
 * 
 * Internal release of the version; software version numbers start with 0.xxx
 */
public class VDS_Portable {
	static {
		// Platform.codeName_();
	}
	public static final int INVERSE_TYPE_RAW_REVERSE = 2;// In inverted acquisition state + data needs to be inverted after loading
	public static final int INVERSE_TYPE_REVERSED = 1;// In inverted acquisition state + data does not need to be inverted after loading
	public static final int INVERSE_TYPE_RAW_FINE = 0;// Not in inverted acquisition state
	
	public static final String FileHeader = "SPBVDS";

	/** Used for judging the first byte of the EXTEND field */
	/** File type judgment */
	public static final int FILEFORMAT_PREUSE = 0;
	public static final int FILEFORMAT_DM = 1;
	public static final int FILEFORMAT_NORMAL = 2;
	public static final int FILEFORMAT_RECORD = 3;
	public static final int FILEFORMAT_REF = 4;
}