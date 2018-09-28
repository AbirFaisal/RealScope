package com.owon.uppersoft.dso.view.trigger;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;

import com.owon.uppersoft.dso.model.WaveFormInfoControl;
import com.owon.uppersoft.dso.model.trigger.TrgTypeDefine;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.model.trigger.TriggerUIInfo;
import com.owon.uppersoft.vds.ui.widget.custom.CComboBox;
import com.owon.uppersoft.vds.ui.widget.help.RadioButtonGroup;

public class ChannelSelectionPane extends JPanel {
	private CardLayout cl = new CardLayout();
	private CComboBox cbbchl;
	private RadioButtonGroup rbg;
	private TriggerControl tc;
	private TriggerPane tp;
	private WaveFormInfoControl wfic;
	private boolean isExtTrgSupport;

	public ChannelSelectionPane(boolean isExtTrgSupport, TriggerControl tc,
			TriggerPane tp, WaveFormInfoControl wfic,
			ChannelSelectItemListner csil) {
		this.tp = tp;
		this.wfic = wfic;
		this.tc = tc;
		this.isExtTrgSupport = isExtTrgSupport;

		setOpaque(false);
		setLayout(cl);

		JPanel sp = new JPanel();
		sp.setLayout(new BorderLayout());
		sp.setOpaque(false);

		cbbchl = new CComboBox();
		onSingleTrgForEdgeChannelSelectJudge();
		tp.setListening(false);

		rbg = new RadioButtonGroup(wfic.getWaveFormInfos(), radioBtnPCL, -1,
				45, 30);
		sp.add(cbbchl, BorderLayout.WEST);
		add(sp, "single");
		add(rbg, "alternate");

		cbbchl.addItemListener(csil);
	}

	public void onSingleTrgForEdgeChannelSelectJudge() {
		tp.setListening(false);

		int chls = wfic.getLowMachineChannels();
		String[] sels;
		// tc.isSingleTrg() && 不限制这个，防止在交替时跳出判断
		if (isExtTrgSupport
				&& TrgTypeDefine.Edge == tc.getSingleTriggerSet().getTrigger().type) {
			sels = new String[chls + 1];
			int i = 0;
			for (; i < chls; i++) {
				sels[i] = "CH" + (i + 1);
			}
			sels[i] = "EXT";
		} else {
			sels = new String[chls];
			int i = 0;
			for (; i < chls; i++) {
				sels[i] = "CH" + (i + 1);
			}
		}

		cbbchl.setModel(new DefaultComboBoxModel(sels));
		int idx = tc.getSingleTrgChannel();
		if (idx >= chls)
			idx = chls;
		cbbchl.setSelectedIndex(idx);
		tp.setListening(true);
	}

	private PropertyChangeListener radioBtnPCL = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (!tp.isListening())
				return;

			String pn = evt.getPropertyName();
			if (pn.equals(RadioButtonGroup.RADIO)) {
				int selIdx = (Integer) evt.getNewValue();
				TriggerUIInfo tpp = tc.getTriggerUIInfo();
				tpp.setCurrentAltChannel(selIdx);
				// alt load
				TrgTypeDefine etd = tpp.getCurrentTriggerSet().getTrigger().type;// 触发模式序号0~3
				tp.simpleSelectCBBMode(etd);
				tp.switch2Pane(etd);
			}
		}
	};

	public void setChannel(int curChl) {
		cbbchl.setSelectedIndex(curChl);
		rbg.setSelected(curChl);
	}

	public void updateSingleChannel() {
		tp.setListening(false);
		TriggerUIInfo tpp = tc.getTriggerUIInfo();
		cbbchl.setSelectedIndex(tpp.getCurrentChannel());
		tp.setListening(true);
	}

	public void updateChlPane() {
		if (tc.isSingleTrg()) {
			cl.show(this, "single");
		} else {
			cl.show(this, "alternate");
		}
	}

	public int getCurrentChannel() {
		if (tc.isSingleTrg()) {
			return cbbchl.getSelectedIndex();
		} else {
			return rbg.getSelection();
		}
	}

	public CComboBox getChannelsComboBox() {
		return cbbchl;
	}
}