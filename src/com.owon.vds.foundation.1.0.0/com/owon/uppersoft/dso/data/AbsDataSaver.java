package com.owon.uppersoft.dso.data;

import static com.owon.uppersoft.vds.core.wf.rt.ChannelsTransportInfo.Status_RT_UnknownErr;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Iterator;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.source.manager.IDMSourceManager;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.wf.dm.DMDataInfo;
import com.owon.uppersoft.vds.core.wf.dm.DMInfo;
import com.owon.uppersoft.vds.core.wf.rt.ChannelDataInfo;
import com.owon.uppersoft.vds.core.wf.rt.ChannelsTransportInfo;

public abstract class AbsDataSaver {

	public AbsDataSaver() {
	}

	protected abstract DMDataInfo createDMDataInfo();

	public abstract DMInfo saveFileM(ControlManager cm,
			File f, PropertyChangeListener pcl, IDMSourceManager sm);

	public void persistPlayingSingleFrameData(DMInfo dmi, RandomAccessFile raf,
			ChannelsTransportInfo cti) {
		WaveFormManager wfm = Platform.getDataHouse().getWaveFormManager();
		int chlnum, counter, chlStatus;
		dmi.status = Status_RT_UnknownErr;
		// PropertyChangeListener pcl = dmi.pcl;

		dmi.DMem = cti.getDMem();
		dmi.dataComplete = cti.getDataComplete();
		dmi.triggerStatus = cti.triggerStatus;
		chlStatus = cti.channelStatus;
		counter = chlnum = dmi.setChannelStatus(chlStatus);// 0?
		// pcl.propertyChange(new PropertyChangeEvent(this,
		// PropertiesItem.CHL_NUM, 0, chlnum));

		long sizePos = 0;
		try {
			sizePos = raf.getFilePointer();
			Iterator<? extends ChannelDataInfo> it = cti
					.iterator_ChannelDataInfo();
			println("cti.hasNext?:" + it.hasNext());
			while (it.hasNext()) {
				ChannelDataInfo cdi = it.next();
				DMDataInfo dmdi = createDMDataInfo();
				WaveForm wf = wfm.getWaveForm(cdi.chl);
				dmi.channels.add(dmdi);
				dmdi.chl = cdi.chl;
				println("dmdi.chl:" + dmdi.chl);
				dmdi.datalen = cdi.datalen;
				println("dmdi.datalen:" + dmdi.datalen);
				dmdi.setFreqBaseNRef(0, 0);// (b, rf);
				dmdi.initPos = cdi.initPos;
				println("dmdi.initPos:" + dmdi.initPos);
				dmdi.screendatalen = cdi.screendatalen;
				int i = 0;
				byte[] arr2nd = new byte[16];
				dmdi.receivePlugInfo(arr2nd, i);
				dmdi.slowMove = cdi.slowMove;

				// pcl.propertyChange(new PropertyChangeEvent(this,
				// PropertiesItem.DATA_LEN, 0, cdi.datalen));

				// RandomAccessFile raf = dmi.raf;
				// pcl.propertyChange(new PropertyChangeEvent(this,
				// PropertiesItem.PROGRESS, 0, left));
				dmdi.filePointer = (int) raf.getFilePointer();
				ByteBuffer bb = wf.getNextFrameADCBuffer(0);// i
				byte[] arr = null;
				if (bb == null || (arr = bb.array()) == null) {
					raf.seek(sizePos);
					return;
				} else {
					println(",filePointer:" + raf.getFilePointer());
					// ArrayLogger.configArray(bb.array(), bb.position(),
					// bb.remaining());
				}
				println("position:" + bb.position() + ",initPos:" + cdi.initPos
						+ ",screendatalen" + cdi.screendatalen);
				raf.write(arr, bb.position() + cdi.initPos, cdi.screendatalen);

				// pcl.propertyChange(new PropertyChangeEvent(this,
				// PropertiesItem.CHL_DONE, 0, chlnum - counter));
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		}
		// pcl.propertyChange(new PropertyChangeEvent(this,
		// PropertiesItem.TRANS_DONE, 0, chlnum));
		// dmi.status = Status_RT_OK;
		// return;

		// pcl.propertyChange(new PropertyChangeEvent(this,
		// PropertiesItem.TRANS_FAIL, null, null));
		// return;

	}

	private void println(String txt) {
	}
}
