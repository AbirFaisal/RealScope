package com.owon.uppersoft.vds.core.usb;

/**
 * USBTimeoutException, thrown when a USB transfer times out
 */
public class USBTimeoutException extends USBException {

	private static final long serialVersionUID = 1L;

	public USBTimeoutException(String message) {
		super(message);
	}

	public USBTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}
}
