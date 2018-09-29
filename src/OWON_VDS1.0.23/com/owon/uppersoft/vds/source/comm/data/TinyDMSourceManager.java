package com.owon.uppersoft.vds.source.comm.data;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Iterator;

import com.owon.uppersoft.dso.model.DMDataInfoTiny;
import com.owon.uppersoft.dso.source.manager.IDMSourceManager;
import com.owon.uppersoft.vds.core.aspect.base.VLog;
import com.owon.uppersoft.vds.core.wf.dm.DMInfo;
import com.owon.uppersoft.vds.core.wf.rt.ChannelsTransportInfo;

public class TinyDMSourceManager implements IDMSourceManager {
	private final ChannelsTransportInfo_Tiny cti;

	public TinyDMSourceManager(ChannelsTransportInfo_Tiny cti) {
		this.cti = cti;
	}

	protected VLog vl = new VLog();

	@Override
	public void acceptDMData(DMInfo ci) {
		ci.DMem = 'M';
		ci.status = ChannelsTransportInfo.Status_RT_OK;

		ci.dataComplete = 1;
		ci.triggerStatus = cti.status;

		Iterator<ChannelDataInfo_Tiny> it = cti.iterator_ChannelDataInfo();
		while (it.hasNext()) {
			ChannelDataInfo_Tiny cdi = it.next();
			DMDataInfoTiny dmdi = new DMDataInfoTiny();
			ci.channels.add(dmdi);
			dmdi.chl = cdi.chl;
			vl.loglnf("chl: %d", cdi.chl);

			cdi.save2DMDataInfo(dmdi);

			dmdi.logReceive();

			RandomAccessFile raf = ci.raf;

			try {
				dmdi.filePointer = (int) raf.getFilePointer();

				ByteBuffer bb = cdi.getALLDMBuffer();
				vl.loglnf("fp: %d", dmdi.filePointer);
				vl.logByteBuffer(bb);
				vl.loglnf("remaining: %d", bb.remaining());
				raf.write(bb.array(), bb.position(), bb.remaining());
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}
}