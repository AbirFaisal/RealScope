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
	 * @return 是否改变了密码状态
	 */
	public boolean input(int cd) {
		cd = cd - KeyEvent.VK_NUMPAD0 + '0';
		// System.err.println(bb.position());
		int b = bb.get();
		// 输入有误重置比较上下文
		if (b != cd) {
			bb.rewind();
			return false;
		}
		if (!bb.hasRemaining()) {
			// 输入正确，打开权限，重置比较上下文
			open = !open;
			bb.rewind();
			return true;
		} else {
			// 输入正确，未完继续
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