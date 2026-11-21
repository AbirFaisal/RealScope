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
 * OfflineChannelsInfo, for recording files
 * 
 */
public class OfflineChannelsInfo extends ChannelsTransportInfo {
	public static final int RECORDPROTOCOL_VDS_INVERSE = 1;// starts from 1, 0 denotes the prototype version
	private static final int RECORDPROTOCOL_VDS_EXTEND = 2;// starts from 2, adding the record format identifier
	private static final int RECORDPROTOCOL_VDS_PK = 3;// starts from 3, adding peak detect
	private static final int RECORDPROTOCOL_VDS_DM = 4;// starts from 4, adding deep memory detection
	public static final int RECORDPROTOCOL_VDS_CURRENT = RECORDPROTOCOL_VDS_DM;// always use the software's current version

	public OfflineChannelsInfo() {
	}

	@Override
	protected ChannelDataInfo createChannelDataInfo() {
		return new ChannelDataInfo();
	}

	/**
	 * Read while checking whether it is a recording format
	 * 
	 * @param ba
	 * @return if not, stop reading the recording file
	 */
	public boolean setMore_Record(CByteArrayInputStream ba) {
		/** Recording format, 4 bytes, low 3 bits reserved, used to distinguish it from other files */
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

	/* Timebase scale, 4 bytes */
	public int timebase;

	/* Horizontal trigger position */
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

	/** For reading only */
	public int machine_type;// version of the host software's handling at export time - model version
	public int record_version;// recording format - version

	public int extend;// extended format - OfflineDM & record file

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
