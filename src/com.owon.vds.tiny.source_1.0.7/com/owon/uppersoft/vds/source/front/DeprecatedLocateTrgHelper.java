package com.owon.uppersoft.vds.source.front;

import java.nio.ByteBuffer;

import com.owon.uppersoft.vds.device.interpolate.PlugPullContext;
import com.owon.uppersoft.vds.device.interpolate.TinyPlugHelper;
import com.owon.uppersoft.vds.util.ArrayLogger;

public class DeprecatedLocateTrgHelper extends LocateTrgHelper {

	/**
	 * 
	 * 继续尝试：
	 * 
	 * 先拉，后插值
	 */
	private int doHandleTrg_PullPlugPull(ByteBuffer trgb, boolean raise,
			int level, int fullscreen, TinyPlugHelper tph, int chl) {
		logln("[doHandleTrg_p2]");

		final int p = trgb.position();
		final int l = trgb.limit();
		final int center = (p + l) >> 1;
		byte[] tarr = trgb.array();
		logln(p + ", " + l + ", " + center);

		logln("[1st getShift]");
		int del = getShiftTwoWay(raise, trgb, level);
		logln("[1st getShift]" + del);

		int i = center + del;
		boolean bingoI = tarr[i] == level;

		i -= 2;
		ArrayLogger.outArray2Logable(this, tarr, i, 4);
		ByteBuffer p_trgb = ByteBuffer.wrap(tarr, i, 4);

		PlugPullContext ppContext = new PlugPullContext(true, chl);
		tph.doPlugJobTrg(fullscreen, p_trgb, ppContext);

		p_trgb = ppContext.getPluged_adc();
		ppContext.logInfo();
		ArrayLogger.outArray2Logable(this, p_trgb.array(), p_trgb.position(),
				p_trgb.remaining());
		double plugrate = tph.getPlugRate(fullscreen);

		logln("[2nd getShift]");
		int del2 = 0;
		if (!bingoI) {
			del2 = getShiftTwoWay(raise, p_trgb, level);
		}
		logln("[2nd getShift]" + del2);

		int result;
		if (del == 0) {
			result = del2;
		} else {
			result = (int) (del2 + (del - 2) * plugrate);
		}

		logln("result: " + result);
		return result;
	}

	/**
	 * 
	 * 最后尝试：
	 * 
	 * 根据fullscreen，区分处理
	 */
	private int doHandleTrg_both(ByteBuffer trgb, boolean raise, int level,
			int fullscreen, TinyPlugHelper tph, int chl) {
		logln("[doHandleTrg_p1]");
		ByteBuffer p_trgb = null;

		int del = getShiftOneWay(raise, p_trgb, level);

		PlugPullContext acp = new PlugPullContext(true, chl);
		tph.doPlugJobTrg(fullscreen, trgb, acp);

		p_trgb = acp.getPluged_adc();
		acp.logInfo();

		int del2 = getShiftOneWay(raise, p_trgb, level);
		return del2;
	}

	/**
	 * 
	 * 获取拉的位移
	 * 
	 * @param raise
	 * @param p_trgb
	 * @param level
	 * @return
	 */
	private int getShiftOneWay(boolean raise, ByteBuffer p_trgb, int level) {
		logln("[getShift]");

		final int p = p_trgb.position();
		final int l = p_trgb.limit();
		final int center = (p + l) >> 1;

		byte[] arr = p_trgb.array();

		// if (true)return 0;
		int i = center;
		int shift;
		/**
		 * 当波形斜率不大的时候，容易拉到
		 * 
		 * 当波形斜率很大的时候，拉的前后容易过头
		 */
		if (raise) {
			logPull("before shift: " + i);
			while (arr[i] > level) {
				i -= 4;
				if (i < p) {
					i = p;
					break;
				} else if (i >= l) {
					i = l - 1;
					break;
				}
			}
			logPull("midle shift: " + i);
			log(arr[i] + " ");
			while (arr[i] < level) {
				i++;
				if (i < p) {
					i = p;
					break;
				} else if (i >= l) {
					i = l - 1;
					break;
				}
				log(arr[i] + " ");
			}
			logln("");
			logPull("after shift: " + i);

			shift = i - center;
			logPull(arr[i] + " ?=> " + level);
			// DBG.outArrayHex(this, arr, i - 2, 4);
		} else {
			logPull("before shift: " + i);
			while (arr[i] > level) {
				i += 4;
				if (i < p) {
					i = p;
					break;
				} else if (i >= l) {
					i = l - 1;
					break;
				}
			}
			logPull("midle shift: " + i);
			while (arr[i] < level) {
				i--;
				if (i < p) {
					i = p;
					break;
				} else if (i >= l) {
					i = l - 1;
					break;
				}
			}
			logPull("after shift: " + i);

			shift = i - center;
			logPull(arr[i] + " ?=> " + level);
		}
		return shift;
	}
}
