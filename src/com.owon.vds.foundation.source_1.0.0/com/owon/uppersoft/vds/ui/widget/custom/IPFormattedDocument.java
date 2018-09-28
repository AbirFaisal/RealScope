package com.owon.uppersoft.vds.ui.widget.custom;

import java.util.ResourceBundle;

import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import com.owon.uppersoft.vds.ui.widget.IPTextKeyListener;

public class IPFormattedDocument extends PlainDocument {
	private JTextField JTF;
	private IPTextKeyListener k;
	private ResourceBundle rb;

	public IPFormattedDocument(JTextField tmpJTF, IPTextKeyListener listener,
			ResourceBundle rb) {
		JTF = tmpJTF;
		k = listener;
		this.rb = rb;
	}

	private boolean check(String nstr) {
		int v = 0;// 首先保证插入该字符串后，是整数；如果不是，则不进行插入操作
		try {
			v = Integer.parseInt(nstr);
		} catch (NumberFormatException e) {
			return false;
		}

		// 如果插入字符串str后，文档超长，则插入失败
		if (nstr.length() > 3) {
			k.nextFocus(JTF);
			// else if((strAfterInsert.length() >3))
			return false;
		} else if (v > 255) {// 如果插入后数据超出范围，插入失败，弹出警告
			JOptionPane.showConfirmDialog(null, v + " "
					+ rb.getString("Error.OutOfRangeIP"), rb
					.getString("M.Utility.MachineNet.TipsTitle"),
					JOptionPane.CLOSED_OPTION);
			JTF.setText("255");
			JTF.setCaretPosition(3);
			return false;
		} else {
			return true;
		}
	}

	/** 另外还有两种情况，即remove或replace，但是不重写他们的方法，因为他们在控件中的使用涉及其它复杂的逻辑 */
	@Override
	public void insertString(int offset, String str, AttributeSet a)
			throws BadLocationException {
		String pstr = getText(0, getLength());
		String nstr = pstr.substring(0, offset) + str + pstr.substring(offset);

		if (check(nstr)) {
			super.insertString(offset, str, a);
		}

	}
}