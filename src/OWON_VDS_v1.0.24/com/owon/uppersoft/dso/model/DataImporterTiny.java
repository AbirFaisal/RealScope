package com.owon.uppersoft.dso.model;

import java.awt.Window;
import java.io.File;
import java.io.UnsupportedEncodingException;

import com.owon.uppersoft.dso.control.IDataImporter;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.util.DBG;
import com.owon.uppersoft.vds.core.data.CByteArrayInputStream;
import com.owon.uppersoft.vds.core.machine.VDS_Portable;
import com.owon.uppersoft.vds.ui.prompt.FadeIOShell;
import com.owon.uppersoft.vds.util.StringPool;

public class DataImporterTiny implements IDataImporter {
//	public CByteArrayInputStream cas;

	public DataImporterTiny() {
	}

	public boolean openfile(DataHouse dh, File f) {
		return openfile(dh, f, dh.getMainWindow().getFrame());
	}

	public boolean openfile(DataHouse dh, File f, Window wnd) {
		if (dh.controlManager.isRuntime()) {
			FadeIOShell fs = new FadeIOShell();
			fs.prompt(I18nProvider.bundle().getString("Status.Forbidload"), wnd);
			return false;
		}
		// Platform.getMainWindow().getChartScreen().promptUp();
		ControlManager cm = Platform.getDataHouse().controlManager;
		dh.releaseDeepMemoryStorage();
		cm.reloadManager.releaseReloadFlag();

		DMInfoTiny ci = new DMInfoTiny(cm.getCoreControl().getDeepProvider());
		CByteArrayInputStream ba = new CByteArrayInputStream(f);
		ci.file = f;
		DMInfoTiny cif = readFileLayer(ci, ba, cm, f.getAbsolutePath());
		ba.dispose();
		if (cif != null) {
			// TODO change the file used by the data storage area
			// ci.file = new File("f:\\0.bin");
			if (ci.DMem == 'M') {
				dh.receiveOfflineDMData(cif);
			} else {
				// dh.receiveOfflineData(ci);
			}
			return true;
		} else {
			if (cm.reloadManager.isReloadDM())
				return false;
			FadeIOShell fs = new FadeIOShell();
			fs.prompt(I18nProvider.bundle().getString("M.Utility.LoadInvalid"),
					null);
			return false;
		}
	}

