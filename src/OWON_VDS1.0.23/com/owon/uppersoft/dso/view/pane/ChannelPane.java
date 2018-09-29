package com.owon.uppersoft.dso.view.pane;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.owon.uppersoft.dso.data.LObject;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.page.ChannelPage;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.view.pane.dock.widget.FunctionPanel;
import com.owon.uppersoft.dso.view.pane.dock.widget.GroupPane;
import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.dso.wf.IChannel;
import com.owon.uppersoft.vds.ui.resource.FontCenter;
import com.owon.uppersoft.vds.ui.widget.custom.CCheckBox;
import com.owon.uppersoft.vds.ui.widget.custom.CComboBox;
import com.owon.uppersoft.vds.ui.widget.custom.ExcludeButtons;
import com.owon.uppersoft.vds.ui.widget.help.RadioButtonGroup;

public class ChannelPane extends FunctionPanel implements ItemListener,
		ActionListener {
	/** 隐藏带宽限制 */
	private boolean bandLimit = true;

	private CCheckBox channelOption, channelOpposite;

	private CComboBox probecbb, couplingcbb;
	private GroupPane chl_detail_pane;
	private RadioButtonGroup rbg;

	private PropertyChangeListener pcl = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			int selidx = (Integer) evt.getNewValue();
			loadCurrentChannel(selidx);
		}
	};

	private ChannelPage cp;

	public ChannelPane(final ControlManager cm, final ChannelPage cp) {
		super(cm);
		this.cp = cp;

		bandLimit = cp.isBandLimit();

		ncgp();
		int idx = cp.getSelectedChannelIndex();
		rbg = new RadioButtonGroup(cp.getChannelNames(), pcl, idx, 64, 30);
		nrip().add(rbg);

		nrip().setPreferredSize(new Dimension(300, 35));
		channelOption = ncb("Label.OnOff");

		chl_detail_pane = ncgp();
		nrip();
		channelOpposite = ncb("M.Channel.Opposite");

		if (bandLimit) {
			nrip();

			ebs = new ExcludeButtons(new LObject[] {
					new LObject("M.Channel.bandlimit_20M"),
					new LObject("M.Channel.Fullband") },
					new PropertyChangeListener() {
						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							int i = (Integer) evt.getNewValue();

							IChannel current = cp.getCurrentChannel();
							current.c_setBandLimit(i == 0);
							cm.pcs.firePropertyChange(
									PropertiesItem.UPDATE_CHLVOLT, null,
									current.getNumber());
						}

					}, 0, 130, 30, FontCenter.getLabelFont());
			ip.add(ebs);
		}

		nrip();
		nlbl("M.Channel.Coupling");

		couplingcbb = nccb(ChannelInfo.COUPLING);

		nrip();
		nlbl("M.Channel.ProbeRate");

		probecbb = nccb(cm.getMachineInfo().ProbeTexts);

		localizeSelf();
		loadCurrentChannel(idx);
		addListeners();
	}

	protected void showChannelDetail(boolean b) {
		chl_detail_pane.setVisible(b);
	}

	protected void addListeners() {
		channelOption.addActionListener(this);
		channelOpposite.addActionListener(this);

		couplingcbb.addItemListener(this);
		probecbb.addItemListener(this);
		listening = true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!listening)
			return;
		if (e.getSource() == channelOption) {
			boolean b = channelOption.isSelected();
			IChannel current = cp.getCurrentChannel();
			current.c_setOn(b);
			showChannelDetail(b);

			DataHouse dh = Platform.getDataHouse();
			Platform.getMainWindow().channelOnOffRepaint(
					dh.getWaveFormManager().getWaveForm(current.getNumber()));
			return;
		}
		if (e.getSource() == channelOpposite) {
			IChannel current = cp.getCurrentChannel();
			current.c_setInverse(channelOpposite.isSelected());
			Platform.getMainWindow().updateShow();
			return;
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (!listening)
			return;
		if (e.getStateChange() != ItemEvent.SELECTED)
			return;

		if (e.getSource() == probecbb) {
			IChannel current = cp.getCurrentChannel();
			int chlidx = probecbb.getSelectedIndex();
			current.setProbeMultiIdx(chlidx);
			cm.updateChannelVoltValueEverywhere(current.getNumber());
			return;
		}
		if (e.getSource() == couplingcbb) {
			IChannel current = cp.getCurrentChannel();
			current.c_setCoupling(couplingcbb.getSelectedIndex());
			Platform.getMainWindow().getToolPane().updateChannels();
			return;
		}
	}

	private boolean listening = false;

	private ExcludeButtons ebs;

	protected void loadCurrentChannel(int chlidx) {
		listening = false;
		cp.setCurrentChannelPageIndex(chlidx);

		IChannel current = cp.getCurrentChannel();
		boolean b = current.isOn();
		showChannelDetail(b);
		// updateBandLimitlbl();
		rbg.setSelected(chlidx);
		channelOption.setSelected(b);
		if (bandLimit) {
			// ebs.setSelected(current.isBandlimit() ? 0 : 1);
			if (current.isForcebandlimit())
				ebs.setFinalSelected(0);
			else
				ebs.undoFinalSelected(current.isBandlimit() ? 0 : 1);

		}
		channelOpposite.setSelected(current.isInverse());
		probecbb.setSelectedIndex(current.getProbeMultiIdx());
		couplingcbb.setSelectedIndex(current.getCouplingIdx());

		listening = true;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String n = evt.getPropertyName();
		if (n.equals(PropertiesItem.APPLY_CHANNELS)) {
			IChannel current = cp.getCurrentChannel();
			loadCurrentChannel(current.getNumber());
			return;
		}
		if (n.equals(PropertiesItem.CHOOSE_CHANNELS)) {
			loadCurrentChannel(cp.getSelectedChannelIndex());
			return;
		}

		listening = false;
		if (n.equals(PropertiesItem.CHANNEL_OPTION)) {
			IChannel current = cp.getCurrentChannel();
			channelOption.setSelected(current.isOn());
			showChannelDetail(current.isOn());
		} else if (n.equals(PropertiesItem.COUPLING_OPTION)) {
			IChannel current = cp.getCurrentChannel();
			couplingcbb.setSelectedIndex(current.getCouplingIdx());
		} else if (n.equals(PropertiesItem.CHANNEL_OPPOSITE)) {
			IChannel current = cp.getCurrentChannel();
			channelOpposite.setSelected(current.isInverse());
		} else if (n.equals(PropertiesItem.PROBECHANGE)) {
			IChannel current = cp.getCurrentChannel();
			probecbb.setSelectedIndex(current.getProbeMultiIdx());
		}
		listening = true;
	}

}
