package com.owon.uppersoft.dso.wf;

import com.owon.uppersoft.dso.model.trigger.TrgCheckType;
import com.owon.uppersoft.vds.core.trigger.help.RightTrgLevelInfoProvider;

/**
 * ChartScreenSelectModel, contains the selected channel, and the selection state of the trigger level and threshold
 * 
 */
public class ChartScreenSelectModel implements RightTrgLevelInfoProvider {
	/**
	 * ------------------------------------------------------------------------
	 * ------------------------------
	 */
	private int screenSelectWFidx;

	public ChartScreenSelectModel() {
		screenSelectWFidx = -1;
	}

	@Override
	public int getScreenSelectWFidx() {
		return screenSelectWFidx;
	}

	public void setScreenSelectWFidx(int idx) {
		screenSelectWFidx = idx;
	}

	public void resetNoneSelect() {
		screenSelectWFidx = -1;
	}

	/**
	 * ------------------------------------------------------------------------
	 * ------------------------------
	 */
	/**
	 * x: name of the clicked or dragged channel, y: if it is a threshold, UppOver = 1, LowOver = 2;
	 */
	private int operateChannel = -1;
	private TrgCheckType operateThredshodType = TrgCheckType.NotOver;

	public void setOperateChannelAndThredshodType(int chl, TrgCheckType type) {
		operateChannel = chl;
		operateThredshodType = type;
	}

	public void resetOperateChannelAndThredshodType() {
		operateChannel = -1;
		operateThredshodType = TrgCheckType.NotOver;
	}

	public TrgCheckType getOperateThredshodType() {
		return operateThredshodType;
	}

	public int getOperateChannel() {
		return operateChannel;
	}

	/**
	 * Process and determine the type
	 * 
	 * @param type
	 * @param channel
	 * @return whether it was selected
	 */
	public void handleCheckedType(int channel, TrgCheckType type) {
		switch (type) {
		case NotOver:
			resetOperateChannelAndThredshodType();
			break;
		case VoltsenseOver:
//			setOperateChannelAndThredshodType(channel, TrgCheckType.NotOver);
//			break;
		case UppOver:
		case LowOver:
			setOperateChannelAndThredshodType(channel, type);
			break;
		default:
			break;
		}
	}

	/**
	 * ------------------------------------------------------------------------
	 * ------------------------------
	 */
	/** Whether to show the trigger level arrow on the right side */
	private boolean arrowDraw = false;

	private boolean trgInfoControlActive = false;

	public boolean isTrgInfoControlActive() {
		return trgInfoControlActive;
	}

	/**
	 * Marks the case where the trigger level control pops up at the bottom right
	 * 
	 * @param active
	 */
	public void setTrgInfoControlActive(boolean active) {
		trgInfoControlActive = active;
	}

	@Override
	public boolean isDrawArrow() {
		return arrowDraw || trgInfoControlActive;
	}

	/**
	 * Marks the case where the mouse is on the right side and the arrow box needs to be drawn
	 * 
	 * @param draw
	 */
	public void setArrowDraw(boolean draw) {
		arrowDraw = draw;
	}

	/**
	 * ------------------------------------------------------------------------
	 * ------------------------------
	 */
	public void update_ChangeLevel(int chl, TrgCheckType type) {
		arrowDraw = true;
		trgInfoControlActive = true;
		setTrgOverInfo(chl, type);
	}

	public void update_DoneLevelChange() {
		if (!onRightArea) {
			arrowDraw = false;
		}
		trgInfoControlActive = false;
		resetEmptyTrgOverInfo();
	}

	public void setTrgOverInfo(int channel, TrgCheckType type) {
		screenSelectWFidx = channel;
		setOperateChannelAndThredshodType(channel, type);
	}

	public void resetEmptyTrgOverInfo() {
		setTrgOverInfo(-1, TrgCheckType.NotOver);
	}

	/**
	 * ------------------------------------------------------------------------
	 * ------------------------------
	 */
	private boolean onRightArea = false;
	private boolean right = false;

	public boolean isOnRightArea() {
		return onRightArea;
	}

	public boolean isRight() {
		return right;
	}

	public void setOnRightArea(boolean onRightArea) {
		this.onRightArea = onRightArea;
	}

	public void setRight(boolean right) {
		this.right = right;
	}

	/**
	 * ------------------------------------------------------------------------
	 * ------------------------------
	 */
	/**
	 * Whether a line needs to be drawn; which channel draws it is determined by selectWFidx
	 * 
	 * selectWFidx is reused, as the index of the selected channels on the left and right sides.
	 * 
	 * so the trigger level line is considered to need drawing only when the right side or the bottom-right control simulated state is satisfied
	 * 
	 * @return
	 */
	@Override
	public boolean shouldLine() {
		// System.out.println("right:"+right+",  isactive:"+isTrgInfoControlActive());
		return (right || trgInfoControlActive)
				&& screenSelectWFidx >= 0;
	}

	protected boolean shouldLevelShow() {
		return (right) && screenSelectWFidx >= 0;
	}

	public boolean isOnShowHtpDetail() {
		return onShowHtpDetail;
	}

	public void setOnShowHtpDetail(boolean onShowHtpDetail) {
		this.onShowHtpDetail = onShowHtpDetail;
	}

	private boolean onShowHtpDetail = false;// , onShowTrgDetail
}