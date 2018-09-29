package com.owon.uppersoft.dso.control;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * IntermediateMouseAdapter，中间适配，可以关闭事件监听
 * 
 */
public class IntermediateMouseAdapter extends MouseAdapter {
	private ChartScreenMouseGesture csmg;

	public IntermediateMouseAdapter(ChartScreenMouseGesture csmg) {
		this.csmg = csmg;
		enableGesture(true);
		pressed = false;
	}

	private boolean on;

	public void enableGesture(boolean b) {
		on = b;
		/** 当点击后随即被禁用，可以视为未点击成功，不支持拖拽 */
		pressed = false;
	}

	private boolean pressed;

	public void mouseDragged(MouseEvent me) {
		if (on && pressed)
			csmg.mouseDragged(me);
	}

	public void mouseExited(MouseEvent me) {
		if (on)
			csmg.mouseExited(me);
	}

	public void mouseMoved(MouseEvent me) {
		if (on)
			csmg.mouseMoved(me);
	}

	public void mousePressed(MouseEvent me) {
		if (on) {
			csmg.mousePressed(me);
			pressed = true;
		}
	}

	public void mouseReleased(MouseEvent me) {
		pressed = false;
		if (on)
			csmg.mouseReleased(me);
	}
}
