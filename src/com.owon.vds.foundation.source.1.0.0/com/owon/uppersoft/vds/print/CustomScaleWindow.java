package com.owon.uppersoft.vds.print;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.owon.uppersoft.vds.core.aspect.Localizable;

public class CustomScaleWindow implements Localizable {

	public static final String ACTION_CANCEL = "Action.Cancel";
	public static final String ACTION_OK = "Action.OK";
	public static final String PRINT_USER_DEFAULT_SCALE = "Print.UserDefaultScale";
	public static final String PRINT_DEFAULT_SCALE = "Print.DefaultScale";

	private PrinterPreviewFrame ppf;

	private JSpinner spinner;
	private JDialog dialog;

	/**
	 * Create the application
	 */
	public CustomScaleWindow(PrinterPreviewFrame p) {
		ppf = p;
		initialize();
		localize(PrinterPreviewControl.bundle());
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setModal(true);
		dialog.setVisible(true);
	}

	private JLabel label;
	private JButton okButton;
	private JButton cancelButton;

	/**
	 * Initialize the contents of the frame
	 */
	private void initialize() {
		dialog = new JDialog();
		dialog.getContentPane().setLayout(new GridBagLayout());
		dialog.setBounds(100, 100, 353, 135);
		dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		SpinnerNumberModel model = new SpinnerNumberModel(ppf.getScale() * 100,
				0, 500, 1);

		label = new JLabel();
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.SOUTHEAST;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.ipadx = 25;
		gridBagConstraints.insets = new Insets(33, 20, 6, 60);
		dialog.getContentPane().add(label, gridBagConstraints);
		spinner = new JSpinner(model);
		final GridBagConstraints gridBagConstraints_2 = new GridBagConstraints();
		gridBagConstraints_2.anchor = GridBagConstraints.SOUTHWEST;
		gridBagConstraints_2.gridx = 1;
		gridBagConstraints_2.gridy = 0;
		gridBagConstraints_2.ipadx = 15;
		gridBagConstraints_2.insets = new Insets(32, 5, 0, 5);
		dialog.getContentPane().add(spinner, gridBagConstraints_2);

		final JLabel label_1 = new JLabel();
		label_1.setText("%");
		final GridBagConstraints gridBagConstraints_4 = new GridBagConstraints();
		gridBagConstraints_4.anchor = GridBagConstraints.SOUTHWEST;
		gridBagConstraints_4.gridx = 2;
		gridBagConstraints_4.gridy = 0;
		gridBagConstraints_4.ipadx = 15;
		gridBagConstraints_4.insets = new Insets(35, 0, 4, 50);
		dialog.getContentPane().add(label_1, gridBagConstraints_4);

		okButton = new JButton();
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ppf.setScale(new Double(spinner.getValue().toString()) / 100);
				dialog.dispose();
			}
		});
		final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
		gridBagConstraints_1.gridx = 0;
		gridBagConstraints_1.gridy = 1;
		gridBagConstraints_1.ipadx = 60;
		gridBagConstraints_1.ipady = 10;
		gridBagConstraints_1.insets = new Insets(20, 20, 14, 60);
		dialog.add(okButton, gridBagConstraints_1);

		cancelButton = new JButton();
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});
		final GridBagConstraints gridBagConstraints_3 = new GridBagConstraints();
		gridBagConstraints_3.gridx = 1;
		gridBagConstraints_3.gridy = 1;
		gridBagConstraints_3.ipadx = 42;
		gridBagConstraints_3.ipady = 14;
		gridBagConstraints_3.insets = new Insets(18, 5, 16, 0);
		dialog.add(cancelButton, gridBagConstraints_3);
	}

	public void localize(ResourceBundle bundle) {
		dialog.setTitle(bundle.getString(PRINT_DEFAULT_SCALE));
		label.setText(bundle.getString(PRINT_USER_DEFAULT_SCALE));
		okButton.setText(bundle.getString(ACTION_OK));
		cancelButton.setText(bundle.getString(ACTION_CANCEL));
	}

}
