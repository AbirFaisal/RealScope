package com.owon.vds.calibration;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.model.WaveFormInfoControl;
import com.owon.uppersoft.vds.calibration.ChannelSets;

public class SelCalSetting {
	public int persistenceIndex;

	public ChannelSets[] css;

	public boolean xymode;
	public boolean measureon;
	public int sampleIndex;
	public int timebase;
	public int htp;
	public int deepIndex;

	public SelCalSetting(ControlManager cm) {
		persistenceIndex = cm.displayControl.getPersistenceIndex();

		WaveFormInfoControl wfic = cm.getWaveFormInfoControl();
		css = wfic.getAllChannelSets();

		xymode = cm.displayControl.isXYModeOn();
		measureon = cm.getMeasureManager().ison();
		sampleIndex = cm.getSampleControl().getModelIdx();

		timebase = cm.getTimeControl().getTimebaseIdx();
		htp = cm.getTimeControl().getHorizontalTriggerPosition();
		deepIndex = cm.getDeepMemoryControl().getDeepIdx();

		logInfo();
	}

	public void logInfo() {
		System.out.println("persistenceIndex: " + persistenceIndex);

		int len = css.length;

		for (int i = 0; i < len; i++) {
			System.out.print(css[i]);
		}

		System.out.println("xymode: " + xymode);
		System.out.println("measureon: " + measureon);
		System.out.println("sampleIndex: " + sampleIndex);
		System.out.println("timebase: " + timebase);
		System.out.println("htp: " + htp);
		System.out.println("deepIndex: " + deepIndex);
	}
}