	protected DMInfoTiny readFileLayer(DMInfoTiny ci, CByteArrayInputStream ba,
			ControlManager cm, String fp) {
		int byteFileHeadLen = 10;
		int WaveNameBytes = 3;
		/** File header 6 bytes */
		byte[] fhb = new byte[byteFileHeadLen];
		ba.get(fhb, 0, byteFileHeadLen); // position skip 6 bytes

		String strFileHead = "";
		try {
			strFileHead = new String(fhb, 0, byteFileHeadLen,
					StringPool.ASCIIString);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (!strFileHead.startsWith(VDS_Portable.FileHeader)) {
			return null;
		}
		/** This way the reference waveform will be written with the correct model information */
		cm.getSoftwareControl().setMachineHeader(strFileHead);

		/** Model information 4 bytes */
		int machID = ba.nextInt();
		ci.machine_type = machID;
		if (!cm.isSuitableMachineType(machID)) {
			// String machine_name = controlManager.getMachineTypeName(machID);
			cm.reloadManager.prepareReloadDM(machID, fp);
			return null;
		}

		/** Version information 4 bytes */
		int intEdition = ba.nextInt();
		ci.file_version = intEdition;

		if (!ci.setMore_OfflineDM(ba)) {
			return null;
		}

		/** File length 4 bytes */
		int intFileSize = ba.nextInt();

		/** Data area marker 0 */
		byte dataMark = ba.nextByte();
		/** Area length */
		int dataLen = ba.nextInt();
		/** Area point data */
		ba.skip(dataLen);

		/** Waveform channel area marker 1 */
		byte wavechannelMark = ba.nextByte();
		/** Area length */
		int infoLen = ba.nextInt();
		int ptr = ba.pointer();

		/** 'T'/'M' 1 byte */
		byte isTorM = ba.nextByte();
		/** Start drawing point position within the transferred data points 4 bytes */
		ci.DMem = isTorM;

		/** Horizontal drawable pixel width 4 bytes */
		int horizonPixel = ba.nextInt();
		/** Vertical drawable pixel width 4 bytes */
		int verticalPixel = ba.nextInt();
		int tbidx = ba.nextInt();
		// tbidx = Define.machine.getTimebaseIndex("20us");//

		ci.timebase = tbidx;
		/** Horizontal trigger position */
		int htp = ba.nextInt();
		ci.horTrgPos = htp;

		ci.setPKDetect_DM(ba);

		ptr = infoLen - (ba.pointer() - ptr);
		ba.skip(ptr);
		/** Area length */
		int wavechannelLen = ba.nextInt();

		while (ba.available() >= 44) {
			ptr = ba.pointer();

			/** Waveform name 3 bytes */
			byte[] wnb = new byte[WaveNameBytes];
			ba.get(wnb, 0, WaveNameBytes);

			String WaveName;
			try {
				WaveName = new String(wnb, 0, WaveNameBytes,
						StringPool.ASCIIString);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return null;
			}

			DMDataInfoTiny cdi = new DMDataInfoTiny();
			cdi.chl = wnb[WaveNameBytes - 1] - 48 - 1;
			if (cdi.chl >= cm.getSupportChannelsNumber()) {
				DBG.outprintln("cdi.chl:" + cdi.chl);
				return null;
			}

			/** Block size 4 bytes */
			int intBlockSize = ba.nextInt();

			cdi.setInverseType_OfflineDM(ba, intEdition);

			int initPos = ba.nextInt();
			ci.initPos = cdi.initPos = initPos;

			/** Full-screen drawing point count 4 bytes */
			int screenDataLen = ba.nextInt();

			// ba.buf().putInt(1000);
			// System.err.println(ba.nextInt());

			// screenDataLen = 1000;//
			cdi.screendatalen = screenDataLen;
			ci.setScreendatalen(cdi);

			cdi.setPullTrg_OfflineDM(ba, intEdition);

			/** Transferred data point count 4 bytes */
			int transNum = ba.nextInt();
			// transNum = 1000000;
			ci.datalen = cdi.datalen = transNum;

			// cdi.initPos = (transNum - screenDataLen) >> 1;

			/** Reserved for slow sweep 4 bytes */
			int slowMove = ba.nextInt();
			ci.slowMove = cdi.slowMove = slowMove;

			/** Zero point position 4 bytes */
			int pos0 = ba.nextInt();
			cdi.pos0 = pos0;

			// System.out.println(cdi.pos0);
			/** Voltage scale 4 bytes */
			int vbIdx = ba.nextInt();
			cdi.vbIdx = vbIdx;
			/** Attenuation ratio exponent 4 bytes */
			int probeMultiIdx = ba.nextInt();
			cdi.probeMultiIdx = probeMultiIdx;
			/** Frequency 4 bytes */
			float frequency = ba.nextFloat();
			cdi.setFreq(frequency);
			/** Period 4 bytes */
			float cycle = ba.nextFloat();

			/** Acquisition point data pointer position 4 bytes */
			int collectedDataP = ba.nextInt();
			// collectedDataP = 10027;
			cdi.filePointer = collectedDataP;

			// DBG.errprintln(cdi.filePointer);

			ci.channels.add(cdi);

			ptr = intBlockSize - (ba.pointer() - ptr);
			// System.out.println(ptr + ", av: " + ba.available());
			ba.skip(ptr);

			cdi.logReceive();
		}
		ci.dataComplete = 1;
		ci.channelNum = ci.channels.size();

		// ci.log();

		return ci;
	}

}
