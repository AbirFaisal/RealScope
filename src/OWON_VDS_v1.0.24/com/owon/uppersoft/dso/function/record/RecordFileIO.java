package com.owon.uppersoft.dso.function.record;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

import com.owon.uppersoft.dso.function.PlayerControl;
import com.owon.uppersoft.dso.function.SoftwareControl;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.mode.control.TimeControl;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.dso.wf.ChannelInverseTranslator;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.data.CByteArrayInputStream;
import com.owon.uppersoft.vds.core.data.OfflineInfo;
import com.owon.uppersoft.vds.core.machine.VDS_Portable;
import com.owon.uppersoft.vds.core.wf.rt.ChannelDataInfo;
import com.owon.uppersoft.vds.core.wf.rt.LoadMedia;
import com.owon.uppersoft.vds.data.Point;
import com.owon.uppersoft.vds.util.StringPool;

public class RecordFileIO {
	private static final int recordVer = OfflineChannelsInfo.RECORDPROTOCOL_VDS_CURRENT;// 文件格式版本

	public static long writeHeader(RandomAccessFile raf, ControlManager cm) {
		try {
			final SoftwareControl sc = cm.getSoftwareControl();
			byte[] b = sc.getMachineHeader().getBytes(StringPool.ASCIIString);
			raf.write(b); // 文件头(10 字节, ASCII)
			raf.writeInt(cm.getMachineTypeForSave()); // 4个字节机型信息 7102
			raf.writeInt(recordVer); // 4个字节版本信息 1

			raf.writeByte(VDS_Portable.FILEFORMAT_RECORD);// 4字节格式信息，后3位保留
			raf.writeByte(0);
			raf.writeByte(0);
			raf.writeByte(0);

			long fileLenPos = raf.getFilePointer();
			raf.writeInt(0);// 文件长度
			raf.writeInt(0);// 录制时间间隔
			raf.writeInt(0);// 帧个数
			return fileLenPos;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

private	static ChannelInverseTranslator cit = new ChannelInverseTranslator();

	/**
	 * 一帧写入
	 * 
	 * @param raf
	 * @param cm
	 * @param wfm
	 * @param cti
	 * @param i
	 */
	public static void writeFrame(RandomAccessFile raf, ControlManager cm,
			WaveFormManager wfm, LoadMedia cti, int i) {
		long sizePos = 0;
		try {
			sizePos = raf.getFilePointer();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			raf.writeInt(0);

			TimeControl tc = cm.getTimeControl();
			raf.writeInt(tc.getTimebaseIdx());
			raf.writeInt(tc.getHorizontalTriggerPosition());
			raf.writeBoolean(cm.isPeakDetectWork());
			raf.writeInt(cm.getDeepMemoryControl().getDeepDataLen());

			Iterator<? extends ChannelDataInfo> it = cti
					.iterator_ChannelDataInfo();
			while (it.hasNext()) {
				ChannelDataInfo cdi = it.next();
				WaveForm wf = wfm.getWaveForm(cdi.chl);
				ChannelInfo ci = wf.wfi.ci;

				raf.write(ci.getNumber());// cd.chl
				long bsizePos = raf.getFilePointer();
				raf.writeInt(0);

				raf.writeInt(ChannelInverseTranslator.getInverseType_Record(ci.isInverse()));

				int initPos = cdi.initPos;
				int fsl = cdi.screendatalen;
				int dl = cdi.datalen;

				// 这里如果做修改，可以节省保存的数据长度
				raf.writeInt(0);// raf.writeInt(initPos);//屏幕起始
				raf.writeInt(fsl);
				raf.writeInt(fsl);// raf.writeInt(dl);

				raf.writeInt(cdi.slowMove);
				raf.writeInt(ci.getPos0());
				raf.writeInt(ci.getVoltbaseIndex());
				raf.writeInt(ci.getProbeMultiIdx());
				raf.writeFloat((float) ci.getFreq());// Hz/S
				raf.writeFloat((float) (1 / ci.getFreq()));// S

				ByteBuffer bb = wf.getNextFrameADCBuffer(i);
				byte[] arr = null;
				if (bb == null || (arr = bb.array()) == null) {
					raf.seek(sizePos);
					return;
				}

				int fillnum = fsl - bb.remaining();
				byte[] b2 = new byte[fillnum];
				raf.write(b2);
				raf.write(arr, bb.position(), bb.remaining());// raf.write(arr,0,dl);

				long bendPos = raf.getFilePointer();

				raf.seek(bsizePos);
				raf.writeInt((int) (bendPos - bsizePos - 4));// -4?块长度存放位置占4字节
				raf.seek(bendPos);
			}

			long endPos = raf.getFilePointer();

			raf.seek(sizePos);
			raf.writeInt((int) (endPos - sizePos - 4));
			raf.seek(endPos);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int readHeader(CByteArrayInputStream ba, PlayerControl pc,
			OfflineChannelsInfo ci) {
		ControlManager cm = Platform.getControlManager();

		int byteFileHeadLen = 10;
		byte[] fhb = new byte[byteFileHeadLen];
		ba.get(fhb, 0, byteFileHeadLen);
		String strFileHead = "";
		try {
			strFileHead = new String(fhb, 0, byteFileHeadLen,
					StringPool.ASCIIString);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		// System.err.println("strFileHead:"+strFileHead);
		if (!strFileHead.startsWith(VDS_Portable.FileHeader)) {
			return -1;
		}

		/** 机型信息 4 字节 */
		int machID = ba.nextInt();
		ci.machine_type = machID;
		if (!cm.isSuitableMachineType(machID)) {
			int r = cm.reloadManager.prepareReloadPlayFile(machID);
			if (r < 0)
				return -1;
		}

		/** 版本信息 4 字节 */
		int intEdition = ba.nextInt();
		ci.record_version = intEdition;

		if (!ci.setMore_Record(ba))
			return -1;

		/** 文件长度 4 字节 */
		int intFileSize = ba.nextInt();
		pc.filesize = intFileSize;

		pc.setTimegap(ba.nextInt());
		pc.counter = ba.nextInt();
		pc.end = pc.counter;

		int p = ba.pointer();
		return p;
	}

	public static void readTimeline(Timeline tl, CByteArrayInputStream ba,
			PlayerControl pc) {
		int c = pc.counter;
		if (c == 0)
			return;

		List<Point> timepoints = tl.timepoints;
		int i = 0;

		int fp = ba.pointer();
		// System.out.println("fp0:" + fp);
		int infoLen = ba.nextInt();// 结构体长度,即单帧长
		ba.skip(infoLen);

		OpenPropertyChangeEvent pce = new OpenPropertyChangeEvent(pc,
				PropertiesItem.TIME_LINE_CHECK, 0, 0);
		pce.newInt = ++i;
		pc.propertyChange(pce);

		Point p = new Point(infoLen, 1);
		// Point p = new Point(infoLen + 4, 1);//方法二,帧头补上4字节
		timepoints.add(p);

		while (i < c) {
			fp = ba.pointer();
			// System.out.println("fp" + i + ":" + fp);
			infoLen = ba.nextInt();

			ba.skip(infoLen);

			pce.newInt = ++i;
			pc.propertyChange(pce);
			// infoLen += 4;//方法二,帧头补上4字节
			if (infoLen != p.x) {
				p.x += 4;// 原帧长加保存该帧长的4字节，才是每帧间隔长度，方法一,帧尾补上4字节
				timepoints.add(p = new Point(infoLen, 1));//
			} else {
				p.y++;
			}

		}
		p.x += 4;// 原帧长加保存该帧长的4字节，才是每帧间隔长度，方法一,帧尾补上4字节
		return;
	}

	public static void readFrame(OfflineChannelsInfo ci,
			CByteArrayInputStream ba) {
		// System.out.println("readfp:" + ba.pointer());
		int infoLen = ba.nextInt();
		// System.out.println("FrameLen: " + infoLen);
		int fptr = ba.pointer();
		// System.out.println("fptr: " + fptr);

		ci.timebase = ba.nextInt();
		ci.horTrgPos = ba.nextInt();
		ci.setPKDetect_Record(ba);
		ci.setDMDetect_Record(ba);

		ci.reset('T');// 清理共享区启存位置
		ChannelDataInfo cdi = null;
		while (ba.pointer() - fptr < infoLen) {// && ba.available() >= 41
			/** 波形名 1 字节 */
			byte wnb = ba.nextByte();
			// System.out.println("CHname: " + wnb);
			cdi = ci.getInstance();
			cdi.chl = wnb;
			/** 块大小 4 字节 */
			int intBlockSize = ba.nextInt();

			int ptr = ba.pointer();

			cdi.setInverseType_Record(ba, ci.record_version);

			int initPos = ba.nextInt();
			cdi.initPos = initPos;

			/** 满屏画图点个数 4字节 */
			int screenDataLen = ba.nextInt();
			// screenDataLen = 1000;
			cdi.screendatalen = screenDataLen;
			// System.out.println(cdi.screendatalen);

			/** 传输的数据点个数 4字节 */
			int transNum = ba.nextInt();
			// transNum = 1000000;
			// System.out.println("transNum:" + transNum);
			cdi.datalen = transNum;

			/** 慢扫描备用 4 字节 */
			int slowMove = ba.nextInt();
			cdi.slowMove = slowMove;

			OfflineInfo oi = cdi.oi;
			/** 零点位置 4 字节 */
			int pos0 = ba.nextInt();
			oi.pos0 = pos0;
			/** 电压档位 4 字节 */
			int vbIdx = ba.nextInt();
			oi.vbIdx = vbIdx;
			/** 衰减倍率指数 4 字节 */
			int probeMultiIdx = ba.nextInt();
			oi.probeMultiIdx = probeMultiIdx;
			/** 频率 4 字节 */
			float frequency = ba.nextFloat();

			cdi.setFreq(oi.frequency = frequency);

			/** 周期 4 字节 */
			float cycle = ba.nextFloat();
			oi.cycle = cycle;

			ByteBuffer bb = ByteBuffer.allocate(transNum);
			byte[] arr = bb.array();
			ba.get(arr, bb.position(), transNum);// (读入文件的)buffer到(共享buffer的)array

			cdi.setUniqueAdcbuf(bb);

			ci.addChannelDataInfo(cdi);
			// System.out.println("ci:"+ci.screendatalen);
			ci.screendatalen = screenDataLen;

			ptr = intBlockSize - (ba.pointer() - ptr);// 若混入垃圾信息，可得其长度
			// System.out.println("skip inside: " + ptr);
			ba.skip(ptr);// 跳过垃圾信息，多层正确性保护
		}
		ci.setDataComplete(1);
		if (cdi != null)
			ci.slowMove = cdi.slowMove;
		// ci.setChannelNum(channels.size());

		// fptr = infoLen - (ba.pointer() - fptr);
		// System.out.println("skip: " + fptr);
		// ba.skip(fptr);
	}
}
