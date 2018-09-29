package com.owon.uppersoft.vds.ui.widget;

import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.DefaultFormatterFactory;

/**
 * CNumberSpinner，即输即提交，验证仅数字，限制范围
 * 
 */
public class CNumberSpinner extends JSpinner {

	/**
	 * @param model
	 *            必须是Long
	 */
	public CNumberSpinner(SpinnerNumberModel model) {
		super(model);
		NumberEditor ne = (NumberEditor) getEditor();

		final JFormattedTextField ftf = ne.getTextField();
		ftf.setFormatterFactory(new DefaultFormatterFactory(
				new LineNumberFormatter(model)));
	}
}