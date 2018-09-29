package com.owon.uppersoft.vds.core.io;
public interface IOPacket{
	int readPacket(byte[] rdbuf, int readlen) ;

	int writePacket(byte[] wrbuf, int sendlen);
}