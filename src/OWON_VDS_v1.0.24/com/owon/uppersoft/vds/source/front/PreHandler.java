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
				/** 对包含慢扫，截取屏幕范围的存储深度 */
				ByteBuffer dmbuf = cdi.getALLDMBuffer();
				/** 先取出满屏位置的adc数据，都去除额外多拿的adc点(50*2) */
				if (isSlowMove) {
					/** 慢扫时，去除"靠前"额外多拿的adc点(50*2)，取靠后的满屏范围内的adc来用 */
					BufferHandleUtil.skipByteBuffer(dmbuf, TRANBUFLEN_PREDM
							+ TRANBUFLEN_SUFDM);
				} else {
					cdi.trimNgetADCByteBuffer();

					/** 解决5k存深不足，在方波升降沿触发会抖的问题，人为偏一个点 */
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
			 * 这些档位不支持峰值检测
			 */
			Iterator<ChannelDataInfo_Tiny> it = ci.iterator_ChannelDataInfo();
			while (it.hasNext()) {
				ChannelDataInfo_Tiny cdi = it.next();
				/** 对不插值不压缩的档位，去除额外多拿的adc点(50*2) */
				ByteBuffer dmbuf = cdi.trimNgetADCByteBuffer();
				/** pk且满屏点数为4k的情况，需要区分出来处理点数才可以画出pk效果 */
				if (pk && fullscreen == (GDefine.AREA_WIDTH << 2)) {
					handelData_4k_pk(dmbuf, cdi, ci, fullscreen);
				} else {
					/** 对满屏点为1k, 2k，峰值与否点数可共用 */
					handelData_noplug(dmbuf, cdi, ci, tb, fullscreen);
				}
			}
		} else {
			Iterator<ChannelDataInfo_Tiny> it = ci.iterator_ChannelDataInfo();
			while (it.hasNext()) {
				ChannelDataInfo_Tiny cdi = it.next();
				/** 对要插值和拉触发的时基档位，截取屏幕范围的存储深度 */
				// ByteBuffer dmbuf =
				cdi.trimNgetADCByteBuffer();
				// handelData_fullscreen(dmbuf, cdi, ci, false, 4);
			}

			// if(1==1)return;
			// or: as (tb >= TB_10kFullScreen)
			
			tli.htpExtraAdjustment = 0;
			tli.htpTuneAdjustment = lcm.getRemainHtp();
			
			/** 对插值的档位，使用完整存储深度 */
			/**
			 * 开启withoutLocateTrg即可在不拉触发时看到数据中央的内容
			 * 
			 * 获得可用的全部adc数据段，接下来拉触发
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