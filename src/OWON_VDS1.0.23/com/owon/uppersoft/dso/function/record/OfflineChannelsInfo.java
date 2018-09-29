package com.owon.uppersoft.dso.function.record;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.owon.uppersoft.vds.core.data.CByteArrayInputStream;
import com.owon.uppersoft.vds.core.machine.VDS_Portable;
import com.owon.uppersoft.vds.core.wf.rt.ChannelDataInfo;
import com.owon.uppersoft.vds.core.wf.rt.ChannelsTransportInfo;
import com.owon.uppersoft.vds.util.format.SFormatter;

/**
 * OfflineChannelsInfo，给录制文件用
 * 
 */
public class OfflineChannelsInfo extends ChannelsTransportInfo {
	public static final int RECORDPROTOCOL_VDS_INVERSE = 1;// 从1开始，0表示原型版本
	private static final int RECORDPROTOCOL_VDS_EXTEND = 2;// 从2开始，加入记录格式标识
	private static final int RECORDPROTOCOL_VDS_PK = 3;// 从3开始，加入峰值检测
	private static final int RECORDPROTOCOL_VDS_DM = 4;// 从4开始，加入深存储检测
	public static final int RECORDPROTOCOL_VDS_CURRENT = RECORDPROTOCOL_VDS_DM;// 始终使用软件当前的版本

	public OfflineChannelsInfo() {
	}

	@Override
	protected ChannelDataInfo createChannelDataInfo() {
		return new ChannelDataInfo();
	}

	/**
	 * 边读取，边判断是否为录制格式
	 * 
	 * @param ba
	 * @return 若否停止读取录制文件
	 */
	public boolean setMore_Record(CByteArrayInputStream ba) {
		/** 录制格式 4字节 低3位保留，用于区别其他文件 */
		if (record_version >= RECORDPROTOCOL_VDS_EXTEND) {
			extend = ba.nextInt();

			int v = (extend >>> 24);
			if (v != VDS_Portable.FILEFORMAT_RECORD) {
				return false;
			}
			return true;
		}
		return true;
	}

	public void setPKDetect_Record(CByteArrayInputStream ba) {
		if (record_version >= RECORDPROTOCOL_VDS_PK) {
			isPKDetect = ba.nextBoolean();
		} else {
			isPKDetect = false;
		}
	}

	public void setDMDetect_Record(CByteArrayInputStream ba) {
		DMlen = -1;
		if (record_version >= RECORDPROTOCOL_VDS_DM) {
			DMlen = ba.nextInt();
		}
	}

	public boolean isPKDetect = false;
	public int DMlen;

	/* 时基挡位 4 字节 */
	public int timebase;

	/* 水平触发位置 */
	public int horTrgPos;

	public void log() {
		config("***************\r\n");
		config(SFormatter.dataformat("dso_id: %d\r\n", machine_type));
		config(SFormatter.dataformat("DMem: %s\r\n", (char) getDMem()));
		config(SFormatter.dataformat("horTrgPos: %d\r\n", horTrgPos));
		config(SFormatter.dataformat("timebase: %d\r\n", timebase));
	}

	private void config(String string) {
	}

	/** 读取专用 */
	public int machine_type;// 导出时的上位机软件处理方式版本-机型版本
	public int record_version;// 录制格式-版本版本

	public int extend;// 扩展格式 -OfflineDM & 记录文件

	private List<ChannelDataInfo> channels = new LinkedList<ChannelDataInfo>();

	public void clearChannels() {
		// System.out.println("clearChannels");
		channels.clear();
	}

	@Override
	public ChannelDataInfo getUnUsedChannelDataInfo(int idx) {
		return channels.get(idx);
	}

	public ChannelDataInfo getChannelDataInfo(int chl) {
		for (ChannelDataInfo cdi : channels) {
			if (cdi.chl == chl)
				return cdi;
		}
		return null;
	}

	public void addChannelDataInfo(ChannelDataInfo cdi) {
		channels.add(cdi);
	}

	public int getChannelCount() {
		return channels.size();
	}

	@Override
	public Iterator<ChannelDataInfo> iterator_ChannelDataInfo() {
		return channels.iterator();
	}

}
