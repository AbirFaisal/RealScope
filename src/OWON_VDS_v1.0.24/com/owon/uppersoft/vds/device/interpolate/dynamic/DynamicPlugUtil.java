package com.owon.uppersoft.vds.device.interpolate.dynamic;

public class DynamicPlugUtil {

	/**
	 * @param in
	 * @param beg
	 * @param end
	 * @param out
	 * @param interpLength
	 * @param sincTable
	 */
	public static void sinc_interps(byte[] in, int beg, int end, int[] out,
			int interpLength, int[][] sincTable) {
		int alen = sincTable[0].length;
		for (int i = beg, m = 0; i < end; i++, m++) {
			for (int j = 0; j < interpLength; j++) {
				int sum = 0;
				for (int k = 0; k < alen; k++) {
					int n = i + k - 2;
					int p = n < beg ? beg : (n > end ? end : n);
					sum += sincTable[j][k] * in[p];
				}
				int h = m * interpLength + j;
				if (h >= 0) {
					int u = sum > 0 ? (sum + 500000) / 1000000
							: (sum - 500000) / 1000000;
					out[h] = (byte) (u > 127 ? 127 : (u < -127 ? -127 : u));
				}
			}
		}
	}

	/**
	 * @param in
	 * @param beg
	 * @param end
	 * @param out
	 * @param interpLength
	 * @param sincTable
	 */
	public static void sinc_interps(byte[] in, int beg, int end, byte[] out,
			int interpLength, int[][] sincTable) {
		int alen = sincTable[0].length;
		for (int i = beg, m = 0; i < end; i++, m++) {
			for (int j = 0; j < interpLength; j++) {
				int sum = 0;
				for (int k = 0; k < alen; k++) {
					int n = i + k - 2;
					int p = n < beg ? beg : (n > end ? end : n);
					sum += sincTable[j][k] * in[p];
				}
				int h = m * interpLength + j;
				if (h >= 0) {
					int u = sum > 0 ? (sum + 500000) / 1000000
							: (sum - 500000) / 1000000;
					out[h] = (byte) (u > 127 ? 127 : (u < -127 ? -127 : u));
				}
			}
		}
	}

	public static int arith_interp_mode(byte[] in, int inbeg, int inend) {
		int min1, max1, baw, min2, max2;
		int i;
		byte[] ch_ad;
		int ch_middle_value;
		int win_low, win_high; // 中间值窗口，上下限
		short status_flag1, status_flag2;
		short edgeSelect;
		int start_counter1, start_counter2;
		int mix1 = 0, mix2 = 0;// 和

		max2 = 0;
		min2 = 0;

		int percentage;// 百分比
		ch_ad = in;
		max1 = ch_ad[inbeg];
		min1 = ch_ad[inbeg];
		for (i = inbeg; i < inend; i++) {
			if (max1 < ch_ad[i]) {
				max1 = ch_ad[i];
			}
			if (min1 > ch_ad[i]) {
				min1 = ch_ad[i];
			}
		}
		baw = Math.abs(max1 - min1);
		ch_middle_value = max1 - baw / 2;

		win_low = ch_middle_value - 7; // 窗口门限，参考FPGA 原来的门限值
		win_high = ch_middle_value + 7;
		status_flag1 = -1;
		start_counter1 = 0;

		status_flag2 = -1;
		start_counter2 = 0;
		edgeSelect = 0;
		for (i = inbeg; i < inend; i++) // 峰值，深存储都是一个数据+
		{
			if (win_high <= ch_ad[i]) {
				if (status_flag2 == -1) {
					if (status_flag1 == 0) {
						edgeSelect++;
						if (edgeSelect == 1) {
							max1 = ch_ad[i] - ch_middle_value;
							min1 = ch_ad[i] - ch_middle_value;
						}
						if (edgeSelect == 2) {
							edgeSelect++;
							max2 = ch_ad[i] - ch_middle_value;
							min2 = ch_ad[i] - ch_middle_value;
						}
					}
					status_flag1 = 1;
				}

				if (edgeSelect > 2) {
					if (status_flag2 == 0) {
						edgeSelect++;
					}
					status_flag2 = 1;
				}
			}

			if (win_low >= ch_ad[i]) {
				if (status_flag2 == -1) {
					if (status_flag1 == 1) {
						edgeSelect++;
						if (edgeSelect == 1) {
							max1 = ch_ad[i] - ch_middle_value;
							min1 = ch_ad[i] - ch_middle_value;
						}
						if (edgeSelect == 2) {
							edgeSelect++;
							max2 = ch_ad[i] - ch_middle_value;
							min2 = ch_ad[i] - ch_middle_value;
						}
					}
					status_flag1 = 0;
				}

				if (edgeSelect > 2) {
					if (status_flag2 == 1) {
						edgeSelect++;
					}
					status_flag2 = 0;
				}
			}

			if ((edgeSelect > 0) && (edgeSelect < 3)) {
				start_counter1++;
				mix1 += (ch_ad[i] - ch_middle_value);
				if (max1 < (ch_ad[i] - ch_middle_value)) {
					max1 = (ch_ad[i] - ch_middle_value);
				}
				if (min1 > (ch_ad[i] - ch_middle_value)) {
					min1 = (ch_ad[i] - ch_middle_value);
				}

			}
			if ((edgeSelect >= 3) && (edgeSelect < 5)) {
				start_counter2++;
				mix2 += (ch_ad[i] - ch_middle_value);
				if (max2 < (ch_ad[i] - ch_middle_value)) {
					max2 = (ch_ad[i] - ch_middle_value);
				}
				if (min2 > (ch_ad[i] - ch_middle_value)) {
					min2 = (ch_ad[i] - ch_middle_value);
				}
			}
			if (edgeSelect >= 4)
				break;
		}
		if (start_counter1 > start_counter2) {
			if (start_counter1 == 0 || min1 == 0 || max1 == 0)
				return 101;
			percentage = Math.abs(mix1 * 100) / start_counter1
					/ (status_flag1 == 1 ? Math.abs(min1) : Math.abs(max1));
		} else {
			if (start_counter2 == 0 || min2 == 0 || max2 == 0)
				return 101;
			percentage = Math.abs(mix2 * 100) / start_counter2
					/ (status_flag2 == 1 ? Math.abs(min2) : Math.abs(max2));
		}
		return (start_counter1 < 6 && start_counter2 < 6) ? 101 : percentage;
	}
}
