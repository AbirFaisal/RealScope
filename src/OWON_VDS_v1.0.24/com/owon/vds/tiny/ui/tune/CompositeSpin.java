package com.owon.vds.tiny.ui.tune;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.owon.uppersoft.vds.ui.layout.OneRowLayout;
import com.owon.vds.tiny.ui.tune.widget.Listenable;

public class CompositeSpin extends JPanel {
	public static final int NUM = 5, SEP = 3;
	private JSpinner[] spins;

	public static void main(String[] args) {
		CompositeSpin es = new CompositeSpin(new Listenable() {
			@Override
			public boolean isListening() {
				return true;
			}
		}) {
			@Override
			public void spinValueChange() {
			}
		};

		es.setValue(99);

		JFrame jf = new JFrame();
		jf.setBounds(100, 100, 500, 90);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.add(es);
		jf.setVisible(true);
	}

	protected Listenable aac;

	public void spinValueChange() {
	}

	public CompositeSpin(final Listenable aac) {
		this.aac = aac;
		setLayout(new OneRowLayout(20));
		spins = new JSpinner[NUM];

		JSpinner last = null;
		for (int i = 0; i < NUM; i++) {
			if (i == NUM - SEP) {
				JLabel sep = new JLabel(",");
				sep.setPreferredSize(new Dimension(5, 28));
				add(sep);
			}
			final SpinnerNumberModel snm = new SpinnerNumberModel(0, -1, 10, 1);
			final JSpinner csp = new JSpinner(snm);
			csp.setPreferredSize(new Dimension(50, 28));
			add(csp);
			spins[i] = csp;

			final JSpinner pre = last;
			last = csp;

			csp.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					if (!aac.isListening())
						return;
					// DBG.errprintln("csp: "+j);
				}
			});
			snm.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					// IndexSelectable aact = (IndexSelectable) aac;
					// aact.setSelectIndex(spinidx);

					Number n = (Number) csp.getValue();
					// DBG.errprintln(j + ": " + n);
					if (n.intValue() == 10) {
						turnoffInternal();
						csp.setValue(0);
						if (pre != null)
							pre.setValue(pre.getNextValue());
						turnonInternal();

						// DBG.errprintln("10");
						if (aac.isListening())
							spinValueChange();// 每次改动都应用一次
						return;
					}
					if (n.intValue() == -1) {
						turnoffInternal();
						csp.setValue(9);
						if (pre != null)
							pre.setValue(pre.getPreviousValue());
						turnonInternal();

						if (aac.isListening())
							spinValueChange();// 每次改动都应用一次
						return;
					}

					if (!internalListening)
						return;
					// DBG.errprintln(j);
					if (aac.isListening())
						spinValueChange();// 每次改动都应用一次
				}
			});

		}
		turnonInternal();
	}

	private boolean internalListening = false;

	private void turnonInternal() {
		internalListening = true;
		// DBG.errprintln("on");
	}

	private void turnoffInternal() {
		internalListening = false;
		// DBG.errprintln("off");
	}

	public int getValue() {
		int v = 0;
		for (int i = 0; i < NUM; i++) {
			JSpinner csp = spins[i];
			v = v * 10 + (Integer) csp.getValue();
		}
		return v;
	}

	public void setValue(int v) {
		int r = 0;
		turnoffInternal();

		for (int i = NUM - 1; i >= 0; i--) {
			JSpinner csp = spins[i];
			r = v % 10;
			csp.setValue(r);
			v = v / 10;
		}

		turnonInternal();
		// td.apply();
	}
}
