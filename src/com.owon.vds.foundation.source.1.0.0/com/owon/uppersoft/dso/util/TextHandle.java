package com.owon.uppersoft.dso.util;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import com.owon.uppersoft.dso.view.pane.LogDialog;

public class TextHandle extends Handler {
	LogDialog ta;

	public TextHandle(LogDialog ta) {
		this.ta = ta;
	}

	@Override
	public void close() throws SecurityException {
	}

	@Override
	public void flush() {
	}

	@Override
	public void publish(LogRecord record) {
		String t = record.getMessage();
		ta.append(t);
	}
}