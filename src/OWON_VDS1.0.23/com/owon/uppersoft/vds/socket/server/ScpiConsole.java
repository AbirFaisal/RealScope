package com.owon.uppersoft.vds.socket.server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.util.LocalizeCenter;

public class ScpiConsole implements Localizable {

	private static ScpiConsole instance = null;

	public static ScpiConsole getInstance() {
		if (instance == null) {
			instance = new ScpiConsole();
		} else if (instance.checkInvisible()) {
			instance = null;
			instance = new ScpiConsole();
		} else {
			instance.toFront();
		}
		return instance;
	}

	public JFrame console;
	private JPanel btnp, viewp;
	private JTextArea consoleArea;
	private boolean writable = true, readable = true, scrollable;
	
	JCheckBox rcb;
	JCheckBox wcb;
	JCheckBox scroll;
	JButton cl;
	JLabel portlb;
	JButton portbt;

	private ScpiConsole() {
		console = new JFrame();
		console.setSize(800, 600);

		JPanel cp = new JPanel();
		cp.setLayout(new BorderLayout());
		console.setContentPane(cp);
		initBtnPane(cp);
		initConsole(cp);

		console.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		console.setLocationRelativeTo(null);
		console.setVisible(true);

		LocalizeCenter lc = Platform.getControlManager().getLocalizeCenter();
		lc.addLocalizable(this);
		localize(I18nProvider.bundle());
	}



	private void initBtnPane(JPanel cp) {
		btnp = new JPanel();
		btnp.setLayout(new FlowLayout());
		btnp.setPreferredSize(new Dimension(console.getWidth(), 50));
		cp.add(btnp, BorderLayout.NORTH);

		rcb = new JCheckBox("Server Read");
		btnp.add(rcb);
		rcb.setSelected(readable);
		rcb.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				readable = rcb.isSelected();
			}
		});

		wcb = new JCheckBox("Server Write");
		btnp.add(wcb);
		wcb.setSelected(writable);
		wcb.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				writable = wcb.isSelected();
			}
		});

		scroll = new JCheckBox("scroll");
		btnp.add(scroll);
		scroll.setSelected(scrollable);
		scroll.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				scrollable = scroll.isSelected();
			}
		});

		cl = new JButton("Clear Console");
		btnp.add(cl);
		cl.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				clearTextArea();
			}
		});
		portlb = new JLabel("Port:");
		btnp.add(portlb);

		final ServerControl sctl = Platform.getControlManager().scpiServer;
		final JTextField porttf = new JTextField();
		porttf.setText(String.valueOf(sctl.getPort()));
		porttf.setColumns(5);
		btnp.add(porttf);

		portbt = new JButton("Set");
		btnp.add(portbt);
		portbt.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				btnp.setEnabled(false);

				int p = -1;
				try {
					p = Integer.parseInt(porttf.getText());
				} catch (Exception e2) {
					porttf.setText(String.valueOf(sctl.getPort()));
					updateText("Set port failed");
					return;
				}

				boolean b = sctl.setPort(p);
				if (b) {
					updateText("Set port  " + sctl.getPort() + "  success");
				} else {
					porttf.setText(String.valueOf(sctl.getPort()));
					updateText("Set port failed");
				}

				btnp.setEnabled(true);
			}
		});

	}

	private void initConsole(JPanel cp) {

		viewp = new JPanel();
		viewp.setLayout(new BorderLayout());

		JScrollPane scrollviewp = new JScrollPane();
		scrollviewp.setBorder(new CompoundBorder(
				new EmptyBorder(0, 10, 10, 10), new LineBorder(Color.black, 1,
						false)));

		consoleArea = new JTextArea();
		consoleArea.setLineWrap(true);
		consoleArea.setWrapStyleWord(true);
		scrollviewp.setViewportView(consoleArea);
		viewp.add(scrollviewp, BorderLayout.CENTER);
		cp.add(viewp, BorderLayout.CENTER);

	}

	/** 控制 */

	public boolean checkInvisible() {
		return !console.isDisplayable();
	}

	public void toFront() {
		if (console != null && console.isDisplayable())
			console.toFront();
	}

	// private Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
	// private Font font2 = new Font(Font.SANS_SERIF, Font.ITALIC, 17);

	public void addReadText(String txt) {
		if (readable) {
			updateText(txt);
		}

	}

	public void addWriteText(String txt) {
		if (writable) {
			updateText("/..   " + txt);
		}
	}

	private void updateText(String txt) {
		consoleArea.append(txt + "\r\n");
		if (scrollable)
			consoleArea.setCaretPosition(consoleArea.getText().length());
	}

	protected void clearTextArea() {
		consoleArea.setText(null);
	}

	/** 模型 */

	public static void main(String[] args) {
		new ScpiConsole();
	}

	@Override
	public void localize(ResourceBundle rb) {
		rcb.setText(rb.getString("ServerConsole.Readable"));
		wcb.setText(rb.getString("ServerConsole.Writable"));
		scroll.setText(rb.getString("ServerConsole.Scroll"));
		cl.setText(rb.getString("ServerConsole.Clear"));
		portlb.setText(rb.getString("M.Utility.MachineNet.Port") + ':');
		portbt.setText(rb.getString("Action.OK"));
	}
}
