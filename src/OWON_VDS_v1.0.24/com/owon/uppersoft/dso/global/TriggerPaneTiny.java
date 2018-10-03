package com.owon.uppersoft.dso.global;

import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.view.trigger.ChannelSelectItemListner;
import com.owon.uppersoft.dso.view.trigger.ChannelSelectionPane;
import com.owon.uppersoft.dso.view.trigger.TriggerPane;
import com.owon.uppersoft.vds.source.comm.TrgModeItemListener_Tiny;

public class TriggerPaneTiny extends TriggerPane {
	public TriggerPaneTiny(ControlManager cm) {
		super(cm);
	}

	@Override
	public ChannelSelectItemListner createChannelSelectItemListner(
			final TriggerPane tp, TriggerControl tc) {
		return new ChannelSelectItemListner(tp, tc) {
			protected void handleSelect(int idx, JComboBox cbb) {
				if (idx != 0
						&& tp.getTriggerUIInfo().getCurrentTriggerSet()
								.isCurrentTrigger_Video()) {
					String message = I18nProvider.bundle().getString(
							"Info.VideoOnlyForCH1"), title = "";

					JOptionPane.showMessageDialog(
							tp.getMainWindow().getFrame(), message, title,
							JOptionPane.INFORMATION_MESSAGE);
					tp.setListening(false);
					cbb.setSelectedIndex(0);
					tp.setListening(true);
					return;
				}
				super.handleSelect(idx, cbb);
			}
		};
	}

	@Override
	public ItemListener createTrgModeItemLisnter(final TriggerPane tp,
			TriggerControl tc, ChannelSelectionPane chlp) {
		return new TrgModeItemListener_Tiny(tp, tc, chlp, cm);
	}
}