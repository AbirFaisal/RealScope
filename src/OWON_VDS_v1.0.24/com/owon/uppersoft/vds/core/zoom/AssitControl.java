package com.owon.uppersoft.vds.core.zoom;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.math.BigDecimal;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.vds.core.aspect.control.ITimeControl;
import com.owon.uppersoft.vds.core.aspect.control.TimeConfProvider;
import com.owon.uppersoft.vds.ui.paint.LineDrawTool;
import com.owon.uppersoft.vds.ui.prompt.FadeIOShell;
import com.owon.uppersoft.vds.util.Pref;

public class AssitControl {

	public static final int MAIN_STATUS = 0, ASSIT_STATUS = 1, ZOOM_STATUS = 2,
			NO_STATUS = -1;
	public static boolean FastMWstwich = true;
	public final int BORDER = 500;
	public int b1, b2;
	public int zhtp, mtbIdx, mhtp;
	private int ztbIdx;
	private ITimeControl tc;

	// View window extension is off by default
	private int selected = MAIN_STATUS, prevSelected = NO_STATUS;

	private int distance;

	private int middleb1b2;
	private TimeConfProvider tcp;
	private PropertyChangeSupport pcs;

	public AssitControl(ITimeControl tc, Pref p, TimeConfProvider tcp,
			final PropertyChangeSupport pcs) {
		this.tc = tc;
		this.tcp = tcp;
		this.pcs = pcs;

		pcs.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				String pn = evt.getPropertyName();
				if (pn.equals(ITimeControl.onHorTrgPosChangedByTimebase)) {
					int tbidx = (Integer) evt.getOldValue();
					int htp = (Integer) evt.getNewValue();
					onHorTrgPosChangedByTimebase(tbidx, htp);
				}
			}
		});

		load(p);
		/** deltbm is the distance from the middle of b1b2 to both boundaries under extension */
		int deltbm = BigDecimal.valueOf(BORDER)
				.multiply(tcp.ratio(ztbIdx, tc.getTimebaseIdx())).intValue();
		b1 = middleb1b2 + deltbm;
		b2 = middleb1b2 - deltbm;
		if (b1 > BORDER || b2 < -BORDER) {
			b1 = BORDER;
			b2 = -BORDER;
		}

		/** Save the M timebase and M horizontal trigger position */
		mtbIdx = tc.getTimebaseIdx();
		mhtp = tc.getHorizontalTriggerPosition();
	}

	/** Under view window setting, the yellow line moves within the screen */
	private void screenMoveWithin(int n, int t) {
		b1 += n;
		b2 += n;
		if (t <= b2)
			tc.c_setHorizontalTriggerPosition(b2);
	}

	/** Under view window setting, b1b2 cannot move to the boundary, causing the horizontal trigger position to move in the opposite direction */
	private void horizontalTriggerMove(int n, int t) {
		int tn = t - n;
		if (tn < b2)
			tc.c_setHorizontalTriggerPosition(b2);
		else
			tc.c_setHorizontalTriggerPosition(tn);
	}

	/** Under view window setting, called when the yellow line is reset to center */
	public void assistMoveb1b2Center() {
		int del = -(b1 + b2) / 2;
		assistSetMoveb1b2(del);
	}

	/**
	 * Under view window setting, called when the yellow line is translated +...-
	 */
	public void assistSetMoveb1b2(int n) {
		int t = tc.getHorizontalTriggerPosition();
		/** This special case check is already included in the next check */
		// if (b1 == border & b2 == -border){
		// return;
		// }
		/**
		 * b1 and b2 are treated as a whole and translated together; the control drag distance is n.
		 * 
		 * Overall there are 3 cases: after translation, adding the movement distance n to both b1 and b2,
		 * 
		 * 1. both are still within the screen b1 += n;b2 += n;
		 * 
		 * 2. to the left of the left screen boundary, handled in two cases: A: b1 is exactly at the left boundary, then the horizontal trigger position moves directly by distance n;
		 * B:b1 is nn short of reaching the left boundary, then the horizontal trigger position moves by distance (n-nn).
		 * 
		 * 3. to the right of the right screen boundary, handled in two cases: A: b2 is exactly at the right boundary, then the horizontal trigger position moves directly by distance n;
		 * B:b2 is nn short of reaching the right boundary, then the horizontal trigger position moves by distance (n-nn).
		 */
		int bn1 = b1 + n, bn2 = b2 + n;
		if (bn1 <= BORDER & bn2 >= -BORDER) {
			// innerScreen(A)
			screenMoveWithin(n, t);
		} else if (bn1 > BORDER) {
			/** b1Closeto LBorder , b1At LBorder */
			if (b1 < BORDER) {
				// b1ClosetoLBorder();
				int nn = BORDER - b1;// nn is the distance from b1 to border
				screenMoveWithin(nn, t);
				n = n - nn;
				horizontalTriggerMove(n, t);
			} else if (b1 == BORDER) {
				// b1AtBorder();
				horizontalTriggerMove(n, t);
			}
		} else if (bn2 < -BORDER) {
			if (b2 > -BORDER) {
				// b2ClosetoRBorder();
				int nn = -BORDER - b2;// nn is the distance from b2 to -border
				screenMoveWithin(nn, t);
				n = n - nn;
				horizontalTriggerMove(n, t);
			} else if (b2 == -BORDER) {
				// b2AtRBorder();
				horizontalTriggerMove(n, t);
			}
		}
		// DBG.dbgln("b1:" + b1 + " b2:" + b2);
		// DBG.dbgln("n:" + n);
		// DBG.dbgln("t"+tc.getHorizontalTriggerPosition());
		// DBG.dbgln();

	}

	/** Under view window setting, called when the timebase is adjusted */
	public void assistSetcomputDeltabb1b2(int ztbIdx, int mtbIdx) {
		this.ztbIdx = ztbIdx;// The ztb set by the UI operation, passed in through here
		if (mtbIdx == ztbIdx) {
			b1 = BORDER;
			b2 = -BORDER;
			return;
		}
		/** deltb */
		int deltbm = BigDecimal.valueOf(BORDER)
				.multiply(tcp.ratio(ztbIdx, mtbIdx)).intValue();
		/** b1, b2 */
		int middleb1b2 = (b1 + b2) / 2;// b1-(b1-b2)/2;
		int mb1 = middleb1b2 + deltbm;
		int mb2 = middleb1b2 - deltbm;
		if (mb1 > BORDER) {
			b1 = BORDER;
			b2 = mb2 - (mb1 - BORDER);
		} else if (mb2 < -BORDER) {
			b1 = mb1 + (-BORDER) - mb2;
			b2 = -BORDER;
		} else {
			b1 = middleb1b2 + deltbm;
			b2 = middleb1b2 - deltbm;
		}
		/** When the timebase of the view window setting is adjusted, limit t>=b2 */
		// int t = tc.getHorizontalTriggerPosition();
		// if (t < b2)
		// tc.c_setHorizontalTriggerPosition(b2);
	}

	public int getZTBidx() {
		return ztbIdx;
	}

	public void setZTBidx(int idx) {
		this.ztbIdx = idx;
	}

	public void setSelected(int s) {
		this.prevSelected = selected;
		this.selected = s;
	}

	public int getSelected() {
		return selected;
	}

	public boolean isPressedtheSame(int i) {
		return prevSelected == i;
	}

	public boolean isonAssistSet() {
		return selected == ASSIT_STATUS;
	}

	private void onHorTrgPosChangedByTimebase(int tbidx, int htp) {
		if (isonZoom()) {
			// Save ztb, needed when switching between A and Z
			this.ztbIdx = tbidx;
			keepZtbNotbiggerthanMtb(tbidx);
		} else if (isonAssistSet()) {
			mtbIdx = tbidx;
			/** The main timebase grows accordingly, and switching A to M makes the trigger position shrink inversely proportionally; switching A to M reloads mhtp, so a new value must be assigned */
			mhtp = htp;
		}
	}

	public boolean isonZoom() {
		return selected == ZOOM_STATUS;
	}

	public boolean isonMain() {
		return selected == MAIN_STATUS;
	}

	/** Relocate the horizontal trigger. Called for M to Z and A to Z */
	private void reLocateHTP2Zoom() {
		/**
		 * Z horizontal trigger's distance from the screen center zero point = (A horizontal trigger's distance from (b1+b2)/2)*Rate
		 * 
		 * Rate = mtb/ztb , deltb is nonzero, Rate = BORDER/deltb
		 */
		// BigDecimal mtb = tc.bdTIMEBASE[tc.getTimebaseIdx()];
		// BigDecimal ztb = tc.bdTIMEBASE[ztbIdx];
		int t = tc.getHorizontalTriggerPosition();

		double Rate = tcp.ratio(tc.getTimebaseIdx(), ztbIdx).doubleValue();
		zhtp = (int) ((t - (b1 + b2) / 2) * Rate);

		tc.c_setTimebase_HorTrgPos(ztbIdx, zhtp);
		// KNOW When switching to Zoom, Selected>0, and detailPane is set so that the main timebase and main horizontal trigger position cannot be adjusted.
	}

	private void Main2Zoom() {
		if (ztbIdx > mtbIdx) {
			ztbIdx = mtbIdx;
			b1 = BORDER;
			b2 = -BORDER;
		}
		reLocateHTP2Zoom();
	}

	public void assistSet2Zoom() {
		reLocateHTP2Zoom();
	}

	/** Relocate b1b2. Called for Z to M and Z to A. In Z state, Z timebase adjustment or ZHTP translation requires relocating (in A state) the b1 b2 */
	private void reLocateb1b2pos2AssistSet() {
		if (ztbIdx == mtbIdx) {
			b1 = BORDER;
			b2 = -BORDER;
			return;
		}

		/**
		 * Find the center point coordinate of b1b2 under M; in M state the distance from mhtp to the b1b2 center = the distance from zhtp to the screen center point in Z state times the ratio, ratio=ztb/mtb;
		 * gives mhtp-middleb1b2=(zhtp-0)*rate
		 */
		int middleb1b2 = mhtp
				- BigDecimal.valueOf(tc.getHorizontalTriggerPosition())
						.multiply(tcp.ratio(ztbIdx, mtbIdx)).intValue();
		int deltbm = BigDecimal.valueOf(BORDER)
				.multiply(tcp.ratio(ztbIdx, mtbIdx)).intValue();
		int mb1 = middleb1b2 + deltbm;
		int mb2 = middleb1b2 - deltbm;
		if (mb1 > BORDER) {
			b1 = BORDER;
			b2 = mb2 - (mb1 - BORDER);
		} else if (mb2 < -BORDER) {
			b1 = mb1 + (-BORDER) - mb2;
			b2 = -BORDER;
		} else {
			b1 = middleb1b2 + deltbm;
			b2 = middleb1b2 - deltbm;
		}

	}

	private void zoom2Main() {
		reLocateb1b2pos2AssistSet();
		/** Load M timebase and M horizontal trigger */
		tc.c_setTimebase_HorTrgPos(mtbIdx, mhtp);
	}

	private void assistSet2Main() {
		// KNOW Save the distance between the horizontal trigger position in A and the b1b2 center.
		distance = tc.getHorizontalTriggerPosition() - (b1 + b2) / 2;
		/** Load M timebase and M horizontal trigger */
		tc.c_setTimebase_HorTrgPos(mtbIdx, mhtp);
	}

	/** Relocate b1b2. Called for M to A. If T is moved under M, keep the position of b1b2 relative to T unchanged after switching to A */
	private void keepb1b2RelativeHtp() {
		int middleb1b2 = mhtp - distance;
		int deltbm = BigDecimal.valueOf(BORDER)
				.multiply(tcp.ratio(ztbIdx, mtbIdx)).intValue();
		// b1 = middleb1b2 + deltb;
		// b2 = middleb1b2 - deltb;
		int mb1 = middleb1b2 + deltbm;
		int mb2 = middleb1b2 - deltbm;

		if (mb1 > BORDER) {
			b1 = BORDER;
			b2 = mb2 - (mb1 - BORDER);
		} else if (mb2 < -BORDER) {
			b1 = mb1 + (-BORDER) - mb2;
			b2 = -BORDER;
		} else {
			b1 = middleb1b2 + deltbm;
			b2 = middleb1b2 - deltbm;
		}
	}

	private void main2AssistSet() {
		/** Save the M timebase and M horizontal trigger position */
		mtbIdx = tc.getTimebaseIdx();
		mhtp = tc.getHorizontalTriggerPosition();

		if (ztbIdx > mtbIdx)
			ztbIdx = mtbIdx;

		keepb1b2RelativeHtp();
	}

	private void zoom2AssistSet() {
		reLocateb1b2pos2AssistSet();

		/** Load M timebase and M horizontal trigger */
		tc.c_setTimebase_HorTrgPos(mtbIdx, mhtp);
	}

	/** Summarize the switches between states into three major state switches */
	public void switch2Zoom(ControlManager cm) {
		boolean isRuntime = cm.isRuntime();
		if (!isRuntime) {
			String msg = I18nProvider.bundle().getString("M.Zoom.Forbid");
			new FadeIOShell().prompt(msg, Platform.getMainWindow().getFrame());
		}

		/** Save the M timebase and M horizontal trigger position */
		mtbIdx = tc.getTimebaseIdx();
		mhtp = tc.getHorizontalTriggerPosition();
		if (prevSelected == MAIN_STATUS)
			Main2Zoom();
		else if (prevSelected == ASSIT_STATUS)
			assistSet2Zoom();
	}

	public void switch2Main() {
		/**
		 * KNOW A state and Z state: if the Z timebase is increased so much that the M timebase changes with it, the stored mtbIdx, mhtpz need recalculation;
		 * already recalculated in the limitZTB() checks of astbcbb and zmtbcbb.
		 */
		if (prevSelected == ASSIT_STATUS)
			assistSet2Main();
		else if (prevSelected == ZOOM_STATUS)
			zoom2Main();
	}

	public void switch2AssistSet() {
		if (prevSelected == MAIN_STATUS)
			main2AssistSet();
		else if (prevSelected == ZOOM_STATUS)
			zoom2AssistSet();
	}

	public void keepZtbNotbiggerthanMtb(int ztbIdx) {
		/** Z state: adjusting the timebase, if the Z timebase is greater than the M timebase, the DetailPane main timebase grows accordingly and is updated */
		if (ztbIdx > mtbIdx) {
			/** Z timebase growing makes the M timebase grow; switching Z to M reloads ac.mtbIdx, so a new value must be assigned to ac.mtbIdx */
			int old_mtb = mtbIdx;
			mtbIdx = ztbIdx;

			/** M timebase growing makes the M trigger position shrink inversely proportionally; switching Z to M reloads ac.mhtp, so it must be recalculated and a new value assigned to ac.mhtp */
			int newmhtp = BigDecimal.valueOf(mhtp)
					.multiply(tcp.ratio(old_mtb, ztbIdx)).intValue();
			mhtp = newmhtp;

			pcs.firePropertyChange(UPDATE_ZOOM_HRI_TRG, null, null);
		}
		pcs.firePropertyChange(UPDATE_ZOOM_TIMEBASE, null, null);
	}

	public static final String UPDATE_ZOOM_HRI_TRG = "updateZoomHriTrg";
	public static final String UPDATE_ZOOM_TIMEBASE = "updateZoomTimebase";

	public void load(Pref p) {
		ztbIdx = p.loadInt("Zoom.ztimebaseidx");
		if (ztbIdx > tc.getTimebaseIdx())
			ztbIdx = tc.getTimebaseIdx();

		middleb1b2 = p.loadInt("Zoom.middleofborders");
	}

	public void persist(Pref p) {
		p.persistInt("Zoom.ztimebaseidx", ztbIdx);
		int middleb1b2 = (b1 + b2) / 2;
		p.persistInt("Zoom.middleofborders", middleb1b2);
	}

	public void drawMtbMhtp(Graphics2D g2, Rectangle bd) {
		if (isonZoom()) {
			Rectangle r = bd;// vc.getLocInfo();
			g2.setColor(Color.YELLOW);
			g2.drawString("M: " + tcp.getTimebaseLabel(mtbIdx),
					r.width * 16 / 20 + 45, r.y + r.height - 15);
			g2.drawString("Tm:  " + tc.getHTPLabel(mtbIdx, mhtp), r.x + r.width
					* 18 / 20, r.y + r.height - 15);

			g2.drawString("W: " + tcp.getTimebaseLabel(ztbIdx),
					r.width * 16 / 20 + 45, r.y + r.height);
			g2.drawString(
					"Tw:  "
							+ tc.getHTPLabel(ztbIdx,
									tc.getHorizontalTriggerPosition()), r.x
							+ r.width * 18 / 20, r.y + r.height);
		}
	}

	public void drawMarks(Graphics2D g2d, Rectangle r) {
		/** ac uses the screen center line as the coordinate origin; when drawing, b1 and b2 here also share that origin */
		int b1 = BORDER + r.x - this.b1;
		int b2 = BORDER + r.x - this.b2;
		if (isonAssistSet()) {
			g2d.setColor(Color.YELLOW);
			LineDrawTool.drawUnequalDashesLine(g2d, r.y, b1, r.y + r.height);
			LineDrawTool.drawUnequalDashesLine(g2d, r.y, b2, r.y + r.height);
		}
		/** Main window timebase, horizontal trigger value */
		drawMtbMhtp(g2d, r);
	}

	public void factoryload(Pref p) {
		load(p);
		switch2Main();
		setSelected(MAIN_STATUS);
	}

	public void changeZoomstatus(ControlManager cm, int idx) {
		if (selected != idx) {
			setSelected(idx);
			switch (idx) {
			case AssitControl.MAIN_STATUS:
				switch2Main();
			case AssitControl.ZOOM_STATUS:
				switch2Zoom(cm);
			}
		}
	}

	public void updateZoomHtp() {
		if (isonZoom()) {
			zhtp = tc.getHorizontalTriggerPosition();
			pcs.firePropertyChange(AssitControl.UPDATE_ZOOM_HRI_TRG, null, null);
		}
	}

}
