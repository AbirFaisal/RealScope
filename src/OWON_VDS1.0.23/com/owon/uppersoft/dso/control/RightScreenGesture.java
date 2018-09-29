package com.owon.uppersoft.dso.control;

import java.awt.Point;
import java.awt.Rectangle;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.model.trigger.TrgCheckType;
import com.owon.uppersoft.dso.model.trigger.helper.TrgLevelCheckContext;
import com.owon.uppersoft.dso.model.trigger.helper.TriggerLevelDelegate;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.dso.view.ViewChart;
import com.owon.uppersoft.dso.wf.ChartScreenSelectModel;
import com.owon.uppersoft.dso.wf.WaveForm;
//import com.owon.uppersoft.vds.data.Point;

/**
 * 处理右侧的鼠标事件
 * 
 * @author Matt
 * 
 */
public class RightScreenGesture implements TrgLevelCheckHandler {
	private TriggerLevelDelegate tld;
	private ChartScreenSelectModel ccsm;
	private TrgLevelCheckContext contxt;

	public RightScreenGesture(TriggerLevelDelegate trgc,
			ChartScreenSelectModel ccsm) {
		this.tld = trgc;
		this.ccsm = ccsm;
		contxt = new TrgLevelCheckContext();
	}

	public void mousePressed(ViewChart vc, WaveFormManager wfm) {
		int rollOverChannel = ccsm.getOperateChannel();
		// System.err.println(rollOverChannel+",
		// "+ccsm.getOperateThredshodType());
		if (rollOverChannel >= 0) {
			ccsm.setScreenSelectWFidx(rollOverChannel);
			tld.changeTrgLabel(rollOverChannel, ccsm.getOperateThredshodType(),
					0, wfm, false);
			// 目的只是设置trgOverInfo.thredshodType进去
			vc.re_paint();
		}
	}

	public void mouseExited(ViewChart vc) {
		if (ccsm.isDrawArrow()) {
			ccsm.setArrowDraw(false);
			vc.re_paint();
		}
		ccsm.setRight(false);
	}

	public void mouseReleased() {
		ccsm.resetNoneSelect();
		ccsm.setRight(false);
	}

	public boolean checkRight(int mousex, int mousey, Rectangle r,
			ViewChart vc, DataHouse dh, ChartScreenMouseGesture csmg,
			WaveFormManager wfm) {
		int aw = 120, x1 = r.x + r.width
				+ dh.controlManager.paintContext.getChartInsets().right;

		int upborder = r.y, downborder = upborder + r.height;

		boolean onRightArea = (mousex >= x1 - aw) && (mousex <= x1);
		ccsm.setOnRightArea(onRightArea);

		boolean rp = false;
		if (onRightArea != ccsm.isDrawArrow()) {
			ccsm.setArrowDraw(onRightArea);
			rp = true;
		}
		boolean trgEnable = dh.controlManager.getTriggerControl().isTrgEnable();
		boolean b = onRightArea && (mousey >= upborder)
				&& (mousey <= downborder);

		b = trgEnable && b;
		if (!b) {
			if (rp) {
				vc.re_paint();
			}
			return false;
		}

		/** 自身内部状态准备完毕，交由delegate进行check，之后从ccsm中获取check-handle结果 */
		contxt.setEnvironment(upborder, downborder, mousey, vc
				.getScreenContext(), CheckAroundSpace);
		ccsm.resetOperateChannelAndThredshodType();

		tld.checkAllAroundTrgLabel(this, wfm);

		int rollOverChannel = ccsm.getOperateChannel();
		boolean right = rollOverChannel >= 0;
		ccsm.setRight(right);
		// if (right) System.out.println(rollOverChannel);

		if (rp) {
			vc.re_paint();
		}
		return right;
	}

	public static final int CheckAroundSpace = 6;

	public void dragRight(int x, int y, boolean ScreenMode_3, ViewChart vc,
			MainWindow mw, WaveFormManager wfm, Point loc0, boolean commit,
			ControlManager cm) {

		if (cm.isTrgLevelDisable())
			return;

		int rollOverChannel = ccsm.getOperateChannel();
		// cm.onShowTrgDetail = true;
		int del = loc0.y - y;
		// System.out.println(trgOverInfo + ", " + rollOverChannel);

		if (ScreenMode_3) {
			loc0.x = x;
			loc0.y = y;
		} else {
			/** 采用鼠标所在位置的近似值进行设置 */
			int rem = del % 2;
			loc0.x = x;
			loc0.y = y + (del > 0 ? rem : (-rem));
			del = del >> 1;
		}

		TrgCheckType othredshodType = ccsm.getOperateThredshodType();
		if (rollOverChannel >= 0) {
			tld.changeTrgLabel(rollOverChannel, othredshodType, del, wfm,
					commit);
			// mainWindow.getToolPane().updateTrgVolt();
		}
		vc.re_paint();
	}

	@Override
	public void setWaveForm(WaveForm wf) {
		contxt.setWaveForm(wf);
	}

	@Override
	public boolean checkAroundTrgAndHandle(int targetv, TrgCheckType trueType,
			TrgCheckType falseType) {
		if (trueType == TrgCheckType.NotOver) {
			checkResult = false;
			handleCheckedType(TrgCheckType.NotOver);
			return false;
		}
		checkResult = contxt.checkAroundTrgLabelInner(targetv);
		if (!checkResult)
			checkResult = contxt.checkAroundTrgLabelOuter(targetv);
		handleCheckedType(checkResult ? trueType : falseType);
		return checkResult;
	}

	@Override
	public boolean checkAroundTrgAndHandleOnTrue(int targetv,
			TrgCheckType trueType) {
		if (trueType == TrgCheckType.NotOver) {
			checkResult = false;
			return false;
		}
		checkResult = contxt.checkAroundTrgLabelInner(targetv);
		if (!checkResult)
			checkResult = contxt.checkAroundTrgLabelOuter(targetv);
		if (checkResult)
			handleCheckedType(trueType);
		return checkResult;
	}

	private boolean checkResult;

	/**
	 * 处理判断类型
	 * 
	 * @param type
	 * @param channel
	 * @return 是否被选择到了
	 */
	private void handleCheckedType(TrgCheckType type) {
		ccsm.handleCheckedType(contxt.channel, type);
	}

}