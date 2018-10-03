package com.owon.uppersoft.dso.view.trigger;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;

import com.owon.uppersoft.dso.model.trigger.TrgTypeDefine;
import com.owon.uppersoft.dso.model.trigger.TrgTypeText;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.model.trigger.TriggerUIInfo;

public class TrgModeItemListener implements ItemListener {
	protected TriggerPane tp;
	protected TriggerControl tc;
	protected ChannelSelectionPane chlp;

	public TrgModeItemListener(TriggerPane tp, TriggerControl tc,
			ChannelSelectionPane chlp) {
		this.tp = tp;
		this.tc = tc;
		this.chlp = chlp;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (!tp.isListening())
			return;
		if (e.getStateChange() != ItemEvent.SELECTED)
			return;

		int chl = chlp.getCurrentChannel();
		TrgTypeText ttt = (TrgTypeText) e.getItem();
		handleSelect(ttt.getTrgTypeDefine(), chl, (JComboBox) e.getSource());
	}

	protected void handleSelect(TrgTypeDefine etd, int chl, JComboBox cbb) {
		if (tc.isOnExtTrgMode() && etd != TrgTypeDefine.Edge) {
			tc.setSingleChannel(0);
		}

		TriggerLoaderPane p = tp.switch2Pane(etd);

		TriggerUIInfo tui = tc.getTriggerUIInfo();
		tui.c_setTrigger(p.getTrigger());
		tp.updateTrgVoltPane();

		chlp.onSingleTrgForEdgeChannelSelectJudge();
	}

}