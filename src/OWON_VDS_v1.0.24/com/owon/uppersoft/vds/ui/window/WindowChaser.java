package com.owon.uppersoft.vds.ui.window;

import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Follows the parent window's movement; only for two arguments that are a Window and a Dialog, where the Dialog minimizes when the Window minimizes
 * 
 */
public class WindowChaser extends ComponentAdapter {
	private Window parent;
	private Point wndLoc;
	private Point dlgLoc;
	private Window chaser;

	public WindowChaser(Window par, Window cha) {
		this.parent = par;
		this.chaser = cha;
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		if (wndLoc == null) {
			wndLoc = parent.getLocation();
			dlgLoc = new Point();
			return;
		}
		int x = wndLoc.x, y = wndLoc.y;
		parent.getLocation(wndLoc);
		chaser.getLocation(dlgLoc);
		dlgLoc.x += wndLoc.x - x;
		dlgLoc.y += wndLoc.y - y;
		chaser.setLocation(dlgLoc);
	}
}