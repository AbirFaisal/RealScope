package com.owon.uppersoft.vds.core.usb;

/**
 * IUSBProtocol，USB连接通讯参数
 */
public interface IUSBProtocol {
	static final short IdVender = (short) 0x5345;
	static final short IdProduct = (short) 0x1234;

	// static final int WriteEp = 0x03;
	// static final int ReadEp = 0x81;
	//
	// static final int DevConfiguration = 1;
	// static final int DevInterface = 0;
	static final int DevAltinterface = -1;

//	static final int RetryTimeout = 2000;
//
//	static final int BufSize = 1 << 14;
//
//	static final int DefaultBMPFileFlag = 1;
//
//	static final int ResponseOnSTARTDataLength = 12;
//
//	static final byte[] STARTMEMDEPThCommand = new byte[] { 'S', 'T', 'A', 'R',
//			'T', 'M', 'E', 'M', 'D', 'E', 'P', 'T', 'H' };
//
//	static final byte[] STARTCommand = new byte[] { 'S', 'T', 'A', 'R', 'T' };
	// STARTMEMDEPThCommand;//
}
