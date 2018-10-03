package com.owon.uppersoft.dso.control;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.owon.uppersoft.dso.function.FFTCursorControl;
import com.owon.uppersoft.dso.function.FFTView;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.vds.data.Point;

/**
 * @RF v2->e.getComponent()
 * 
 */
public class FFTMouseAdapter extends MouseAdapter {
	private final WaveFormManager wfm;
	private Point loc0 = new Point();
	private FFTCursorControl fftctr;
	private Component v2;

	public FFTMouseAdapter(WaveFormManager wfm, FFTCursorControl fftctr,
			Component v2) {
		this.wfm = wfm;
		this.v2 = v2;
		this.fftctr = fftctr;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		v2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	@Override
	public void mouseExited(MouseEvent e) {
		v2.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	@Override
	public void mousePressed(MouseEvent e) {
		loc0.x = e.getX();
		loc0.y = e.getY();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		v2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		fftctr.isOnCursorChecking(e, v2);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		boolean onCursorDragging = fftctr.dragFFTCursor(e);
		if (!onCursorDragging) {
			int dx = e.getX() - loc0.x;
			int dy = e.getY() - loc0.y;
			// System.out.println(dx + ", " + dy);
			int cd;
			FFTView fftv = wfm.getFFTView();
			if (Math.abs(dy) > Math.abs(dx)) {
				fftv.increY(dy);
				cd = Cursor.S_RESIZE_CURSOR;
			} else {
				fftv.increX(dx);
				cd = Cursor.W_RESIZE_CURSOR;
			}
			v2.setCursor(Cursor.getPredefinedCursor(cd));
			loc0.x = e.getX();
			loc0.y = e.getY();
		}
		// 在运行时，如果刷新率够快，可以不用重绘
		// 拖拽的时候重绘可能PaintContext里面的内容还不是合适可用的，已加同步锁解决
		// if (wfm.getDataHouse().isDMLoad())
		v2.repaint();
	}
}