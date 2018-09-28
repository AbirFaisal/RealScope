package com.owon.uppersoft.vds.ui.dialog;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.owon.uppersoft.vds.ui.layout.OneColumnLayout;

public class ProgressMonitorDialog extends JDialog {
	public static final String DefaultLabelText = "Progressing",
			DefaultBarText = "going...", DefaultTitleName = "PMDialog",
			DefaultButtonText = "cancel";

	// private String labelText = DefaultLabelText, barText = DefaultBarText;
	private String titleName = DefaultTitleName,
			buttonText = DefaultButtonText;
	private int WIDTH = 580, LENGTH = 150;
	private JLabel label;
	private JProgressBar pBar;
	private JPanel panel;
	private JButton button;

	private int min = 0, max = 100;

	private ProgressMonitorDialog(String title) {
		this(null, title, DefaultLabelText, DefaultBarText, 0, 100);
	}

	public ProgressMonitorDialog(int Min, int Max) {
		this(null, DefaultTitleName, DefaultLabelText, DefaultBarText, Min, Max);
	}

	public ProgressMonitorDialog(Component parentComponent, String title,
			String message, String note, int min, int max) {
		super();
		CreatDialog();
		setTitle(title);
		label.setText(message);
		pBar.setString(note);
		pBar.setMinimum(min);
		pBar.setMaximum(max);

		if (parentComponent != null) {
			setLocationRelativeTo(parentComponent);
			// TODO....close、focus
		} else {
			setLocationRelativeTo(null);
		}
		pack();
		setVisible(true);
	}

	private void CreatDialog() {
		Container contentPanel = this.getContentPane();
		setSize(new Dimension(WIDTH, LENGTH));
		setLayout(new OneColumnLayout(new Insets(0, 2, 2, 2), 0));
		setResizable(false);

		pBar = new JProgressBar(min, max);
		pBar.setStringPainted(true);
		pBar.setBorderPainted(true);

		label = new JLabel();
		panel = new JPanel();
		button = new JButton();

		setTitle(titleName);
		//label.setText(labelText);
		button.setText(buttonText);
		label.setPreferredSize(new Dimension(580, 40));
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		panel.add(button);
		contentPanel.add(label);
		contentPanel.add(pBar);
		contentPanel.add(panel);

		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// if(ProgressMonitorDialog.this!=null)
				close();
			}

		});

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				close();
			}
		});
	}

	public void setMinimum(int min) {
		this.min = min;
		if (pBar != null)
			pBar.setMinimum(min);
	}

	public void setMaximum(int max) {
		this.max = max;
		if (pBar != null)
			pBar.setMaximum(max);
	}

	public void setModel(BoundedRangeModel newModel) {
		if (pBar != null)
			pBar.setModel(newModel);
		this.repaint();
	}

	public void updateUI() {
		if (pBar != null)
			pBar.updateUI();
		this.repaint();
	}

	public void setMessage(String s) {
		//this.labelText = s;
		if (label != null)
			label.setText(s);
	}

	public void setNote(String s) {
		//this.barText = s;
		if (pBar != null)
			pBar.setString(s);
	}

	public void setValue(int v) {
		if (pBar != null) {
			if (v >= min && v <= max) {
			} else if (v < min)
				v = min;
			else if (v > max)
				v = max;

			pBar.setValue(v);
		}
	}

	public void addProgressValue(int del) {
		if (pBar != null) {
			int v = pBar.getValue() + del;
			if (v > pBar.getMaximum())
				v = pBar.getMaximum();
			pBar.setValue(v);
		}
	}

	public void setValue_Note(int v) {
		if (pBar != null) {
			if (v >= min && v <= max) {
			} else if (v < min)
				v = min;
			else if (v > max)
				v = max;

			pBar.setValue(v);

			setNote(v + "%");
		}
	}

	public void addProgressValue_Note(int del) {
		if (pBar != null) {
			int v = pBar.getValue() + del;
			if (v > pBar.getMaximum())
				v = pBar.getMaximum();
			pBar.setValue(v);

			setNote(v + "%");
		}
	}

	public void setButtonText(String buttonText) {
		this.buttonText = buttonText;
		button.setText(buttonText);
	}

	// 以此 ，可获取JProgressBar的所有方法。
	public JProgressBar getProgressBar() {
		return pBar;
	}

	// 可考虑增删其他组件的方法，如按钮。
	/**
	 * 
	 * 
	 */

	public void close() {
		dispose();
		pBar = null;
	}

	public boolean isCanceled() {
		return pBar == null;
	}

	public static void main(String args[]) {
		ProgressMonitorDialog p = new ProgressMonitorDialog(
				"ProgressMonitorDialog");
		p.setSize(400, 150);
		p.setButtonText("CANCEL");

		p.setMessage("Checking...");
		p.setNote("Contrasting Version.......");
		p.setMinimum(1);
		p.setMaximum(100);
		for (int i = 0; i < 100; i++) {
			if (i == 50)
				p.setNote("Contrast is done，download now?");
			if (i == 90)
				p.setNote("Download list has built,Contrast is closing...");

			if (p.getProgressBar() != null)
				p.setValue(i);
			if (p.isCanceled()) {
				System.out.println("iscanceled " + i);
				break;
			}
			try {
				Thread.sleep(50);
				// System.out.println(p.getProgressBar().getValue());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
