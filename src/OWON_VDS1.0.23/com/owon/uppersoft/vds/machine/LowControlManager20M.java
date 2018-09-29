package com.owon.uppersoft.vds.machine;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.source.comm.ext.IntObject;

public class LowControlManager20M extends LowControlManger {
	public LowControlManager20M(ControlManager cm) {
		super(cm);
	}

	public int getHTPWithoutCompress(int htp, IntObject io) {
		/** 可能和居中有一像素偏差 */
		htp++;

		int tb = cm.getTimeControl().getTimebaseIdx();

		int fullscreen = getMachineInfo().getFullScreen(ChannelSampleConfig,
				getDeepMemoryControl().getDeepIdx())[tb];
		if (tb >= VDS1022.TB_5kFullScreen) {
			int screenDMDataLen = cm.getDeepMemoryControl().getDeepDataLen();
			/** 这里用5k在拉动触发电平到一点距离以后才没有偏差 */
			return (int) (htp * screenDMDataLen / (double) GDefine.AREA_WIDTH);
		} else if (tb >= VDS1022.TB_1_4kFullScreen) {
			int compress = fullscreen / GDefine.AREA_WIDTH;
			return htp * compress;
		} else {
			/** 使用浮点，应对FULLSCREEN_400的情况，处理仍不完善 */
			double pointGap = GDefine.AREA_WIDTH / (double) fullscreen;
			io.value = (htp % (int) pointGap);
			cm.pcs.firePropertyChange(PropertiesItem.APPEND_TXTLINE, null,
					("io.value: " + io.value));
			return (int) (htp / pointGap);
		}
	}
}