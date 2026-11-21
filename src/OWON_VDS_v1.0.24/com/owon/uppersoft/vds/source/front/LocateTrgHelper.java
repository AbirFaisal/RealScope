package com.owon.uppersoft.vds.source.front;

import java.nio.ByteBuffer;

import com.owon.uppersoft.vds.core.aspect.base.Logable;
import com.owon.uppersoft.vds.core.aspect.base.VLog;
import com.owon.uppersoft.vds.device.interpolate.PlugPullContext;
import com.owon.uppersoft.vds.device.interpolate.TinyPlugHelper;
import com.owon.uppersoft.vds.util.format.SFormatter;

/**
 * Note: inversion is before trigger locating, averaging is after trigger locating, no peak detect,
 * 
 * Scale Full-screen Interpolation-factor Count-after-interpolation
 * 
 */
public class LocateTrgHelper implements Logable {

	public static final int MAX_PLUGED_LENGTH = 3000;

	public LocateTrgHelper() {
	}

	public void logPull(Object o) {
		// System.out.println(o);
	}

	public void logPull2(Object o) {
		System.out.println(o);
	}

	@Override
	public void log(Object o) {
		// System.out.print(o);
	}

	@Override
	public void logln(Object o) {
		// System.out.println(o);
	}

	protected VLog vl = new VLog();

	public static final int LEVEL_UPPEST = 125, LEVEL_LOWEST = -125;

	public int handleTrg(ByteBuffer trgb, boolean raise, int level,
			int fullscreen, TinyPlugHelper tph, int chl) {
		logPull("[handleTrg]");
		/**
		 * when the trigger level exceeds the adc limits, the adc is automatically clamped to this range,
		 * 
		 * so the result of the trigger locating comparison is inevitably that no corresponding position is found, and locating can be skipped
		 */
		if (!(level <= LEVEL_UPPEST && level >= LEVEL_LOWEST)) {
			return 0;
		}

		logPull(raise ? "raise" : "fall");

		if (trgb == null)
			return Integer.MAX_VALUE;

		final int p = trgb.position();
		final int l = trgb.limit();
		final int len = l - p;

		if (len <= 0)
			return Integer.MAX_VALUE;

		return doHandleTrg_plugFirst(trgb, raise, level, fullscreen, tph, chl);
	}

	private void limitTrgBuffer(ByteBuffer trgb, TinyPlugHelper tph,
			int fullscreen) {
		int max_pluged_len = MAX_PLUGED_LENGTH * 2;
		double plugRate = tph.getPlugRate(fullscreen);

		int len = (int) (max_pluged_len / plugRate) + 2;

		int p = trgb.position();
		int l = trgb.limit();
		logPull2(p + ", " + l + "," + len);
		if (len > l - p) {
			return;
		}

		int beg = ((p + l) >> 1) - (len >> 1);
		logPull2("limitTrgBuffer: " + beg + "," + len);
		trgb.limit(beg + len);
		trgb.position(beg);
	}

	/**
	 * Simple attempt:
	 * 
	 * interpolate trgb first, then locate
	 */
	private int doHandleTrg_plugFirst(ByteBuffer trgb, boolean raise,
			int level, int fullscreen, TinyPlugHelper tph, int chl) {
		logln("[doHandleTrg_p0]");
		ByteBuffer p_trgb = null;

		PlugPullContext ppContext = new PlugPullContext(true, chl);

		limitTrgBuffer(trgb, tph, fullscreen);

		tph.doPlugJobTrg(fullscreen, trgb, ppContext);

		int skip = ppContext.getSkip();
		int demand = ppContext.getDemand();
		p_trgb = ppContext.getPluged_adc();
		String msg = SFormatter.UIformat(
				"skip: %d, demand: %d while limit: %d", skip, demand,
				p_trgb.limit());
		logPull(msg);

		int result = getShiftTwoWay(raise, p_trgb, level);
		logln("result: " + result);

		/** print the data around the locked point after trigger locating */
		// final int p = p_trgb.position();
		// final int l = p_trgb.limit();
		// final int center = (p + l) >> 1;
		// byte[] tarr = p_trgb.array();
		// int i = center + result - 60;
		// ArrayLogger.outArray2Logable(vl, tarr, i, 120);
		return result;
	}

	/**
	 * 
	 * get the locating shift
	 * 
	 * @param raise
	 * @param p_trgb
	 * @param level
	 * @return
	 */
	protected int getShiftTwoWay(boolean raise, ByteBuffer p_trgb, int level) {
		logln("[getShift]");

		final int p = p_trgb.position();
		final int l = p_trgb.limit();
		final int center = (p + l) >> 1;
		logln(p + ", " + center + ", " + l);
		byte[] arr = p_trgb.array();

		/** print the data after interpolation */
		// DBG.outArray2Logable(this, arr, p, l - p);
		// if (true)return 0;
		int shift;

		int defaultTraversal = MAX_PLUGED_LENGTH;
		/**
		 * when the waveform slope is small, it is easy to locate
		 * 
		 * when the waveform slope is large, the locating easily overshoots before or after
		 * 
		 * the maximum traversal range is limited to 500 points here
		 */
		int maxleft = center - p - 1 - LocateTrgUtil.Step;
		if (maxleft < 0)
			maxleft = 0;
		maxleft = Math.min(maxleft, defaultTraversal);

		int maxrigth = l - center - 1 - LocateTrgUtil.Step;
		if (maxrigth < 0)
			maxrigth = 0;
		maxrigth = Math.min(maxrigth, defaultTraversal);

		shift = ltu.trigpull(arr, raise ? 0 : 1, level, center, maxrigth,
				maxleft);
		return shift;
	}

	private LocateTrgUtil ltu = new LocateTrgUtil();

}