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
	 * When extra original points are taken before and after for trigger pull, this value is the starting position of the trigger pull judgment; if there is interpolation, this value is also the starting position of the interpolation
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

	/** Trigger pull parameters are used only when interpolation is needed */
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
	 * Store purely channel-related waveform info into raf
	 * 
	 * @param raf
	 * @throws IOException
	 */
	public void writeRandomAccessFile(RandomAccessFile raf) throws IOException {
		raf.writeInt(initPos);
		// Starting drawing point position within the transmitted data points
		raf.writeInt(screendatalen); // Number of full-screen drawing points:

		raf.writeInt(pi.plugDataLength);
		raf.writeInt(pi.sinePlugRate);
		raf.writeFloat((float) pi.linearPlugRate);
		raf.writeInt(pi.pluggedTrgOffset);

		raf.writeInt(datalen); // Number of transmitted data points:
		raf.writeInt(slowMove);//

		raf.writeInt(pos0);
		raf.writeInt(vbIdx);
		raf.writeInt(probeMultiIdx);

		raf.writeFloat(getFrequencyFloat());
		raf.writeFloat(1 / getFrequencyFloat());
	}

}