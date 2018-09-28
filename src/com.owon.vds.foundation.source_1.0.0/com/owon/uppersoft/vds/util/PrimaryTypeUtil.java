package com.owon.uppersoft.vds.util;

import java.math.BigDecimal;

import com.owon.uppersoft.vds.util.format.SFormatter;

public class PrimaryTypeUtil {
	public static final byte[] clone_bytes(byte[] bs) {
		return clone_bytes(bs, 0, bs.length);
	}

	public static final byte[] clone_bytes(byte[] bs, int beg, int len) {
		byte[] bb = new byte[len];
		System.arraycopy(bs, beg, bb, 0, len);
		return bb;
	}

	public static final boolean canHoldAsInt(double db) {
		return db == (int) db;
	}

	public static final boolean canHoldAsInt(BigDecimal db) {
		try {
			db.intValueExact();
			return true;
		} catch (RuntimeException e) {
			// e.printStackTrace();
			return false;
		}
	}

	public static final boolean canHoldAsLong(BigDecimal db) {
		try {
			db.longValueExact();
			return true;
		} catch (RuntimeException e) {
			// e.printStackTrace();
			return false;
		}
	}

	public static final BigDecimal divide(int a, int b) {
		return divide(BigDecimal.valueOf(a), BigDecimal.valueOf(b));
	}

	public static final BigDecimal divide(int a, BigDecimal b) {
		return divide(BigDecimal.valueOf(a), b);
	}

	public static final BigDecimal divide(BigDecimal a, int b) {
		return divide(a, BigDecimal.valueOf(b));
	}

	public static final BigDecimal divide(BigDecimal a, BigDecimal b) {
		try {
			return a.divide(b);
		} catch (ArithmeticException e) {
			e.printStackTrace();
			return BigDecimal.valueOf(a.doubleValue() / b.doubleValue());
		}
	}

	public static final String getIPAddress(byte[] ip, int off) {
		StringBuilder t_ip = new StringBuilder();
		int len = off + 4;
		for (int i = off; i < len; i++) {
			if (i != off)
				t_ip.append('.');
			t_ip.append(ip[i] & 0xff);
		}
		return t_ip.toString();
	}

	public static final String getMACAddress(byte[] mac, int off) {
		StringBuilder t_mac = new StringBuilder();
		int len = off + 6;
		String n;
		for (int i = off; i < len; i++) {
			// if (i != off) {
			// t_mac.append("-");
			// }
			n = Integer.toHexString(mac[i] & 0xff);
			if (n.length() < 2) {
				t_mac.append('0');
			}
			t_mac.append(n);
		}
		return t_mac.toString();
	}

	public static boolean isAlpha(int b) {
		return b >= 32 && b <= 126;
	}

	public static boolean isAlpha(byte b) {
		return b >= 32 && b <= 126;
	}

	public static void main(String[] args) {
		System.err.println(isAlpha((byte) 58));
		byte[] b = { 58, 83, 68, 83, 76, 83, 84, 80, 48 };
		// DBG.dbgArrayAlpha(b, 0, b.length);

		// testArraycopy();
		// System.out.println(canHoldAsInt(20000.00000));

		// byte[] d = { (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte)
		// 2 };
		// System.out.println(getMACAddress(d, 0));

		System.err
				.println(Integer.toHexString(PrimaryTypeUtil.cut(0xfffa2, 2)));
	}

	public static int cut(int value, int bytes) {
		return value & ((1 << (bytes * 8)) - 1);
	}

	public static void testByte2Int() {
		int len = 20000000;
		int[] a = new int[len];
		byte[] b = new byte[len];
		for (int i = 0; i < len; i++) {
			a[i] = 2;
			b[i] = 2;
		}

		int v;
		long t1 = System.currentTimeMillis();
		for (int i = 0; i < len; i++) {
			v = (b[i] + 5) * 10;
		}
		long t2 = System.currentTimeMillis() - t1;
		System.out.println(t2);

		t1 = System.currentTimeMillis();
		for (int i = 0; i < len; i++) {
			v = (a[i] + 5) * 10;
		}
		t2 = System.currentTimeMillis() - t1;
		System.out.println(t2);
	}

	public static String toBytesString_2(int v) {
		String ht = Integer.toBinaryString((v >>> 8) & 0xff);
		ht = to8bits(ht);
		String lt = Integer.toBinaryString(v & 0xff);
		lt = to8bits(lt);

		String t = "[toBytesString: ]" + SFormatter.UIformat("%s", ht) + ' '
				+ SFormatter.UIformat("%s", lt);

		return t;
	}

	public static String to8bits(String t) {
		while (t.length() < 8) {
			t = '0' + t;
		}
		return t;
	}

	public static void testArraycopy() {
		byte[] b1 = new byte[800000];
		byte[] b2 = new byte[1000000];

		long t1 = System.currentTimeMillis(), t2;
		for (int i = 0; i < 1000; i++) {
			System.arraycopy(b1, 0, b2, 0, 800000);
		}
		t2 = System.currentTimeMillis() - t1;
		System.out.println(t2);
	}
}
