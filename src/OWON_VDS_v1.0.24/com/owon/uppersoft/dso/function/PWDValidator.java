package com.owon.uppersoft.dso.function;

import java.awt.event.KeyEvent;
import java.nio.ByteBuffer;

public class PWDValidator {
	private ByteBuffer bb = ByteBuffer.wrap("13524".getBytes());
	private boolean open;

	public PWDValidator() {
		open = false;
		bb.rewind();
	}

	/**
	 * @param cd
	 * @return whether the password state changed
	 */
	public boolean input(int cd) {
		cd = cd - KeyEvent.VK_NUMPAD0 + '0';
		// System.err.println(bb.position());
		int b = bb.get();
		// Input is wrong, reset the comparison context
		if (b != cd) {
			bb.rewind();
			return false;
		}
		if (!bb.hasRemaining()) {
			// Input is correct, enable permissions and reset the comparison context
			open = !open;
			bb.rewind();
			return true;
		} else {
			// Input is correct but not yet complete, continue
			return false;
		}
	}

	public boolean isOpen() {
		return open;
	}
	
	public void setOpen(boolean open) {
		this.open = open;
	}
}