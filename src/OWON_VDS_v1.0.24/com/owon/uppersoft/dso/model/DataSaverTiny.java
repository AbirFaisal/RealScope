package com.owon.uppersoft.dso.model;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;

import com.owon.uppersoft.dso.data.AbsDataSaver;
import com.owon.uppersoft.dso.function.SoftwareControl;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.mode.control.TimeControl;
import com.owon.uppersoft.dso.pref.Define;
import com.owon.uppersoft.dso.source.comm.effect.ReConfigUtil;
import com.owon.uppersoft.dso.source.manager.IDMSourceManager;
import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.dso.wf.ChannelInverseTranslator;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.machine.VDS_Portable;
import com.owon.uppersoft.vds.core.wf.dm.DMDataInfo;
import com.owon.uppersoft.vds.core.wf.dm.DMInfo;
import com.owon.uppersoft.vds.util.FileUtil;
import com.owon.uppersoft.vds.util.StringPool;

public class DataSaverTiny extends AbsDataSaver {

	private int fileVer = DMInfoTiny.FILEPROTOCOL_VDS_CURRENT;// file format version
	private ChannelInverseTranslator cit;

	public DataSaverTiny() {
		cit = new ChannelInverseTranslator();
	}

	/**
	 * Save the file, always filled with the current waveform channel information, usable for RT loading into deep memory or saving a file
	 * 
	 * KNOW need to handle the case where it cannot be obtained: the deep memory data has been lost and matching needs to be redone
	 * 
	 * @param ca
	 * @param cm
	 * @param f
	 * @param pcl
	 * @return
	 */
	public DMInfo saveFileM(ControlManager cm, File f,
			PropertyChangeListener pcl, IDMSourceManager sm) {
		byte t = 'M';
		DMInfoTiny ci = new DMInfoTiny(cm.getCoreControl().getDeepProvider());

		ci.file = f;
		ci.DMem = t;
		ci.dmidx = cm.getDeepMemoryControl().getDeepIdx();
		ci.chlflag = cm.getWaveFormInfoControl().getWaveFormFlag();
		// DBG.dbgln("chlflag: " + Integer.toBinaryString(ci.chlflag));

		long fileLenPos, t1, t2, p1;
		try {
			FileUtil.checkPath(f);
			if (!f.exists()) {
				f.createNewFile();
			}
			RandomAccessFile raf = new RandomAccessFile(f, "rw");
			raf.setLength(0);
			final SoftwareControl sc = cm.getSoftwareControl();
			// KNOW the original header was 10 characters long, later the header is assigned the model number, generally 7 bytes, so "SPB" is added for offline files to load properly
			byte[] b = (sc.getMachineHeader()).getBytes(StringPool.ASCIIString);
			raf.write(b); // file header (10 bytes, ASCII)("SPB" 3 bytes + sc.machine_header 7 bytes)
			raf.writeInt(cm.getMachineTypeForSave()); // 4 bytes model info 7102
			raf.writeInt(fileVer); // 4 bytes version info 1

			raf.writeByte(VDS_Portable.FILEFORMAT_DM);
			raf.writeByte(0);
			raf.writeByte(0);
			raf.writeByte(0);

			fileLenPos = raf.getFilePointer();
			raf.writeInt(0);// file length

			/* data area */
			raf.writeByte(0);// data area entry marker is 0 (1 byte, byte)
			p1 = raf.getFilePointer();// data area entry pointer
			raf.writeInt(0);// data area length

			t1 = raf.getFilePointer();// pointer position of the acquisition point data
			/** Receive data */
			ci.raf = raf;
			ci.pcl = pcl;

			if (ci.chlflag != 0) {
				DataHouse dh = Platform.getDataHouse();
				if (dh.isPlayRecord()) {
				} else
					sm.acceptDMData(ci);

				if (ci.status == ReConfigUtil.Status_RT_NoDM) {
					raf.close();
					return ci;
				}
			}

			t2 = raf.getFilePointer();
			// System.out.println("save dataQlen:"+(t2-t1));
			raf.seek(p1);
			raf.writeInt((int) (t2 - t1));
			raf.seek(t2);

			/* waveform channel area */
			raf.writeByte(1);// waveform channel area entry marker, first byte of the data area stored as 1 (1 byte, byte)
			p1 = raf.getFilePointer();
			raf.writeInt(0);// info area length
			t1 = raf.getFilePointer();

			raf.writeByte(t); // whether deep memory 'T'/'M': (1 byte) memory depth index
			raf.writeInt(GDefine.AREA_WIDTH);
			raf.writeInt(Define.def.AREA_HEIGHT);

			Iterator<DMDataInfo> it = ci.channels.iterator();
			ci.channelNum = ci.channels.size();

			WaveFormManager wfm = Platform.getDataHouse().getWaveFormManager();
			TimeControl tc = cm.getTimeControl();
			int tbidx = tc.getTimebaseIdx();
			raf.writeInt(tbidx);
			raf.writeInt(tc.getHorizontalTriggerPosition());
			raf.writeBoolean(cm.isPeakDetectWork());

			t2 = raf.getFilePointer();

			raf.seek(p1);
			raf.writeInt((int) (t2 - t1));
			raf.seek(t2);

			p1 = raf.getFilePointer();
			raf.writeInt(0);// waveform channel area length
			t1 = raf.getFilePointer();
			// iterative storage

			// BigDecimal bd = controlManager.getCurrentSampleRateBD_kHz();
			// MachineType mi = controlManager.getMachine();

			while (it.hasNext()) {
				DMDataInfo cd = it.next();
				WaveForm wf = wfm.getWaveForm(cd.chl);
				ChannelInfo chi = wf.wfi.ci;

				/** Compute freq when obtaining dm; the fft case is not considered here */
				// cd.computeFreq(bd, mi);
				// chi.updateFreqLabel(cd.getFrequencyFloat());
				chi.snapShot2ChannelDataInfo(cd, wf);

				long blockstart = raf.getFilePointer();
				raf.writeBytes(wf.toString());

				long blockLenPos = raf.getFilePointer();
				raf.writeInt(0); // (2)block length: temporarily store 0

				// currently only deep memory files are saved, handled as INVERSE_TYPE_RAW_REVERSE
				raf.writeInt(ChannelInverseTranslator.getInverseType_DM(chi.isInverse()));

				cd.writeRandomAccessFile(raf);
				ci.initPos = cd.initPos;
				ci.datalen = cd.datalen;
				ci.setScreendatalen(cd);
				ci.slowMove = cd.slowMove;

				raf.writeInt((int) cd.filePointer);// pointer position of the acquisition point data

				long blockEnd = raf.getFilePointer();
				int segmentLen = (int) (blockEnd - blockstart);
				raf.seek(blockLenPos);
				raf.writeInt(segmentLen); // (2)block length: store the correct value in place of 0
				raf.seek(blockEnd);
			}
			t2 = raf.getFilePointer();
			// System.out.println("save waveQlen:"+(t2-t1));
			raf.seek(p1);
			raf.writeInt((int) (t2 - t1));

			raf.seek(fileLenPos);
			raf.writeInt((int) raf.length());

			raf.close();
			return ci;
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	@Override
	protected DMDataInfo createDMDataInfo() {
		return new DMDataInfoTiny();
	}

}
