package com.owon.uppersoft.vds.util.format;

import com.owon.uppersoft.vds.util.StringPool;

/**
 * SimpleStringFormatter，字符格式
 * 
 */
public class SimpleStringFormatter {

	public static final int DefaultLength = 12;
	public static final char DefaultHoldPlace = ' ';

	private int length;
	private char holdPlace;
	public final String emptyblock;

	private int dmidx;

	public SimpleStringFormatter(int dmidx) {
		this(DefaultLength, DefaultHoldPlace, dmidx);
	}

	public SimpleStringFormatter(int length, char aHoldPlace, int dmidx) {
		this.dmidx = dmidx;
		this.length = length;
		holdPlace = aHoldPlace;

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append(holdPlace);
		}
		emptyblock = sb.toString();
	}

	/**
	 * 将值转化为字符串，靠右
	 * 
	 * @param value
	 * @return
	 */
	public String valueToStringOnRight(int value) {
		return valueToStringOnRight(SFormatter.UIformat("%0" + (dmidx + 5) + 'd',
				value));
	}

	/**
	 * 将值转化为字符串，靠右
	 * 
	 * @param value
	 * @return
	 */
	public String valueToStringOnRight(double value) {
		return valueToStringOnRight(String.valueOf(value));
	}

	/**
	 * 将值转化为字符串，靠右
	 * 
	 * @param value
	 * @return
	 */
	public String valueToStringOnRight(String value) {
		if (value == null) {
			return emptyblock;
		}

		char[] charArray = emptyblock.toCharArray();
		char[] valueArray = value.toCharArray();
		int l = valueArray.length;
		System.arraycopy(valueArray, 0, charArray, length - l, l);

		return new String(charArray);
	}

	/**
	 * 将值转化为字符串，靠左
	 * 
	 * @param value
	 * @return
	 */
	public String valueToStringOnLeft(int value) {
		return valueToStringOnLeft(SFormatter.UIformat("%0" + (dmidx + 5) + 'd',
				value));
	}

	/**
	 * 将值转化为字符串，靠左
	 * 
	 * @param value
	 * @return
	 */
	public String valueToStringOnLeft(double value) {
		return valueToStringOnLeft(String.valueOf(value));
	}

	/**
	 * 将值转化为字符串，靠左
	 * 
	 * @param value
	 * @return
	 */
	public String valueToStringOnLeft(String value) {
		if (value == null) {
			return emptyblock;
		}

		char[] charArray = emptyblock.toCharArray();
		char[] valueArray = value.toCharArray();
		int l = valueArray.length;
		System.arraycopy(valueArray, 0, charArray, 0, l);

		return new String(charArray);
	}

	public static void main(String[] args) {
		System.out.println(SFormatter.UIformat("%010d", 12));

		SimpleStringFormatter ssf = new SimpleStringFormatter(1);

		StringBuilder sb = new StringBuilder();

		String ln = StringPool.LINE_SEPARATOR;

		sb.append(ssf.valueToStringOnRight(57.8));
		sb.append(ln);
		sb.append(ssf.valueToStringOnRight(578));
		sb.append(ln);
		sb.append(ssf.valueToStringOnRight("CH1/10"));
		sb.append(ln);
		sb.append(ssf.valueToStringOnLeft(578));
		sb.append(ln);
		sb.append(ssf.valueToStringOnLeft(5.78));
		sb.append(ln);

		sb.append(ln);
		String a = null;
		sb.append(ssf.valueToStringOnRight(a));
		sb.append(ln);
		sb.append(ssf.valueToStringOnRight(StringPool.EmptyString));
		System.out.print(sb);
	}
}
