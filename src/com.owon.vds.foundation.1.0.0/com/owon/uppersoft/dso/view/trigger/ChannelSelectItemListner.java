package com.owon.uppersoft.dso.view.trigger;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;

import javax.swing.JComboBox;

import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.model.trigger.TriggerUIInfo;
import com.owon.uppersoft.dso.util.PropertiesItem;

public class ChannelSelectItemListner implements ItemListener {

	protected TriggerPane tp;
	protected TriggerControl tc;

	public ChannelSelectItemListner(TriggerPane tp, TriggerControl tc) {
		this.tp = tp;
		this.tc = tc;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (!tp.isListening())
			return;
		if (e.getStateChange() != ItemEvent.SELECTED)
			return;

		JComboBox cbb = (JComboBox) e.getSource();
		int idx = cbb.getSelectedIndex();

		handleSelect(idx, cbb);
	}

	protected void handleSelect(int idx, JComboBox cbb) {
		// 已包含if (idx >= wfic.getLowMachineChannels())
		tc.setSingleChannel(idx);
		// single
		TriggerLoaderPane lp = tp.getSelectPane();
		TriggerUIInfo tui = tc.getTriggerUIInfo();
		tui.c_setTrigger(lp.submitTrigger());

		int chl = tui.getCurrentChannel();
		/** KNOW 单一触发时，通道改动，影响触发电平的电压数值 */
		lp.fireProperty(new PropertyChangeEvent(tc,
				PropertiesItem.NEXT_SINGLE_CHANNEL, -1, chl));
		tp.updateTrgVoltPane();
	}
}