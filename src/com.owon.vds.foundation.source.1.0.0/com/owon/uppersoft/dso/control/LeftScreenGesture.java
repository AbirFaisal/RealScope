package com.owon.uppersoft.dso.control;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;

import com.owon.uppersoft.dso.function.MarkCursorControl;
import com.owon.uppersoft.dso.function.perspective.CompositeWaveForm;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.view.ChartScreen;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.dso.view.ViewChart;
import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.dso.wf.ChartScreenSelectModel;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.paint.ScreenContext;
//import com.owon.uppersoft.vds.data.Point;

/**
 * 处理左侧的鼠标事件
 * 
 * @author Matt
 * 
 */
public class LeftScreenGesture {
	private ChartScreenSelectModel ccsm;

	public LeftScreenGesture(ChartScreenSelectModel ccsm) {
		this.ccsm = ccsm;
	}

	private int rollOverChannel;

	public void mousePressed(ViewChart vc, WaveFormManager wfm) {
		if (rollOverChannel >= 0) {
			/**
			 * 必须同时满足两者， 只针对左侧，
			 * 
			 * 同时只有当出现了新的有效值才会覆盖前值，这样才是鼠标选中的前置效果要求的条件
			 */
			ccsm.setScreenSelectWFidx(rollOverChannel);
			ccsm.setRight(false);
			vc.re_paint();
		}
	}

	private boolean left;

	public boolean checkLeft(int x, int y, Rectangle r, ViewChart vc,
			ChartScreenMouseGesture csmg, WaveFormManager wfm, boolean lastLeft) {
		rollOverChannel = -1;
		left = false;
		if (x < r.x) {
			left = true;

			rollOverChannel = wfm.isWaveFormRollOver(vc.getScreenContext(), r,
					y);
			if (rollOverChannel >= 0) {
				csmg.cursor(Cursor.N_RESIZE_CURSOR);
			} else {
				csmg.cursor(Cursor.DEFAULT_CURSOR);
			}
		} else if (lastLeft) {// || left
			csmg.cursor(Cursor.DEFAULT_CURSOR);
		}
		return left;
	}

	public void dragLeft(int x, int y, ScreenContext pc, ChartScreen cs,
			MainWindow mw, WaveFormManager wfm, Point loc0,
			MarkCursorControl mcctr, boolean commit) {
		int selectWFidx = ccsm.getScreenSelectWFidx();
		if (selectWFidx < 0)
			return;

		boolean ScreenMode_3 = pc.isScreenMode_3();
		int hc = pc.getHcenter();
		int dy = loc0.y - y;
		if (selectWFidx == CompositeWaveForm.CompositeWaveFormIndex) {
			CompositeWaveForm cwf = wfm.getCompositeWaveForm();
			cwf.onShowPos0 = true;
			int yv = compute(cwf.getYloc(), dy, x, y, loc0, ScreenMode_3);
			cwf.setYlocIncrement(yv, ScreenMode_3, hc);
			// yv = mcctr.limitPos0Edge(yv);// 限制零点位置
			cs.rebuffer();
		} else {
			WaveForm wf = wfm.getWaveForm(selectWFidx);
			ChannelInfo ci = wf.wfi.ci;
			int pos0 = ci.getPos0();
			wf.onShowPos0 = true;
			int yv = compute(pos0, dy, x, y, loc0, ScreenMode_3);
			wfm.setZeroYLoc(wf, yv, commit);
			mcctr.computeYValues(ci.getPos0(), ci.getVoltValue());
			// yv = mcctr.limitPos0Edge(yv);// 限制零点位置

			mw.getToolPane().getInfoPane().updatePos0(ci.getNumber());
			mw.update_Pos0();
		}
	}

	private int compute(int pos0, int dy, int x, int y, Point loc0,
			boolean ScreenMode_3) {
		int yv;
		if (ScreenMode_3) {
			yv = pos0 + dy;
			loc0.x = x;
			loc0.y = y;
		} else {
			/** 采用鼠标所在位置的近似值进行设置 */
			yv = pos0 + (dy >> 1);// (pc.hcenter - y) >> 1;
			int rem = dy % 2;
			loc0.x = x;
			loc0.y = y + (dy > 0 ? rem : (-rem));
		}
		return yv;
	}

	public void mouseReleased(WaveFormManager wfm) {
		int selectWFidx = ccsm.getScreenSelectWFidx();
		int wfnum = wfm.getWaveFormInfoControl().getLowMachineChannels();
		if (selectWFidx == CompositeWaveForm.CompositeWaveFormIndex) {
			wfm.getCompositeWaveForm().onShowPos0 = false;
		} else if (selectWFidx >= 0 && selectWFidx < wfnum) {
			WaveForm wf = wfm.getWaveForm(selectWFidx);
			wf.onShowPos0 = false;
		}
		ccsm.resetNoneSelect();
	}
}