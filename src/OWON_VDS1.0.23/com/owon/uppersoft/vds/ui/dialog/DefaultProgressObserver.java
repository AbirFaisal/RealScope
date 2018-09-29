package com.owon.uppersoft.vds.ui.dialog;

import java.awt.Window;

public class DefaultProgressObserver implements ProgressObserver {
	@Override
	public Window getWindow() {
		return null;
	}

	@Override
	public void setMaximum(int max) {
	}

	@Override
	public void setValue(int v) {
	}

	@Override
	public void shutdown() {
	}

	@Override
	public void increaseValue(int del) {
	}
}