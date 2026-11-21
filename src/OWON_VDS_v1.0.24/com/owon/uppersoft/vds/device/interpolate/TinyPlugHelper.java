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
	 * Can be further optimized by changing EXTRA so that after the trigger pull only a limited number of points are interpolated, and the trigger pull offset is done after interpolation
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
		 * The original array src must be used so that the extra 50 pixels on each side can be fetched, for sine interpolation and trigger pull
		 * 
		 * buf is 1 longer, for tail interpolation
		 * 
		 * Later it also needs to change to 10x to solve the limited range problem, implement trigger pull, and solve the movement amount corresponding to htp
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
		int skip = (int) (EXTRA * plugRate);// Total interpolation factor*50
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

		/** Interpolate one extra point to ensure the boundary is displayed */
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

		/** Interpolate one extra point to ensure the boundary is displayed */
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
