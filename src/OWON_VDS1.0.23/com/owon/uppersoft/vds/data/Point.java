package com.owon.uppersoft.vds.data;

/**
 * 用于坐标或是高宽
 * 
 */
public class Point {
	public int x, y;

	public Point() {
	}

	public Point(int x, int y) {
		set(x, y);
	}

	public void set(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Point copy() {
		return new Point(x, y);
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 == null || !(arg0 instanceof Point))
			return false;
		Point that = (Point) arg0;
		return that.x == x && that.y == y;
	}

	@Override
	public String toString() {
		return x + "," + y;
	}
}
