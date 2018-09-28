package com.owon.uppersoft.vds.ui.window;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * 最大小化动画效果
 * 
 * 通过增加动画帧的间隔时间，减少帧次数，来达到在不同性能设备上的一直表现
 * 
 */
public class MaxMinAnimator {
	public static final int TitleDockHeight = 30;// TitleDialog高度
	public static final int AnimationFrameCount = 3;// 改变次数
	public static final int AnimationGapTime = 10;// 时间间隔

	private Font titleFont;
	private Color titlebg;

	public MaxMinAnimator() {
	}

	public void setFontNColor(Font titleFont, Color titlebg) {
		this.titleFont = titleFont;
		this.titlebg = titlebg;
	}

	private JDialog createTitle(final Window wnd, int x, int y, int bw, int tdh) {
		JDialog td = new JDialog();
		td.setUndecorated(true);
		JLabel bl = new JLabel(wnd.getName(), SwingConstants.CENTER);
		bl.setForeground(Color.WHITE);
		bl.setFont(titleFont);
		bl.setOpaque(false);
		td.getContentPane().setBackground(titlebg);
		td.add(bl);
		td.setBounds(x, y, bw, tdh);
		td.setVisible(true);
		return td;
	}

	private int x, y, tx, ty, wx, wy, tw, ww;

	private class Incre {
		int start, end, time;
		int icr, cur;
		int relate;

		public Incre(int s, int e, int t) {
			setRange(s, e, t);
		}

		void setRange(int s, int e, int t) {
			cur = start = s;
			end = e;
			time = t;

			if (end == start) {
				relate = 0;
				icr = 0;
			} else if (end > start) {
				relate = 1;
				icr = (end - start) / t;
				if (icr == 0)
					icr = 1;
			} else {
				relate = -1;
				icr = (end - start) / t;
				if (icr == 0)
					icr = -1;
			}
		}

		boolean needInc() {
			switch (relate) {
			default:
			case 0:
				return false;
			case 1:
				return !(cur >= end);
			case -1:
				return !(cur <= end);
			}
		}

		void incre() {
			switch (relate) {
			default:
			case 0:
				return;
			case 1:
				if (needInc()) {
					if (cur + icr >= end)
						cur = end;
					else
						cur += icr;
				}
				return;
			case -1:
				if (needInc()) {
					if (cur + icr <= end)
						cur = end;
					else
						cur += icr;
				}
				return;
			}
		}
	}

	public void max(final Rectangle tloc, final Rectangle wloc, final Window wnd) {
		new Thread() {
			@Override
			public void run() {
				execMax(tloc, wloc, wnd);
			}
		}.start();
	}

	public void min(final Rectangle tloc, final Rectangle wloc, final Window wnd) {
		new Thread() {
			@Override
			public void run() {
				execMin(tloc, wloc, wnd);
			}
		}.start();
	}

	private void execMax(final Rectangle tloc, final Rectangle wloc,
			final Window wnd) {
		tx = tloc.x;
		ty = tloc.y;
		wx = wloc.x;
		wy = wloc.y;
		tw = tloc.width;
		ww = wloc.width;

		y = ty;
		x = tx;

		JDialog td = createTitle(wnd, x, y, tw, TitleDockHeight);
		Incre ix = new Incre(tx, wx, AnimationFrameCount), iy = new Incre(ty,
				wy, AnimationFrameCount), iw = new Incre(tw, ww,
				AnimationFrameCount);
		try {
			while (ix.needInc() || iy.needInc()) {
				ix.incre();
				iy.incre();
				iw.incre();
				td.setBounds(ix.cur, iy.cur, iw.cur, TitleDockHeight);
				Thread.sleep(AnimationGapTime);
			}
			td.setBounds(ix.end, iy.end, iw.end, TitleDockHeight);
			Thread.sleep(AnimationGapTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		td.dispose();
		wnd.setBounds(wloc);
		wnd.setVisible(true);
	}

	private void execMin(final Rectangle tloc, final Rectangle wloc,
			final Window wnd) {
		tx = tloc.x;
		ty = tloc.y;
		wx = wloc.x;
		wy = wloc.y;
		tw = tloc.width;
		ww = wloc.width;

		JDialog td = createTitle(wnd, x, y, tw, TitleDockHeight);
		Incre ix = new Incre(wx, tx, AnimationFrameCount), iy = new Incre(wy,
				ty, AnimationFrameCount), iw = new Incre(ww, tw,
				AnimationFrameCount);
		try {
			while (ix.needInc() || iy.needInc()) {
				ix.incre();
				iy.incre();
				iw.incre();
				td.setBounds(ix.cur, iy.cur, iw.cur, TitleDockHeight);
				Thread.sleep(AnimationGapTime);
			}
			td.setBounds(ix.end, iy.end, iw.end, TitleDockHeight);
			Thread.sleep(AnimationGapTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		td.dispose();
		wnd.setVisible(false);
	}

}
