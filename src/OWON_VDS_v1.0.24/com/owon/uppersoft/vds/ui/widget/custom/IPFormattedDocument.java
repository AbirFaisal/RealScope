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
		int v = 0;// First ensure the result after inserting the string is an integer; if not, do not perform the insertion
		try {
			v = Integer.parseInt(nstr);
		} catch (NumberFormatException e) {
			return false;
		}

		// If the document becomes too long after inserting the string str, the insertion fails
		if (nstr.length() > 3) {
			k.nextFocus(JTF);
			// else if((strAfterInsert.length() >3))
			return false;
		} else if (v > 255) {// If the data is out of range after insertion, the insertion fails and a warning pops up
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

	/** There are also two other cases, remove and replace, but their methods are not overridden because their use in the control involves other complex logic */
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