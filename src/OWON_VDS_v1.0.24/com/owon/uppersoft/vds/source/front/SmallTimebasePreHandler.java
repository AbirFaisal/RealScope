package com.owon.uppersoft.vds.source.front;

import java.nio.ByteBuffer;
import java.util.Iterator;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.model.trigger.AbsTrigger;
import com.owon.uppersoft.dso.model.trigger.EdgeTrigger;
import com.owon.uppersoft.dso.model.trigger.PulseTrigger;
import com.owon.uppersoft.dso.model.trigger.SlopeTrigger;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.model.trigger.TriggerSet;
import com.owon.uppersoft.vds.core.aspect.base.VLog;
import com.owon.uppersoft.vds.core.wf.rt.ChannelDataInfo;
import com.owon.uppersoft.vds.device.interpolate.PlugPullContext;
import com.owon.uppersoft.vds.device.interpolate.TinyPlugHelper;
import com.owon.uppersoft.vds.source.comm.data.ChannelDataInfo_Tiny;
import com.owon.uppersoft.vds.source.comm.data.ChannelsTransportInfo_Tiny;
import com.owon.uppersoft.vds.util.ArrayLogger;

public class SmallTimebasePreHandler {

	public SmallTimebasePreHandler(ControlManager cm) {
		channelsNumber = cm.getSupportChannelsNumber();
		tc = cm.getTriggerControl();
	}

	public void handle(ChannelsTransportInfo_Tiny ci, int fullscreen,
			TinyPlugHelper plugHelper, TrgLocateInfo tli) {
		if (withoutLocateTrg) {
			withoutLocateT(ci, fullscreen, plugHelper);
		} else {
			locateTrg(ci, fullscreen, plugHelper, tli);
		}
	}

	public static final boolean withoutLocateTrg = 0 == 1;

	private void withoutLocateT(ChannelsTransportInfo_Tiny ci, int fullscreen,
			TinyPlugHelper plugHelper) {
		Iterator<ChannelDataInfo_Tiny> it = ci.iterator_ChannelDataInfo();
		while (it.hasNext()) {
			ChannelDataInfo cdi_ = it.next();
			ChannelDataInfo_Tiny cdi = (ChannelDataInfo_Tiny) cdi_;
			ByteBuffer dmbuf = cdi.getALLDMBuffer();

			logData(dmbuf, cdi, fullscreen);

			PlugPullContext acp = plugHelper.handle(dmbuf, cdi, fullscreen);
			acp.arRange();
			ByteBuffer adcbuf = acp.getPluged_adc();
			ci.tailhandle(cdi, adcbuf, 0);
		}
	}

	private void logData(ByteBuffer bb, ChannelDataInfo_Tiny cdi, int fullscreen) {
		/** Print full screen actual data */
		final int bp = bb.position();
		final int bl = bb.limit();
		byte[] barr = bb.array();
		int ptr = (bp + bl) >> 1;
		ptr -= (fullscreen >> 1);
		ArrayLogger.outArray2Logable(vl, barr, ptr, fullscreen);

		/** Print pull trigger segment data */
		ByteBuffer bb2 = cdi.getTrgLocBuffer();
		final int p = bb2.position();
		final int l = bb2.limit();
		final int len = l - p;
		byte[] arr = bb2.array();
		logln("chl: trg data[100]" + cdi.chl);
		ArrayLogger.outArray2Logable(vl, arr, p, len);
	}

	private void logln(Object txt) {
		vl.logln(txt);
	}

	private VLog vl = new VLog();

