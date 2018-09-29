package com.owon.uppersoft.vds.core.rt;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class AverageContainer {
	private FloatBuffer avgbuf;

	public void updateAverage(ByteBuffer adcbuf, int avgtimes, boolean avgon) {
		if (!avgon || avgtimes == 1 || adcbuf == null) {
			avgbuf = null;
			return;
		}

		// int chl = cdi.chl;
		final int mat = avgtimes - 1;

		final int p_pix = 0;

		final int p_adc = adcbuf.position();
		final int l_adc = adcbuf.limit();// p_adc + 100;//

		byte[] adcs = adcbuf.array();
		final int datalen = l_adc - p_adc;
		//System.out.println("[]chl: " + chl);
		//System.out.println("[] " + p_adc + ", " + l_adc);
		//DBG.dbgArray(adcs, p_adc, 10);
		if (avgbuf == null || avgbuf.remaining() < datalen) {
			// 第一次平均值就用adc数据直接赋值
			avgbuf = FloatBuffer.allocate(datalen);
			float[] avgs = avgbuf.array();
			//System.out.println(": " + (l_adc - p_adc));
			for (int i = p_adc, j = p_pix; i < l_adc; i++, j++) {
				avgs[j] = (float) adcs[i];

				//if (j < 10)
				//	System.out.println(avgs[j]);
			}
			//System.out.println();
			return;
		}

		float[] avgs = avgbuf.array();
		float v;
		/**
		 * KNOW 此后使用计算结果，公式：
		 * 
		 * AVG(此)=(AVG(前)*(T-1)+adc(此))/T
		 */
		for (int i = p_adc, j = p_pix; i < l_adc; i++, j++) {
			//if (j < 10)
			//	System.out.println(avgs[j] + ", " + adcs[i]);
			v = (avgs[j] * mat + adcs[i]) / avgtimes;
			// v = adcs[i];
			avgs[j] = v;

			adcs[i] = (byte) v;
		}
		//DBG.dbgArray(adcs, p_adc, 10);
	}

	/**
	 * KNOW 平均值采样时，当前的采样点仍会保留并被使用，只是画图用的是平均点的效果
	 * 
	 * 停止后只要设置
	 * 
	 * @param pc
	 * @return true表示已经完成了像素点数组的坐标计算，后面将跳出；false表示需要执行常规像素点数组的坐标计算，同时会清除内存
	 */
	private boolean checkAverage(boolean ScreenMode_3, int yb, boolean avgon,
			int avgtimes, IntBuffer pixbuf, ByteBuffer adcbuf) {
		int at = avgtimes;
		boolean b = at > 1 && avgon;
		if (!b) {
			// System.err.println("check no");
			avgbuf = null;
			return false;
		}

		int mat = at - 1;
		int i, j;

		// avgbuf和pixbuf的索引都从0开始
		int p_pix = 0;
		int[] pixs = pixbuf.array();

		int p_adc = adcbuf.position();
		int l_adc = adcbuf.limit();
		byte[] adcs = adcbuf.array();
		int datalen = l_adc - p_adc;
		if (avgbuf == null || avgbuf.remaining() < datalen) {
			// 第一次平均值就用adc数据直接赋值
			avgbuf = FloatBuffer.allocate(datalen);
			float[] avgs = avgbuf.array();
			if (ScreenMode_3) {
				for (i = p_adc, j = p_pix; i < l_adc; i++, j++) {
					pixs[j] = (int) (yb - (avgs[j] = (float) adcs[i]));
				}
			} else {
				for (i = p_adc, j = p_pix; i < l_adc; i++, j++) {
					pixs[j] = (int) (yb - ((avgs[j] = (float) adcs[i]) * 2));
				}
			}
			pixbuf.position(0);
			pixbuf.limit(j);
			// System.err.println("check first");
			return true;
		}

		float[] avgs = avgbuf.array();
		float v;// TODO 将v用赋值表达式直接替换可能提高效率
		/**
		 * KNOW 此后使用计算结果，公式：
		 * 
		 * AVG(此)=(AVG(前)*(T-1)+adc(此))/T
		 */
		if (ScreenMode_3) {
			for (i = p_adc, j = p_pix; i < l_adc; i++, j++) {
				v = (avgs[j] * mat + adcs[i]) / at;
				pixs[j] = (int) (yb - (avgs[j] = v));
			}
		} else {
			for (i = p_adc, j = p_pix; i < l_adc; i++, j++) {
				v = (avgs[j] * mat + adcs[i]) / at;
				pixs[j] = (int) (yb - ((avgs[j] = v) * 2));
			}
		}
		pixbuf.position(0);
		pixbuf.limit(j);
		// System.err.println("check keep");
		return true;
	}
}