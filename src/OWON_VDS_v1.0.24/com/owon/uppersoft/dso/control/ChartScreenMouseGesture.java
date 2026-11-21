package com.owon.uppersoft.dso.control;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import com.owon.uppersoft.dso.function.MarkCursorControl;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.mode.control.TimeControl;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.view.ChartScreen;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.dso.view.ViewChart;
import com.owon.uppersoft.dso.wf.ChartScreenSelectModel;
import com.owon.uppersoft.vds.core.aspect.control.ITimeControl;
import com.owon.uppersoft.vds.core.paint.ScreenContext;

//import com.owon.uppersoft.vds.data.Point;

/**
 * MouseGesture, mouse gestures TODO zero-point limit
 * 
 */
public class ChartScreenMouseGesture extends MouseAdapter {

	private boolean left = false, top = false, centre = false, right = false;
	private Point loc0;

	private ViewChart vc;
	private JPanel pa;
	private WaveFormManager wfm;
	private TimeControl tc;
	private MarkCursorControl mcctr;
	private ControlManager cm;
	private ChartScreen cs;
	private DataHouse dh;
	private MainWindow mw;

	private RightScreenGesture rsg;
	private LeftScreenGesture lsg;

	public RightScreenGesture getRightScreenGesture() {
		return rsg;
	}

	private ChartScreenSelectModel ccsm;
	private IntermediateMouseAdapter ima;

	public ChartScreenMouseGesture(MainWindow mw, ViewChart c, DataHouse dh,
			JPanel pa, ChartScreen cs) {
		this.mw = mw;
		this.vc = c;
		this.cs = cs;
		this.dh = dh;
		ccsm = cs.getChartScreenSelectModel();
		this.pa = pa;
		cm = dh.controlManager;
		rsg = new RightScreenGesture(cm.getCoreControl()
				.getTriggerLevelDelegate(), ccsm);
		lsg = new LeftScreenGesture(ccsm);

		wfm = dh.getWaveFormManager();
		tc = cm.getTimeControl();
		loc0 = new Point();
		ima = new IntermediateMouseAdapter(this);
		pa.addMouseListener(ima);
		pa.addMouseMotionListener(ima);
		mcctr = cm.mcctr;
		/** Get the ViewChart and DataHouse objects and pass them into the MarkCursorControl class below for permanent use */
		mcctr.acceptImporter(c, dh);
		/** Get the ViewChart object and pass it into the AssitControl class below for permanent use */
	}

	public void enableGesture(boolean b) {
		ima.enableGesture(b);
	}

	public void resetPressed() {
		top = false;
		left = false;
	}

	public void mousePressed(MouseEvent e) {
		loc0.x = e.getX();
		loc0.y = e.getY();

		if (left) {
			lsg.mousePressed(vc, wfm);
		}
		if (right) {
			rsg.mousePressed(vc, wfm);
		}
		if (top) {
			// If the connection is paused, adjusting htp changes the waveform, so it is necessary to load DM
			boolean operatable = Platform.getControlApps().interComm
					.isTimeOperatableNTryGetDM();
			if (!operatable)
				return;

		}
		if (centre) {
			if (!mcctr.ison)
				cm.pcs.firePropertyChange(PropertiesItem.TURN_ON_MARKBULLETIN,
						null, true);
		}
	}

	public void mouseReleased(MouseEvent e) {
		ScreenContext pc = vc.getScreenContext();
		int x = e.getX(), y = e.getY();
		if (left) {
			lsg.dragLeft(x, y, pc, cs, mw, wfm, loc0, mcctr, true);

			lsg.mouseReleased(wfm);
		}
		if (right) {
			rsg.dragRight(x, y, pc.isScreenMode_3(), vc, mw, wfm, loc0, true,
					cm);
			rsg.mouseReleased();
		}
		if (top) {
			dragTop(x, y, true);
			ccsm.setOnShowHtpDetail(false);
		}

		vc.re_paint();
	}