	// 50+...+50!!!
	private void locateTrg(ChannelsTransportInfo_Tiny ci, int fullscreen,
			TinyPlugHelper plugHelper, TrgLocateInfo tli) {
		logln("locateTrg");

		/**
		 * 单一触发：触发的通道拉多少，其它通道也拉多少
		 * 
		 * 交替触发：各拉各的
		 */

		if (tc.isSingleTrg()) {
			// logln("isSingleTrg");
			TriggerSet ts = tc.getSingleTriggerSet();
			if (ts != null) {
				int trgchl = ts.getChannel();
				int shift = 0;
				if (!tc.isExtTrg(trgchl)) {
					shift = handleLocate(ci, fullscreen, plugHelper, ts,
							trgchl, tli);
				}
				logln("shift: " + shift);

				for (int chl = 0; chl < channelsNumber; chl++) {
					ChannelDataInfo_Tiny cdi = ci.getChannelDataInfo(chl);
					if (cdi == null || trgchl == chl)
						continue;

					logln("single >>>>>>>>>>>>>>>>>>>>>>>> channel " + chl);
					// logData(bb, cdi, fullscreen);
					// ArrayLogger.outArray2Logable(vl, dmbuf.array(),
					// dmbuf.position()
					// + (dmbuf.remaining() >> 1) - 20, 40);
					/** 50个点无用，除非有不插值却要拉触发的情况，比如dm==1k或其它 */
					shiftChannelRTBuffer(ci, fullscreen, plugHelper, cdi, shift);
				}
			}
		} else {
			/** 目前先在交替的情况下测试 */
			logln("alternate trg: chls = " + channelsNumber);
			for (int chl = 0; chl < channelsNumber; chl++) {
				TriggerSet ts = tc.getAlternateTriggerSet(chl);
				if (ts == null)
					continue;

				handleLocate(ci, fullscreen, plugHelper, ts, chl, tli);
			}
		}
	}

	/**
	 * Completing the various details of the pull trigger, then checking
	 * the hardware frequency, and saving the file format, dm, etc.
	 * 
	 * @param ci
	 * @param fullscreen
	 * @param acp
	 * @param pluger
	 * @param ts
	 * @param chl
	 */
	private int handleLocate(ChannelsTransportInfo_Tiny ci, int fullscreen,
			TinyPlugHelper pluger, TriggerSet ts, int chl, TrgLocateInfo tli) {
		ChannelDataInfo_Tiny cdi = ci.getChannelDataInfo(chl);
		logln("handleLocate chl" + chl + " not null:" + (cdi != null));
		if (cdi == null)
			return 0;

		logln(">>>>>>>>>> channel " + chl);
		ByteBuffer trgbb = cdi.getTrgLocBuffer();
		/** Print full screen and pull trigger data */
		// logData(bb, cdi, fullscreen);
		int shift = 0;
		logln("handleLocate");
		AbsTrigger at = ts.getTrigger();

		boolean shouldShift = true;
		boolean raise = false;
		int level = 0;
		if (!cdi.trg_d || at == ts.video) {
			shouldShift = false;
		} else if (at == ts.slope) {
			SlopeTrigger st = ts.slope;
			int up = st.c_getUppest(), low = st.c_getLowest();
			raise = st.condition >= 3;
			level = raise ? up : low;
		} else if (at == ts.edge) {
			EdgeTrigger et = ts.edge;
			level = ts.getVoltsense().c_getVoltsense();
			raise = et.raisefall == 0;
		} else if (at == ts.pulse) {
			PulseTrigger pt = ts.pulse;
			level = ts.getVoltsense().c_getVoltsense();
			raise = pt.condition >= 3;
		}

		if (!shouldShift) {
			shift = 0;
		} else {
			shift = loctrger.handleTrg(trgbb, raise, level, fullscreen, pluger,
					chl);
			shift += tli.htpTuneAdjustment;
			shift += tli.htpExtraAdjustment;
		}

		shiftChannelRTBuffer(ci, fullscreen, pluger, cdi, shift);
		return shift;
	}

	private static void shiftChannelRTBuffer(ChannelsTransportInfo_Tiny ci,
	                                         int fullscreen, TinyPlugHelper pluger, ChannelDataInfo_Tiny cdi,
	                                         int shift) {
		cdi.dmfullscreen = fullscreen;
		ByteBuffer dmbuf = cdi.getALLDMBuffer();

		// ArrayLogger.outArray2Logable(vl, dmbuf.array(),
		// dmbuf.position()
		// + (dmbuf.remaining() >> 1) - 20, 40);

		PlugPullContext ppContext = pluger.handle(dmbuf, cdi, fullscreen);
		ByteBuffer adcbuf = ppContext.getPluged_adc();

		// ArrayLogger.outArray2Logable(vl, adcbuf.array(),
		// adcbuf.position()
		// + (adcbuf.remaining() >> 1) - 20, 40);

		ppContext.shift(shift);
		cdi.pi.pluggedTrgOffset = ppContext.getSkip();
		adcbuf = ppContext.getPluged_adc();
		ci.tailhandle(cdi, adcbuf, 0);
	}

	private LocateTrgHelper loctrger = new LocateTrgHelper();
	private int channelsNumber;
	private TriggerControl tc;
}