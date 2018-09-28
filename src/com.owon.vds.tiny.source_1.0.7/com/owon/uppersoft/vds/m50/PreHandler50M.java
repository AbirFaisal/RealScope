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
				/** 对满屏点为1k, 2.5k */
				handelData_noplug(dmbuf, cdi, ci, tb, fullscreen);
			}
		} else {
			Iterator<ChannelDataInfo_Tiny> it = ci.iterator_ChannelDataInfo();
			while (it.hasNext()) {
				ChannelDataInfo_Tiny cdi = it.next();
				/** 对要插值和拉触发的时基档位，截取屏幕范围的存储深度 */
				// ByteBuffer dmbuf =
				cdi.trimNgetADCByteBuffer();
				// handelData_fullscreen(dmbuf, cdi, ci, htp, false, 4);
			}

			// if(1==1)return;
			// or: as (tb >= TB_10kFullScreen)

			tli.htpExtraAdjustment = (tb == VDS2052.TB_5ns ? -20 : 0);
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
			new DynamicPlugHelper50M(allChlNum);
		}
		return tph;
	}

	private TinyPlugHelper tph;
}
