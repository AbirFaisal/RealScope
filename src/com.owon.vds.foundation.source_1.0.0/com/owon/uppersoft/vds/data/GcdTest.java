package com.owon.uppersoft.vds.data;

/**
 * 求最大公约数，辗转相除
 * 
 */
public class GcdTest {
	// 循环实现
	public static final int gcd1(int a, int b) {
		if (b == 0) {
			if (a == 0)
				return 1;
			return a;
		}
		int k = 0;
		do {
			// 得到余数
			k = a % b;
			// 根据辗转相除法,把被除数赋给除数
			a = b;
			// 余数赋给被除数
			b = k;
		} while (k != 0);
		// 返回被除数
		return a;
	}

	// 逆归实现
	public static final int gcd2(int a, int b) {
		// 直到满足此条件逆归退出
		if (b == 0) {
			return a;
		}
		if (a < 0) {
			return gcd2(-a, b);
		}
		if (b < 0) {
			return gcd2(a, -b);
		}
		return gcd2(b, a % b);
	}

	public static void main(String[] args) {
		System.out.println(gcd1(888, 458));
		System.out.println(gcd2(888, 458));
	}

}