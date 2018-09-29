package com.owon.uppersoft.dso.machine.aspect;

import java.math.BigDecimal;

import com.owon.uppersoft.dso.model.WFTimeScopeControl;
import com.owon.uppersoft.dso.model.WaveFormInfoControl;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.vds.core.paint.ScreenContext;
import com.owon.uppersoft.vds.core.wf.dm.DMInfo;
import com.owon.uppersoft.vds.core.wf.rt.LoadMedia;

public interface IMultiWFManager {

	public abstract void receiveRTData(LoadMedia cti, ScreenContext pc,
			WaveFormManager wfm, boolean freshFreq);

	public abstract void receiveOfflineData(LoadMedia cti, ScreenContext pc,
			WaveFormManager wfm);

	public abstract void receiveOfflineDMData(DMInfo cti, ScreenContext pc,
			BigDecimal tbbd, int tbIdx, WaveFormManager wfm);

	public abstract void receiveRTDMData(DMInfo ci, ScreenContext pc,
			BigDecimal tbbd, int tbIdx, WaveFormManager wfm);

	/**   */
	public abstract void simulateReloadAsDM(ScreenContext pc,
			WaveFormInfoControl wfic, WFTimeScopeControl wftsc,
			WaveFormManager wfm);

}