package com.owon.uppersoft.vds.device.interpret;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.owon.uppersoft.dso.source.comm.effect.SubmitorFactory;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.vds.core.comm.BufferredSourceManager;
import com.owon.uppersoft.vds.core.comm.effect.JobUnitDealer;
import com.owon.uppersoft.vds.core.comm.job.JobUnit;
import com.owon.uppersoft.vds.device.interpret.util.DefaultCMDResponser;
import com.owon.uppersoft.vds.device.interpret.util.Sendable;
import com.owon.uppersoft.vds.source.comm.Submitor2;
import com.owon.uppersoft.vds.tool.simple.UsbCommunicator;
import com.owon.uppersoft.vds.ui.layout.OneColumnLayout;
import com.owon.vds.tiny.firm.DeviceFlashCommunicator;
import com.owon.vds.tiny.firm.pref.DefaultPrefControl;
import com.owon.vds.tiny.firm.pref.PrefSync;
import com.owon.vds.tiny.tune.TinyTuneFunction;

public class Tune2 implements PropertyChangeListener {
	public static final String APPEND_TXTLINE2 = "APPEND_TXTLINE2";

	private TinyTuneFunction tf;

	public Tune2(Window wnd, final PropertyChangeSupport pcs,
			TinyTuneFunction tf) {
		this.tf = tf;
		JDialog dlg = new JDialog(wnd);
		dlg.getContentPane().setLayout(new BorderLayout());

		final JPanel southpanel = new JPanel();
		southpanel.setLayout(new BorderLayout());
		dlg.add(southpanel, BorderLayout.CENTER);

		CreateSouthPane(southpanel);

		final JPanel northpanel = new JPanel();
		dlg.add(northpanel, BorderLayout.NORTH);
		northpanel.setLayout(new OneColumnLayout());

		CreateAddrPanel(northpanel);
		CreateHexPanel(northpanel);

		dlg.pack();
		dlg.setSize(1300, 600);
		dlg.setLocationRelativeTo(null);
		dlg.setVisible(true);

		dlg.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				pcs.removePropertyChangeListener(Tune2.this);
			}
		});
		pcs.addPropertyChangeListener(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String pn = evt.getPropertyName();
		// System.out.println(pn);
		if (pn.equals(PropertiesItem.APPEND_TXTLINE)) {
			Object o = evt.getNewValue();
			addTextLine(o);
		} else if (pn.equals(PropertiesItem.APPEND_TXT)) {
			Object o = evt.getNewValue();
			addText(o);
		} else if (pn.equals(APPEND_TXTLINE2)) {
			Object o = evt.getNewValue();
			trglbl.setText(o.toString());
		}
	}

	private JTextArea consoleArea;

	private void CreateSouthPane(JPanel southpanel) {
		final JScrollPane scrollPane2 = new JScrollPane();
		scrollPane2.setPreferredSize(new Dimension(800, 200));
		scrollPane2.setBorder(new CompoundBorder(
				new EmptyBorder(0, 10, 10, 10), new LineBorder(Color.black, 1,
						false)));

		consoleArea = new JTextArea();
		consoleArea.setLineWrap(true);
		consoleArea.setWrapStyleWord(true);
		scrollPane2.setViewportView(consoleArea);

		southpanel.add(scrollPane2, BorderLayout.CENTER);
	}

	private void CreateAddrPanel(JPanel northpanel) {
		JPanel addrpanel = new JPanel();
		addrpanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		northpanel.add(addrpanel, BorderLayout.SOUTH);

		JLabel lblOx = new JLabel("addr 0x");
		addrpanel.add(lblOx);

		final JTextField tf_add = new JTextField();
		addrpanel.add(tf_add);
		tf_add.setColumns(6);

		JLabel label = new JLabel("bytes 0x");
		addrpanel.add(label);

		final JTextField tf_bytes = new JTextField();
		addrpanel.add(tf_bytes);
		tf_bytes.setColumns(6);

		JLabel label_1 = new JLabel("value 0x");
		addrpanel.add(label_1);

		final JTextField tf_value = new JTextField();
		addrpanel.add(tf_value);
		tf_value.setColumns(6);

		// JButton btnAdd = new JButton("add");
		// btnAdd.addActionListener(new ActionListener() {
		// public void actionPerformed(ActionEvent e) {
		// um.setNdrawTrglevel(tf_add.getText(), tf_bytes
		// .getText(), tf_value.getText());
		// ucc.add2CMDList(tf_add, tf_bytes, tf_value);
		// setInAddcmd(true);}});
		// addrpanel.add(btnAdd);

		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JobUnitDealer jud = getJobUnitDealer();
				Sendable sd = new Sendable(jud);

				int add = getInt(tf_add);
				int bytes = getInt(tf_bytes);
				int value = getInt(tf_value);

				sd.sendCMD(add, bytes, value, null);

				addTextLine("[Send: ]" + Integer.toHexString(add) + ", "
						+ Integer.toHexString(bytes) + ", "
						+ Integer.toHexString(value));
			}

		});
		addrpanel.add(btnSend);

		JButton btnReload = new JButton("Reload txt");
		btnReload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/** 从txt中载入原型设置，使用默认配置覆盖当前设置 */
				tf.createTuneTexter().resetup();
			}

		});
		addrpanel.add(btnReload);
		JButton btnClear = new JButton("Clear");
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				consoleArea.setText(null);
			}

		});
		addrpanel.add(btnClear);

		final JButton usbComm = new JButton("t");
		usbComm.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (um == null || !um.getFrame().isDisplayable()) {
					// USBSourceManager usm = cm.sourceManager
					// .getUSBSourceManager();
					um = new UsbCommunicator();
				}
			}

		});
		addrpanel.add(usbComm);

		// final JCheckBox jcb = new JCheckBox("before fpga down");
		// addrpanel.add(jcb);
		final JButton rf = new JButton("read flash");
		rf.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JobUnitDealer sbm = getJobUnitDealer();

				sbm.addJobUnit(new JobUnit() {
					@Override
					public void doJob(BufferredSourceManager sm) {
						DeviceFlashCommunicator dflash = new DeviceFlashCommunicator();
						byte[] devPref = dflash.fetchPrefernce(sm, false);
						DefaultPrefControl pc = tf.getDefaultPrefControl();
						pc.loadSyncImageFromDevice(devPref);
					}

					@Override
					public String getName() {
						return "fetch flash";
					}

					@Override
					public boolean merge(JobUnit ju) {
						return false;
					}

				});

			}

		});
		addrpanel.add(rf);

		final JLabel jl = new JLabel("n: 0x");
		addrpanel.add(jl);

		final JTextField jl2 = new JTextField("0");
		addrpanel.add(jl2);
		jl2.setColumns(4);

		String[] strs = new String[] { "all n", "i*60", "(0~59)...", "random" };
		final JComboBox jcb = new JComboBox(strs);
		addrpanel.add(jcb);

		final JButton wf = new JButton("write flash");
		wf.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JobUnitDealer sbm = getJobUnitDealer();

				int type = jcb.getSelectedIndex();
				String t = jl2.getText();
				int v = 0;
				try {
					v = Integer.parseInt(t, 16);
				} catch (NumberFormatException e1) {
					e1.printStackTrace();
				}

				byte[] devpref = prepareDevPref(type, v);
				new PrefSync().sendPrefernce_Write(devpref, sbm);
			}

		});
		addrpanel.add(wf);

	}

	public JobUnitDealer getJobUnitDealer() {
		return tf.getTinyTuneDelegate().getJobUnitDealer();
	}

	public static byte[] prepareDevPref(int type, int lvalue) {
		int len = DeviceFlashCommunicator.FLASH_SIZE;
		byte[] devPref = new byte[len];
		switch (type) {
		case 3: {
			Random r = new Random();
			for (int i = 0, j = 0; i < len; i++, j++) {
				devPref[i] = (byte) r.nextInt(256);
			}
			break;
		}
		case 2: {
			for (int i = 0, j = 0; i < len; i++, j++) {
				devPref[i] = (byte) (i % 60);
			}
			break;
		}
		case 1: {
			for (int i = 0, j = 0; i < len; i++, j++) {
				devPref[i] = (byte) (i / 60);
			}
			break;
		}
		case 0: {
			// Random r = new Random();
			for (int i = 0, j = 0; i < len; i++, j++) {
				devPref[i] = (byte) (lvalue);// r.nextInt(256);
			}
			break;
		}
		}
		return devPref;
	}

	private UsbCommunicator um;
	private JLabel trglbl;

	public int getInt(JTextField tf) {
		return Integer.parseInt(tf.getText(), 16);
	}

	private void CreateHexPanel(JPanel northpanel) {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEADING));

		northpanel.add(panel, BorderLayout.SOUTH);

		JLabel lblfo = new JLabel(" HEX byte 0x");
		panel.add(lblfo);
		final JTextField tf_byte = new JTextField();
		tf_byte.setColumns(5);
		panel.add(tf_byte);

		JLabel lblint = new JLabel(" HEX Int ");
		panel.add(lblint);
		final JTextField tf_Int = new JTextField();
		tf_Int.setColumns(6);
		panel.add(tf_Int);

		JButton btnSend2 = new JButton("Send");
		btnSend2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JobUnitDealer jud = getJobUnitDealer();
				Sendable sd = new Sendable(jud);
				int byt = getInt(tf_byte);
				int v = getInt(tf_Int);
				sd.sendCMD2(byt, v, new DefaultCMDResponser());
				addTextLine("[Send: ]" + Integer.toHexString(byt) + ", "
						+ Integer.toHexString(v));
			}
		});
		panel.add(btnSend2);

		trglbl = new JLabel();
		panel.add(trglbl);
		trglbl.setPreferredSize(new Dimension(300, 30));
	}

	private void addText(Object t) {
		consoleArea.append(t.toString());
		consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
	}

	private void addTextLine(Object t) {
		consoleArea.append(t.toString());
		consoleArea.append("\r\n");
		consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
	}
}
