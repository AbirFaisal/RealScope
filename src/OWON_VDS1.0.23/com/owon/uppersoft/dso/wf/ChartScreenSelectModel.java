package com.owon.uppersoft.dso.wf;

import com.owon.uppersoft.dso.model.trigger.TrgCheckType;
import com.owon.uppersoft.vds.core.trigger.help.RightTrgLevelInfoProvider;

/**
 * ChartScreenSelectModel，包含选取的通道，及触发电平和阈值的选取状态
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
		setScreenSelectWFidx(-1);
	}

	/**
	 * ------------------------------------------------------------------------
	 * ------------------------------
	 */
	/**
	 * x:点击或拖拽的通道名，y：如果为阈值，UppOver = 1, LowOver = 2;
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
	 * 处理判断类型
	 * 
	 * @param type
	 * @param channel
	 * @return 是否被选择到了
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
	/** 是否显示右侧的触发电平箭头 */
	private boolean arrowDraw = false;

	private boolean trgInfoControlActive = false;

	public boolean isTrgInfoControlActive() {
		return trgInfoControlActive;
	}

	/**
	 * 标记在右下角弹出触发电平控件的情况
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
	 * 标记鼠标在右侧需要画箭头框的情况
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
		setArrowDraw(true);
		setTrgInfoControlActive(true);
		setTrgOverInfo(chl, type);
	}

	public void update_DoneLevelChange() {
		if (!onRightArea)
			setArrowDraw(false);
		setTrgInfoControlActive(false);
		resetEmptyTrgOverInfo();
	}

	public void setTrgOverInfo(int channel, TrgCheckType type) {
		setScreenSelectWFidx(channel);
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
	 * 是否出现了需要画线的情况，至于哪个通道画，有selectWFidx确定
	 * 
	 * selectWFidx被重复使用了，作为左右两侧选定通道的索引。
	 * 
	 * 则需要判断只有满足右侧或是右下角控件模拟状态时，才认为需要画触发电平线
	 * 
	 * @return
	 */
	@Override
	public boolean shouldLine() {
		// System.out.println("right:"+right+",  isactive:"+isTrgInfoControlActive());
		return (right || isTrgInfoControlActive())
				&& getScreenSelectWFidx() >= 0;
	}

	protected boolean shouldLevelShow() {
		return (right) && getScreenSelectWFidx() >= 0;
	}

	public boolean isOnShowHtpDetail() {
		return onShowHtpDetail;
	}

	public void setOnShowHtpDetail(boolean onShowHtpDetail) {
		this.onShowHtpDetail = onShowHtpDetail;
	}

	private boolean onShowHtpDetail = false;// , onShowTrgDetail
}