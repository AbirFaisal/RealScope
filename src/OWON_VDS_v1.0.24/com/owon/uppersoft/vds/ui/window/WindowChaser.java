package com.owon.uppersoft.vds.ui.window;

import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * 跟随父窗口移动，只有两个参数之间是Window和Dialog的关系，Dialog会随着Window最小化而最小化
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