package com.owon.uppersoft.vds.core.wf.rt;

import java.util.Iterator;
import java.util.LinkedList;

public abstract class LoadMedia {

	public LoadMedia() {
	}

	public abstract int getChannelCount();

	/**
	 * Dual use: after filling the needed cdi, traverse the channel data from it for amplitude values; after that traverse channels when needed
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
	 * Use an existing ChannelDataInfo object in the buffer pool, reset before use
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

	/** Number of frames obtained in a single acquisition, the same for each channel */
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
