package com.owon.uppersoft.vds.device.interpolate;

import java.nio.ByteBuffer;

import com.owon.uppersoft.vds.core.plug.Plug5;
import com.owon.uppersoft.vds.core.wf.rt.PlugInfo;

public class P5H extends TinyPlugHelper {
	protected static int sinplugrate = 10;

	protected ByteBuffer plugJob(int fullscreen, ByteBuffer bf, PlugInfo pi,
			boolean forTrg, int chl) {
		ByteBuffer p_adc = null;

		switch (fullscreen) {
		case 400:// 200ns, vds1022从本档位开始拉触发
			p_adc = lineplug2_5(bf);

			pi.linearPlugRate = 2.5;
			pi.sinePlugRate = 1;
			break;
		case 200:// 100ns
			// bb2 = lineplug(bf, 5);
			p_adc = sineplug5(bf);

			pi.linearPlugRate = 1;
			pi.sinePlugRate = 5;
			break;
		case 100:// 50ns
			// bb2 = lineplug(internelBuf, 10);
			p_adc = lineplug(sineplug5(bf), 2);

			pi.linearPlugRate = 2;
			pi.sinePlugRate = 5;
			break;
		case 40:// 20ns
			// bb2 = lineplug(internelBuf, 25);
			p_adc = lineplug(sineplug5(bf), 5);

			pi.linearPlugRate = 5;
			pi.sinePlugRate = 5;
			break;
		case 20:// 10ns
			// bb2 = lineplug(internelBuf, 50);
			p_adc = lineplug(sineplug5(bf), 10);

			pi.linearPlugRate = 10;
			pi.sinePlugRate = 5;
			break;
		case 10:// 5ns
			// bb2 = lineplug(internelBuf,100);
			p_adc = lineplug(sineplug5(bf), 20);

			pi.linearPlugRate = 20;
			pi.sinePlugRate = 5;
			break;
		}
		/**
		 * 开启下方注释即可在不拉触发时看到数据中央的内容
		 * 
		 * 获得可用的全部adc数据段，接下来拉触发
		 * 
		 * 单一触发：触发的通道拉多少，其它通道也拉多少
		 * 
		 * 交替触发：各拉各的
		 */
		return (p_adc);
	}

	public double getPlugRate(int fullscreen) {
		int sinplugrate = 5;
		switch (fullscreen) {
		case 400:// 200ns, vds1022从本档位开始拉触发
			return 2.5;
		case 200:// 100ns
			return sinplugrate;
		case 100:// 50ns
			return sinplugrate * 2;
		case 40:// 20ns
			return sinplugrate * 5;
		case 20:// 10ns
			return sinplugrate * 10;
		case 10:// 5ns
			return sinplugrate * 20;
		}
		return -1;
	}

	protected final ByteBuffer sineplug5(ByteBuffer bf) {
		int pr = 5;
		int p = bf.position();
		int len = bf.remaining();
		int pluglen = len;
		// DBG.configArray(bf.array(), p, len);

		byte[] src = new byte[len + pr];
		System.arraycopy(bf.array(), p, src, 0, len);

		int datalen = len * pr;
		ByteBuffer bb2 = ByteBuffer.allocate(datalen);
		byte[] dest = bb2.array();

		logln("[sine plug]" + len + " >> " + datalen);

		Plug5.sine_interpolate(dest, src, pluglen);
		bb2.position(0);
		bb2.limit(datalen);
		/** 这里不限界adc，因为插值的时候需要在int基础上限界了 */
		// trimADCByteBuffer(bb2);
		// DBG.configArray(bb2.array(), bb2.position(), bb2.limit());
		return bb2;
	}

}