package com.owon.uppersoft.vds.core.paint;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.aspect.Decorate;

/**
 * MoveLineMark，移动的线形光标。 鼠标事件监听器各自监听界面事件，故在明显不需监听的情况下条件跳转来避免多余处理。
 * 光标分水平和垂直，处理代码被统一化了，需分离处理的时候，只需拷贝代码，同时将判断方法替换掉就可以了。
 * 
 * @deprecated
 */
public class MoveLineMark extends MouseAdapter implements Decorate {
	public static final int DEFINE_AREA_WIDTH = GDefine.AREA_WIDTH;

	public static final int Sensitivity = 2;

	public boolean vertical = false;
	public int loc = 0, loc0 = -1;
	public boolean onmark, select = false;
	private Component c;

	public MoveLineMark(boolean vertical, int loc, Component c) {
		this.vertical = vertical;
		this.loc = loc;
		this.c = c;
	}

	public int adjustLoc(int l) {
		int range = getRange(vertical, pc) - getTail(vertical);
		if (l >= range) {
			l = range;
		}
		int leading = getLeading(vertical);
		if (l <= leading) {
			l = leading;
		}
		return l;
	}

	public void setLoc(int l) {
		int ll = adjustLoc(l);
		loc = ll;
		c.repaint();
	}

	public int getLoc() {
		return loc;
	}

	public void mouseDragged(MouseEvent e) {
		if (select) {
			int loc1 = getLoc(vertical, e);

			loc += loc1 - loc0;
			loc0 = loc1;

			int tmp = adjustLoc(loc);
			if (loc != tmp)
				loc0 = loc = tmp;
			c.repaint();
		}
	}

	public void mouseMoved(MouseEvent e) {
		int locn, c;
		locn = getLoc(vertical, e);
		c = getCursor(vertical);
		boolean ison = locn < loc + Sensitivity && locn > loc - Sensitivity;
		Cursor cs = ison ? Cursor.getPredefinedCursor(c) : Cursor
				.getDefaultCursor();
		if (onmark != ison)
			this.c.setCursor(cs);
		onmark = ison;
	}

	public void mousePressed(MouseEvent e) {
		if (onmark) {
			select = true;
			loc0 = getLoc(vertical, e);
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (select) {
			loc0 = -1;
			select = false;
		}
	}

	private Insets chartInsets;
	private PaintContext pc;

	public void adjustView(ScreenContext sc, Rectangle bound) {
		this.pc = (PaintContext) sc;
		chartInsets = pc.getChartInsets();
		int range = getRange(vertical, pc) - getTail(vertical);

		if (range <= loc) {
			loc = range;
		}
	}

	protected final int getCursor(boolean vertical) {
		return vertical ? Cursor.W_RESIZE_CURSOR : Cursor.N_RESIZE_CURSOR;
	}

	protected final int getRange(boolean vertical, PaintContext pc) {
		return vertical ? DEFINE_AREA_WIDTH : pc.heightrange;
	}

	protected final int getRange(boolean vertical, Dimension sz) {
		return vertical ? sz.width : sz.height;
	}

	protected final int getLoc(boolean vertical, MouseEvent e) {
		return vertical ? e.getX() : e.getY();
	}

	protected final int getLeading(boolean vertical) {
		return vertical ? chartInsets.left : chartInsets.top;
	}

	protected final int getTail(boolean vertical) {
		return vertical ? chartInsets.right : chartInsets.bottom;
	}

	public static final Color CO_MARK = new Color(150, 160, 170);
	public static final Color CO_MARK_SELECT = new Color(200, 190, 180);

	public void paintView(Graphics2D g2d, ScreenContext sc, Rectangle r) {
		g2d.setColor(select ? CO_MARK_SELECT : CO_MARK);
		if (vertical) {
			g2d.drawLine(loc, 0, loc, pc.heightrange);
		} else {
			g2d.drawLine(0, loc, DEFINE_AREA_WIDTH, loc);
		}
	}

	public void paintMore(Graphics2D g2d, PaintContext pc) {
		g2d.setColor(select ? CO_MARK_SELECT : CO_MARK);
		if (vertical) {
			g2d.fillRect(loc - 2, 0, 5, pc.heightrange);
		} else {
			g2d.fillRect(0, loc - 2, DEFINE_AREA_WIDTH, 5);
		}
	}
}
