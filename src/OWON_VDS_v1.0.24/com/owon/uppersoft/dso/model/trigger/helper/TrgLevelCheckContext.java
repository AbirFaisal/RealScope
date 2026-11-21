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
	 * Step 1
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
	 * Step 2
	 * 
	 * @param wf
	 */
	public void setWaveForm(WaveForm wf) {
		this.channel = wf.getChannelNumber();
		this.inverted = wf.isInverted();
		this.pos0 = wf.getPos0ForADC();
	}

	/**
	 * Step 3
	 * 
	 * @param targetv
	 * @return
	 */
	public boolean checkAroundTrgLabelInner(int targetv) {
		// When inverted, flip the trigger information when judging the range to determine whether it is nearby
		if (inverted) {
			targetv = ChannelInfo.getLevelFromPos0(targetv, pos0);
		}

		if (screenMode_3)
			targetv = hc - targetv;
		else
			targetv = hc - (targetv << 1);

		// Using "<=,>=" instead of "<,>" lets points on the boundary be handled together with those outside the boundary
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
	 * Step 4
	 * 
	 * @param targetv
	 * @return
	 */
	public boolean checkAroundTrgLabelOuter(int targetv) {
		// When inverted, flip the trigger information when judging the range to determine whether it is nearby
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