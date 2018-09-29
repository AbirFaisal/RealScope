package com.owon.uppersoft.vds.core.plug;

/**
 * 这是一个对语音信号(0.3kHz~3.4kHz)进行低通滤波的C语言程序， 低通滤波的截止频率为800Hz，滤波器采用19点的有限冲击响应FIR滤波。
 * 语音信号的采样频率为8kHz，每个语音样值按16位整型数存放在insp.dat文件中。
 */
public class FIR_Filter {
	public static int length = 180;
	/** 语音帧长为180点＝22.5ms@8kHz采样 */
	public static int[] x1 = new int[length + 20];
	public static double[] h = { 0.01218354, -0.009012882, -0.02881839,
			-0.04743239, -0.04584568, -0.008692503, 0.06446265, 0.1544655,
			0.2289794, 0.257883, 0.2289794, 0.1544655, 0.06446265,
			-0.008692503, -0.04584568, -0.04743239, -0.02881839, -0.009012882,
			0.01218354 };

	/** 低通滤波浮点子程序 */
	public static void filter(int xin[], int xout[], int n, double h[]) {
		int i, j;
		double sum;
		for (i = 0; i < length; i++) {
			x1[n + i - 1] = xin[i];
			// System.out.println((n + i - 1) + ", " + i);
			// 18~197, 0~179
		}
		for (i = 0; i < length; i++) {
			sum = 0.0;
			for (j = 0; j < n; j++) {
				sum += h[j] * x1[i - j + n - 1];
				if (i == 1) {
					System.out.println(j + ", " + (i - j + n - 1));
					// 0~18, 18+n~n
				}
			}
			xout[i] = (int) sum;
		}
		for (i = 0; i < (n - 1); i++) {
			x1[n - i - 2] = xin[length - 1 - i];
			// System.out.println((n - i - 2) + ", " + (length - 1 - i));
			// 17~0, 179~162
		}
	}

	public static void main(String[] args) {
		int[] xin = new int[length];
		int[] xout = new int[length];
		filter(xin, xout, h.length, h);
	}

}
