package com.owon.uppersoft.vds.core.plug;

import java.util.ArrayDeque;
import java.util.Iterator;

/**
 * 正弦插值，算出9个点，就把1点插成了10个
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
		/** 矩阵先列后行，讲系数均分到每一个系数数组中去 */
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
				System.out.print("(" + PlugArgs2[j][k] + ", " + t + ')');
			}
			// compute(x);
			b[i] = (byte) compute(x);// (x >>> 15);// (x >>> 20) & 0xFF;
			System.out.print("   = " + x + ' ');
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
				System.out.print("(" + PlugArgs2[j][k] + ", " + t + ')');
			}
			// compute(x);
			b[i] = (byte) compute(x);// (x >>> 15);// (x >>> 20) & 0xFF;
			System.out.print("   = " + x + ' ');
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
		// 只判断后23位，再+1
		int x = ((v >>> 13) & base1) + 1;// 18

		// if(true)return (byte) x;

		// 只判断8,14位上的位是否满足条件
		int y = x & _98;
		if (y == _81) {
			x = 0x7F;
		} else if (y == _91) {
			x = 0x80;
		} else {
			// 加1后的值的1到7位
			int z = x >>> 1;
			z = z & base3;
			// 拼上第9位
			y = x & _91;
			y = y >>> 2;
			x = y | z;
		}
		return x;
	}

	private static int base1 = (1 << 10) - 1, base3 = (1 << 7) - 1,
			_81 = 1 << 8, _91 = 1 << 9, _98 = _81 | _91;

}
