package com.owon.uppersoft.vds.ui.dialog;

import java.awt.Window;

public interface ProgressObserver {
	Window getWindow();

	void setMaximum(int max);

	// 在smart中用到
	void setValue(int v);

	void increaseValue(int del);

	void shutdown();
}