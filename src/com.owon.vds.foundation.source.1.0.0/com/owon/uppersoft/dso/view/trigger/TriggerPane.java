package com.owon.uppersoft.dso.view.trigger;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.owon.uppersoft.dso.data.LObject;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.global.Principle;
import com.owon.uppersoft.dso.model.trigger.AbsTrigger;
import com.owon.uppersoft.dso.model.trigger.TrgTypeDefine;
import com.owon.uppersoft.dso.model.trigger.TrgTypeText;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.model.trigger.TriggerDefine;
import com.owon.uppersoft.dso.model.trigger.TriggerSet;
import com.owon.uppersoft.dso.model.trigger.TriggerUIInfo;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.dso.view.pane.dock.widget.FunctionPanel;
import com.owon.uppersoft.dso.view.pane.dock.widget.GroupPane;
import com.owon.uppersoft.dso.view.pane.dock.widget.ItemPane;
import com.owon.uppersoft.vds.ui.resource.FontCenter;
import com.owon.uppersoft.vds.ui.widget.custom.CCheckBox;
import com.owon.uppersoft.vds.ui.widget.custom.CComboBox;
import com.owon.uppersoft.vds.ui.widget.custom.ExcludeButtons;

/**
 * TriggerPane，触发模式为单触或是并行，单触是定位一个通道的触发事件同时快照所有通道；并行是通道各自触发在一个对齐的时间点
 * 
 */
public abstract class TriggerPane extends FunctionPanel {

	private boolean listening = false;

	public void simpleSelectCBBMode(TrgTypeDefine idx) {
		this.listening = false;
		selectTrgTypeComboBox(idx);
		this.listening = true;
	}

	private void selectTrgTypeComboBox(TrgTypeDefine idx) {
		int i = trgTypeModel.getIndexOf(new TrgTypeText(idx));
		if (i < 0)
			return;
		cbbmode.setSelectedIndex(i);
	}

	private PropertyChangeListener single_alt_PCL = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (!isListening())
				return;

