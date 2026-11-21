package com.owon.uppersoft.vds.core.plug;

import java.util.ArrayDeque;
import java.util.Iterator;

/**
 * Sine interpolation: compute 9 points, turning 1 point into 10
 * 
 * @author Matt
 * 
 */
public class PlugValueUtil {
	public static int row = Plug10_old.PlugRate, col = 8;

	public static int[][] PlugArgs2 = new int[row][col];
	// public static double[][] PlugArgs2 = new double[row][col];

	public static ArrayDeque<Byte> ADCStack = new ArrayDeque<Byte>(col);

	public static void main(String[] args) {
		System.out.println(-1 & 0xFF);

		int v;
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++) {
				v = (short) PlugArgs2[i][j];
				System.out.print(v + "    ");
			}
			System.out.println();
		}
	}

	static {
		/** Matrix is column first then row; distribute the coefficients evenly into each coefficient array */
		int k = 0;
		int v;
		int[] pint = Plug10_old.PlugArgs_int;
		for (int j = 0; j < col; j++) {
			for (int i = 0; i < row; i++) {
				v = (short) pint[k++];
				PlugArgs2[i][j] = v;
			}
		}
	}

	public static void insertInt(byte v) {
		if (ADCStack.size() + 1 > col) {
			byte r = ADCStack.removeLast();
			// System.out.print("remove: " + r + " ");
		}
		ADCStack.addFirst(v);
		// System.out.print("add: " + v);
	}

	public static void fill(int[] b, int start, int len, byte v) {
		insertInt(v);
		int end = start + len;
		int x, t;
		int ql = ADCStack.size(), qe = ql - 1;
		// System.out.println(" ql:" + ql);
		for (int i = start, j = 0; i < end; i++, j++) {
			x = 0;
			// System.out.println("j:" + j);
			Iterator<Byte> ia = ADCStack.iterator();
			for (int k = 0; k < ql; k++) {
				t = ia.next() & 0xFF;
				x += PlugArgs2[j][k] * t;
				// System.out.print("( " + k + ", " + (k) + " )");
				System.out.print("(" + PlugArgs2[j][k] + ", " + t + ")");
			}
			// compute(x);
			b[i] = (byte) compute(x);// (x >>> 15);// (x >>> 20) & 0xFF;
			System.out.print("   = " + x + " ");
			System.out.println();
		}
		System.out.println();
	}

	public static void fill(byte[] b, int start, int len, byte v) {
		insertInt(v);
		int end = start + len;
		int x, t;
		int ql = ADCStack.size(), qe = ql - 1;
		// System.out.println(" ql:" + ql);
		for (int i = start, j = 0; i < end; i++, j++) {
			x = 0;
			// System.out.println("j:" + j);
			Iterator<Byte> ia = ADCStack.iterator();
			for (int k = 0; k < ql; k++) {
				t = ia.next() & 0xFF;
				x += PlugArgs2[j][k] * t;
				// System.out.print("( " + k + ", " + (k) + " )");
				System.out.print("(" + PlugArgs2[j][k] + ", " + t + ")");
			}
			// compute(x);
			b[i] = (byte) compute(x);// (x >>> 15);// (x >>> 20) & 0xFF;
			System.out.print("   = " + x + " ");
			System.out.println();
		}
		System.out.println();
	}

	/**
	 * <code>
	 * 
	assign datum_round[9:0] = datum_all[22:13] + 1;
	
	always @(posedge clk) begin
		case (datum_round[9:8])
			2'b01 : datum_dout <= 8'h7F;
			2'b10 : datum_dout <= 8'h80;
			default: datum_dout <= {datum_round[9], datum_round[7:1]};
		endcase
	end
	<code>
	
	 * @param v
	 * @return
	 */
	public static int compute(int v) {
		// Only check the last 23 bits, then +1
		int x = ((v >>> 13) & base1) + 1;// 18

		// if(true)return (byte) x;

		// Only check whether bits 8 and 14 satisfy the condition
		int y = x & _98;
		if (y == _81) {
			x = 0x7F;
		} else if (y == _91) {
			x = 0x80;
		} else {
			// Bits 1 to 7 of the value after adding 1
			int z = x >>> 1;
			z = z & base3;
			// Append bit 9
			y = x & _91;
			y = y >>> 2;
			x = y | z;
		}
		return x;
	}

	private static int base1 = (1 << 10) - 1, base3 = (1 << 7) - 1,
			_81 = 1 << 8, _91 = 1 << 9, _98 = _81 | _91;

}
