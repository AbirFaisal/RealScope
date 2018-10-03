package com.owon.uppersoft.vds.data;

import java.awt.Color;

public class RGB {
	public int red, green, blue;

	public RGB() {
		this(Color.WHITE);
	}

	public RGB(String hex) {
		if (hex == null || hex.length() != 6) {
			setRGB(Color.WHITE);
			return;
		}
		hex = hex.trim();
		this.red = Integer.parseInt(hex.substring(0, 2), 16);
		this.green = Integer.parseInt(hex.substring(2, 4), 16);
		this.blue = Integer.parseInt(hex.substring(4, 6), 16);
	}

	public RGB(Color c) {
		setRGB(c);
	}

	public void setRGB(Color c) {
		this.red = c.getRed();
		this.green = c.getGreen();
		this.blue = c.getBlue();
	}

	public RGB(int red, int green, int blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	public Color getColor() {
		return new Color(red, green, blue);
	}

	public String toHexString() {
		StringBuilder sb2 = new StringBuilder(6);
		String ht;
		ht = Integer.toHexString(red);
		if (ht.length() == 1) {
			sb2.append(0);
		}
		sb2.append(ht);
		ht = Integer.toHexString(green);
		if (ht.length() == 1) {
			sb2.append(0);
		}
		sb2.append(ht);
		ht = Integer.toHexString(blue);
		if (ht.length() == 1) {
			sb2.append(0);
		}
		sb2.append(ht);
		return sb2.toString();
	}

	@Override
	public String toString() {
		return "(" + red + "," + green + "," + blue + ")";
	}

	public short getRGB565() {
		return (short) (((red << 8) & 0xF800) | ((green << 3) & 0x7E0) | (blue >>> 3));
	}

	public static float[] toHSB(int rgb, float[] hsb) {
		int red = (rgb >> 16) & 0xFF;
		int green = (rgb >> 8) & 0xFF;
		int blue = (rgb >> 0) & 0xFF;
		Color.RGBtoHSB(red, green, blue, hsb);
		return hsb;
	}

	public static void main(String[] args) {
		String f = "66cc00";
		System.out.println(new RGB(f).toHexString());
	}
}
