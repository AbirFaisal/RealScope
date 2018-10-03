package com.owon.uppersoft.dso.wf.dm2;

public class TimebaseDiluteInfo {

	private int tbidx;

	private int fullscreen;
	private int sine_rate;
	private int line_rate;
	private int rates;
	private int points, ext_points;

	public TimebaseDiluteInfo(int tbidx, int fullscreen, int sine_rate,
			int line_rate) {
		this.tbidx = tbidx;
		this.fullscreen = fullscreen;
		this.sine_rate = sine_rate;
		this.line_rate = line_rate;
		rates = sine_rate * line_rate;
		points = fullscreen * rates;
		ext_points = (fullscreen + (50 << 1)) * rates;
	}

	public void logself() {
		logln("tbidx: " + tbidx);
		logln("fullscreen: " + fullscreen);
		logln("sine_rate: " + sine_rate);
		logln("line_rate: " + line_rate);
		logln("rates: " + rates);
		logln("points: " + points);
		logln("ext_points: " + ext_points);
		logln("initPos_Pluged: " + initPos_Pluged);
	}

	public void logln(Object o) {
		System.err.println(o);
	}

	public int getRates() {
		return rates;
	}

	public int getTbidx() {
		return tbidx;
	}

	public int getFullscreen() {
		return fullscreen;
	}

	public int getSine_rate() {
		return sine_rate;
	}

	public int getLine_rate() {
		return line_rate;
	}

	public int getPoints() {
		return points;
	}

	public int getExtPoints() {
		return ext_points;
	}

	private int initPos_Pluged;

	public void setupInitPos_Pluged(int initPos, int pluggedTrgOffset) {
		initPos_Pluged = initPos * rates + pluggedTrgOffset;
	}

	public int getInitPos_Pluged() {
		return initPos_Pluged;
	}

	/**
	 * -...+
	 * 
	 * @param m
	 */
	public void move(int m) {
		initPos_Pluged += m;
	}

}