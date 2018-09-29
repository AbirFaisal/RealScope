package com.owon.uppersoft.vds.core.wf.rt;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import com.owon.uppersoft.dso.function.record.OfflineChannelsInfo;
import com.owon.uppersoft.vds.core.data.CByteArrayInputStream;
import com.owon.uppersoft.vds.core.data.OfflineInfo;
import com.owon.uppersoft.vds.core.machine.VDS_Portable;

public class ChannelDataInfo extends FreqInfo {

	public static final int Max_8bit = 125, Min_8bit = -125;
	public static final int Max = Max_8bit, Min = Min_8bit;

	/** 通用 */
	public int chl;
	public int datalen;
	public int initPos;
	public int screendatalen;
	public int slowMove;

	public ChannelDataInfo() {
		reset();
	}

	private int inverseType = VDS_Portable.INVERSE_TYPE_RAW_FINE;

	private void resetInverseType() {
		setInverseType(VDS_Portable.INVERSE_TYPE_RAW_FINE);
	}

	public void setInverseType_Record(CByteArrayInputStream ba, int intEdition) {
		if (intEdition >= OfflineChannelsInfo.RECORDPROTOCOL_VDS_INVERSE) {
			setInverseType(ba.nextInt());
		} else {
			resetInverseType();
		}
	}

	/** 只给运行时使用 */
	private LinkedList<ByteBuffer> buflist = new LinkedList<ByteBuffer>();

	public LinkedList<ByteBuffer> getBuflist() {
		return buflist;
	}

	public void reset() {
		BeyondMax = BeyondMin = false;
		buflist.clear();
	}

	public void setUniqueAdcbuf(ByteBuffer bb) {
		buflist.clear();
		addAdcBuf(bb);
	}

	public ByteBuffer getUniqueAdcbuf() {
		int size = buflist.size();
		if (size <= 0)
			return null;
		return buflist.peek();
	}

	public ByteBuffer popAdcbuf() {
		int size = buflist.size();
		if (size <= 0)
			return null;
		return buflist.pop();
	}

	public void reduceAdcbuf() {
		int bsize = buflist.size();
		if (bsize == 0)
			return;
		ByteBuffer bb = buflist.peekLast();
		setUniqueAdcbuf(bb);
	}

	public void addAdcBuf(ByteBuffer bb) {
		addSpecialAdcBuf(bb);
	}

	private void addSpecialAdcBuf(ByteBuffer bb) {
		buflist.add(bb);
	}

	public OfflineInfo oi = new OfflineInfo();

	/** 以下三者实际未使用 */
	public int xoffset;

	public void forceGround(int pos0) {
		// System.err.println("gnd");
		int size = buflist.size();
		if (size <= 0)
			return;

		ByteBuffer fb = buflist.peek();
		gndByteBuffer(fb, pos0);
		setUniqueAdcbuf(fb);
	}

	protected void gndByteBuffer(ByteBuffer fb, int pos0) {
		int v = pos0;
		if (v > Max) {
			v = Max;
		} else if (v < Min) {
			v = Min;
		}

		int p = fb.position();
		int l = fb.limit();
		Arrays.fill(fb.array(), p, l, (byte) v);
	}

	public void setInverseType(int inverseType) {
		this.inverseType = inverseType;
	}

	public int getInverseType() {
		return inverseType;
	}

	/**
	 * 在adc数据取出绘图前进行必要的反相处理
	 * 
	 * 在载入数据的入口就做了反相，后续如录制、参考波形，都可以使用现成的
	 * 
	 * 越界判断目前是每帧皆判断
	 * 
	 * @param pos0
	 * @param inverse
	 */
	public void handleADCBoundNInverse(int pos0, boolean inverse) {
		/** LinkedList 的迭代器使用链式迭代，可找到最后一帧 */
		Iterator<ByteBuffer> lb = buflist.iterator();
		while (lb.hasNext()) {
			ByteBuffer buf = lb.next();
			handleADCBoundNInverseForFrame(pos0, inverse, buf);
		}
	}

	/**
	 * 在adc数据取出绘图前进行必要的反相处理
	 * 
	 * 在载入数据的入口就做了反相，后续如录制、参考波形，都可以使用现成的
	 * 
	 * 越界判断目前是每帧皆判断
	 * 
	 * @param pos0
	 * @param inverse
	 */
	public final void handleADCBoundNInverseForFrame(int pos0, boolean inverse,
			ByteBuffer buf) {
		if (inverse) {
			final int p = buf.position(), l = buf.limit();
			byte[] bb = buf.array();
			int dpos0 = pos0 << 1;

			int v;
			for (int i = p; i < l; i++) {
				v = dpos0 - bb[i];

				if (v > Max) {
					bb[i] = Max;
					BeyondMax = true;
				} else if (v < Min) {
					bb[i] = Min;
					BeyondMin = true;
				} else {
					bb[i] = (byte) v;
				}
			}
		} else {
			final int p = buf.position(), l = buf.limit();
			byte[] bb = buf.array();

			int v;
			for (int i = p; i < l; i++) {
				v = bb[i];

				if (v > Max) {
					bb[i] = Max;
					BeyondMax = true;
				} else if (v < Min) {
					bb[i] = Min;
					BeyondMin = true;
				}
			}
		}
	}

	public boolean BeyondMax = false, BeyondMin = false;
}
