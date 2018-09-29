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
 * MouseGesture，鼠标手势 TODO 零点限界
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
		/** 获取ViewChart 、DataHouse 对象，传递到以下MarkCursorControl类内部,常驻利用 */
		mcctr.acceptImporter(c, dh);
		/** 获取ViewChart对象，传递到以下AssitControl类内部,常驻利用 */
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
			// 如果连接暂停，调动htp,波形将变动，需要载入DM
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
		// 鼠标位于中部时的操作
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
		//		code = Cursor.HAND_CURSOR;//触发光标测量的手势
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
		// TODO 这里是否可以改成fire值到detailPane,来更新htp,zhtp?
		ccsm.setOnShowHtpDetail(true);
		int dx = loc0.x - x;
		tc.c_addHorizontalTriggerPosition(dx, commit);

		// DetailPane下更新htp
		if (!cm.getZoomAssctr().isonAssistSet())
			cm.pcs.firePropertyChange(ITimeControl.onHTPChanged, null, null);
		// Zoom下更新zhtp
		cm.getZoomAssctr().updateZoomHtp();

		cm.mcctr.computeXValues();
		// updateCursorValues();
		loc0.x = x;
		loc0.y = y;
	}

	private boolean checkTop(int x, int y, Rectangle r) {
		if (dh.isHorTrgPosFrozen()) {
			/** 慢扫不可用 */
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
				// 备用
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