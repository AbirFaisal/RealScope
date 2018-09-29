package com.owon.uppersoft.vds.ui.widget;

import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.NumberFormatter;

public class LineNumberFormatter extends NumberFormatter {
	private SpinnerNumberModel model;

	public LineNumberFormatter(SpinnerNumberModel model) {
		this.model = model;
		setCommitsOnValidEdit(true);
	}

	private boolean check(String nstr) {
		Number maxn = (Number) model.getMaximum();
		Number minn = (Number) model.getMinimum();

		long v;
		try {
			v = Long.parseLong(nstr);
		} catch (NumberFormatException e) {
			// e.printStackTrace();
			return false;
		}

		boolean b = true;

		if (maxn != null) {
			long max = maxn.longValue();
			b &= v <= max;
		}
		if (minn != null) {
			long min = minn.longValue();
			b &= v >= min;
		}
		return b;
	}

	@Override
	public Object stringToValue(String text) throws ParseException {
		// System.out.println(text);
		if (check(text)) {
			return super.stringToValue(text);
		} else {
			JFormattedTextField ftf = getFormattedTextField();
			Object n = ftf.getValue();
			// 这里取出并存入的都是Long
			ftf.setValue(n);
			return n;
		}
	}
}