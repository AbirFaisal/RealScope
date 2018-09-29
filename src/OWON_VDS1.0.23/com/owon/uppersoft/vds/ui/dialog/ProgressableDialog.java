package com.owon.uppersoft.vds.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.owon.uppersoft.vds.ui.resource.FontCenter;

public class ProgressableDialog extends JDialog implements ProgressObserver {

	private JProgressBar jpb;
	private String title, canceltxt, cancelPrompt;
	private ProgressExecutor runner;

	public ProgressableDialog(Window owner, boolean modal,
			final ProgressExecutor runner, String title, String canceltxt,
			String cancelPrompt) {
		super(owner, modal ? DEFAULT_MODALITY_TYPE : ModalityType.MODELESS);

		this.runner = runner;
		this.title = title;
		this.canceltxt = canceltxt;
		this.cancelPrompt = cancelPrompt;

		setSize(500, 99);
		setLocationRelativeTo(owner);
		jpb = CreateProgressBar();

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				runner.execute(ProgressableDialog.this);
			}
		});
	}

	@Override
	public Window getWindow() {
		return this;
	}

	@Override
	public void setMaximum(int max) {
		jpb.setMaximum(max);
	}

	@Override
	public void setValue(int v) {
		jpb.setValue(v);
	}

	@Override
	public void shutdown() {
		dispose();
	}

	@Override
	public void increaseValue(int del) {
		setValue(jpb.getValue() + del);
	}

	public JProgressBar CreateProgressBar() {
		JPanel base = new JPanel();
		base.setLayout(new BorderLayout());
		base.setBackground(Color.darkGray);
		setLayout(new BorderLayout());
		setUndecorated(true);
		final JProgressBar jpb = new JProgressBar();
		JLabel lbl = new JLabel(title);
		lbl.setForeground(Color.lightGray);
		lbl.setFont(FontCenter.getTitleFont());
		add(base, BorderLayout.CENTER);

		JPanel btnPane = new JPanel();
		btnPane.setLayout(new FlowLayout(FlowLayout.TRAILING));
		btnPane.setOpaque(false);
		JButton cancelbtn = new JButton(canceltxt);
		// cancel.setBackground(Color.lightGray);
		btnPane.add(cancelbtn);

		cancelbtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int re = JOptionPane.showConfirmDialog(ProgressableDialog.this,
						cancelPrompt, null, JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);

				boolean cancel = (re == JOptionPane.YES_OPTION);
				if (cancel)
					runner.cancel(new Runnable() {
						@Override
						public void run() {
							dispose();
						}
					});
			}
		});

		base.add(lbl, BorderLayout.NORTH);
		base.add(jpb, BorderLayout.CENTER);
		base.add(btnPane, BorderLayout.SOUTH);

		jpb.setPreferredSize(new Dimension(400, 90));

		return jpb;
	}

	public static void main(String[] args) {
		ProgressableDialog scd = new ProgressableDialog(null, true,
				new ProgressExecutor() {

					@Override
					public void cancel(Runnable afterCancel) {
					}

					@Override
					public void execute(final ProgressObserver po) {
						new Thread() {
							@Override
							public void run() {
								int i = 0;
								po.setMaximum(5);
								try {
									while (i < 5) {
										i++;
										Thread.sleep(1000);
										po.setValue(i);
									}
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								po.shutdown();
							}
						}.start();
					}
				}, "...", "cancel", "are you sure?");
		scd.setVisible(true);

	}
}