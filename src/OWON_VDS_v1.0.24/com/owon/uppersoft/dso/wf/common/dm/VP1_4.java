package com.owon.uppersoft.dso.wf.common.dm;

import static com.owon.uppersoft.vds.util.LoadArrayUtil._4for1;
import static com.owon.uppersoft.vds.util.LoadArrayUtil._4for1_ByMinMax;

import java.math.BigDecimal;
import java.nio.ByteBuffer;

import com.owon.uppersoft.vds.core.deep.struct.RangeInfo4in1;
import com.owon.uppersoft.vds.core.rt.WFDrawRTUtil;
import com.owon.uppersoft.vds.util.PrimaryTypeUtil;

public class VP1_4 extends VirtualPoint {

	private RangeInfo4in1 ri = new RangeInfo4in1();

	public VP1_4(int sample) {
		this.sample = sample;
		pixel = 1;
		computeRate();
		if (sample == 1) {
			setPoint(1);
		} else {
			setPoint(4);
		}
	}

	public VP1_4(BigDecimal rate) {
		initialize(rate);
	}

	@Override
	public int getDrawMode() {
		if (sample == 1) {
			return WFDrawRTUtil.DrawMode1p;
		} else {
			return WFDrawRTUtil.DrawMode4in1;
		}
	}

	/**
	 * Used when the compression ratio is so large that the full-screen count cannot be computed
	 * 
	 * @param rate
	 */
	protected void initialize(BigDecimal rate) {
		setRate(rate);
		if (!PrimaryTypeUtil.canHoldAsInt(rate)) {
			errprintln("VirtualPoint.initialize  !PrimaryTypeUtil.canHoldAsInt(rate)");
		}
		sample = rate.intValue();
		pixel = 1;
		if (sample == 1) {
			setPoint(1);
		} else {
			setPoint(4);
		}
	}

	@Override
	protected int getDilute2EndRemaining(boolean neg, int re) {
		// Since pixel == 1, there is no leftover pixel offset
		return 0;
	}

	@Override
	public void compressBySavePoints_Enough(ByteBuffer cba,
			byte[] arr, int abeg, int tbeg) {
		byte[] tmp = tmparray;

		if (sample > MaxCompressTempBufferSize) {
			compressFor1_4Special(arr, abeg, cba, tmp, sample,
					MaxCompressTempBufferSize);
			return;
		}

		cba.get(tmp, 0, sample);
		if (sample == 1) {
			// 1:1
			arr[abeg] = tmp[tbeg];
		} else if (sample < 4) {
			// Apply the 1:2 and 1:3 cases to 1:4 as well, and gather the maximum and minimum starts
			_4for1(arr, abeg, tmp, tbeg, sample);
		} else {
			_4for1(arr, abeg, tmp, tbeg, sample);
		}
	}

	@Override
	public void compressBySavePoints(ByteBuffer cba, byte[] arr,
			int abeg, int tbeg, int num, boolean head) {
		// head ...xxx
		// tail xxx...
		byte[] tmp = tmparray;

		if (sample > MaxCompressTempBufferSize) {
			compressFor1_4Special(arr, abeg, cba, tmp, num,
					MaxCompressTempBufferSize);
			return;
		}

		cba.get(tmp, 0, num);
		if (sample == 1) {
			// 1:1
			arr[abeg] = tmp[tbeg];
		} else if (sample < 4) {
			// Apply the 1:2 and 1:3 cases to 1:4 as well, and gather the maximum and minimum starts
			_4for1(arr, abeg, tmp, tbeg, num);
		} else {
			_4for1(arr, abeg, tmp, tbeg, num);
		}
	}

	/**
	 * Reuse the buffer in a loop, comparing to obtain extreme values and other information
	 * 
	 * @param arr
	 * @param abeg
	 * @param cba
	 * @param tmp
	 * @param tbeg
	 * @param num
	 * @param bufsize
	 */
	private void compressFor1_4Special(byte[] arr, int abeg,
			ByteBuffer cba, byte[] tmp, int num, int bufsize) {

		doCompressFor1_4Special(ri, cba, tmp, num, bufsize);

		ri.fillArray(arr, abeg);
	}

	/**
	 * Reuse the buffer in a loop, comparing to obtain extreme values and other information
	 * 
	 * @param min_max
	 * @param first_last
	 * @param cba
	 * @param tmp
	 * @param num
	 * @param bufsize
	 */
	private void doCompressFor1_4Special(RangeInfo4in1 ri,
			ByteBuffer cba, byte[] tmp, int num, int bufsize) {

		byte first = 0;
		int i = num, len;

		first = cba.get();
		ri.setAllAs1(first);
		if (num == 1) {
			return;
		}

		while (i > 0) {
			if (i < bufsize)
				len = i;
			else
				len = bufsize;

			cba.get(tmp, 0, len);
			_4for1_ByMinMax(ri, tmp, 0, len);

			i -= len;
		}
		ri.setFirst(first);
	}

}