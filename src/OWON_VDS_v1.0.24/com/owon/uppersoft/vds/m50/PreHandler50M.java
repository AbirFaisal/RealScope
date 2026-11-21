package com.owon.uppersoft.vds.m50;

import java.nio.ByteBuffer;
import java.util.Iterator;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.vds.device.interpolate.TinyPlugHelper;
import com.owon.uppersoft.vds.machine.LowControlManger;
import com.owon.uppersoft.vds.machine.VDS2052;
import com.owon.uppersoft.vds.source.comm.data.ChannelDataInfo_Tiny;
import com.owon.uppersoft.vds.source.comm.data.ChannelsTransportInfo_Tiny;
import com.owon.uppersoft.vds.source.front.AbsPreHandler;
import com.owon.uppersoft.vds.util.BufferHandleUtil;

public class PreHandler50M extends AbsPreHandler {

	public PreHandler50M(ControlManager cm, LowControlManger lcm) {
		super(cm, lcm);
	}

	public int getTB_5kFullScreenIndex() {
		return VDS2052.TB_5kFullScreen;
	}

	public int getTB_1_4kkFullScreenIndex() {
		return VDS2052.TB_1_4kFullScreen;
	}

	public void preLoadReceiveData(ChannelsTransportInfo_Tiny ci) {
		vl.logln("preLoadReceiveData channels: " + ci.getChannelCount());

		int tb = tc.getTimebaseIdx();
		// int htp = tc.getHorizontalTriggerPosition();
		int fullscreen = getFullScreen();
		// vl.logln("preLoadReceiveData >>> ");
		// vl.logln("fullscreen: " + fullscreen);

		boolean pk = lcm.alsoSupportPeakDetect();

		if (tb >= getTB_5kFullScreenIndex()) {
			boolean isSlowMove = cm.getMachineInfo().isSlowMove(tb);

			Iterator<ChannelDataInfo_Tiny> it = ci.iterator_ChannelDataInfo();
			while (it.hasNext()) {
				ChannelDataInfo_Tiny cdi = it.next();
				/** for slow sweep included, the memory depth is cut to the screen range */
				ByteBuffer dmbuf = cdi.getALLDMBuffer();
				/** first take the adc data at the full-screen position, removing the extra adc points (50*2) in all cases(50*2) */
				if (isSlowMove) {
					/** in slow sweep, remove the extra adc points (50*2) taken at the "front" and use the adc within the later full-screen range */
					BufferHandleUtil.skipByteBuffer(dmbuf, TRANBUFLEN_PREDM
							+ TRANBUFLEN_SUFDM);
				} else {
					cdi.trimNgetADCByteBuffer();

					/** fixes the jitter when triggering on square-wave edges due to insufficient 5k memory depth, by deliberately offsetting one point */
					int p = dmbuf.position();
					int l = dmbuf.limit();
					dmbuf.limit(l + 1);
					dmbuf.position(p + 1);
				}
				int points2pix = pk ? 2 : 4;
				handelData_fullscreen(dmbuf, cdi, ci, isSlowMove, points2pix);
			}
		} else if (tb >= getTB_1_4kkFullScreenIndex()) {
			/**
			 * these scales do not support peak detect
			 */
			Iterator<ChannelDataInfo_Tiny> it = ci.iterator_ChannelDataInfo();
			while (it.hasNext()) {
				ChannelDataInfo_Tiny cdi = it.next();
				/** for scales without interpolation or compression, remove the extra adc points (50*2)(50*2) */
				ByteBuffer dmbuf = cdi.trimNgetADCByteBuffer();
				/** for full-screen point counts of 1k, 2.5k1k, 2.5k */
				handelData_noplug(dmbuf, cdi, ci, tb, fullscreen);
			}
		} else {
			Iterator<ChannelDataInfo_Tiny> it = ci.iterator_ChannelDataInfo();
			while (it.hasNext()) {
				ChannelDataInfo_Tiny cdi = it.next();
				/** for timebase scales that need interpolation and trigger locating, the memory depth is cut to the screen range */
				// ByteBuffer dmbuf =
				cdi.trimNgetADCByteBuffer();
				// handelData_fullscreen(dmbuf, cdi, ci, htp, false, 4);
			}

			// if(1==1)return;
			// or: as (tb >= TB_10kFullScreen)

			tli.htpExtraAdjustment = (tb == VDS2052.TB_5ns ? -20 : 0);
			tli.htpTuneAdjustment = lcm.getRemainHtp();

			/** for interpolated scales, use the full memory depth */
			/**
			 * enabling withoutLocateTrg shows the content at the center of the data without trigger locating
			 * 
			 * obtain all the usable adc data segments, then perform trigger locating
			 * 
			 */
			TinyPlugHelper plugHelper = getTinyPlugHelper(cm
					.getAllChannelsNumber());

			stp.handle(ci, fullscreen, plugHelper, tli);
		}
	}

	private TinyPlugHelper getTinyPlugHelper(int allChlNum) {
		if (tph == null) {
			tph = // new P10H();
			new DynamicPlugHelper50M(allChlNum);
		}
		return tph;
	}

	private TinyPlugHelper tph;
}
