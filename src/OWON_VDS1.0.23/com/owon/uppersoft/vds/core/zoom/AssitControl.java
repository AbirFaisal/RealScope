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

	// 视窗扩展默认关闭
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
		/** Deltbm is the middle to two boundary distance of b1b2 */
		int deltbm = BigDecimal.valueOf(BORDER)
				.multiply(tcp.ratio(ztbIdx, tc.getTimebaseIdx())).intValue();
		b1 = middleb1b2 + deltbm;
		b2 = middleb1b2 - deltbm;
		if (b1 > BORDER || b2 < -BORDER) {
			b1 = BORDER;
			b2 = -BORDER;
		}

		/** 保存M时基、M水平触发位置 */
		mtbIdx = tc.getTimebaseIdx();
		mhtp = tc.getHorizontalTriggerPosition();
	}

	/** 视窗设定下 黄线屏幕内移动 */
	private void screenMoveWithin(int n, int t) {
		b1 += n;
		b2 += n;
		if (t <= b2)
			tc.c_setHorizontalTriggerPosition(b2);
	}

	/** Under the window setting, b1b2 cannot move to the boundary, causing the horizontal trigger position to move backward. */
	private void horizontalTriggerMove(int n, int t) {
		int tn = t - n;
		if (tn < b2)
			tc.c_setHorizontalTriggerPosition(b2);
		else
			tc.c_setHorizontalTriggerPosition(tn);
	}

	/** 视窗设定下 黄线重置居中时时调用 */
	public void assistMoveb1b2Center() {
		int del = -(b1 + b2) / 2;
		assistSetMoveb1b2(del);
	}

	/**
	 * 视窗设定下 黄线平移时调用 +...-
	 */
	public void assistSetMoveb1b2(int n) {
		int t = tc.getHorizontalTriggerPosition();
		/** 该特例判断已经包含在下个判断 */
		// if (b1 == border & b2 == -border){
		// return;
		// }
		/**
		 * b1、b2视为一个整体平移，控件拖动距离为n。
		 * 
		 * 总体分3种情况:平移后,b1、b2加移动距离n两者，
		 * 
		 * 1.都仍在屏幕内 b1 += n;b2 += n;
		 * 
		 * 2.在屏幕左界的左边，分两种情况处理： A:b1正好在左界，那么水平触发位置直接移动距离n;
		 * B:b1差nn距离才到达左界，那么水平触发位置移动距离(n-nn)。
		 * 
		 * 3.在屏幕右界的右边，分两种情况处理： A:b2正好在右界，那么水平触发位置直接移动距离n;
		 * B:b2差nn距离才到达右界，那么水平触发位置移动距离(n-nn)。
		 */
		int bn1 = b1 + n, bn2 = b2 + n;
		if (bn1 <= BORDER & bn2 >= -BORDER) {
			// innerScreen(A)
			screenMoveWithin(n, t);
		} else if (bn1 > BORDER) {
			/** b1Closeto LBorder , b1At LBorder */
			if (b1 < BORDER) {
				// b1ClosetoLBorder();
				int nn = BORDER - b1;// nn是b1到border的距离
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
				int nn = -BORDER - b2;// nn是b2到-border距离
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

	/** 视窗设定下 时基调整时调用 */
	public void assistSetcomputDeltabb1b2(int ztbIdx, int mtbIdx) {
		this.ztbIdx = ztbIdx;// 界面操作设定的ztb，通过这里传入
		if (mtbIdx == ztbIdx) {
			b1 = BORDER;
			b2 = -BORDER;
			return;
		}
		/** deltb */
		int deltbm = BigDecimal.valueOf(BORDER)
				.multiply(tcp.ratio(ztbIdx, mtbIdx)).intValue();
		/** b1、b2 */
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
		/** 当视窗设定的时基调整时限制t>=b2 */
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
		return getSelected() == ASSIT_STATUS;
	}

	private void onHorTrgPosChangedByTimebase(int tbidx, int htp) {
		if (isonZoom()) {
			setZTBidx(tbidx);// 保存ztb,A与Z互切时要用
			keepZtbNotbiggerthanMtb(tbidx);
		} else if (isonAssistSet()) {
			mtbIdx = tbidx;
			/** 主时基跟着变大，A切M导致触发位置反比例变小;A切M是重新载入mhtp,要赋新值 */
			mhtp = htp;
		}
	}

	public boolean isonZoom() {
		return getSelected() == ZOOM_STATUS;
	}

	public boolean isonMain() {
		return getSelected() == MAIN_STATUS;
	}

	/** 重新定位平触。 M到Z 、A到Z时调用 */
	private void reLocateHTP2Zoom() {
		/**
		 * Z平触 同屏幕中心零点位置的距离＝（A平触 同(b1+b2)/2的距离）*Rate
		 * 
		 * Rate = mtb/ztb , deltb不为零时Rate = BORDER/deltb
		 */
		// BigDecimal mtb = tc.bdTIMEBASE[tc.getTimebaseIdx()];
		// BigDecimal ztb = tc.bdTIMEBASE[ztbIdx];
		int t = tc.getHorizontalTriggerPosition();

		double Rate = tcp.ratio(tc.getTimebaseIdx(), ztbIdx).doubleValue();
		zhtp = (int) ((t - (b1 + b2) / 2) * Rate);

		tc.c_setTimebase_HorTrgPos(ztbIdx, zhtp);
		// KNOW 切到Zoom时，则Selected>0，detailPane设成不能调主时基,主水平触发位置。
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

	/** 重新定位b1b2。Z到M 、Z到A时调用。 Z状态下Z时基调整、ZHTP平移需重新定位A状态下的b1 b2 */
	private void reLocateb1b2pos2AssistSet() {
		if (ztbIdx == mtbIdx) {
			b1 = BORDER;
			b2 = -BORDER;
			return;
		}

		/**
		 * 求M下b1b2中心点坐标，M状态下mhtp到b1b2中心距离＝Z状态下的zhtp到屏幕中心点的距离乘以倍率，倍率=ztb/mtb;
		 * 得mhtp-middleb1b2=(zhtp-0)*rate
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
		/** 载入 M时基 M平触 */
		tc.c_setTimebase_HorTrgPos(mtbIdx, mhtp);
	}

	private void assistSet2Main() {
		// KNOW 保存A下平触位置与b1b2中心的距离。
		distance = tc.getHorizontalTriggerPosition() - (b1 + b2) / 2;
		/** 载入 M时基 M平触 */
		tc.c_setTimebase_HorTrgPos(mtbIdx, mhtp);
	}

	/** 重新定位b1b2。M到A时调用。 如果在M下有移动T，切到A下保持b1b2相对T位置不变 */
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
		/** 保存M时基、M水平触发位置 */
		mtbIdx = tc.getTimebaseIdx();
		mhtp = tc.getHorizontalTriggerPosition();

		if (ztbIdx > mtbIdx)
			ztbIdx = mtbIdx;

		keepb1b2RelativeHtp();
	}

	private void zoom2AssistSet() {
		reLocateb1b2pos2AssistSet();

		/** 载入 M时基 M平触 */
		tc.c_setTimebase_HorTrgPos(mtbIdx, mhtp);
	}

	/** 各状态切换汇总成三大状态切换 */
	public void switch2Zoom(ControlManager cm) {
		boolean isRuntime = cm.isRuntime();
		if (!isRuntime) {
			String msg = I18nProvider.bundle().getString("M.Zoom.Forbid");
			new FadeIOShell().prompt(msg, Platform.getMainWindow().getFrame());
		}

		/** 保存M时基、M水平触发位置 */
		mtbIdx = tc.getTimebaseIdx();
		mhtp = tc.getHorizontalTriggerPosition();
		if (prevSelected == MAIN_STATUS)
			Main2Zoom();
		else if (prevSelected == ASSIT_STATUS)
			assistSet2Zoom();
	}

	public void switch2Main() {
		/**
		 * KNOW A状态和Z状态下若调大Z时基，大到M时基跟着改变时， 存的mtbIdx 、mhtpz需重算;
		 * 已在astbcbb和zmtbcbb的limitZTB()判断中重算。
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
		/** Z状态下调时基,Z时基大于M时基，则DetailPane主时基跟着变大更新 */
		if (ztbIdx > mtbIdx) {
			/** Z时基变大导致M时基跟着变大；Z切M是重新载入ac.mtbIdx,要赋ac.mtbIdx新值 */
			int old_mtb = mtbIdx;
			mtbIdx = ztbIdx;

			/** M时基跟着变大，导致M触发位置反比例变小;Z切M是重新载入ac.mhtp,要重计算、赋ac.mhtp新值 */
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
		/** ac中以屏幕中线为坐标原点，画图时这里的b1、b2也相应与其统一原点 */
		int b1 = BORDER + r.x - this.b1;
		int b2 = BORDER + r.x - this.b2;
		if (isonAssistSet()) {
			g2d.setColor(Color.YELLOW);
			LineDrawTool.drawUnequalDashesLine(g2d, r.y, b1, r.y + r.height);
			LineDrawTool.drawUnequalDashesLine(g2d, r.y, b2, r.y + r.height);
		}
		/** 主视窗时基、水平触发值 */
		drawMtbMhtp(g2d, r);
	}

	public void factoryload(Pref p) {
		load(p);
		switch2Main();
		setSelected(MAIN_STATUS);
	}

	public void changeZoomstatus(ControlManager cm, int idx) {
		if (getSelected() != idx) {
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
