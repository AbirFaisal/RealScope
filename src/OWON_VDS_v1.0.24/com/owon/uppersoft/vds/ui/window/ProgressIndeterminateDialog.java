package com.owon.uppersoft.vds.ui.window;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;

public class ProgressIndeterminateDialog implements Runnable {

	private JFrame parent;
	private String title;
	private boolean modal;

	private JDialog dlg;
	private TitledBorder titleBorder;
	private Thread porIndDlg;

	public ProgressIndeterminateDialog(final JFrame parent,
			final boolean modal, final String title, String cancelt,
			String cancelPrompt) {
		this.parent = parent;
		this.modal = modal;
		this.title = title;
		this.cancelPrompt = cancelPrompt;
		this.canceltxt = cancelt;

		porIndDlg = new Thread(this);
	}

	private String canceltxt, cancelPrompt;

	private void init() {
		dlg = new JDialog(parent, modal);
		Container content = dlg.getContentPane();

		JProgressBar autopgb = new JProgressBar();
		autopgb.setIndeterminate(true);
		autopgb.setStringPainted(false);
		autopgb.setPreferredSize(new Dimension(400, 90));
		titleBorder = BorderFactory.createTitledBorder(title);
		autopgb.setBorder(titleBorder);

		JPanel btnPane = new JPanel();
		btnPane.setLayout(new FlowLayout(FlowLayout.TRAILING));

		JButton cancelbtn = new JButton(canceltxt);
		btnPane.add(cancelbtn);

		cancelbtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				WindowUtil.showCancelDialog(cancelPrompt, dlg);
			}
		});

		content.add(autopgb, BorderLayout.CENTER);
		content.add(btnPane, BorderLayout.SOUTH);

		dlg.setSize(400, 99);
		dlg.setLocationRelativeTo(parent);
		dlg.setUndecorated(true);
		dlg.setVisible(true);
	}

	@Override
	public void run() {
		init();
	}

	public void startIndeterminateDlg() {
		porIndDlg.start();
	}

	void runIndeterminateDlg() {
		porIndDlg.run();
	}

	void addFocusListener(FocusListener newFocusListener) {
		dlg.addFocusListener(newFocusListener);
		dlg.requestFocus();
	}

	public void close() {
		dlg.dispose();
		if (porIndDlg != null) {
			try {
				porIndDlg.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public static void main(String[] args) {
		final ProgressIndeterminateDialog pig = new ProgressIndeterminateDialog(
				null, false, "Waiting...", "cancel", "are you sure?");
		pig.runIndeterminateDlg();
		pig.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
			}

			@Override
			public void focusLost(FocusEvent e) {
				pig.close();
			}
		});
	}

}
