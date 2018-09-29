package com.owon.uppersoft.vds.device.interpolate;

import java.nio.ByteBuffer;

import com.owon.uppersoft.vds.core.plug.Plug10;
import com.owon.uppersoft.vds.core.wf.rt.PlugInfo;

/**
 * 使用在最小时基5ns可能因为拉触发遍历界限太小，对大周期波形锁不住，可调整LocateTrgHelper.MAX_PLUGED_LENGTH至较大数值
 * 
 */
public class P10H extends TinyPlugHelper {
	protected static int sinplugrate = 10;

	public P10H() {
	}

	protected ByteBuffer plugJob(int fullscreen, ByteBuffer bf, PlugInfo pi,
			boolean forTrg, int chl) {
		ByteBuffer p_adc = null;

		switch (fullscreen) {
		case 400:// 200ns, vds1022从本档位开始拉触发
			p_adc = lineplug(bf, 5);

			pi.linearPlugRate = 5;
			pi.sinePlugRate = 1;
			break;
		case 200:// 100ns
			p_adc = sineplug10(bf);// lineplug(bf, 5);

			pi.linearPlugRate = 1;
			pi.sinePlugRate = 10;
			break;
		case 100:// 50ns
			// bb2 = lineplug(internelBuf, 10);
			p_adc = sineplug10(bf);

			pi.linearPlugRate = 1;
			pi.sinePlugRate = 10;
			break;
		case 40:// 20ns
			// bb2 = lineplug(internelBuf, 25);
			p_adc = lineplug(sineplug10(bf), 5);

			pi.linearPlugRate = 5;
			pi.sinePlugRate = 10;
			break;
		case 20:// 10ns
			// bb2 = lineplug(internelBuf, 50);
			p_adc = lineplug(sineplug10(bf), 5);

			pi.linearPlugRate = 5;
			pi.sinePlugRate = 10;
			break;
		case 10:// 5ns
			// bb2 = lineplug(internelBuf,100);
			p_adc = lineplug(sineplug10(bf), 10);

			pi.linearPlugRate = 10;
			pi.sinePlugRate = 10;
			break;
		}
		return (p_adc);
	}

	public double getPlugRate(int fullscreen) {
		int sinplugrate = 10;
		switch (fullscreen) {
		case 400:// 200ns, vds1022从本档位开始拉触发
			return 5;
		case 200:// 100ns
			return sinplugrate;
		case 100:// 50ns
			return sinplugrate;
		case 40:// 20ns
			return sinplugrate * 5;
		case 20:// 10ns
			return sinplugrate * 5;
		case 10:// 5ns
			return sinplugrate * 10;
		}
		return -1;
	}

	protected final ByteBuffer sineplug10(ByteBuffer bf) {
		int pr = 10;
		int p = bf.position();
		int len = bf.remaining();
		int pluglen = len;

		byte[] src = new byte[len + pr];
		System.arraycopy(bf.array(), p, src, 0, len);

		int datalen = len * pr;
		ByteBuffer bb = ByteBuffer.allocate(datalen);
		byte[] dest = bb.array();
		logln(len + ", " + datalen);
		Plug10.sine_interpolate(dest, src, pluglen, 0, 0);
		bb.position(0);
		bb.limit(datalen);
		/** 这里不限界adc，因为插值的时候需要在int基础上限界了 */
		// trimADCByteBuffer(bb);
		return bb;
	}

}