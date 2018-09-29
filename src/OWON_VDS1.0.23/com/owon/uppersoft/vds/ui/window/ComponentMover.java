package com.owon.uppersoft.vds.ui.window;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * ComponentMover，移动组件
 * 
 */
public class ComponentMover implements MouseMotionListener, MouseListener {
	private int x, y;
	private Point loc;
	private Window w;
	private Component comp;
	private boolean isPressed;

	public ComponentMover(Window w, Component c) {
		comp = c;
		comp.addMouseMotionListener(this);
		comp.addMouseListener(this);
		this.w = w;
		loc = w.getLocation();
	}

	public void resetComponent(Component c) {
		comp.removeMouseMotionListener(this);
		comp.addMouseListener(this);
		comp = c;
		comp.addMouseMotionListener(this);
		comp.addMouseListener(this);
	}

	public void mouseDragged(MouseEvent e) {
		if (!isPressed)
			return;
		if (w instanceof Frame) {
			Frame f = (Frame) w;
			if (f.getExtendedState() == Frame.MAXIMIZED_BOTH) {
				return;
			}
		}
		int x1 = e.getXOnScreen();
		int y1 = e.getYOnScreen();
		w.setLocation(loc.x += x1 - x, loc.y += y1 - y);
		x = x1;
		y = y1;
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
		isPressed = false;
	}

	public void mouseClicked(MouseEvent e) {
		/** 在此可添加双击最大化的代码 */
	}

	public void mousePressed(MouseEvent e) {
		x = e.getXOnScreen();
		y = e.getYOnScreen();
		loc = w.getLocation(loc);
		isPressed = true;
	}

}