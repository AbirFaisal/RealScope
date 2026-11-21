package com.owon.uppersoft.vds.data;

/**
 * Find the greatest common divisor, Euclidean algorithm
 * 
 */
public class GcdTest {
	// Loop implementation
	public static final int gcd1(int a, int b) {
		if (b == 0) {
			if (a == 0)
				return 1;
			return a;
		}
		int k = 0;
		do {
			// Get the remainder
			k = a % b;
			// Per the Euclidean algorithm, assign the dividend to the divisor
			a = b;
			// Assign the remainder to the dividend
			b = k;
		} while (k != 0);
		// Return the dividend
		return a;
	}

	// Recursive implementation
	public static final int gcd2(int a, int b) {
		// Recursion exits once this condition is met
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