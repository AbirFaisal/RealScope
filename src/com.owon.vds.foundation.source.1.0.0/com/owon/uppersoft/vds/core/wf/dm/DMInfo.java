package com.owon.uppersoft.vds.core.wf.dm;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.LinkedList;

import com.owon.uppersoft.vds.core.aspect.control.DeepProvider;
import com.owon.uppersoft.vds.core.data.CByteArrayInputStream;
import com.owon.uppersoft.vds.core.machine.VDS_Portable;
import com.owon.uppersoft.vds.util.format.SFormatter;

public abstract class DMInfo {

	public byte DMem = 0;
	public LinkedList<DMDataInfo> channels = new LinkedList<DMDataInfo>();
	public int timebase;
	public int horTrgPos;

	public int machine_type;

	public int dataComplete;
	public int channelNum;

	public int status;
	public byte chlflag;
	public int dmidx;

	private int screendatalen;

	public int datalen, slowMove;
	public int initPos;

	public RandomAccessFile raf;
	public PropertyChangeListener pcl;
	public File file;

	public int triggerStatus;

	private int channelStatus;

	public DMInfo(DeepProvider dp) {
		this.dp = dp;
	}

	public int file_version;
	protected boolean isPKDetect = false;

	public void setPKDetect_DM(CByteArrayInputStream ba) {
		isPKDetect = ba.nextBoolean();
	}

	public void setPKDetect(boolean isPKDetect) {
		this.isPKDetect = isPKDetect;
	}

	public boolean isPKDetect() {
		return isPKDetect;
	}

	public int extend;// 扩展格式 -OfflineDM & 记录文件

	/**
	 * 边读取，边判断文件有无问题
	 * 
	 * @param ba
	 * @return 文件是否可用
	 */
	public boolean setMore_OfflineDM(CByteArrayInputStream ba) {
		extend = ba.nextInt();

		int v = (extend >>> 24);
		if (v != VDS_Portable.FILEFORMAT_DM) {
			return false;
		} else {
			return true;
		}
	}

	public void log() {
		config("***************\r\n");
		config(SFormatter.dataformat("dso_id: %d\r\n", machine_type));
		config(SFormatter.dataformat("file_format_id: %d\r\n", file_version));
		config(SFormatter.dataformat("DMem: %s\r\n", (char) DMem));
		config(SFormatter.dataformat("horTrgPos: %d\r\n", horTrgPos));
		config(SFormatter.dataformat("timebase: %d\r\n", timebase));
	}

	private void config(String txt) {
		System.out.println(txt);
	}

	public int setChannelStatus(int channelStatus) {
		this.channelStatus = channelStatus;
		return channelNum = Integer.bitCount(channelStatus);
	}

	public int getChannelStatus() {
		return channelStatus;
	}

	private DeepProvider dp;

	public int getDeepMemoryLength(int dmidx) {
		return dp.getLength(dmidx);
	}

	public int getScreendatalen() {
		return screendatalen;
	}

	public void setScreendatalen(DMDataInfo di) {
		screendatalen = di.screendatalen;
	}

}