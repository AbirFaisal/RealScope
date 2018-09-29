package com.owon.uppersoft.vds.core.wf.dm;

import static com.owon.uppersoft.vds.util.format.EndianUtil.nextFloatB;
import static com.owon.uppersoft.vds.util.format.EndianUtil.nextIntB;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import com.owon.uppersoft.vds.core.data.CByteArrayInputStream;
import com.owon.uppersoft.vds.core.machine.VDS_Portable;
import com.owon.uppersoft.vds.core.wf.rt.FreqInfo;
import com.owon.uppersoft.vds.core.wf.rt.PlugInfo;
import com.owon.uppersoft.vds.util.format.SFormatter;

public abstract class DMDataInfo extends FreqInfo {

	public int datalen;
	public int slowMove;
	public int pos0;
	public int filePointer;

	/**
	 * 在有拉触发拿前后各多拿原始点的情况下，这个值为拉触发判断的起始位置，如果有插值，这个值也是插值的起始位置
	 */
	public int initPos;

	public int screendatalen;

	public int chl;

	public int probeMultiIdx;
	public int vbIdx;

	public DMDataInfo() {
	}

	public void setPullTrg_OfflineDM(CByteArrayInputStream ba, int intEdition) {
		pi.plugDataLength = ba.nextInt();
		pi.sinePlugRate = ba.nextInt();
		pi.linearPlugRate = ba.nextFloat();
		pi.pluggedTrgOffset = ba.nextInt();
	}

	public void setInverseType_OfflineDM(CByteArrayInputStream ba,
			int intEdition) {
		inverseType = ba.nextInt();
	}

	/** 拉触发参数只在需要插值的情况下使用 */
	public PlugInfo pi = new PlugInfo();

	public int receivePlugInfo(byte[] arr2nd, int i) {
		pi.plugDataLength = nextIntB(arr2nd, i);
		i += 4;
		pi.sinePlugRate = nextIntB(arr2nd, i);
		i += 4;
		pi.linearPlugRate = nextFloatB(arr2nd, i);
		i += 4;
		pi.pluggedTrgOffset = nextIntB(arr2nd, i);
		i += 4;

		return i;
	}

	public void receivePlugInfo(ByteBuffer bb) {
		pi.plugDataLength = bb.getInt();
		pi.sinePlugRate = bb.getInt();
		pi.linearPlugRate = bb.getFloat();
		pi.pluggedTrgOffset = bb.getInt();
		println("pi.plugDataLength: " + pi.plugDataLength + ",4");
		println("pi.sinePlugRate: " + pi.sinePlugRate + ",4");
		println("pi.linearPlugRate: " + pi.linearPlugRate + ",4");
		println("pi.pluggedTrgOffset: " + pi.pluggedTrgOffset + ",4");
	}

	private void println(String txt) {
		// System.err.println(txt);
	}

	public int readPlugInfo(byte[] arr2nd, int i) {
		pi.plugDataLength = nextIntB(arr2nd, i);
		i += 4;
		pi.sinePlugRate = nextIntB(arr2nd, i);
		i += 4;
		pi.linearPlugRate = nextFloatB(arr2nd, i);
		i += 4;
		pi.pluggedTrgOffset = nextIntB(arr2nd, i);
		i += 4;
		return i;
	}

	public void logReceive() {
		configln("-------DMDataInfo-------\r\n");
		configln(SFormatter.dataformat("chl: %d", chl));
		configln(SFormatter.dataformat("initPos: %d", initPos));
		configln(SFormatter.dataformat("screendatalen: %d", screendatalen));
		configln(SFormatter.dataformat("datalen: %d", datalen));
		configln(SFormatter.dataformat("slowMove: %d", slowMove));

		configln(SFormatter.dataformat("plugDataLength: %d", pi.plugDataLength));
		configln(SFormatter.dataformat("sinePlugRate: %d", pi.sinePlugRate));
		configln(SFormatter.dataformat("linearPlugRate: %.4f",
				pi.linearPlugRate));
		configln(SFormatter.dataformat("pluggedTrgOffset: %d",
				pi.pluggedTrgOffset));

		configln(SFormatter.dataformat("filePointer: %d", filePointer));
	}

	private void configln(String txt) {
		System.out.println(txt);
	}

	public boolean shouldInverse;

	public int inverseType = VDS_Portable.INVERSE_TYPE_RAW_FINE;

	protected void resetInverseType() {
		inverseType = VDS_Portable.INVERSE_TYPE_RAW_FINE;
	}

	/**
	 * 将纯粹的一些通道相关波形信息存入raf
	 * 
	 * @param raf
	 * @throws IOException
	 */
	public void writeRandomAccessFile(RandomAccessFile raf) throws IOException {
		raf.writeInt(initPos);
		// 在传输的数据点中的起始画图点位置
		raf.writeInt(screendatalen); // 满屏画图点个数:

		raf.writeInt(pi.plugDataLength);
		raf.writeInt(pi.sinePlugRate);
		raf.writeFloat((float) pi.linearPlugRate);
		raf.writeInt(pi.pluggedTrgOffset);

		raf.writeInt(datalen); // 传输的数据点个数：
		raf.writeInt(slowMove);//

		raf.writeInt(pos0);
		raf.writeInt(vbIdx);
		raf.writeInt(probeMultiIdx);

		raf.writeFloat(getFrequencyFloat());
		raf.writeFloat(1 / getFrequencyFloat());
	}

}