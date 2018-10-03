package com.owon.uppersoft.vds.m50;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.machine.LowControlManger;
import com.owon.uppersoft.vds.machine.VDS2052;
import com.owon.uppersoft.vds.source.comm.ext.IntObject;

public class LowControlManager50M extends LowControlManger {
	public LowControlManager50M(ControlManager cm) {
		super(cm);
	}

	public int getHTPWithoutCompress(int htp, IntObject io) {
		/** 可能和居中有一像素偏差 */
		htp++;

		int tb = cm.getTimeControl().getTimebaseIdx();

		int fullscreen = getMachineInfo().getFullScreen(ChannelSampleConfig,
				getDeepMemoryControl().getDeepIdx())[tb];
		if (tb >= VDS2052.TB_5kFullScreen) {
			int screenDMDataLen = cm.getDeepMemoryControl().getDeepDataLen();
			/** 这里用5k在拉动触发电平到一点距离以后才没有偏差 */
			return (int) (htp * screenDMDataLen / (double) GDefine.AREA_WIDTH);
		} else if (tb >= VDS2052.TB_1_4kFullScreen) {
			return (int) (htp * fullscreen / (double) GDefine.AREA_WIDTH);
		} else {
			/** 这里的pointGap都是整数 */
			double pointGap = GDefine.AREA_WIDTH / (double) fullscreen;
			// System.err.println(pointGap);
			io.value = (htp % (int) pointGap);
			cm.pcs.firePropertyChange(PropertiesItem.APPEND_TXTLINE, null,
					("io.value: " + io.value));
			return (int) (htp / pointGap);
		}
	}
}