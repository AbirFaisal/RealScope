package com.owon.uppersoft.vds.source.comm;

import javax.swing.JComboBox;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.model.trigger.TrgTypeDefine;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.view.trigger.ChannelSelectionPane;
import com.owon.uppersoft.dso.view.trigger.TrgModeItemListener;
import com.owon.uppersoft.dso.view.trigger.TriggerPane;

public class TrgModeItemListener_Tiny extends TrgModeItemListener {

	private ControlManager cm;

	public TrgModeItemListener_Tiny(TriggerPane tp, TriggerControl tc,
			ChannelSelectionPane chlp, ControlManager cm) {
		super(tp, tc, chlp);
		this.cm = cm;
	}

	@Override
	protected void handleSelect(TrgTypeDefine etd, int chl, JComboBox cbb) {
		boolean alow = cm.singleVideoAlow(etd, chl, tc);
		if (!alow)
			return;
		super.handleSelect(etd, chl, cbb);
	}
}