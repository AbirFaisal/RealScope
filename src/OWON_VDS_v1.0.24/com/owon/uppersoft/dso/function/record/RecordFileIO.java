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
	private static final int recordVer = OfflineChannelsInfo.RECORDPROTOCOL_VDS_CURRENT;// file format version

	public static long writeHeader(RandomAccessFile raf, ControlManager cm) {
		try {
			final SoftwareControl sc = cm.getSoftwareControl();
			byte[] b = sc.getMachineHeader().getBytes(StringPool.ASCIIString);
			raf.write(b); // file header (10 bytes, ASCII)
			raf.writeInt(cm.getMachineTypeForSave()); // 4 bytes of model info 7102
			raf.writeInt(recordVer); // 4 bytes of version info 1

			raf.writeByte(VDS_Portable.FILEFORMAT_RECORD);// 4 bytes of format info, the last 3 bits reserved
			raf.writeByte(0);
			raf.writeByte(0);
			raf.writeByte(0);

			long fileLenPos = raf.getFilePointer();
			raf.writeInt(0);// file length
			raf.writeInt(0);// recording time interval
			raf.writeInt(0);// number of frames
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
	 * Write one frame
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

				// If this is modified, the length of saved data can be reduced
				raf.writeInt(0);// raf.writeInt(initPos);// screen start
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
				raf.writeInt((int) (bendPos - bsizePos - 4));// -4? the block length storage position takes 4 bytes
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

		/** Model info, 4 bytes */
		int machID = ba.nextInt();
		ci.machine_type = machID;
		if (!cm.isSuitableMachineType(machID)) {
			int r = cm.reloadManager.prepareReloadPlayFile(machID);
			if (r < 0)
				return -1;
		}

		/** Version info, 4 bytes */
		int intEdition = ba.nextInt();
		ci.record_version = intEdition;

		if (!ci.setMore_Record(ba))
			return -1;

		/** File length, 4 bytes */
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
		int infoLen = ba.nextInt();// structure length, i.e. the length of a single frame
		ba.skip(infoLen);

		OpenPropertyChangeEvent pce = new OpenPropertyChangeEvent(pc,
				PropertiesItem.TIME_LINE_CHECK, 0, 0);
		pce.newInt = ++i;
		pc.propertyChange(pce);

		Point p = new Point(infoLen, 1);
		// Point p = new Point(infoLen + 4, 1);// method 2, pad the frame header with 4 bytes
		timepoints.add(p);

		while (i < c) {
			fp = ba.pointer();
			// System.out.println("fp" + i + ":" + fp);
			infoLen = ba.nextInt();

			ba.skip(infoLen);

			pce.newInt = ++i;
			pc.propertyChange(pce);
			// infoLen += 4;// method 2, pad the frame header with 4 bytes
			if (infoLen != p.x) {
				p.x += 4;// the original frame length plus the 4 bytes that store the frame length is the interval length between frames, method 1, pad the frame tail with 4 bytes
				timepoints.add(p = new Point(infoLen, 1));//
			} else {
				p.y++;
			}

		}
		p.x += 4;// the original frame length plus the 4 bytes that store the frame length is the interval length between frames, method 1, pad the frame tail with 4 bytes
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

		ci.reset('T');// clear the start-store position of the shared area
		ChannelDataInfo cdi = null;
		while (ba.pointer() - fptr < infoLen) {// && ba.available() >= 41
			/** Waveform name, 1 byte */
			byte wnb = ba.nextByte();
			// System.out.println("CHname: " + wnb);
			cdi = ci.getInstance();
			cdi.chl = wnb;
			/** Block size, 4 bytes */
			int intBlockSize = ba.nextInt();

			int ptr = ba.pointer();

			cdi.setInverseType_Record(ba, ci.record_version);

			int initPos = ba.nextInt();
			cdi.initPos = initPos;

			/** Number of points drawn on the full screen, 4 bytes */
			int screenDataLen = ba.nextInt();
			// screenDataLen = 1000;
			cdi.screendatalen = screenDataLen;
			// System.out.println(cdi.screendatalen);

			/** Number of transmitted data points, 4 bytes */
			int transNum = ba.nextInt();
			// transNum = 1000000;
			// System.out.println("transNum:" + transNum);
			cdi.datalen = transNum;

			/** Reserved for slow sweep, 4 bytes */
			int slowMove = ba.nextInt();
			cdi.slowMove = slowMove;

			OfflineInfo oi = cdi.oi;
			/** Zero-point position, 4 bytes */
			int pos0 = ba.nextInt();
			oi.pos0 = pos0;
			/** Voltage scale, 4 bytes */
			int vbIdx = ba.nextInt();
			oi.vbIdx = vbIdx;
			/** Attenuation ratio exponent, 4 bytes */
			int probeMultiIdx = ba.nextInt();
			oi.probeMultiIdx = probeMultiIdx;
			/** Frequency, 4 bytes */
			float frequency = ba.nextFloat();

			cdi.setFreq(oi.frequency = frequency);

			/** Period, 4 bytes */
			float cycle = ba.nextFloat();
			oi.cycle = cycle;

			ByteBuffer bb = ByteBuffer.allocate(transNum);
			byte[] arr = bb.array();
			ba.get(arr, bb.position(), transNum);// (file being read) buffer to (shared buffer's)array

			cdi.setUniqueAdcbuf(bb);

			ci.addChannelDataInfo(cdi);
			// System.out.println("ci:"+ci.screendatalen);
			ci.screendatalen = screenDataLen;

			ptr = intBlockSize - (ba.pointer() - ptr);// If garbage info got mixed in, its length can be obtained
			// System.out.println("skip inside: " + ptr);
			ba.skip(ptr);// skip the garbage info, multi-layer correctness protection
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
