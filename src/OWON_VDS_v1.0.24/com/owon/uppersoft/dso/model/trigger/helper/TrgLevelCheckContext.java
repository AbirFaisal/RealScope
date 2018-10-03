package com.owon.uppersoft.dso.model.trigger.helper;

import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.paint.ScreenContext;

public class TrgLevelCheckContext {

	public int upborder;
	public int downborder;
	public int mousey;
	public int hc;
	public boolean screenMode_3;
	public boolean inverted;
	public int pos0;

	public int checkAroundSpace;
	private int checkAroundSpaceOut;
	public int channel;

	public TrgLevelCheckContext() {
	}

	/**
	 * 第1步
	 * 
	 * @param upborder
	 * @param downborder
	 * @param mousey
	 * @param pc
	 * @param space
	 */
	public void setEnvironment(int upborder, int downborder, int mousey,
			ScreenContext pc, int space) {
		this.upborder = upborder;
		this.downborder = downborder;
		this.mousey = mousey;

		this.hc = pc.getHcenter();
		this.screenMode_3 = pc.isScreenMode_3();

		this.checkAroundSpace = space;
		checkAroundSpaceOut = space << 1;
	}

	/**
	 * 第2步
	 * 
	 * @param wf
	 */
	public void setWaveForm(WaveForm wf) {
		this.channel = wf.getChannelNumber();
		this.inverted = wf.isInverted();
		this.pos0 = wf.getPos0ForADC();
	}

	/**
	 * 第3步
	 * 
	 * @param targetv
	 * @return
	 */
	public boolean checkAroundTrgLabelInner(int targetv) {
		// 反相在判断范围的时候，把触发信息反一下判断是否在附近
		if (inverted) {
			targetv = ChannelInfo.getLevelFromPos0(targetv, pos0);
		}

		if (screenMode_3)
			targetv = hc - targetv;
		else
			targetv = hc - (targetv << 1);

		// 用"<=,>=",不用"<,>"可以把在边界归到边界外一并处理
		if (targetv <= upborder || targetv >= downborder) {
			return false;
		}
		if (targetv > mousey - checkAroundSpace
				&& targetv <= mousey + checkAroundSpace) {
			return true;
		}
		return false;
	}
	
	/**
	 * 第4步
	 * 
	 * @param targetv
	 * @return
	 */
	public boolean checkAroundTrgLabelOuter(int targetv) {
		// 反相在判断范围的时候，把触发信息反一下判断是否在附近
		if (inverted) {
			targetv = ChannelInfo.getLevelFromPos0(targetv, pos0);
		}

		if (screenMode_3)
			targetv = hc - targetv;
		else
			targetv = hc - (targetv << 1);
		// System.out.println("trgLevelCheckContex:"+(checkAroundSpace << 1));
		// System.out.println(targetv+","+upborder);

		if ((mousey < upborder + checkAroundSpaceOut) && (targetv <= upborder)) {
			return true;
		} else if (mousey > downborder - checkAroundSpaceOut
				&& targetv >= downborder) {
			return true;
		}
		return false;
	}

}