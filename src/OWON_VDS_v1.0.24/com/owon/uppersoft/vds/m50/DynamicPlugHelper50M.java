package com.owon.uppersoft.vds.m50;

import java.nio.ByteBuffer;

import com.owon.uppersoft.vds.core.wf.rt.PlugInfo;
import com.owon.uppersoft.vds.device.interpolate.TinyPlugHelper;
import com.owon.uppersoft.vds.device.interpolate.dynamic.DynamicPlugUtil;
import com.owon.uppersoft.vds.device.interpolate.dynamic.SineTables;

/**
 * 
 * 最右边为在插值后屏幕内1k点基础上增加的点数=（50+50）*插值倍数
 * 
 * 5ns 25(100MS/s) 40 4000
 * 
 * 10ns 50(100MS/s) 20 2000
 * 
 * 20ns 100(100MS/s) 10 1000
 * 
 * 50ns 250(100MS/s) 4 400
 * 
 * 100ns 500(100MS/s) 2 200
 * 
 */
public class DynamicPlugHelper50M extends TinyPlugHelper {
	private ByteBuffer[] temp, trgs;

	public DynamicPlugHelper50M(int allChlNum) {
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
			bb = ByteBuffer.allocate(6000);
			// dm: (25+50+50)*40+1000预留; trg:100*100
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
		// int percentage = DynamicPlugUtil.arith_interp_mode(arr,
		// p, l);
		// logln("percentage: " + percentage);
		int plugrate = (int) getPlugRate(fullscreen);
		logln("plugrate: " + plugrate);

		if (false) {
			return doLineJob(fullscreen, plugrate, rtbuf, pi);
		}
		// percentage <= 68 || percentage > 100
		// sine
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
		return p_adc;
	}

	private ByteBuffer doLineJob(int fullscreen, int plugrate, ByteBuffer bf,
			PlugInfo pi) {
		// return GDefine.AREA_WIDTH / (double)fullscreen;
		ByteBuffer p_adc = lineplug(bf, plugrate);
		pi.sinePlugRate = 1;
		pi.linearPlugRate = plugrate;
		return p_adc;
	}

	public double getPlugRate(int fullscreen) {
		// return GDefine.AREA_WIDTH / (double)fullscreen;
		switch (fullscreen) {
		case 500:// 100ns, vds2052从本档位开始拉触发
			return 2;
		case 250:// 50ns
			return 4;
		case 100:// 20ns
			return 10;
		case 50:// 10ns
			return 20;
		case 25:// 5ns
			return 40;
		default:
			return -1;
		}
	}
}