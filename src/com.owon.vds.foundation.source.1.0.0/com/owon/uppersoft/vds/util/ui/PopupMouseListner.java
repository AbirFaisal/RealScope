package com.owon.uppersoft.vds.util.ui;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * PopupMouseListner，鼠标事件模拟窗口图标的行为
 * 
 * @author Matt
 * 
 */
public class PopupMouseListner implements MouseListener {
	private Robot robot;
	private Runnable r;

	public PopupMouseListner(Runnable r) {
		this.r = r;
	}

	@Override
	public void mouseReleased(MouseEvent e) { // popup menu.
		if (robot == null) {
			try {
				robot = new Robot();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		// send a &quot;ALT+SPACE&quot; keystroke to popup window
		// menu.
		robot.keyPress(KeyEvent.VK_ALT);
		robot.keyPress(KeyEvent.VK_SPACE);
		robot.keyRelease(KeyEvent.VK_ALT);
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			r.run();
		}
	}
}