			String pn = evt.getPropertyName();
			if (pn.equals(ExcludeButtons.EXCLUDE)) {
				int idx = (Integer) evt.getNewValue();
				tc.c_setChannelMode(idx);

				updateChl_loadTrgCtrl();

				updateTrgVoltPane();
				mw.getTitlePane().updateBtnSingle();
			}
		}
	};

	private void updateChl_loadTrgCtrl() {
		updateChlPane();
		loadTriggerControl();
		chlp.onSingleTrgForEdgeChannelSelectJudge();
	}

	public final void updateTrgVoltPane() {
		mw.re_paint();
		mw.getToolPane().getTrgInfoPane().updateInfos(tc);
	}

	private GroupPane snaPart, channelPart, modePart;
	private ExcludeButtons single_alt_btns;

	private CComboBox cbbmode;
	private ChannelSelectionPane chlp;
	private JPanel promptPart;

	private MainWindow mw;
	private TriggerControl tc;

	public MainWindow getMainWindow() {
		return mw;
	}

	public TriggerControl getTriggerControl() {
		return tc;
	}

	public TriggerPane(ControlManager cm) {
		super(cm);
		this.mw = Platform.getMainWindow();
		this.tc = cm.getTriggerControl();
		Principle pp = cm.getPrinciple();

		tlps = new TriggerLoaderPane[TrgTypeDefine.VALUES.length];

		this.listening = false;

		snaPart = ncgp();

		String[] sna_txts = TriggerDefine.SINGLE_ALT;
		int chlmode = tc.getChannelMode();
		if (!tc.isAlternativeSupport()) {
			sna_txts = new String[] { sna_txts[0] };
		}
		single_alt_btns = new ExcludeButtons(LObject.getLObjects(sna_txts),
				single_alt_PCL, chlmode, 130, 60, FontCenter.getLabelFont());
		nrip().add(single_alt_btns);

		channelPart = ncgp();
		ItemPane ip = nrip();
		nlbl("M.Trg.Source");
		chlp = new ChannelSelectionPane(cm.getMachine().isExtTrgSupport(), tc,
				this, cm.getCoreControl().getWaveFormInfoControl(),
				createChannelSelectItemListner(this, tc));
		ip.add(chlp);

		int channelNumber = cm.getSupportChannelsNumber();
		if (channelNumber == 1) {
			chlp.getChannelsComboBox().setEnabled(false);
		}

		modePart = ncgp();
		nrip();
		nlbl("M.Trg.Mode");

		Vector<TrgTypeText> ttds = new Vector<TrgTypeText>();
		for (TrgTypeDefine ttd : TrgTypeDefine.VALUES) {
			if (ttd == TrgTypeDefine.Video
					&& !cm.getMachine().isVideoTrgSupport()) {
				continue;
			}
			ttds.add(new TrgTypeText(ttd));
		}
		trgTypeModel = new DefaultComboBoxModel(ttds);
		cbbmode = nccb(trgTypeModel);

		promptPart = ncgp();
		nrip();
		nlbl("M.Utility.MachineNet.TipsTitle");
		nrip().setPreferredSize(new Dimension(250, 70));
		nlbld("M.Trg.Hide");

		trgPane = new JPanel();
		trgPane.setOpaque(false);
		trgPane.setLayout(new BorderLayout());
		add(trgPane);

		if (cm.getMachine().isTrgEdgeMiddleSupport()) {
			cbtn = new CCheckBox();
			final TriggerUIInfo tui = tc.getTriggerUIInfo();
			cbtn.setSelected(tui.isAuto_trglevel_middle());
			cbtn.setFont(FontCenter.getLabelFont());

			cbtn.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					tui.setAuto_trglevel_middle(cbtn.isSelected());
				}
			});
			add(cbtn);
		}

		/** 先设置模型，更新语言，然后添加事件监听器 */
		loadTriggerControl();
		localizeSelf();

		cbbmode.addItemListener(createTrgModeItemLisnter(this, tc, chlp));
		this.listening = true;

		updateChlPane();
		setTrgPaneVisible(tc.isTrgEnable());
	}

	protected abstract ItemListener createTrgModeItemLisnter(
			TriggerPane triggerPane, TriggerControl tc2,
			ChannelSelectionPane chlp2);

	protected abstract ChannelSelectItemListner createChannelSelectItemListner(
			TriggerPane triggerPane, TriggerControl tc2);

	private JPanel trgPane;

	private void updateChlPane() {
		chlp.updateChlPane();
	}

	/**
	 * 根据单触还是交替的装载触发信息
	 */
	private void loadTriggerControl() {
		this.listening = false;

		TriggerUIInfo tui = tc.getTriggerUIInfo();
		int curChl = tui.getCurrentChannel();
		/** 因为把listening关掉了，所以不会触发多余事件 */
		chlp.setChannel(curChl);

		AbsTrigger at = tui.getCurrentTriggerSet().getTrigger();
		TrgTypeDefine idx = at.type;
		selectTrgTypeComboBox(idx);

		switch2Pane(idx);
		this.listening = true;
	}

	private TriggerLoaderPane[] tlps;

	private TriggerLoaderPane getTriggerLoaderPane(TrgTypeDefine ttd) {
		int idx = ttd.ordinal();
		TriggerLoaderPane tlp = tlps[idx];//
		if (tlp == null) {
			switch (ttd) {
			case Slope:
				tlp = new SlopePane(this);
				break;
			case Edge:
				tlp = new EdgePane(this);
				break;
			case Video:
				tlp = new VideoPane(this);
				break;
			case Pulse:
				tlp = new PulsePane(this);
				break;
			}
			tlps[idx] = tlp;
		}
		return tlp;
	}

	public TriggerLoaderPane switch2Pane(TrgTypeDefine idx) {
		if (selectPane != null)
			trgPane.remove(selectPane);
		selectPane = getTriggerLoaderPane(idx);

		/** 装载对应Trigger的内容，根据内容和当前语言设置文本 */
		selectPane.loadTrigger(getTriggerUIInfo().getCurrentChannel());
		selectPane.localizeSelf();
		/***/

		trgPane.add(selectPane, BorderLayout.CENTER);

		updateUI();
		return selectPane;
	}

	public TriggerUIInfo getTriggerUIInfo() {
		return tc.getTriggerUIInfo();
	}

	public TriggerLoaderPane getSelectPane() {
		return selectPane;
	}

	protected TriggerSet curTrgSet() {
		return getTriggerUIInfo().getCurrentTriggerSet();
	}

	@Override
	public void localize(ResourceBundle rb) {
		super.localize(rb);
		selectPane.localize(rb);

		if (cbtn != null)
			cbtn.setText(rb.getString("M.Trg.auto_trglevel_middle"));
	}

	private TriggerLoaderPane selectPane;
	private JCheckBox cbtn;
	private DefaultComboBoxModel trgTypeModel;

	public void bodySend() {
		tc.doSubmit();
	}

	public void submitHoldOff(int chl, TriggerLoaderPane tp) {
		tc.getVTPatchable().submitHoldOff(chl, tp.getTrigger());
	}

	private void setTrgPaneVisible(boolean b) {
		snaPart.setVisible(b);
		channelPart.setVisible(b);
		modePart.setVisible(b);
		trgPane.setVisible(b);// selectPane
		promptPart.setVisible(!b);

		if (cbtn != null)
			cbtn.setVisible(b);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		TriggerLoaderPane tlp = selectPane;
		if (tlp == null)
			return;
		String pn = evt.getPropertyName();
		if (pn.equals(PropertiesItem.APPLY_TRIGGER)) {
			this.listening = false;
			// cbbsna.setSelectedIndex(tc.channelMode);
			single_alt_btns.setSelected(tc.getChannelMode());
			// updateChlPane();
			// loadTriggerControl();
			// chlp.onSingleTrgForEdgeChannelSelectJudge();
			updateChl_loadTrgCtrl();
			return;
		} else if (pn.equals(PropertiesItem.NEXT_SINGLE_CHANNEL)) {
			chlp.updateSingleChannel();
		} else if (pn.equals(PropertiesItem.SWITCH_SLOWMOVE)) {
			setTrgPaneVisible(false);
		} else if (pn.equals(PropertiesItem.SWITCH_NormalMOVE)) {
			setTrgPaneVisible(true);
		} else if (pn.equals(PropertiesItem.CHOOSE_TRGMODECB)) {
			TrgTypeDefine idx = (TrgTypeDefine) evt.getNewValue();
			simpleSelectCBBMode(idx);
		}

		tlp.fireProperty(evt);
	}

	public void setListening(boolean listening) {
		this.listening = listening;
	}

	public boolean isListening() {
		return listening;
	}

}