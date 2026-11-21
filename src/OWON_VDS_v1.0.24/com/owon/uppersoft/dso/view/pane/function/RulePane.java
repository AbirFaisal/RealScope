package com.owon.uppersoft.dso.view.pane.function;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.owon.uppersoft.dso.function.PFRuleManager;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.page.function.RulePage;
import com.owon.uppersoft.dso.view.pane.dock.widget.FunctionPanel;
import com.owon.uppersoft.vds.function.rule.AdditionComboBoxModel;
import com.owon.uppersoft.vds.function.rule.PFRuleControl;
import com.owon.uppersoft.vds.function.rule.RuleDetail;
import com.owon.uppersoft.vds.ui.prompt.FadeIOShell;
import com.owon.uppersoft.vds.ui.widget.custom.CButton;
import com.owon.uppersoft.vds.ui.widget.custom.CCheckBox;
import com.owon.uppersoft.vds.ui.widget.custom.CComboBox;
import com.owon.uppersoft.vds.ui.widget.custom.CToggleButton;
import com.owon.uppersoft.vds.ui.widget.help.ToggleListener;

/**
 * 
 * <code>
 * Pass/fail: on, off	
 Operation: start, stop
 Output: pass, fail, beep
 Stop on output (on, off)
 Info display (on, off)

 Rule: source (ch1,ch2,ch3,ch4)
 Horizontal setting (x divisions)
 Vertical setting (x divisions)
 Create rule
 <code>
 * 
 * @author Matt
 * 
 */
public class RulePane extends FunctionPanel {
	private static final String MATH_ITEM = "Math";
	private PFRuleControl pfrc;
	private JSpinner horspin;
	private JSpinner verspin;
	private CComboBox chlccb;
	private WaveFormManager wfm;
	private PFRuleManager ruleManager;
	private CToggleButton rstbn;
	private CToggleButton abtbn;
	private ToggleListener able_al;
	private ToggleListener run_al;

	private void disableRule() {
		boolean ea = rstbn.isSelected();
		/** Only when disabled and still running, flip the run button state once to stop */
		if (ea)
			rstbn.setSelected(run_al.toggle(null, ea));
		pfrc.disableRule();
		rstbn.setEnabled(false);
	}

	private void enableRule() {
		pfrc.enableRule();
		config_enable();
		rstbn.setEnabled(true);
	}

	public RulePane(final ControlManager cm) {
		super(cm);
		wfm = Platform.getDataHouse().getWaveFormManager();
		ruleManager = cm.ruleManager;
		pfrc = ruleManager.getPFRuleControl();

		ncgp();
		nrip();
		able_al = new ToggleListener() {
			@Override
			public boolean toggle(ActionEvent e, boolean select) {
				if (!checkConnect())
					return select;

				boolean b = pfrc.isRuleEnable();
				if (b) {
					disableRule();
				} else {
					enableRule();
				}
				if (!cm.isRuntime())
					Platform.getMainWindow().updateShow();
				return !select;
			}
		};
		run_al = new ToggleListener() {
			@Override
			public boolean toggle(ActionEvent e, boolean select) {
				if (!checkConnect())
					return select;

				boolean b = ruleManager.isChecking();
				if (b) {
					ruleManager.stopCheck();
				} else {
					ruleManager.runCheck();
				}
				return !select;
			}
		};
		/** The text and its state are opposite; a click is needed to switch over */
		abtbn = ntbtn("M.Rule.disable", "M.Rule.enable", able_al, 125, 28,
				pfrc.isRuleEnable());
		rstbn = ntbtn("M.Rule.stop", "M.Rule.run", run_al, 125, 28,
				ruleManager.isChecking());

		rstbn.setEnabled(pfrc.isRuleEnable());

		// nbtnt("d").addActionListener(new ActionListener() {
		// @Override
		// public void actionPerformed(ActionEvent e) {
		// rstbn.setSelected(false);
		// }
		// });

		ncgp();
		createRulePart(cm);

		ncgp();
		nrip();
		final CComboBox pfccb = nccb(RulePage.pf, 110, 28);
		pfccb.setSelectedIndex(ruleManager.getOutputRule());
		pfccb.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() != ItemEvent.SELECTED)
					return;

