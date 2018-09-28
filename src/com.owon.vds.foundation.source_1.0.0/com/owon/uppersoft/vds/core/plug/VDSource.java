package com.owon.uppersoft.vds.core.plug;

import java.nio.ShortBuffer;
import java.util.Arrays;

/**
 * VDSource，虚拟的数据来源
 * 
 */
public class VDSource {
	public static final int ran = 10;

	public VDSource() {
	}

	public boolean connectable() {
		return true;
	}

	public static byte[] genSimulateSine(int off) {
		int p = 10, bn = 100, offin = 0;
		byte[] t1 = genSine(127, 10, offin, bn + 1);
		byte[] t2 = new byte[p * bn + off - 1];

		// 更改插值使用的内容：线性&插0
		genSimulatePlugZero(t2, off - 1, t1, offin, bn, p);
		return t2;
	}

	/**
	 * outlen只有1个点时不插值
	 * 
	 * 例如in[0~n-1]:共n个元素
	 * 
	 * in[0],in[1]; in[1],in[2];...; in[n-2],in[n-1]
	 * 
	 * 其中各插入pr-1个点
	 * 
	 * @param out
	 * @param in
	 * @param offout
	 * @param offin
	 * @param inlen
	 *            >=2, 其中inlen-1个点被扩充了pr倍
	 * @param pr
	 * @return
	 */
	public static void genSimulateLinearPlug(byte[] out, int offout, byte[] in,
			int offin, int inlen, int pr) {
		double v1;
		int v2, k;
		double r;
		int inend = offin + inlen;
		for (int i = offin, j = offout; i + 1 < inend; i++) {
			v1 = in[i];
			v2 = in[i + 1];
			r = (v2 - v1) / pr;
			out[j] = (byte) v1;
			j++;
			for (k = 1; k < pr; k++) {
				v1 += r;
				out[j] = (byte) v1;
				j++;
			}
		}
	}

	/**
	 * outlen只有1个点时不插值
	 * 
	 * 例如in[0~n-1]:共n个元素
	 * 
	 * in[0],in[1]; in[1],in[2];...; in[n-2],in[n-1]
	 * 
	 * 其中各插入pr-1个点
	 * 
	 * @param out
	 * @param in
	 * @param offout
	 * @param offin
	 * @param inlen
	 *            >=2, 其中inlen-1个点被扩充了pr倍
	 * @param pr
	 * @return
	 */
	public static int[] genSimulateLinearPlug(int[] out, int offout, int[] in,
			int offin, int inlen, int pr) {
		double v1;
		int v2, k;
		double r;
		int inend = offin + inlen;
		for (int i = offin, j = offout; i + 1 < inend; i++) {
			v1 = in[i];
			v2 = in[i + 1];
			r = (v2 - v1) / pr;
			out[j] = (int) v1;
			j++;
			for (k = 1; k < pr; k++) {
				v1 += r;
				out[j] = (int) v1;
				j++;
			}
		}
		return out;
	}

	public static void genSimulateLinearPlug_2_3(byte[] out, int offout,
			byte[] in, int offin, int inlen, int iPos) {
		double v1;
		int v2;
		double r;

		int i, j;
		v2 = in[offin++];
		// System.err.println(offin + ", " + offout);

		int inend = offin + inlen;

		if (iPos % 2 == 0) {// 1,2
			for (i = offin, j = offout; i + 2 < inend;) {
				v1 = v2;
				v2 = in[i++];
				r = (v2 - v1) / 2;
				out[j++] = (byte) v1;
				out[j++] = (byte) (v1 + r);
				out[j++] = (byte) v2;

				v1 = v2;
				v2 = in[i++];

				r = (v2 - v1) / 3;
				out[j++] = (byte) (v1 + r);
				out[j++] = (byte) (v1 + r + r);
			}
		} else {// 2,1
			for (i = offin, j = offout; i + 2 < inend;) {
				v1 = v2;
				v2 = in[i++];
				r = (v2 - v1) / 3;
				out[j++] = (byte) v1;
				out[j++] = (byte) (v1 + r);
				out[j++] = (byte) (v1 + r + r);
				out[j++] = (byte) v2;

				v1 = v2;
				v2 = in[i++];

				r = (v2 - v1) / 2;
				out[j++] = (byte) (v1 + r);

			}
		}
		// System.err.println(i + ", " + j);
	}

	/**
	 * outlen只有1个点时不插值
	 * 
	 * 例如in[0~2]:共3个元素
	 * 
	 * in[0], x, in[1], x, x;
	 * 
	 * in[1], x, in[2], x, x;
	 * 
	 * @param out
	 * @param in
	 * @param offout
	 * @param offin
	 * @param inlen
	 *            >=2, 其中inlen-1个点每2个点被扩充成5个点
	 * @return
	 */
	public static int[] genSimulateLinearPlug_2_3(int[] out, int offout,
			int[] in, int offin, int inlen, int iPos) {
		double v1;
		int v2;
		double r;

		int i, j;
		v2 = in[offin++];
		// System.err.println(offin + ", " + offout);

		int inend = offin + inlen;

		if (iPos % 2 == 0) {
			for (i = offin, j = offout; i + 2 < inend;) {
				v1 = v2;
				v2 = in[i++];
				r = (v2 - v1) / 2;
				out[j++] = (int) v1;
				out[j++] = (int) (v1 + r);
				out[j++] = v2;

				v1 = v2;
				v2 = in[i++];

				r = (v2 - v1) / 3;
				out[j++] = (int) (v1 + r);
				out[j++] = (int) (v1 + r + r);
			}
		} else {
			for (i = offin, j = offout; i + 2 < inend;) {
				v1 = v2;
				v2 = in[i++];
				r = (v2 - v1) / 3;
				out[j++] = (int) v1;
				out[j++] = (int) (v1 + r);
				out[j++] = (int) (v1 + r + r);
				out[j++] = v2;

				v1 = v2;
				v2 = in[i++];

				r = (v2 - v1) / 2;
				out[j++] = (int) (v1 + r);
			}
		}
		// System.err.println(i + ", " + j);
		return out;
	}

