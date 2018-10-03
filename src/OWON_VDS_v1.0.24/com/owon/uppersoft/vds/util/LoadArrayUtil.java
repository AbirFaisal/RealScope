package com.owon.uppersoft.vds.util;

import com.owon.uppersoft.vds.core.data.MinMax;
import com.owon.uppersoft.vds.core.deep.struct.RangeInfo4in1;


public class LoadArrayUtil {
	public static final void carry1by1(byte[] dest, int destPos, byte[] src,
			int srcPos, int length) {
		System.arraycopy(src, srcPos, dest, destPos, length);
	}

	public static final void carry1by1_HeadTail(byte[] dest, int destPos,
			byte[] src, int srcPos, int num, int point, boolean head) {
		if (head) {
			int i = destPos + (point - num);
			System.arraycopy(src, srcPos, dest, i, num);

			byte v = dest[i--];
			while (i >= destPos) {
				dest[i--] = v;
			}
		} else {
			int i = destPos + num;
			System.arraycopy(src, srcPos, dest, destPos, num);

			byte v = dest[i - 1];
			while (num < point) {
				dest[i++] = v;
				num++;
			}
		}
	}

	public static final void _4for1(byte[] arr, int abeg, byte[] tmp, int tbeg,
			int num) {
		byte b, max, min, first, last;
		int j, tlen = tbeg + num;
		first = max = min = b = tmp[tbeg];
		for (j = tbeg + 1; j < tlen; j++) {
			b = tmp[j];
			if (b > max)
				max = b;
			else if (b < min)
				min = b;
		}
		last = b;

		arr[abeg++] = first;
		arr[abeg++] = min;
		arr[abeg++] = max;
		arr[abeg] = last;
	}

	public static final void _4for1(RangeInfo4in1 ri, int[] tmp, int tbeg,
			int num) {
		int b, max, min, first, last;
		int j, tlen = tbeg + num;
		max = min = first = b = tmp[tbeg];
		for (j = tbeg + 1; j < tlen; j++) {
			b = tmp[j];
			if (b > max)
				max = b;
			else if (b < min)
				min = b;
		}
		last = b;

		ri.setAll(min, max, first, last);
	}
	
	public static final void _4for1(RangeInfo4in1 ri, byte[] tmp, int tbeg,
			int num) {
		byte b, max, min, first, last;
		int j, tlen = tbeg + num;
		max = min = first = b = tmp[tbeg];
		for (j = tbeg + 1; j < tlen; j++) {
			b = tmp[j];
			if (b > max)
				max = b;
			else if (b < min)
				min = b;
		}
		last = b;

		ri.setAll(min, max, first, last);
	}
	
	public static final void _2for1(MinMax ri, byte[] tmp, int tbeg,
			int num) {
		byte b, max, min, first, last;
		int j, tlen = tbeg + num;
		max = min = first = b = tmp[tbeg];
		for (j = tbeg + 1; j < tlen; j++) {
			b = tmp[j];
			if (b > max)
				max = b;
			else if (b < min)
				min = b;
		}
		last = b;

		ri.set(min, max);
	}

	public static final void _4for1_ByMinMax(RangeInfo4in1 ri, byte[] tmp,
			int tbeg, int num) {
		byte b, max, min, first, last;
		int j, tlen = tbeg + num;
		max = (byte) ri.getMax();
		min = (byte) ri.getMin();
		first = b = tmp[tbeg];
		for (j = tbeg + 1; j < tlen; j++) {
			b = tmp[j];
			if (b > max)
				max = b;
			else if (b < min)
				min = b;
		}
		last = b;

		ri.setAll(min, max, first, last);
	}
}
