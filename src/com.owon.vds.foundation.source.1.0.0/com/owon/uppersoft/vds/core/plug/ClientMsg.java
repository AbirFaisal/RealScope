package com.owon.uppersoft.vds.core.plug;

import java.nio.ShortBuffer;

public class ClientMsg {
	public static final int RecvDataAck = 3;
	public static final int RecvAcknReqNxt = 4;
	public static final int ReqNxt = 5;

	public static final int SINE = 0;
	public static final int Square = 1;
	public static final int Triangle = 2;
	public static final int RampUp = 3;
	public static final int RampDown = 4;

	public static final int RequestData = 0;

	public int msgType = RequestData;

	public int genType = SINE;
	public int recordlength;
	public int peroidpoint;
	public int beginidx, lenidx;
	public int amplitude;

	public int yoffset = 0;

	public ShortBuffer sbuf;

	public ClientMsg() {
	}
}