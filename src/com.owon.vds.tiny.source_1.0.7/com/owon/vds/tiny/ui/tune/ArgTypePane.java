package com.owon.vds.tiny.ui.tune;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.owon.uppersoft.vds.core.tune.IntVolt;
import com.owon.uppersoft.vds.ui.layout.OneColumnLayout;
import com.owon.uppersoft.vds.util.ui.ComboboxMouseWheelSupport;
import com.owon.vds.tiny.firm.pref.model.ArgType;
import com.owon.vds.tiny.tune.TinyTuneDelegate;
import com.owon.vds.tiny.tune.detail.DefaultCalArgType;
import com.owon.vds.tiny.ui.tune.control.CalibrationArgType;
import com.owon.vds.tiny.ui.tune.widget.Listenable;

public class ArgTypePane extends JPanel implements CalibrationArgType,
		Listenable {
	protected DefaultCalArgType cmdt;
	protected TinyTuneDelegate ttd;

	public ArgTypePane(DefaultCalArgType cmdt, TinyTuneDelegate ttd) {
		this.cmdt = cmdt;
		this.ttd = ttd;
	}

	protected void setListening(boolean listening) {
		this.listening = listening;
	}

	public boolean isListening() {
		return listening;
	}

	private JComboBox vbcbb;

	public void contentUpdate() {
		listening = false;
		refreshContentWhileSuspend();
		listening = true;
	}

	@Override
	public void onVoltbaseChange(int channel, int idx) {
	}

	/** 由外部调用的方法，在内部，附加内容，并转移到其它方法由子类实现 */
	public void create(Container base, IntVolt[] volts) {
		int channelNumber = cmdt.channelNumber;
		spinners = new CompositeSpin[channelNumber];
		base.setLayout(new BorderLayout());

		JPanel vbp = new JPanel();
		vbp.setLayout(new OneColumnLayout(new Insets(35, 20, 10, 20), 2));
		vbcbb = createVBCBBForChannel(vbp, channelNumber, volts);
		base.add(vbp, BorderLayout.WEST);

		JPanel spinp = new JPanel();
		spinp.setLayout(new FlowLayout(20, 20, 10));
		base.add(spinp, BorderLayout.CENTER);
		for (int ch = 0; ch < channelNumber; ch++) {
			JPanel chlPane = new JPanel();
			// chlPane.setPreferredSize(new Dimension(450, 40));
			FlowLayout f = new FlowLayout(FlowLayout.TRAILING);
			chlPane.setLayout(f);

			JLabel chlcbb0 = new JLabel("CH" + (ch + 1) + ':');
			chlcbb0.setPreferredSize(new Dimension(50, 28));
			chlPane.add(chlcbb0);
			spinp.add(chlPane);

			// vbcbbs[ch] = createVBCBBForChannel(chlPane, ch);s[ch]
			spinners[ch] = createESpin(chlPane, ch, vbcbb);
		}

	}

	protected JComboBox createVBCBBForChannel(Container base,
			final int channelNumber, IntVolt[] volts) {
		final JComboBox vbcbb = new JComboBox(volts);
		int initIdx = 0;// td.getVBIndexForChannel(chl);
		vbcbb.setSelectedIndex(initIdx);
		vbcbb.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!isListening())
					return;
				if (e.getStateChange() == ItemEvent.SELECTED) {
					for (int ch = 0; ch < channelNumber; ch++) {
						vbchangeForChannel(vbcbb, ch);
					}
				}
			}
		});
		vbcbb.setMaximumRowCount(20);
		new ComboboxMouseWheelSupport(vbcbb);
		base.add(vbcbb);
		return vbcbb;
	}

	public void refreshContentWhileSuspend() {
		/** 未创建时无行为 */
		if (spinners == null)
			return;
		int channelNumber = cmdt.channelNumber;
		for (int i = 0; i < channelNumber; i++) {
			int v0 = cmdt.getArgs()[i][vbcbb.getSelectedIndex()];
			spinners[i].setValue(v0);
		}
		// System.out.println("update:" + spinner0.getValue());
	}

	public void sync2Device() {
		int channelNumber = cmdt.channelNumber;
		int vb = vbcbb.getSelectedIndex();
		for (int i = 0; i < channelNumber; i++) {
			int v = spinners[i].getValue();
			cmdt.getArgs()[i][vb] = v;
			// System.out.println("store:" + v);
			ttd.argChange(cmdt.getId(), i, vb);
		}
	}

	protected CompositeSpin[] spinners;

	protected CompositeSpin createESpin(Container base, final int chl,
			final JComboBox vbccb) {
		final int id = cmdt.getId();

		final CompositeSpin es = new CompositeSpin(this) {
			@Override
			public void spinValueChange() {
				int vb = vbccb.getSelectedIndex();
				int v = getValue();
				cmdt.getArgs()[chl][vb] = v;

				String msg = ArgType.VALUES[id] + "args[" + chl + "][" + vb
						+ "] = " + v;
				System.out.println(msg);

				ttd.argChange(id, chl, vb);
			}
		};
		base.add(es);
		return es;
	}

	protected void vbchangeForChannel(JComboBox vbcbb, int chl) {
		int vb = vbcbb.getSelectedIndex();
		setListening(false);
		// System.out.println(vbcbb.getSelectedItem().toString()+","+
		// chlcbb.getSelectedItem().toString());
		refreshContentWhileSuspend();
		// spinstore();// 每次改动都应用一次
		if (vb >= 0) {
			ttd.vbChangeForChannel(chl, vb);
		}
		setListening(true);
	}

	@Override
	public void createContent(IntVolt[] volts) {
		create(this, volts);
		listening = true;
	}

	private boolean listening = false;

}
