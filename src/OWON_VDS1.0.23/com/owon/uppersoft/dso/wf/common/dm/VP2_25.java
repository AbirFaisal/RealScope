package com.owon.uppersoft.dso.wf.common.dm;

import static com.owon.uppersoft.vds.util.LoadArrayUtil._4for1;

import java.nio.ByteBuffer;

import com.owon.uppersoft.vds.core.rt.WFDrawRTUtil;

public class VP2_25 extends VirtualPoint {
	public VP2_25() {
		sample = 25;
		pixel = 2;
		computeRate();
		setPoint(8);
	}

	@Override
	protected int getDilute2EndRemaining(boolean neg, int re) {
		if (neg)
			return 13;
		else
			return 12;
	}

	@Override
	public int getDrawMode() {
		return WFDrawRTUtil.DrawMode4in1;
	}

	@Override
	public void compressBySavePoints_Enough(ByteBuffer cba,
			byte[] arr, int abeg, int tbeg) {
		byte[] tmp = tmparray;
		cba.get(tmp, 0, sample);
		_4for1(arr, abeg, tmp, tbeg, 12);
		_4for1(arr, abeg + 4, tmp, tbeg + 12, 13);
	}

	@Override
	public void compressBySavePoints(ByteBuffer cba, byte[] arr,
			int abeg, int tbeg, int num, boolean head) {
		byte[] tmp = tmparray;
		cba.get(tmp, 0, num);
		if (head) {
			int lastNum = 13;
			if (num <= lastNum) {
				int i = abeg + 4;
				_4for1(arr, i, tmp, tbeg, num);
				byte v = arr[i];
				arr[i--] = v;
				arr[i--] = v;
				arr[i--] = v;
				arr[i] = v;
			} else {
				_4for1(arr, abeg + 4, tmp, tbeg + num - lastNum, lastNum);
				_4for1(arr, abeg, tmp, tbeg, num - lastNum);
			}
		} else {
			int firstNum = 12;
			if (num <= firstNum) {
				int i = abeg;
				_4for1(arr, i, tmp, tbeg, num);
				i += 3;
				byte v = arr[i];
				arr[++i] = v;
				arr[++i] = v;
				arr[++i] = v;
				arr[++i] = v;
			} else {
				_4for1(arr, abeg, tmp, tbeg, firstNum);
				_4for1(arr, abeg + 4, tmp, tbeg + firstNum, num - firstNum);
			}
		}
	}

}