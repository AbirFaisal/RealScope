package com.owon.uppersoft.dso.control;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * IntermediateMouseAdapter, intermediate adapter, can turn off event listening
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
		/** Once clicked it is immediately disabled and can be regarded as an unsuccessful click; dragging is not supported */
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
