package com.owon.uppersoft.vds.ui.widget;

import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.DefaultFormatterFactory;

/**
 * CNumberSpinner, commits on every input, validates digits only, limits the range
 * 
 */
public class CNumberSpinner extends JSpinner {

	/**
	 * @param model
	 *            must be Long
	 */
	public CNumberSpinner(SpinnerNumberModel model) {
		super(model);
		NumberEditor ne = (NumberEditor) getEditor();

		final JFormattedTextField ftf = ne.getTextField();
		ftf.setFormatterFactory(new DefaultFormatterFactory(
				new LineNumberFormatter(model)));
	}
}