	protected boolean isCommitImmediately() {
		return !dh.isOptimizeDragCommandSend();
	}

	public void mouseDragged(MouseEvent e) {
		ScreenContext pc = vc.getScreenContext();

		int x = e.getX(), y = e.getY();

		boolean commit = isCommitImmediately();
		// System.out.println(left + "," + top + "," + centre + "," + right);
		if (left) {
			lsg.dragLeft(x, y, pc, cs, mw, wfm, loc0, mcctr, true);
		} else if (top) {
			dragTop(x, y, true);
		} else if (centre) {
			dragCenter(x, y);
		} else if (right) {
			rsg.dragRight(x, y, pc.isScreenMode_3(), vc, mw, wfm, loc0, true,
					cm);
		}
	}

	public void mouseMoved(MouseEvent e) {
		Rectangle r = vc.getLocInfo();

		int x = e.getX(), y = e.getY();
		x /= DataHouse.xRate;
		y /= DataHouse.yRate;
		
		if (left = lsg.checkLeft(x, y, r, vc, this, wfm, left)) {
			return;
		} else if (checkTop(x, y, r)) {
			return;
		} else if (checkCentre(x, y, r)) {
			return;
		} else {
			right = rsg.checkRight(x, y, r, vc, dh, this, wfm);
		}
	}

	public void mouseExited(MouseEvent e) {
		rsg.mouseExited(vc);
	}

	private void dragCenter(int x, int y) {
		// operations when the mouse is in the middle
		mcctr.dragMarkCursor(x, y, loc0);
		loc0.x = x;
		loc0.y = y;
		vc.re_paint();
	}

	private boolean checkCentre(int x, int y, Rectangle r) {
		int x0 = r.x, xw = r.x + r.width, y0 = r.y, yh = r.y + r.height;
		if (x > x0 && x < xw && y > y0 && y < yh) {
			ScreenContext pc = vc.getScreenContext();
			int code = mcctr.checkMark(pc.isScreenMode_3(), x, y);

		//	if (x > x0 && x < x0 + 20 && y > yh - 20 && y < yh) {
		//		code = Cursor.HAND_CURSOR;//gesture that triggers cursor measurement
		//	}
			centre = (code != Cursor.DEFAULT_CURSOR);
			cursor(code);
		} else if (centre) {
			centre = false;
			cursor(Cursor.DEFAULT_CURSOR);
		}

		return centre;
	}

	private void dragTop(int x, int y, boolean commit) {
		// TODO Could this be changed to fire the value to detailPane to update htp,zhtp?
		ccsm.setOnShowHtpDetail(true);
		int dx = loc0.x - x;
		tc.c_addHorizontalTriggerPosition(dx, commit);

		// DetailPane: update htp
		if (!cm.getZoomAssctr().isonAssistSet())
			cm.pcs.firePropertyChange(ITimeControl.onHTPChanged, null, null);
		// Zoom: update zhtp
		cm.getZoomAssctr().updateZoomHtp();

		cm.mcctr.computeXValues();
		// updateCursorValues();
		loc0.x = x;
		loc0.y = y;
	}

	private boolean checkTop(int x, int y, Rectangle r) {
		if (dh.isHorTrgPosFrozen()) {
			/** slow sweep is not available */
			return top = false;
		}
		if (cm.getZoomAssctr().isonAssistSet()) {
			return top = false;
		}

		if (y > 0 && y < r.y) {
			top = true;
			int pos = tc.getHorizontalTriggerPosition();
			pos = r.x + (r.width >> 1) - pos;
			if (x > pos - 5 && x < pos + 5) {
				// reserved
			}
			cursor(Cursor.W_RESIZE_CURSOR);// Cursor.HAND_CURSOR
		} else if (top) {
			top = false;
			cursor(Cursor.DEFAULT_CURSOR);
		}
		return top;
	}

	public final void cursor(int code) {
		pa.setCursor(Cursor.getPredefinedCursor(code));
	}
}