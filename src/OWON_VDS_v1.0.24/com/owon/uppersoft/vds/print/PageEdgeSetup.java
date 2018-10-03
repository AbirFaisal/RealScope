package com.owon.uppersoft.vds.print;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.owon.uppersoft.vds.core.aspect.Localizable;

public class PageEdgeSetup implements Localizable {

	private static final String ACTION_DEFAULT = "Action.Default";
	private static final String PRINT_SET_PRINT_SPACE = "Print.SetPrintSpace";
	private static final String PRINT_DOWN_SPACE = "Print.DownSpace";
	private static final String PRINT_UP_SPACE = "Print.UpSpace";
	private static final String PRINT_RIGHT_SPACE = "Print.RightSpace";
	private static final String PRINT_LEFT_SPACE = "Print.LeftSpace";
	private static final String PRINT_SET_PAGE_SPACE = "Print.SetPageSpace";

	private JSpinner downSpinner;
	private JSpinner upSpinner;
	private JSpinner rightSpinner;
	private JSpinner leftSpinner;
	private JDialog dialog;

	private PrinterPreviewFrame ppf;

	/**
	 * Create the application
	 */
	public PageEdgeSetup(PrinterPreviewFrame value) {
		ppf = value;
		initialize();
		localize(PrinterPreviewControl.bundle());
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setModal(true);
		dialog.setVisible(true);
	}

	private JLabel label;
	private JLabel label_1;
	private JLabel label_2;
	private JLabel label_3;
	private JLabel label_4;
	private JButton sureButton;
	private JButton defaultButton;
	private JButton cancelButton;

	/**
	 * Initialize the contents of the frame
	 */
	private void initialize() {
		dialog = new JDialog();
		final BorderLayout borderLayout = new BorderLayout();
		borderLayout.setVgap(10);
		borderLayout.setHgap(10);
		dialog.getContentPane().setLayout(borderLayout);
		dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		label_4 = new JLabel();
		label_4.setPreferredSize(new Dimension(0, 40));
		dialog.getContentPane().add(label_4, BorderLayout.NORTH);

		final JPanel panel = new JPanel();
		final GridLayout gridLayout = new GridLayout(0, 4);
		gridLayout.setVgap(10);
		gridLayout.setHgap(10);
		panel.setLayout(gridLayout);
		dialog.getContentPane().add(panel);

		label = new JLabel();
		label.setPreferredSize(new Dimension(0, 0));
		panel.add(label);

		SpinnerNumberModel model1 = new SpinnerNumberModel(ppf
				.getPageEdgeSpace().x, 0, 100, 1);

		leftSpinner = new JSpinner(model1);
		panel.add(leftSpinner);

		label_1 = new JLabel();
		panel.add(label_1);
		SpinnerNumberModel model2 = new SpinnerNumberModel(ppf
				.getPageEdgeSpace().y, 0, 100, 1);

		rightSpinner = new JSpinner(model2);
		panel.add(rightSpinner);

		label_2 = new JLabel();
		panel.add(label_2);
		SpinnerNumberModel model3 = new SpinnerNumberModel(ppf
				.getPageEdgeSpace().width, 0, 100, 1);

		upSpinner = new JSpinner(model3);
		panel.add(upSpinner);

		label_3 = new JLabel();
		panel.add(label_3);
		SpinnerNumberModel model4 = new SpinnerNumberModel(ppf
				.getPageEdgeSpace().height, 0, 100, 1);

		downSpinner = new JSpinner(model4);
		panel.add(downSpinner);

		final JPanel panel_1 = new JPanel();
		panel_1.setPreferredSize(new Dimension(0, 40));
		dialog.getContentPane().add(panel_1, BorderLayout.SOUTH);

		sureButton = new JButton();
		panel_1.add(sureButton);
		sureButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Rectangle r = new Rectangle((Integer) (leftSpinner.getValue()),
						(Integer) (rightSpinner.getValue()),
						(Integer) (upSpinner.getValue()),
						(Integer) (downSpinner.getValue()));
				ppf.setPageEdgeSpace(r);
				dialog.dispose();
			}
		});

		defaultButton = new JButton();
		panel_1.add(defaultButton);
		defaultButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				leftSpinner.setValue(20);
				rightSpinner.setValue(20);
				upSpinner.setValue(20);
				downSpinner.setValue(20);
			}
		});

		cancelButton = new JButton();
		panel_1.add(cancelButton);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});
	}

	public void localize(ResourceBundle bundle) {
		dialog.setTitle(bundle.getString(PRINT_SET_PAGE_SPACE));
		label.setText(bundle.getString(PRINT_LEFT_SPACE));
		label_1.setText(bundle.getString(PRINT_RIGHT_SPACE));
		label_2.setText(bundle.getString(PRINT_UP_SPACE));
		label_3.setText(bundle.getString(PRINT_DOWN_SPACE));
		label_4.setText(bundle.getString(PRINT_SET_PRINT_SPACE));
		sureButton.setText(bundle.getString(CustomScaleWindow.ACTION_OK));
		defaultButton.setText(bundle.getString(ACTION_DEFAULT));
		cancelButton.setText(bundle.getString(CustomScaleWindow.ACTION_CANCEL));
	}
}
