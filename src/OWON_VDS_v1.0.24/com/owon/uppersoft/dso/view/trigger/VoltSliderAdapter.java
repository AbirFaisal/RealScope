package com.owon.uppersoft.dso.view.trigger;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.model.trigger.TriggerSet;
import com.owon.uppersoft.dso.model.trigger.TriggerUIInfo;
import com.owon.uppersoft.dso.model.trigger.VoltsensableTrigger;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.vds.ui.slider.SliderAdapter;

public class VoltSliderAdapter extends SliderAdapter {
	private TriggerControl trgc;
	private ChannelInfo ci;
	private int halfRange;
	private VoltsensableTrigger vt;

	public VoltSliderAdapter(TriggerControl trgc, ChannelInfo ci,
			int halfRange, VoltsensableTrigger vt) {
		this.trgc = trgc;
		this.ci = ci;
		this.halfRange = halfRange;
		this.vt = vt;
	}

	@Override
	public void on50percent() {
		TriggerUIInfo tui = trgc.getTriggerUIInfo();
		TriggerSet ts = tui.getCurrentTriggerSet();
		if (vt instanceof VoltsensableTrigger) {
			WaveFormManager wfm = Platform.getDataHouse().getWaveFormManager();
			wfm.getWaveForm(ci.getNumber()).setTrg50Percent(trgc);
			// updateUpLow();
		}
	}

	@Override
	public void valueChanged(int oldV, int newV) {
		int v = newV;

		v = halfRange - v;

		// 附加反相
		v = ci.getInverseValue(v);

		boolean changed = vt.c_setVoltsense(v);
		if (changed) {
			trgc.getVTPatchable().submiteVoltsense(ci.getNumber(), vt);
		}
		ControlManager cm = Platform.getControlManager();
		cm.pcs.firePropertyChange(PropertiesItem.REPAINT_CHARTSCREEN, null,
				null);
		cm.pcs.firePropertyChange(PropertiesItem.UPDATE_VOLTSENSE, -1,
				ci.getNumber());
	}

	@Override
	public void onDispose() {
		Platform.getMainWindow().update_DoneVoltsense();
	}
}
