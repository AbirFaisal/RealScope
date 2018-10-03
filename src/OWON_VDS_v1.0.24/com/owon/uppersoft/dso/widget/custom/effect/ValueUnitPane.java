package com.owon.uppersoft.dso.widget.custom.effect;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import com.owon.uppersoft.vds.ui.layout.OneRowLayout;
import com.owon.uppersoft.vds.ui.widget.CSpinner;
import com.owon.uppersoft.vds.ui.widget.custom.CComboBox;

/**
 * ValueUnitPane
 * 
 * @author Matt
 * @deprecated
 */
public class ValueUnitPane extends JPanel {

	private static final String[] SEC = new String[] { "nS", "uS", "mS", "S" };

	private final JSpinner spinner;
	private final CComboBox comboBox;

	/**
	 * Create the panel
	 */
	private ValueUnitPane(SpinnerModel sm, ComboBoxModel cbm) {
		setOpaque(false);
		setLayout(new OneRowLayout(0));

		spinner = new CSpinner(sm);
		spinner.setPreferredSize(new Dimension(60, 28));
		add(spinner);

		comboBox = new CComboBox(cbm, true);
		spinner.setPreferredSize(new Dimension(100, 28));
		add(comboBox);
	}

	protected void setSpinner(int v) {
		spinner.setValue(v);
	}

	protected void setUnitIndex(int uidx) {
		comboBox.setSelectedIndex(uidx);
	}

	protected void set(int v, int uidx) {
		spinner.setValue(v);
		comboBox.setSelectedIndex(uidx);
	}

	public static void main(String[] args) {
		final JFrame jf = new JFrame();
		jf.setLayout(new FlowLayout());

		final ValueUnitPane vup = new ValueUnitPane(new SpinnerNumberModel(),
				new DefaultComboBoxModel(SEC));
		jf.add(vup);
		vup.set(10, 0);

		JButton jb = new JButton("change");
		jb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				vup.set(100000, 2);
				jf.pack();
			}
		});
		jf.add(jb);

		jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		jf.pack();
		jf.setVisible(true);
	}

}