				int idx = pfccb.getSelectedIndex();
				ruleManager.setOutputRule(idx);
			}
		});
		final CCheckBox mccb = ncb("M.Rule.msg");
		mccb.setSelected(ruleManager.isShowMsg());
		mccb.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				ruleManager.setShowMsg(mccb.isSelected());
				Platform.getMainWindow().re_paint();
			}
		});

		nrip();
		final CCheckBox rccb = ncb("M.Rule.ring");
		rccb.setSelected(ruleManager.isRing());
		rccb.setPreferredSize(new Dimension(110, 28));
		rccb.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				ruleManager.setRing(rccb.isSelected());
			}
		});

		final CCheckBox soccb = ncb("M.Rule.stopOnce");
		soccb.setSelected(ruleManager.isStopOnOutput());
		soccb.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				ruleManager.setStopOnOutput(soccb.isSelected());
			}
		});

		ncgp();
		nrip();
		nlbl("M.Rule.rule");
		nrip();
		CButton savebtn = nbtn("M.Rule.save");
		CButton usebtn = nbtn("M.Rule.use");
		CButton removebtn = nbtn("M.Rule.remove");

		nrip();
		final CComboBox ruleccb = nccb();
		final AdditionComboBoxModel acbm = new AdditionComboBoxModel(
				ruleManager.getRuleList(), I18nProvider.bundle().getString(
						"M.Rule.new"));
		ruleccb.setModel(acbm);
		ruleccb.setSelectedIndex(0);
		ruleccb.setPreferredSize(new Dimension(250, 28));// record
		savebtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int chl = chlccb.getSelectedIndex();
				double hor = (Double) horspin.getValue();
				double ver = (Double) verspin.getValue();
				int idx = ruleccb.getSelectedIndex();

				int ic = ruleccb.getItemCount();
				if (idx == ic - 1) {
					RuleDetail rd = new RuleDetail(chl, hor, ver);
					acbm.addElement(rd);
					ruleccb.setSelectedIndex(idx);
					return;
				}
				RuleDetail rd = (RuleDetail) acbm.getElementAt(idx);
				rd.set(chl, hor, ver);
				acbm.fireContentsChanged(acbm, 0, ic);
			}
		});
		removebtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// KNOW the call info contains only the channel and the horizontal/vertical division counts
				int idx = ruleccb.getSelectedIndex();
				int ic = ruleccb.getItemCount();
				if (idx == ic - 1) {
					return;
				}
				acbm.removeElementAt(idx);
				ruleccb.setSelectedIndex(idx);
			}
		});
		usebtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// KNOW the call info contains only the channel and the horizontal/vertical division counts
				int idx = ruleccb.getSelectedIndex();

				if (idx == ruleccb.getItemCount() - 1) {
					return;
				}

				fillDetail((RuleDetail) acbm.getElementAt(idx));

				// append possible enable action
				config_enable();
			}
		});

		localizeSelf();

		// set_all_visible(false);
	}

	private void fillDetail(RuleDetail rd) {
		chlccb.setSelectedIndex(rd.chl);
		horspin.setValue(rd.hor);
		verspin.setValue(rd.ver);
	}

	private void createRulePart(final ControlManager cm) {
		nrip();

		Object[] ns;
		if (cm.getAllChannelsNumber() == 1) {
			ns = cm.getCoreControl().getWaveFormInfos();
			chlccb = nccb(ns);
			chlccb.setEnabled(false);
		} else {
			ns = getComposite_suffix(cm.getCoreControl().getWaveFormInfos(),
					MATH_ITEM);
			chlccb = nccb(ns);
		}

		chlccb.setSelectedIndex(pfrc.getRuleChannel());
		chlccb.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() != ItemEvent.SELECTED)
					return;

				int chl = chlccb.getSelectedIndex();
				checkChannelAvailable(chl);
			}
		});
		CButton cbtn = nbtn("M.Rule.create");

		nrip();
		nlbl("M.Rule.Hor");// .setPreferredSize(new Dimension(0, 28));

		SpinnerModel horsm = new SpinnerNumberModel(pfrc.getHor(), 0.0, 10,
				0.02);
		horspin = new JSpinner(horsm);
		horspin.setPreferredSize(new Dimension(80, 28));
		ip.add(horspin);
		nnlbl("M.Rule.Div");

		nrip();
		nlbl("M.Rule.Ver");// .setPreferredSize(new Dimension(100, 28));
		SpinnerModel versm = new SpinnerNumberModel(pfrc.getVer(), 0.0, 5, 0.04);
		verspin = new JSpinner(versm);
		verspin.setPreferredSize(new Dimension(80, 28));
		ip.add(verspin);
		nnlbl("M.Rule.Div");

		cbtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				config_enable();
			}
		});

	}

	/**
	 * Save and append possible enable reset
	 */
	private void config_enable() {
		int chl = chlccb.getSelectedIndex();
		boolean b = checkChannelAvailable(chl);
		if (!b)
			return;

		double hor = (Double) horspin.getValue();
		double ver = (Double) verspin.getValue();

		/**
		 * Append possible enable; this requires that enable is turned on, and further that the scope is running, so no extra running check is needed here
		 * 
		 * so the two buttons that call this method do not need to check whether a scope is connected
		 * 
		 * Pressing "use" resets the rule, but drawing the rule needs a new waveform; when stopped there is no new waveform, so the drawn rule is not shown
		 */
		ruleManager.resetPFCounts();
		pfrc.configRule(chl, hor, ver);
	}

	/**
	 * @param chl
	 * @return whether the channel can be used
	 */
	private boolean checkChannelAvailable(int chl) {
		boolean b = true;

		// Using the last item of the cbb for the check is error-prone: single-channel models have no math
		if (chlccb.getModel().getElementAt(chl).toString().equals(MATH_ITEM)) {
			if (!wfm.getCompositeWaveForm().isOn()) {
				// Math
				b = false;
			}
		} else if (!wfm.getWaveForm(chl).isOn()) {
			// ch1~4
			b = false;
		}

		if (!b) {
			FadeIOShell pv = new FadeIOShell();
			pv.prompt(I18nProvider.bundle().getString("M.Rule.chlnotavlb"),
					Platform.getMainWindow().getFrame());
		}
		return b;
	}

	/**
	 * @return whether the scope is connected and can run
	 */
	private boolean checkConnect() {
		boolean b = Platform.getDataHouse().controlManager.sourceManager
				.isConnected();
		if (!b) {
			FadeIOShell pv = new FadeIOShell();
			pv.prompt(I18nProvider.bundle().getString("M.Rule.notconnect"),
					Platform.getMainWindow().getFrame());
		}
		return b;
	}

}
