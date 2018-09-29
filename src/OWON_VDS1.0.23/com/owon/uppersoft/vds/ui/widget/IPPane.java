package com.owon.uppersoft.vds.ui.widget;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.ui.layout.OneRowLayout;
import com.owon.uppersoft.vds.ui.widget.custom.CLabel;
import com.owon.uppersoft.vds.ui.widget.custom.IPFormattedDocument;

public class IPPane extends JPanel implements Localizable {
	public static final int TextNum = 4;
	private CLabel clb;
	private JTextField jtfs[];

	public IPPane(ResourceBundle rb) {
		setOpaque(false);
		setLayout(new OneRowLayout(new Insets(3, 3, 3, 3), 0));
		clb = new CLabel();

		add(clb);
		clb.setPreferredSize(new Dimension(90, 0));
		int len = TextNum;
		jtfs = new JTextField[len];
		IPTextKeyListener k = new IPTextKeyListener(jtfs);
		for (int i = 0; i < len; i++) {
			JTextField jt = jtfs[i] = new JTextField(2);
			jt.setOpaque(true);
			jt.setBorder(null);
			jt.setPreferredSize(new Dimension(25, 30));
			final IPFormattedDocument ipf = new IPFormattedDocument(jt, k, rb);
			jt.setDocument(ipf);
			jt.setHorizontalAlignment(JTextField.RIGHT);

			add(jt);
			jt.addKeyListener(k);
			if (i == len - 1)
				continue;
			JLabel lb = new JLabel(".");
			lb.setBorder(null);
			lb.setBackground(Color.WHITE);
			lb.setOpaque(true);
			lb.setPreferredSize(new Dimension(2, 30));
			add(lb);
		}

	}

	public void setLabelName(String labname) {
		clb.setName(labname);
	}

	public void setIP(byte[] ip) {
		for (int i = 0; i < jtfs.length; i++) {
			JTextField jt = jtfs[i];
			jt.setText(String.valueOf(ip[i] & 0xff));
		}
	}

	public void saveIP2Array(byte[] ip) {
		JTextField[] iptfs = jtfs;
		// ipa = PrimaryTypeUtil.getAddress(iptfs);
		int pt = 0;
		int len = iptfs.length;
		for (int i = 0; i < len; i++) {
			try {
				pt = Integer.parseInt(iptfs[i].getText());
			} catch (Exception ex) {
			}
			pt = pt & 0xff;
			iptfs[i].setText(String.valueOf(pt));
			ip[i] = (byte) pt;
			// System.out.print((address[i] & 0xff) + ".");
		}
	}

	@Override
	public void localize(ResourceBundle rb) {
		clb.localize(rb);
	}
}