package com.owon.uppersoft.vds.device.interpolate;

import java.nio.ByteBuffer;

import com.owon.uppersoft.vds.core.aspect.base.Logable;
import com.owon.uppersoft.vds.core.plug.VDSource;
import com.owon.uppersoft.vds.core.wf.rt.PlugInfo;
import com.owon.uppersoft.vds.source.comm.data.ChannelDataInfo_Tiny;
import com.owon.uppersoft.vds.util.format.SFormatter;

public abstract class TinyPlugHelper implements Logable {

	public static final int EXTRA = 50;

	/**
	 * 可再优化改变EXTRA使得其在拉触发以后只插有限的点，在插值后再做拉触发偏移
	 * 
	 * @param acp
	 * @param cdi
	 * @param fullscreen
	 */
	public PlugPullContext handle(ByteBuffer dmbuf, ChannelDataInfo_Tiny cdi,
			int fullscreen) {
		PlugPullContext ppContext = new PlugPullContext(false, cdi.chl);
		ppContext.setRaw_adc(dmbuf);
		logln("[inside pluging...]fullscreen " + fullscreen);

		PlugInfo pi = cdi.pi;

		byte[] src = dmbuf.array();
		int p = dmbuf.position();
		int l = dmbuf.limit();

		// System.err.println(cdi.chl + ", " + p + ", " + l);
		int ptr = ((p + l) >> 1) - (fullscreen >> 1);

		/**
		 * 必须使用原始数组src，这样才能多获取左右的50个像素点，用于正弦插值和拉触发
		 * 
		 * buf长度多1，用于尾部插值
		 * 
		 * 后续还要改10倍，解决范围有限问题，实现拉触发，解决htp对应的移动量
		 */

		int beg = ptr - EXTRA;
		int range = fullscreen + 1 + (EXTRA << 1);

		cdi.dmInitPos = beg - p;
		pi.plugDataLength = range;

		logln(SFormatter.UIformat("ptr: %d, fullscreen: %d", ptr, fullscreen));
		logln(SFormatter.UIformat("[Rerange to]beg: %d, range: %d", beg, range));

		ByteBuffer rt_buf = ByteBuffer.wrap(src, beg, range);
		doPlugJob(fullscreen, rt_buf, ppContext, pi);

		logln("[outside pluging...]");
		return ppContext;
	}

	public void doPlugJobTrg(int fullscreen, ByteBuffer rt_buf,
			PlugPullContext ppContext) {
		double plugRate = getPlugRate(fullscreen);
		int skip = 0;
		int demand = (int) (rt_buf.remaining() * plugRate);
		int chl = ppContext.getPlugedBuffer_Id();
		ByteBuffer p_adc = plugJob(fullscreen, rt_buf, new PlugInfo(),
				ppContext.isForTrg(), chl);
		ppContext.setPluged_adc(p_adc);
		ppContext.markDemandRange(skip, demand);
	}

	protected void doPlugJob(int fullscreen, ByteBuffer rt_buf,
			PlugPullContext ppContext, PlugInfo pi) {
		double plugRate = getPlugRate(fullscreen);
		int skip = (int) (EXTRA * plugRate);// 总插值倍数*50
		int demand = (int) (fullscreen * plugRate);
		int chl = ppContext.getPlugedBuffer_Id();
		ByteBuffer p_adc = plugJob(fullscreen, rt_buf, pi,
				ppContext.isForTrg(), chl);
		ppContext.setPluged_adc(p_adc);
		ppContext.markDemandRange(skip, demand);
	}

	protected abstract ByteBuffer plugJob(int fullscreen, ByteBuffer rt_buf,
			PlugInfo pi, boolean forTrg, int chl);

	public abstract double getPlugRate(int fullscreen);

	protected static final ByteBuffer lineplug(ByteBuffer bf, int pr) {
		int chllen = bf.remaining();
		byte[] src = bf.array();
		int ptr = bf.position();
		ByteBuffer bb2;
		int datalen = chllen * pr;

		/** 多插一个点，确保边界显示 */
		int plen = chllen;
		bb2 = ByteBuffer.allocate(datalen);
		byte[] dest = bb2.array();
		VDSource.genSimulateLinearPlug(dest, 0, src, ptr, plen, pr);

		// System.arraycopy(src, ptr, dest, 0, chllen);
		// System.arraycopy(src, ptr, dest, chllen, chllen);
		// System.err.println(ptr + ", " + chllen + ", " + datalen);

		bb2.position(0);
		bb2.limit(datalen);
		return bb2;
	}

	protected static final ByteBuffer lineplug2_5(ByteBuffer bf) {
		int chllen = bf.remaining();
		byte[] src = bf.array();
		int ptr = bf.position();
		ByteBuffer bb2;
		int datalen = (int) (chllen * 2.5);

		/** 多插一个点，确保边界显示 */
		bb2 = ByteBuffer.allocate(datalen);
		byte[] dest = bb2.array();
		VDSource.genSimulateLinearPlug_2_3(dest, 0, src, ptr, chllen, 0);

		// System.arraycopy(src, ptr, dest, 0, chllen);
		// System.arraycopy(src, ptr, dest, chllen, chllen);
		// System.err.println(ptr + ", " + chllen + ", " + datalen);

		bb2.position(0);
		bb2.limit(datalen);
		return bb2;
	}

	public void log(Object o) {
		// PrincipleTiny.dblog(o);
		System.out.print(o);
	}

	public void logln(Object o) {
		// PrincipleTiny.dblogln(txt);
		System.out.println(o);
	}

}
