package com.owon.uppersoft.dso.wf.common.dm;

import static com.owon.uppersoft.vds.util.LoadArrayUtil.carry1by1_HeadTail;

import java.nio.ByteBuffer;

import com.owon.uppersoft.vds.core.rt.WFDrawRTUtil;

public class VP4_5 extends VirtualPoint {
	public VP4_5() {
		sample = 5;
		pixel = 4;
		computeRate();
		setPoint(5);
	}

	@Override
	protected int getDilute2EndRemaining(boolean neg, int re) {
		if (neg) {
			return 1;
		}
		return 0;
	}

	@Override
	public int getDrawMode() {
		return WFDrawRTUtil.DrawMode4_5;
	}

	@Override
	public void compressBySavePoints_Enough(ByteBuffer cba,
			byte[] dest, int destPos, int srcPos) {
		byte[] src = tmparray;
		cba.get(src, 0, sample);
		System.arraycopy(src, srcPos, dest, destPos, getPoint());
	}

	@Override
	public void compressBySavePoints(ByteBuffer cba, byte[] arr,
			int abeg, int tbeg, int num, boolean head) {
		byte[] tmp = tmparray;
		cba.get(tmp, 0, num);
		carry1by1_HeadTail(arr, abeg, tmp, tbeg, num, getPoint(), head);
	}

}