package com.owon.uppersoft.vds.data;

public class LocRectangle {
	public int x0, x1, y0, y1;

	public LocRectangle() {
	}

	public LocRectangle(int x0, int x1, int y0, int y1) {
		set(x0, x1, y0, y1);
	}

	public void set(int x0, int x1, int y0, int y1) {
		this.x0 = x0;
		this.x1 = x1;
		this.y0 = y0;
		this.y1 = y1;
	}
}