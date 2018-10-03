package com.owon.uppersoft.vds.ui.window;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;

//import com.sun.awt.AWTUtilities;

/**
 * WndSizer，支持调整窗口大小
 * 
 */
public class ShellResizer implements MouseMotionListener, MouseListener {
	private JFrame frm;
	private int x, y;
	private Point loc;
	private boolean resize;
	private int dir;

	public static final int BORDERWIDTH = 3;

	private int MIN_HEIGHT;
	private int MIN_WIDTH;
	private int MAX_HEIGHT;
	private int MAX_WIDTH;

	public ShellResizer(JFrame wnd, Component panel, int mIN_HEIGHT,
			int mIN_WIDTH, int mAX_HEIGHT, int mAX_WIDTH) {
		this.frm = wnd;
		MIN_HEIGHT = mIN_HEIGHT;// 400
		MIN_WIDTH = mIN_WIDTH; // 600
		MAX_HEIGHT = mAX_HEIGHT;
		MAX_WIDTH = mAX_WIDTH;
		panel.addMouseMotionListener(this);
		panel.addMouseListener(this);
		loc = wnd.getLocation();
		resize = false;
	}

	public void mouseDragged(MouseEvent e) {
		if (frm.getExtendedState() == Frame.MAXIMIZED_BOTH) {
			return;
		}
		if (resize) {
			int x1 = e.getPoint().x;
			int y1 = e.getPoint().y;
			int x2 = frm.getSize().width;
			int y2 = frm.getSize().height;
			int x3 = e.getXOnScreen();
			int y3 = e.getYOnScreen();
			switch (dir) {
			case 0:
				if (y1 > MAX_HEIGHT)
					y1 = MAX_HEIGHT;
				if (y1 < MIN_HEIGHT)
					y1 = MIN_HEIGHT;
				frm.setSize(x2, y1);
				break;
			case 1:
				if (x2 + x - x3 > MAX_WIDTH || x2 + x - x3 < MIN_WIDTH)
					x3 = x;
				frm.setLocation(loc.x += x3 - x, loc.y);
				frm.setSize(x2 + x - x3, y2);
				x = x3;
				break;
			case 2:
				if (y2 + y - y3 > MAX_HEIGHT || y2 + y - y3 < MIN_HEIGHT)
					y3 = y;
				frm.setLocation(loc.x, loc.y += y3 - y);
				frm.setSize(x2, y2 + y - y3);
				y = y3;
				break;
			case 3:
				if (x1 > MAX_WIDTH)
					x1 = MAX_WIDTH;
				if (x1 < MIN_WIDTH)
					x1 = MIN_WIDTH;
				frm.setSize(x1, y2);
				break;
			case 4:
				if (x1 > MAX_WIDTH)
					x1 = MAX_WIDTH;
				if (x1 < MIN_WIDTH)
					x1 = MIN_WIDTH;
				if (y1 > MAX_HEIGHT)
					y1 = MAX_HEIGHT;
				if (y1 < MIN_HEIGHT)
					y1 = MIN_HEIGHT;
				frm.setSize(x1, y1);
				break;
			case 5:
				if (y1 > MAX_HEIGHT)
					y1 = MAX_HEIGHT;
				if (y1 < MIN_HEIGHT)
					y1 = MIN_HEIGHT;
				if (x2 + x - x3 > MAX_WIDTH || x2 + x - x3 < MIN_WIDTH)
					x3 = x;
				frm.setLocation(loc.x += x3 - x, loc.y);
				frm.setSize(x2 + x - x3, y1);
				x = x3;
				break;
			case 6:
				if (x2 + x - x3 > MAX_WIDTH || x2 + x - x3 < MIN_WIDTH)
					x3 = x;
				if (y2 + y - y3 > MAX_HEIGHT || y2 + y - y3 < MIN_HEIGHT)
					y3 = y;
				frm.setLocation(loc.x += x3 - x, loc.y += y3 - y);
				frm.setSize(x2 + x - x3, y2 + y - y3);
				x = x3;
				y = y3;
				break;
			case 7:
				if (x1 > MAX_WIDTH)
					x1 = MAX_WIDTH;
				if (x1 < MIN_WIDTH)
					x1 = MIN_WIDTH;
				if (y2 + y - y3 > MAX_HEIGHT || y2 + y - y3 < MIN_HEIGHT)
					y3 = y;
				frm.setLocation(loc.x, loc.y += y3 - y);
				frm.setSize(x1, y2 + y - y3);
				y = y3;
				break;
			default:

			}
			// WindowUtil.ShapeWindow(frm, def.WND_SHAPE_ARC);
			//AWTUtilities.setWindowOpaque(frm, false);
			frm.setBackground(Color.GRAY);
		}
	}

	public void mouseMoved(MouseEvent e) {
		if (frm.getExtendedState() == Frame.MAXIMIZED_BOTH) {
			return;
		}
		int x1 = e.getPoint().x;
		int y1 = e.getPoint().y;
		int x2 = frm.getSize().width;
		int y2 = frm.getSize().height;
		int bw = BORDERWIDTH;
		if (x2 - x1 < bw && x2 - x1 > -bw && y1 < bw && y1 > -bw) {
			frm.setCursor(c(Cursor.NE_RESIZE_CURSOR));
			dir = 7;
		} else if (x1 < bw && x1 > -bw && y1 < bw && y1 > -bw) {
			frm.setCursor(c(Cursor.NW_RESIZE_CURSOR));
			dir = 6;
		} else if (x1 < bw && x1 > -bw && y2 - y1 < bw && y2 - y1 > -bw) {
			frm.setCursor(c(Cursor.SW_RESIZE_CURSOR));
			dir = 5;
		} else if (x2 - x1 < bw && x2 - x1 > -bw && y2 - y1 < bw
				&& y2 - y1 > -bw) {
			frm.setCursor(c(Cursor.SE_RESIZE_CURSOR));
			dir = 4;
		} else if (x2 - x1 < bw && x2 - x1 > -bw) {
			frm.setCursor(c(Cursor.W_RESIZE_CURSOR));
			dir = 3;
		} else if (y1 < bw && y1 > -bw) {
			frm.setCursor(c(Cursor.N_RESIZE_CURSOR));
			dir = 2;
		} else if (x1 < bw && x1 > -bw) {
			frm.setCursor(c(Cursor.E_RESIZE_CURSOR));
			dir = 1;
		} else if (y2 - y1 < bw && y2 - y1 > -bw) {
			dir = 0;
			frm.setCursor(c(Cursor.S_RESIZE_CURSOR));
		} else {
			frm.setCursor(c(Cursor.DEFAULT_CURSOR));
			dir = -1;
		}
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public static final Cursor c(int v) {
		return Cursor.getPredefinedCursor(v);
	}

	public void mouseExited(MouseEvent e) {
		if (resize)
			return;
		frm.setCursor(c(Cursor.DEFAULT_CURSOR));
		dir = -1;
	}

	public void mousePressed(MouseEvent e) {
		if (dir != -1) {
			x = e.getXOnScreen();
			y = e.getYOnScreen();
			loc = frm.getLocation(loc);
			resize = true;
		}
	}

	public void mouseReleased(MouseEvent e) {
		resize = false;
		frm.setCursor(c(Cursor.DEFAULT_CURSOR));
	}

}