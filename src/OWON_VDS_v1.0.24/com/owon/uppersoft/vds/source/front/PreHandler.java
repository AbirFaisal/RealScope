package com.owon.uppersoft.vds.source.front;

import java.nio.ByteBuffer;
import java.util.Iterator;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.data.MinMax;
import com.owon.uppersoft.vds.device.interpolate.TinyPlugHelper;
import com.owon.uppersoft.vds.device.interpolate.dynamic.DynamicPlugHelper;
import com.owon.uppersoft.vds.machine.LowControlManger;
import com.owon.uppersoft.vds.machine.VDS1022;
import com.owon.uppersoft.vds.source.comm.data.ChannelDataInfo_Tiny;
import com.owon.uppersoft.vds.source.comm.data.ChannelsTransportInfo_Tiny;
import com.owon.uppersoft.vds.util.BufferHandleUtil;
import com.owon.uppersoft.vds.util.LoadArrayUtil;

public class PreHandler extends AbsPreHandler {

	public int getTB_5kFullScreenIndex() {
		return VDS1022.TB_5kFullScreen;
	}

	public int getTB_1_4kkFullScreenIndex() {
		return VDS1022.TB_1_4kFullScreen;
	}

	public PreHandler(ControlManager cm, LowControlManger lcm) {
		super(cm, lcm);

		vl.on = false;
	}

	public void preLoadReceiveData(ChannelsTransportInfo_Tiny ci) {
		vl.logln("preLoadReceiveData channels: " + ci.getChannelCount());

		int tb = tc.getTimebaseIdx();
//		int htp = tc.getHorizontalTriggerPosition();
		int fullscreen = getFullScreen();
		// vl.logln("preLoadReceiveData >>> ");
		// vl.logln("fullscreen: " + fullscreen);

		boolean pk = lcm.alsoSupportPeakDetect();

		if (tb >= getTB_5kFullScreenIndex()) {
			boolean isSlowMove = cm.getMachineInfo().isSlowMove(tb);

			Iterator<ChannelDataInfo_Tiny> it = ci.iterator_ChannelDataInfo();
			while (it.hasNext()) {
				ChannelDataInfo_Tiny cdi = it.next();
				/** For cases involving slow sweep, take the memory depth within the screen range */
				ByteBuffer dmbuf = cdi.getALLDMBuffer();
				/** First take the adc data at the full-screen position, removing the extra fetched adc points (50*2) */
				if (isSlowMove) {
					/** During slow sweep, remove the extra adc points fetched at the "front" (50*2), and use the adc within the full-screen range at the back */
					BufferHandleUtil.skipByteBuffer(dmbuf, TRANBUFLEN_PREDM
							+ TRANBUFLEN_SUFDM);
				} else {
					cdi.trimNgetADCByteBuffer();

					/** Fix the jitter when triggering on square-wave edges due to insufficient 5k memory depth, by deliberately offsetting one point */
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
			 * These scales do not support peak detect
			 */
			Iterator<ChannelDataInfo_Tiny> it = ci.iterator_ChannelDataInfo();
			while (it.hasNext()) {
				ChannelDataInfo_Tiny cdi = it.next();
				/** For scales with no interpolation and no compression, remove the extra fetched adc points (50*2) */
				ByteBuffer dmbuf = cdi.trimNgetADCByteBuffer();
				/** pk with 4k full-screen points needs to be told apart and its point count handled in order to draw the pk effect */
				if (pk && fullscreen == (GDefine.AREA_WIDTH << 2)) {
					handelData_4k_pk(dmbuf, cdi, ci, fullscreen);
				} else {
					/** For 1k, 2k full-screen points, the point count can be shared whether or not peak detect is on */
					handelData_noplug(dmbuf, cdi, ci, tb, fullscreen);
				}
			}
		} else {
			Iterator<ChannelDataInfo_Tiny> it = ci.iterator_ChannelDataInfo();
			while (it.hasNext()) {
				ChannelDataInfo_Tiny cdi = it.next();
				/** For timebase scales that need interpolation and trigger locating, take the memory depth within the screen range */
				// ByteBuffer dmbuf =
				cdi.trimNgetADCByteBuffer();
				// handelData_fullscreen(dmbuf, cdi, ci, false, 4);
			}

			// if(1==1)return;
			// or: as (tb >= TB_10kFullScreen)
			
			tli.htpExtraAdjustment = 0;
			tli.htpTuneAdjustment = lcm.getRemainHtp();
			
			/** For interpolated scales, use the full memory depth */
			/**
			 * Enable withoutLocateTrg to see the content at the center of the data without trigger locating
			 * 
			 * Obtain the whole usable adc data segment, then locate the trigger
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
			new DynamicPlugHelper(allChlNum);
		}
		return tph;
	}

	protected TinyPlugHelper tph;

	protected void handelData_4k_pk(ByteBuffer dmbuf, ChannelDataInfo_Tiny cdi,
			ChannelsTransportInfo_Tiny ci, int fullscreen) {
		vl.logln("handelData_4k_pk");

		int points2pix = 2;

		int pixs = GDefine.AREA_WIDTH;
		int rtlen = pixs * points2pix;

		ByteBuffer rtbuf = allocate(cdi.chl, rtlen);
		byte[] rt_arr = rtbuf.array();

		byte[] dm_arr = dmbuf.array();
		int dm_beg = dmbuf.position();// begADC;//
		int dm_end = dmbuf.limit();
		// DBG.outArrayAlpha(src, p + 50, 20);

		int dm_srceen_beg = ((dm_beg + dm_end) >> 1) - (fullscreen >> 1);
		// adcctr - (htp * chllen / screenWidth);
		int dm_srceen_end = dm_srceen_beg + fullscreen;
		int div = fullscreen / pixs;

		int dm_srceen_ptr = dm_srceen_beg;
		int rt_ptr = 0;
		MinMax ri = new MinMax();
		rtbuf.position(rt_ptr);
		vl.logln(cdi.chl + ": " + dm_srceen_beg + " -> " + dm_srceen_end);
		while (dm_srceen_ptr != dm_srceen_end) {
			LoadArrayUtil._2for1(ri, dm_arr, dm_srceen_ptr, div);
			dm_srceen_ptr += div;
			rt_ptr = ri.fillArray(rt_arr, rt_ptr);
		}
		// vl.logln(i);
		rtbuf.limit(rt_ptr);

		cdi.dmInitPos = dm_srceen_beg - dm_beg;
		cdi.dmfullscreen = fullscreen;
		cdi.dmslowMove = 0;

		ci.tailhandle(cdi, rtbuf, 0);
	}

}