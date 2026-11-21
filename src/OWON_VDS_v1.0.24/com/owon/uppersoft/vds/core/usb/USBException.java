package com.owon.uppersoft.vds.core.usb;

import java.io.IOException;

/**
 * USBException, thrown when a USB operation fails
 */
public class USBException extends IOException {

	private static final long serialVersionUID = 1L;

	public USBException(String message) {
		super(message);
	}

	public USBException(String message, Throwable cause) {
		super(message, cause);
	}
}
