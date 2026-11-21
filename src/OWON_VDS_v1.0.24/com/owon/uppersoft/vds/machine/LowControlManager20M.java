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
		/** may be off by one pixel from the center */
		htp++;

		int tb = cm.getTimeControl().getTimebaseIdx();

		int fullscreen = getMachineInfo().getFullScreen(ChannelSampleConfig,
				getDeepMemoryControl().getDeepIdx())[tb];
		if (tb >= VDS1022.TB_5kFullScreen) {
			int screenDMDataLen = cm.getDeepMemoryControl().getDeepDataLen();
			/** 5k is used here so that there is no deviation only after the trigger level is dragged some distance */
			return (int) (htp * screenDMDataLen / (double) GDefine.AREA_WIDTH);
		} else if (tb >= VDS1022.TB_1_4kFullScreen) {
			int compress = fullscreen / GDefine.AREA_WIDTH;
			return htp * compress;
		} else {
			/** use floating point to handle the FULLSCREEN_400 case; the handling is still incomplete */
			double pointGap = GDefine.AREA_WIDTH / (double) fullscreen;
			io.value = (htp % (int) pointGap);
			cm.pcs.firePropertyChange(PropertiesItem.APPEND_TXTLINE, null,
					("io.value: " + io.value));
			return (int) (htp / pointGap);
		}
	}
}