package com.owon.uppersoft.dso.delegate;

import java.beans.PropertyChangeSupport;

import com.owon.uppersoft.dso.global.CoreControl;
import com.owon.uppersoft.dso.mode.control.SysControl;
import com.owon.uppersoft.dso.model.WaveFormInfoControl;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.model.trigger.TriggerExtendDelegate;
import com.owon.uppersoft.dso.model.trigger.TriggerSet;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.wf.ChannelInfo;

public class DefaultTriggerExtendDelegate implements TriggerExtendDelegate {
	private CoreControl cc;
	private PropertyChangeSupport pcs;
	private WaveFormInfoControl wfic;
	private SysControl sc;

	public DefaultTriggerExtendDelegate(CoreControl cc,
			PropertyChangeSupport pcs, WaveFormInfoControl wfic) {
		this.cc = cc;
		this.pcs = pcs;
		this.wfic = wfic;
	}

	@Override
	public boolean isExtTrgSupport() {
		return cc.getMachine().isExtTrgSupport();
	}

	@Override
	public void broadcastSweepOnce() {
		pcs.firePropertyChange(PropertiesItem.T_2SweepOnce, null, null);
	}

	@Override
	public void handleSingleTrgChannel2Ext() {
		SysControl sc = getSysControl();
		int old = sc.getSyncOutput();
		int newidx = SysControl.SYNOUT_TrgIn;
		sc.c_setSyncOut(newidx);
		/** 改变更新到UtilityPane */
		pcs.firePropertyChange(PropertiesItem.SYNCOUTPUTCHANGE, old, newidx);
	}

	private SysControl getSysControl() {
		return cc.getSysControl();
	}

	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcs;
	}

	@Override
	public void handleSingleTrgChannelReturnFromExt() {
		SysControl sc = getSysControl();
		int old = sc.getSyncOutput();
		int newidx = SysControl.SYNOUT_TrgOut;
		sc.c_setSyncOut(newidx);

		/** 改变更新到UtilityPane */
		/** 提示拔掉外部触发信源 */
		pcs.firePropertyChange(PropertiesItem.SYNCOUTPUTCHANGE, old, newidx);
	}

	@Override
	public void handleSingleTrgChannelLevelTransport(int oldchl, int newchl,
			TriggerSet singlets, TriggerControl tc) {
		ChannelInfo ci = wfic.getWaveFormChannelInfo(oldchl);
		int pos0 = ci.getPos0();
		int vb0 = ci.getVoltbaseIndex();

		ci = wfic.getWaveFormChannelInfo(newchl);
		int vb1 = ci.getVoltbaseIndex();
		int destpos0 = ci.getPos0();

		cc.getPos0_VBChangeInfluence().thredshold_voltsense_ByVoltBase(
				ci.getNumber(), pos0, vb0, vb1, null, destpos0);
		pcs.firePropertyChange(PropertiesItem.CHARTSCREEN_UPDATESHOW, null,
				null);
	}
}