package com.owon.uppersoft.vds.ui.widget.help;

import java.awt.Component;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * ComponentChraser，追随组件移动
 * 
 */
public class ComponentChaser extends MouseAdapter {
	private Window aim, chraser;
	private Runnable r;

	public ComponentChaser(Window aim, Window chraser, Component c, Runnable r) {
		this.aim = aim;
		this.chraser = chraser;
		this.r = r;
		c.addMouseListener(this);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
			if (r != null)
				r.run();
		}
	}

	private Point wndLoc;
	private Point dlgLoc;
	private final int Left1 = -280, Left2 = 15, Right1 = 735, Right2 = 1024,
			Top = 47, Range = 10;

	@Override
	public void mouseReleased(MouseEvent e) {
		Window jf = aim;
		Window dlg = chraser;

		wndLoc = jf.getLocation();
		dlgLoc = dlg.getLocation();
		int dlgleft = dlgLoc.x - wndLoc.x;
		int dlgtop = dlgLoc.y - wndLoc.y;

		if (dlgleft > Left1 - Range && dlgleft < Left1 + Range) {
			dlg.setLocation(wndLoc.x + Left1, dlgLoc.y);
		} else if (dlgleft > Left2 - Range && dlgleft < Left2 + Range
				&& dlgtop > Top - Range && dlgtop < Top + Range) {
			dlg.setLocation(wndLoc.x + Left2, wndLoc.y + Top);
		} else if (dlgleft > Right1 - Range && dlgleft < Right1 + Range
				&& dlgtop > Top - Range && dlgtop < Top + Range) {
			dlg.setLocation(wndLoc.x + Right1, wndLoc.y + Top);
		} else if ((dlgleft > Right2 - Range && dlgleft < Right2 + Range))
			dlg.setLocation(wndLoc.x + Right2, dlgLoc.y);

		// adsorbEdge();
	}

}