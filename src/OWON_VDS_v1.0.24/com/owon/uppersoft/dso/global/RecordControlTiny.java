package com.owon.uppersoft.dso.global;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Iterator;

import com.owon.uppersoft.dso.function.RecordControl;
import com.owon.uppersoft.dso.mode.control.TimeControl;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.dso.wf.ChannelInverseTranslator;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.wf.rt.ChannelDataInfo;
import com.owon.uppersoft.vds.core.wf.rt.LoadMedia;
import com.owon.uppersoft.vds.util.Pref;

public class RecordControlTiny extends RecordControl {
	public RecordControlTiny(ControlManager cm, Pref p) {
		super(cm, p);

		cit = new ChannelInverseTranslator();
	}

	private ChannelInverseTranslator cit;

	@Override
	public int writeOnce(RandomAccessFile raf, ControlManager cm,
			WaveFormManager wfm, LoadMedia cti, RecordControl rc) {
		int ct = rc.getCounter();
		int maxframe = rc.getMaxframe();
		int c = cti.getFrameCount();
		int i = 0;
		while (i < c) {
			writeFrame(raf, cm, wfm, cti, i);
			i++;
			if (++ct >= maxframe)
				break;
		}
		return i;
	}

	/**
	 * 一帧写入
	 * 
	 * @param raf
	 * @param cm
	 * @param wfm
	 * @param cti
	 * @param i
	 */
	public void writeFrame(RandomAccessFile raf, ControlManager cm,
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

				// 慢扫时起始位置含了慢扫个数，必须置零
				int pos = bb.position();
				if (tc.isOnSlowMoveTimebase())
					pos = 0;

				raf.write(arr, pos + initPos, fsl);// raf.write(arr,0,dl);

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
}