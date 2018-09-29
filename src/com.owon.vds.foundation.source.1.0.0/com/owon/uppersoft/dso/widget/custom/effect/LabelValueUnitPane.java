package com.owon.uppersoft.dso.widget.custom.effect;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.vds.core.trigger.help.IStepSpinnerModel;
import com.owon.uppersoft.vds.ui.layout.OneRowLayout;
import com.owon.uppersoft.vds.ui.widget.CSpinner;
import com.owon.uppersoft.vds.ui.widget.custom.CComboBox;
import com.owon.uppersoft.vds.ui.widget.custom.CLabel;
import com.owon.uppersoft.vds.ui.widget.custom.LButton;

public class LabelValueUnitPane extends JPanel {

	public JSpinner spinner;
	public CComboBox comboBox;

	private JLabel label;
	public LButton resetBtn;

	/**
	 * Create the panel
	 */
	public LabelValueUnitPane(String txt, final IStepSpinnerModel sm,
			ComboBoxModel cbm) {
		setOpaque(false);
		setLayout(new OneRowLayout());

		label = new CLabel();
		label.setText(txt);
		add(label);

		spinner = new CSpinner(sm);
		spinner.setPreferredSize(new Dimension(80, 28));

		add(spinner);

		comboBox = new CComboBox(cbm, true);
		comboBox.setPreferredSize(new Dimension(60, 28));
		add(comboBox);

		resetBtn = new LButton();
		resetBtn.setForeground(Color.WHITE);
		resetBtn.setPreferredSize(new Dimension(40, 28));
		String t1 = I18nProvider.bundle().getString("Option.reset");
		t1 = "<html><font><u>" + t1 + "<u></font></html>";
		resetBtn.setText(t1);
		add(resetBtn);

		resetBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sm.setValue(sm.createDefaultValue());
				comboBox.setSelectedIndex(0);
			}
		});

		resetBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	public void setLabel(String txt) {
		label.setText(txt);
	}

	public void setSpinner(int v) {
		spinner.setValue(v);
	}

	public void setUnitIndex(int uidx) {
		comboBox.setSelectedIndex(uidx);
	}

	public void set(String txt, int v, int uidx) {
		label.setText(txt);
		spinner.setValue(v);
		comboBox.setSelectedIndex(uidx);
	}

}
