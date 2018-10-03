package com.owon.uppersoft.dso.model.trigger.helper;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.owon.uppersoft.dso.model.trigger.common.IValueChange;

public class LabelValueUnitContactor implements ChangeListener, ItemListener {
	private IValueChange tp;
	private JComboBox cbb;
	private JSpinner spin;

	private boolean listening = false;

	public LabelValueUnitContactor(IValueChange tp, JComboBox cbb,
			JSpinner spin) {
		this.tp = tp;
		this.cbb = cbb;
		this.spin = spin;
		cbb.addItemListener(this);

		JSpinner.DefaultEditor jd = (JSpinner.DefaultEditor) spin.getEditor();
		JFormattedTextField jftf = jd.getTextField();
		jftf.setEditable(false);
		jftf.setFocusable(false);
		jftf.setHorizontalAlignment(JFormattedTextField.RIGHT);

		spin.addChangeListener(this);

		listening = true;
	}

	public void updateStepIndex(int idx) {
		listening = false;
		cbb.setSelectedIndex(idx);
		listening = true;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (!listening)
			return;
		tp.stateChanged(spin.getValue());

	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (!listening)
			return;
		if (e.getStateChange() != ItemEvent.SELECTED)
			return;

		int idx = cbb.getSelectedIndex();
		EnumTypeNValue<? extends IenumDelegate> v = (EnumTypeNValue<? extends IenumDelegate>) spin
				.getValue();
		v.nextStep(idx);
	}
}