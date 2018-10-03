package com.owon.uppersoft.vds.core.wf.rt;

import java.util.Iterator;
import java.util.LinkedList;

public abstract class LoadMedia {

	public LoadMedia() {
	}

	public abstract int getChannelCount();

	/**
	 * 两用：在填充需要的cdi后，从中遍历幅值的通道数据；幅值后在需要时遍历通道
	 * 
	 * @return
	 */
	public abstract Iterator<? extends ChannelDataInfo> iterator_ChannelDataInfo();

	public abstract <T extends ChannelDataInfo> void addChannelDataInfo(T cdi);

	public abstract <T extends ChannelDataInfo> T getChannelDataInfo(int idx);

	public abstract <T extends ChannelDataInfo> T getUnUsedChannelDataInfo(
			int idx);

	protected abstract void clearChannels();

	public void gatherEnoughChannelDataInfos(int num) {
		while (getChannelCount() < num) {
			addChannelDataInfo(getInstance());
		}
	}

	private LinkedList<ChannelDataInfo> use = new LinkedList<ChannelDataInfo>();

	/**
	 * 使用缓冲池内已有的ChannelDataInfo对象，使用前重置
	 * 
	 * @return
	 */
	public ChannelDataInfo getInstance() {
		if (use.isEmpty())
			return createChannelDataInfo();

		ChannelDataInfo cdi = use.removeLast();
		cdi.reset();
		return cdi;
	}

	protected abstract ChannelDataInfo createChannelDataInfo();

	public void retireInstance(ChannelDataInfo cdi) {
		use.addLast(cdi);
	}

	/** 单次获取得到的帧的个数，各通道相同 */
	private int frameCount = 1;

	public void setFrameCount(int frameCount) {
		this.frameCount = frameCount;
	}

	public int getFrameCount() {
		return frameCount;
	}

	public void setDataComplete(int dataComplete) {
		this.dataComplete = dataComplete;
	}

	public int getDataComplete() {
		return dataComplete;
	}

	private int dataComplete = 0;
}