	/**
	 * outlen只有1个点时不插值
	 * 
	 * @param out
	 * @param in
	 * @param offout
	 * @param offin
	 * @param inlen
	 *            >=2
	 * @param pr
	 * @return
	 */
	public static byte[] genSimulatePlug(byte[] out, int offout, byte[] in,
			int offin, int inlen, int pr) {
		int v1, v2, r, k;
		int inend = offin + inlen;
		for (int i = offin, j = offout; i + 1 < inend; i++) {
			v1 = in[i];
			v2 = in[i + 1];
			r = (v2 - v1) / pr;
			out[j] = (byte) v1;
			j++;
			for (k = 1; k < pr; k++) {
				v1 += r;
				out[j] = (byte) v1;
				j++;
			}
		}
		return out;
	}

	/**
	 * 
	 * 插0值
	 * 
	 * @param out
	 * @param in
	 * @param offout
	 * @param offin
	 * @param inlen
	 *            >=2
	 * @param pr
	 * @return
	 */
	public static byte[] genSimulatePlugZero(byte[] out, int offout, byte[] in,
			int offin, int inlen, int pr) {
		int v1, k;
		int inend = offin + inlen;
		for (int i = offin, j = offout; i < inend; i++) {
			v1 = in[i];
			out[j] = (byte) v1;
			j++;
			for (k = 1; k < pr; k++) {
				out[j] = 0;
				j++;
			}
		}
		return out;
	}

	public static byte[] genSine() {
		return genSine(100, 17, 0, 40);
	}

	public static byte[] genSine(int am, int period, int beg, int len) {
		int end = beg + len;
		byte[] array = new byte[end];
		Arrays.fill(array, 0, beg, (byte) 0);
		byte v;

		for (int i = beg, j = 0; i < end; i++, j++) {
			v = (byte) (Math.sin(2 * Math.PI * (j % period) / (double) period) * am
			// + Math.random() * ran
			);

			array[i] = v;
			System.out.print(v + " ");
		}
		System.out.println();
		return array;
	}

	public static byte[] genTriangle() {
		int am = 80;
		int period = 120;
		int beg = 0;
		int len = 1024;
		byte[] array = new byte[1024];

		byte v;
		int hp = period >> 1;
		int end = beg + len;
		for (int i = beg, j = i; i < end; i++, j++) {
			if (j < hp)
				v = (byte) (4 * am * j / period - am + Math.random() * ran);
			else
				v = (byte) (-4 * am * j / period + 3 * am + Math.random() * ran);
			if (j >= period)
				j = 0;

			array[i] = v;
		}
		return array;
	}

	protected void genSquare(ClientMsg cm) {
		ShortBuffer sbuf = cm.sbuf;
		int am = cm.amplitude;
		int period = cm.peroidpoint;
		int beg = sbuf.position();
		int len = sbuf.limit() - beg;
		int hh = cm.yoffset;
		short[] array = sbuf.array();

		short v;
		int hp = period >> 1;
		int end = beg + len;
		for (int i = beg, j = i; i < end; i++, j++) {
			if (j < hp)
				v = (short) (am + Math.random() * ran + hh);
			else
				v = (short) (-am + Math.random() * ran + hh);
			if (j >= period)
				j = 0;
			if (v > 511)
				array[i] = 511;
			else if (v < -512)
				array[i] = -512;
			else
				array[i] = v;
		}
	}

	protected void genRampUp_Down(ClientMsg cm, boolean up) {
		ShortBuffer sbuf = cm.sbuf;
		int am = cm.amplitude;
		int period = cm.peroidpoint;
		int beg = sbuf.position();
		int len = sbuf.limit() - beg;
		int hh = cm.yoffset;
		short[] array = sbuf.array();

		short v;
		if (up) {
			int end = beg + len;
			for (int i = beg, j = i; i < end; i++, j++) {
				v = (short) (-2 * am * j / period + am + Math.random() * ran + hh);
				if (j >= period)
					j = 0;
				if (v > 511)
					array[i] = 511;
				else if (v < -512)
					array[i] = -512;
				else
					array[i] = v;
			}
		} else {
			int end = beg + len;
			for (int i = beg, j = i; i < end; i++, j++) {
				v = (short) (2 * am * j / period - am + Math.random() * ran + hh);
				if (j >= period)
					j = 0;
				if (v > 511)
					array[i] = 511;
				else if (v < -512)
					array[i] = -512;
				else
					array[i] = v;
			}
		}
	}

}
