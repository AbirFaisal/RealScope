package com.owon.uppersoft.vds.source.comm.data;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.owon.uppersoft.vds.core.wf.rt.ChannelDataInfo;
import com.owon.uppersoft.vds.core.wf.rt.ChannelsTransportInfo;

public class ChannelsTransportInfo_Tiny extends ChannelsTransportInfo {
	
	public boolean fetchUntilData = false;
	
	public int channel_trg_status;
	
	@Override
	protected ChannelDataInfo createChannelDataInfo() {
		return new ChannelDataInfo_Tiny();
	}

	public void tailhandle(ChannelDataInfo_Tiny cdi, ByteBuffer result, int slow) {
		cdi.setResultRTBuf(result);

		slowMove = cdi.slowMove = slow;
		// logln("slowMove: " + cdi.slowMove);
		int len = result.remaining();
		points = cdi.datalen = len;
		screendatalen = cdi.screendatalen = len;
		// logln("screendatalen: " + len);
	}

	public Iterator<ChannelDataInfo_Tiny> iterator_ChannelDataInfo() {
		return channels.iterator();
	}

	@Override
	public ChannelDataInfo_Tiny getUnUsedChannelDataInfo(int idx) {
		return channels.get(idx);
	}

	private List<ChannelDataInfo_Tiny> channels = new LinkedList<ChannelDataInfo_Tiny>();

	public void clearChannels() {
		// System.out.println("clearChannels");
		channels.clear();
	}

	public ChannelDataInfo_Tiny getChannelDataInfo(int chl) {
		for (ChannelDataInfo_Tiny cdi : channels) {
			if (cdi.chl == chl)
				return cdi;
		}
		return null;
	}

	public void addChannelDataInfo(ChannelDataInfo cdi) {
		channels.add((ChannelDataInfo_Tiny) cdi);
	}

	public int getChannelCount() {
		return channels.size();
	}

	public void resetTrg_d() {
		for (ChannelDataInfo_Tiny cdi : channels) {
			cdi.trg_d = false;
		}
	}

}