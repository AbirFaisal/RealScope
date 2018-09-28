package com.owon.uppersoft.vds.core.comm;

import java.io.IOException;

public interface ISource {
	int read(byte[] arr, int start, int len) throws IOException;
	
//	public static final String USBPort = "USB";
//	public static final String SerialPort = "SerialPort";

//	public static final String Software = "Software";

//	public static final String PortType = "PortType";
//	public static final String Parity = "Parity";
//	public static final String BaudRate = "BaudRate";
//	public static final String StopBits = "StopBits";
//	public static final String DataBits = "DataBits";
}
