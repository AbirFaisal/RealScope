package com.owon.uppersoft.vds.ui.widget;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;

public class IPTextKeyListener extends KeyAdapter {
	private final JTextField[] jtf;

	public IPTextKeyListener(JTextField[] jtf) {
		this.jtf = jtf;
	}

	public void previousFocus(JTextField text) {
		JTextField[] text_ = jtf;
		if (text == text_[0]) {
			return;
		} else if (text == text_[1]) {
			text_[0].requestFocus();
			toLast(text_[0]);
		} else if (text == text_[2]) {
			text_[1].requestFocus();
			toLast(text_[1]);
		} else if (text == text_[3]) {
			text_[2].requestFocus();
			toLast(text_[2]);
		}
	}

	public void toLast(JTextField text) {
		int len = text.getText().length();
		text.setCaretPosition(len);
	}

	public void nextFocus(JTextField text) {
		JTextField[] text_ = jtf;
		if (text == text_[3]) {
			return;
		} else if (text == text_[0]) {
			text_[1].requestFocus();
			toFirst(text_[1]);
		} else if (text == text_[1]) {
			text_[2].requestFocus();
			toFirst(text_[2]);
		} else if (text == text_[2]) {
			text_[3].requestFocus();
			toFirst(text_[3]);
		}
	}

	public void toFirst(JTextField text) {
		text.setCaretPosition(0);
	}

	public void keyPressed(KeyEvent e) {
		JTextField text = (JTextField) e.getSource();
		int position = text.getCaretPosition();
		int tlen = text.getText().length();
		// System.out.println("key:"+e.getKeyCode());

		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
			previousFocus(text);
			break;
		case KeyEvent.VK_LEFT:
			if (position == 0) {
				previousFocus(text);
			}
			break;
		case KeyEvent.VK_DOWN:
			nextFocus(text);
			break;
		case KeyEvent.VK_RIGHT:
			if (tlen == position) {
				nextFocus(text);
			}
			break;
		case KeyEvent.VK_BACK_SPACE:
			if (position == 0) {
				previousFocus(text);
			}
			break;
		case KeyEvent.VK_DELETE:
			if (position == text.getText().length()) {
				nextFocus(text);
			}
			break;
		case KeyEvent.VK_PERIOD:
		case KeyEvent.VK_DECIMAL:
			// if (position == text.getText().length()) {
			nextFocus(text);
			// }
			break;
		}
	}
}