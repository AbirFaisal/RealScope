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

	private int fileVer = DMInfoTiny.FILEPROTOCOL_VDS_CURRENT;// 文件格式版本
	private ChannelInverseTranslator cit;

	public DataSaverTiny() {
		cit = new ChannelInverseTranslator();
	}

	/**
	 * 保存文件，始终使用当前波形通道信息填充，可用于RT载入深存储或是保存文件
	 * 
	 * KNOW 需要处理拿不到的情况：深存储数据已丢失，需要重现匹配
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
			// KNOW 原头文件长10个字符，后头文件赋值为机型号一般7个字节,补上"SPB"读入离线文件才正常
			byte[] b = (sc.getMachineHeader()).getBytes(StringPool.ASCIIString);
			raf.write(b); // 文件头(10 字节, ASCII)("SPB"3字节+sc.machine_header 7字节)
			raf.writeInt(cm.getMachineTypeForSave()); // 4个字节机型信息 7102
			raf.writeInt(fileVer); // 4个字节版本信息 1

			raf.writeByte(VDS_Portable.FILEFORMAT_DM);
			raf.writeByte(0);
			raf.writeByte(0);
			raf.writeByte(0);

			fileLenPos = raf.getFilePointer();
			raf.writeInt(0);// 文件长度

			/* 数据区 */
			raf.writeByte(0);// 数据区入口标记为0(1字节，byte)
			p1 = raf.getFilePointer();// 数据区入口指针
			raf.writeInt(0);// 数据区长度

			t1 = raf.getFilePointer();// 采集点数据的指针位置
			/** 接收数据 */
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

			/* 波形通道区 */
			raf.writeByte(1);// 波形通道区入口标记 数据区第一字节存为1(1字节，byte)
			p1 = raf.getFilePointer();
			raf.writeInt(0);// 信息区长度
			t1 = raf.getFilePointer();

			raf.writeByte(t); // 是否深存储 'T'/'M': (1字节)存储深度索引档位
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
			raf.writeInt(0);// 波形通道区长度
			t1 = raf.getFilePointer();
			// 迭代存储

			// BigDecimal bd = cm.getCurrentSampleRateBD_kHz();
			// MachineType mi = cm.getMachine();

			while (it.hasNext()) {
				DMDataInfo cd = it.next();
				WaveForm wf = wfm.getWaveForm(cd.chl);
				ChannelInfo chi = wf.wfi.ci;

				/** 在获取dm的时候计算freq，这里不考虑fft的情况 */
				// cd.computeFreq(bd, mi);
				// chi.updateFreqLabel(cd.getFrequencyFloat());
				chi.snapShot2ChannelDataInfo(cd, wf);

				long blockstart = raf.getFilePointer();
				raf.writeBytes(wf.toString());

				long blockLenPos = raf.getFilePointer();
				raf.writeInt(0); // (2)块长度：暂时存0

				// 目前只保存深存储文件，其处理方式是INVERSE_TYPE_RAW_REVERSE
				raf.writeInt(cit.getInverseType_DM(chi.isInverse()));

				cd.writeRandomAccessFile(raf);
				ci.initPos = cd.initPos;
				ci.datalen = cd.datalen;
				ci.setScreendatalen(cd);
				ci.slowMove = cd.slowMove;

				raf.writeInt((int) cd.filePointer);// 采集点数据的指针位置

				long blockEnd = raf.getFilePointer();
				int segmentLen = (int) (blockEnd - blockstart);
				raf.seek(blockLenPos);
				raf.writeInt(segmentLen); // (2)块长度：替代0存入正确值
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
