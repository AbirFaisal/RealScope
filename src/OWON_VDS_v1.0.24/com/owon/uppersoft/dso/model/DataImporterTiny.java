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
			// TODO 改变数据存储区使用的文件
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
		/** 文件头 6 字节 */
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
		/** 这样参考波形就会写入正确的机型信息 */
		cm.getSoftwareControl().setMachineHeader(strFileHead);

		/** 机型信息 4 字节 */
		int machID = ba.nextInt();
		ci.machine_type = machID;
		if (!cm.isSuitableMachineType(machID)) {
			// String machine_name = controlManager.getMachineTypeName(machID);
			cm.reloadManager.prepareReloadDM(machID, fp);
			return null;
		}

		/** 版本信息 4 字节 */
		int intEdition = ba.nextInt();
		ci.file_version = intEdition;

		if (!ci.setMore_OfflineDM(ba)) {
			return null;
		}

		/** 文件长度 4 字节 */
		int intFileSize = ba.nextInt();

		/** 数据区标记0 */
		byte dataMark = ba.nextByte();
		/** 区长度 */
		int dataLen = ba.nextInt();
		/** 区点数据 */
		ba.skip(dataLen);

		/** 波形通道区标记1 */
		byte wavechannelMark = ba.nextByte();
		/** 区长度 */
		int infoLen = ba.nextInt();
		int ptr = ba.pointer();

		/** 'T'/'M' 1 字节 */
		byte isTorM = ba.nextByte();
		/** 在传输的数据点中的起始画图点位置 4字节 */
		ci.DMem = isTorM;

		/** 水平可画的像素点宽 4 字节 */
		int horizonPixel = ba.nextInt();
		/** 竖直可画的像素点宽 4 字节 */
		int verticalPixel = ba.nextInt();
		int tbidx = ba.nextInt();
		// tbidx = Define.machine.getTimebaseIndex("20us");//

		ci.timebase = tbidx;
		/** 水平触发位置 */
		int htp = ba.nextInt();
		ci.horTrgPos = htp;

		ci.setPKDetect_DM(ba);

		ptr = infoLen - (ba.pointer() - ptr);
		ba.skip(ptr);
		/** 区长度 */
		int wavechannelLen = ba.nextInt();

		while (ba.available() >= 44) {
			ptr = ba.pointer();

			/** 波形名 3 字节 */
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

			/** 块大小 4 字节 */
			int intBlockSize = ba.nextInt();

			cdi.setInverseType_OfflineDM(ba, intEdition);

			int initPos = ba.nextInt();
			ci.initPos = cdi.initPos = initPos;

			/** 满屏画图点个数 4字节 */
			int screenDataLen = ba.nextInt();

			// ba.buf().putInt(1000);
			// System.err.println(ba.nextInt());

			// screenDataLen = 1000;//
			cdi.screendatalen = screenDataLen;
			ci.setScreendatalen(cdi);

			cdi.setPullTrg_OfflineDM(ba, intEdition);

			/** 传输的数据点个数 4字节 */
			int transNum = ba.nextInt();
			// transNum = 1000000;
			ci.datalen = cdi.datalen = transNum;

			// cdi.initPos = (transNum - screenDataLen) >> 1;

			/** 慢扫描备用 4 字节 */
			int slowMove = ba.nextInt();
			ci.slowMove = cdi.slowMove = slowMove;

			/** 零点位置 4 字节 */
			int pos0 = ba.nextInt();
			cdi.pos0 = pos0;

			// System.out.println(cdi.pos0);
			/** 电压档位 4 字节 */
			int vbIdx = ba.nextInt();
			cdi.vbIdx = vbIdx;
			/** 衰减倍率指数 4 字节 */
			int probeMultiIdx = ba.nextInt();
			cdi.probeMultiIdx = probeMultiIdx;
			/** 频率 4 字节 */
			float frequency = ba.nextFloat();
			cdi.setFreq(frequency);
			/** 周期 4 字节 */
			float cycle = ba.nextFloat();

			/** 采集点数据指针位置 4 字节 */
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
