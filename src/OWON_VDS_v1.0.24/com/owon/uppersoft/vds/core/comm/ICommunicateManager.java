package com.owon.uppersoft.vds.core.comm;

/**
 * ICommunicateManager, separates the relatively low-level generic data transfer interface into a standalone class
 * 
 */
public interface ICommunicateManager {
	/**
	 * For the remote end, after a transmission error occurs, can be used to try to recover and reconnect
	 * 
	 * Using this communication-class-based method avoids involving details of the communication method such as port, ip, etc., and these need not be set during reconnection, which is convenient
	 */
	boolean tryRescue();

	int retryTimes();

	/**
	 * In a concrete implementation, determined by the implementation's connection state;
	 * 
	 * In the unified SourceManager/BufferredSourceManager, determined by a specific variable
	 * 
	 * @return
	 */
	boolean isConnected();

	/**
	 * Write command
	 */
	int write(byte[] arr, int len);

	/**
	 * Receive a single command
	 */
	int acceptResponse(byte[] arr, int len);

}