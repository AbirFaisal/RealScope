package com.owon.uppersoft.vds.ui.widget;

import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.text.DefaultFormatter;

public class CSpinner extends JSpinner {
	public CSpinner(SpinnerModel model) {
		super(model);
		DefaultEditor ne = (DefaultEditor) getEditor();

		final JFormattedTextField ftf = ne.getTextField();

		DefaultFormatter df = (DefaultFormatter) ftf.getFormatter();
		df.setCommitsOnValidEdit(true);
	}
}