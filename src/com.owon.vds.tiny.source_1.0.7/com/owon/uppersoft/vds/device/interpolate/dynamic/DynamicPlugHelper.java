package com.owon.uppersoft.vds.device.interpolate.dynamic;

import java.nio.ByteBuffer;

import com.owon.uppersoft.vds.core.wf.rt.PlugInfo;
import com.owon.uppersoft.vds.device.interpolate.TinyPlugHelper;

/**
 * 
 * 最右边为在插值后屏幕内1k点基础上增加的点数=（50+50）*插值倍数
 * 
 * 5ns 10(100MS/s) 100 10000
 * 
 * 10ns 20(100MS/s) 50 5000
 * 
 * 20ns 40(100MS/s) 25 2500
 * 
 * 50ns 100(100MS/s) 10 1000
 * 
 * 100ns 200(100MS/s) 5 500
 * 
 * 200ns 400(100MS/s) 2.5 250
 * 
 */
public class DynamicPlugHelper extends TinyPlugHelper {

	private static final int FULLSCREEN_400 = 400;
	private ByteBuffer[] temp, trgs;

	public DynamicPlugHelper(int allChlNum) {
		temp = new ByteBuffer[allChlNum];
		trgs = new ByteBuffer[allChlNum];
	}

	private ByteBuffer allocate(boolean forTrg, int len, int chl) {
		ByteBuffer[] bbs = forTrg ? trgs : temp;

		if (chl < 0 || chl >= bbs.length) {
			System.err.println("err DynamicPlugHelper allocate chl: " + chl
					+ ", len: " + len + ", forTrg: " + forTrg);
			return null;
		}
		ByteBuffer bb = bbs[chl];
		if (bb == null) {
			bb = bbs[chl] = ByteBuffer.allocate(12000);
			// dm: (10+50+50)*100+1000预留; trg:100*100
		}
		// Arrays.fill(bb.array(), (byte)0);

		bb.position(0);
		bb.limit(len);
		return bb;
	}

	protected ByteBuffer plugJob(int fullscreen, ByteBuffer rtbuf, PlugInfo pi,
			boolean forTrg, int chl) {
		ByteBuffer p_adc = null;
		final int p = rtbuf.position();
		final int l = rtbuf.limit();
		final int len = l - p;
		byte[] arr = rtbuf.array();

		// for (int i = p; i < l; i++) {
		// log(arr[i] + ", ");}
		// logln("");
		// logln(l - p);
		/** 还有问题，判断是否线性的处理先关闭 */
		// int percentage = DynamicPlugUtil.arith_interp_mode(arr, p, l);
		// logln("percentage: " + percentage);
		int plugrate = (int) getPlugRate(fullscreen);
		logln("plugrate: " + plugrate);

		if (false) {
			return doLineJob(fullscreen, plugrate, rtbuf, pi);
		}
		// percentage <= 68 || percentage > 100
		// sine
		if (fullscreen == FULLSCREEN_400) {
			// 200ns, vds1022从本档位开始拉触发
			p_adc = lineplug2_5(rtbuf);
			pi.sinePlugRate = 1;
			pi.linearPlugRate = 2.5;
		} else {
			int uselen = len * plugrate;
			ByteBuffer bb = allocate(forTrg, uselen, chl);
			byte[] out = bb.array();
			int[][] sincTable = SineTables.sincTabSp(plugrate);

			if (sincTable == null) {
				return doLineJob(fullscreen, plugrate, rtbuf, pi);
			}
			DynamicPlugUtil.sinc_interps(arr, p, l, out, plugrate, sincTable);
			// 100ns时，5倍插值效果
			// Plug5.sine_interpolate(out, arr, len);
			p_adc = ByteBuffer.wrap(out, 0, uselen);
			pi.sinePlugRate = plugrate;
			pi.linearPlugRate = 1;
		}
		return p_adc;
	}

	private ByteBuffer doLineJob(int fullscreen, int plugrate, ByteBuffer bf,
			PlugInfo pi) {
		// line
		ByteBuffer p_adc = null;
		switch (fullscreen) {
		case FULLSCREEN_400:// 200ns, vds1022从本档位开始拉触发
			p_adc = lineplug2_5(bf);

			pi.sinePlugRate = 1;
			pi.linearPlugRate = 2.5;
			break;
		case 200:// 100ns
		case 100:// 50ns
		case 40:// 20ns
		case 20:// 10ns
		case 10:// 5ns
			p_adc = lineplug(bf, plugrate);

			pi.sinePlugRate = 1;
			pi.linearPlugRate = plugrate;
			break;
		}
		return p_adc;
	}

	public double getPlugRate(int fullscreen) {
		switch (fullscreen) {
		case FULLSCREEN_400:// 200ns, vds1022从本档位开始拉触发
			return 2.5;
		case 200:// 100ns
			return 5;
		case 100:// 50ns
			return 10;
		case 40:// 20ns
			return 25;
		case 20:// 10ns
			return 50;
		case 10:// 5ns
			return 100;
		default:
			return -1;
		}
	}

}