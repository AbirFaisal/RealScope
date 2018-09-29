package com.owon.uppersoft.dso.view.trigger;

import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.util.ResourceBundle;

import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.owon.uppersoft.dso.data.LObject;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.model.trigger.AbsTrigger;
import com.owon.uppersoft.dso.model.trigger.PulseTrigger;
import com.owon.uppersoft.dso.model.trigger.TriggerDefine;
import com.owon.uppersoft.dso.model.trigger.condition.TrgConditionSpinnerModel;
import com.owon.uppersoft.dso.model.trigger.helper.ETV_Holdoff;
import com.owon.uppersoft.dso.model.trigger.helper.LabelValueUnitContactor;
import com.owon.uppersoft.dso.model.trigger.holdoff.HoldoffSpinnerModel;
import com.owon.uppersoft.dso.source.comm.effect.SubmitorFactory;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.dso.widget.custom.effect.LabelValueUnitPane;
import com.owon.uppersoft.vds.core.comm.effect.Submitable;
import com.owon.uppersoft.vds.core.trigger.help.EnumNValue;
import com.owon.uppersoft.vds.ui.layout.OneRowLayout;
import com.owon.uppersoft.vds.ui.widget.custom.CComboBox;
import com.owon.uppersoft.vds.ui.widget.custom.CLabel;

public class PulsePane extends TriggerLoaderPane implements ItemListener,
		ChangeListener {

	private CComboBox cbbcoupling;
	private CComboBox cbbsweep;
	private CComboBox cbbpcondition;
	private CLabel lblcdt;

	private HoldoffSpinnerModel hospin;
	private TrgConditionSpinnerModel conspin;

	private LabelValueUnitPane hovup;
	private ChannelInfo ci;

	public void updateTrVtValueLbl(int chl) {
		ci = Platform.getDataHouse().getWaveFormManager().getWaveForm(chl).wfi.ci;
		int voltbase = ci.getVoltageLabel().getValue();
		int pos0 = ci.getPos0();

		String txt = getTrigger().getLabelText(ci.isInverse(), voltbase, pos0);
		txt = "<html><font><u>" + txt + "<u></font></html>";
		trvtValueLbl.setText(txt);
	}

	public void loadTrigger(int chl) {
		this.setChannel(chl);
		updateTrVtValueLbl(chl);

		listening = false;
		PulseTrigger pt = getTrigger();

		cbbpcondition.setSelectedIndex(pt.condition);
		conspin.setValue(pt.trgcondition);
		conlvuc.updateStepIndex(pt.trgcondition.getStepIndex());

		hospin.setValue(pt.etvho);
		holvuc.updateStepIndex(pt.etvho.getStepIndex());

		cbbcoupling.setSelectedIndex(pt.coupling);

		if (getTriggerPane().getTriggerControl().isSweepEnable()) {
			cbbsweep.setSelectedIndex(pt.getSweep());
			cbbsweep.setEnabled(true);
		} else {
			cbbsweep.setSelectedIndex(pt.setSweep(0));
			cbbsweep.setEnabled(false);
		}

		listening = true;
	}

	public AbsTrigger submitTrigger() {
		PulseTrigger pt = getTrigger();
		pt.condition = cbbpcondition.getSelectedIndex();
		pt.trgcondition.set((EnumNValue) conspin.getValue());
		pt.etvho.set((EnumNValue) hospin.getValue());
		pt.setSweep(cbbsweep.getSelectedIndex());
		pt.coupling = cbbcoupling.getSelectedIndex();
		return pt;
	}

	public PulseTrigger getTrigger() {
		return getTriggerPane().curTrgSet().pulse;
	}

	public PulsePane(TriggerPane trp) {
		super(trp);

		PulseTrigger pt = getTrigger();
		ETV_Holdoff etvho = pt.etvho;

		nrip(new OneRowLayout(new Insets(3, 3, 3, 3), 1));
		lblcdt = new CLabel();
		cbbpcondition = new CComboBox();
		cbbpcondition.setModel(new DefaultComboBoxModel(LObject
				.getLObjects(TriggerDefine.CONDITIONS_PULSE)));

		ip.add(lblcdt);
		ip.add(cbbpcondition);

		nrip();
		conspin = new TrgConditionSpinnerModel(pt.trgcondition);
		DefaultComboBoxModel stepmodel2 = new DefaultComboBoxModel(
				TriggerDefine.CONDITION_STEP_TIME_UNIT);
		convup = new LabelValueUnitPane("", conspin, stepmodel2);
		conlvuc = new LabelValueUnitContactor(new DefaultValueChange() {
			@Override
			protected void submitChange() {
				Submitable sbm = SubmitorFactory.reInit();

				byte alpha = getTriggerPane().getTriggerControl()
						.getChannelModeAlpha();
				getTrigger().submitCondition(alpha, getChannel(), sbm);

				sbm.apply_trg();
			}
		}, convup.comboBox, convup.spinner);
		ip.add(convup);

		nrip();
		trvtLbl = new CLabel();
		trvtValueLbl = new CLabel();
		ip.add(trvtLbl);
		ip.add(trvtValueLbl);
		trvtValueLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		trvtValueLbl.addMouseListener(new VoltLevelMouseAdapter(this,
				getTriggerPane().getTriggerControl(),
				PulseTrigger.VoltSenseHalfRange));

		nrip();
		sweeplbl = new CLabel();
		ip.add(sweeplbl);

		cbbsweep = new CComboBox();
		ip.add(cbbsweep);

		DefaultComboBoxModel stepmodel = new DefaultComboBoxModel(
				TriggerDefine.HOLDOFF_STEP_TIME_UNIT);
		hospin = new HoldoffSpinnerModel(etvho);
		hovup = new LabelValueUnitPane("", hospin, stepmodel);
		holvuc = new LabelValueUnitContactor(new HoldoffValueChange(),
				hovup.comboBox, hovup.spinner);

		nrip().add(hovup);

		// nrip();
		couplinglbl = new CLabel();
		// ip.add(couplinglbl);
		cbbcoupling = new CComboBox();
		// ip.add(cbbcoupling);

		/** 先设置模型，更新语言，然后添加事件监听器 */
		cbbcoupling.setModel(new DefaultComboBoxModel(TriggerDefine.COUPLING));

		cbbsweep.setModel(new DefaultComboBoxModel(LObject
				.getLObjects(TriggerDefine.SWEEP)));

		addListeners();
	}

	protected void addListeners() {
		cbbsweep.addItemListener(this);
		cbbcoupling.addItemListener(this);
		cbbpcondition.addItemListener(this);
		listening = true;
	}

	private boolean listening = false;
	private CLabel sweeplbl;
	private CLabel couplinglbl;
	private CLabel trvtLbl, trvtValueLbl;
	private LabelValueUnitContactor holvuc;
	private LabelValueUnitContactor conlvuc;
	private LabelValueUnitPane convup;

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (!listening)
			return;
		if (e.getStateChange() != ItemEvent.SELECTED)
			return;

		if (e.getSource() == cbbsweep) {
			int idx = cbbsweep.getSelectedIndex();
			getTrigger().c_setSweep(idx, getTriggerPane().getTriggerControl());
			return;
		}
		if (e.getSource() == cbbcoupling) {
			int idx = cbbcoupling.getSelectedIndex();
			getTrigger().coupling = idx;
			bodySend();
			return;
		}
		if (e.getSource() == cbbpcondition) {
			int idx = cbbpcondition.getSelectedIndex();
			getTrigger().condition = idx;
			bodySend();
			getTriggerPane().updateTrgVoltPane();
			return;
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (!listening)
			return;
	}

	@Override
	public void localize(ResourceBundle rb) {
		lblcdt.setText(rb.getString("M.Trg.P.Condition") + ": ");
		trvtLbl.setText(rb.getString("M.Trg.TrgVolt") + ": ");
		sweeplbl.setText(rb.getString("M.Trg.TrigMode") + ": ");
		couplinglbl.setText(rb.getString("M.Trg.Coupling") + ": ");
		hovup.setLabel(rb.getString("M.Trg.Holdoff") + ": ");

		boolean tmp = listening;
		listening = false;

		cbbpcondition.setSelectedItem(cbbpcondition.getSelectedItem());
		cbbsweep.setSelectedItem(cbbsweep.getSelectedItem());
		listening = tmp;
	}

	@Override
	public void bodySend() {
		getTriggerPane().bodySend();
	}

	@Override
	public void fireProperty(PropertyChangeEvent evt) {
		String n = evt.getPropertyName();
		if (n.equals(PropertiesItem.UPDATE_VOLTSENSE)) {
			int chl = (Integer) evt.getNewValue();
			int idx = getChannel();
			if (chl == idx)
				updateTrVtValueLbl(idx);
		} else if (n.equals(PropertiesItem.NEXT_STATUS)) {
			listening = false;
			cbbpcondition.setSelectedIndex(getTrigger().condition);
			listening = true;
		} else if (n.equals(PropertiesItem.T_SweepOnce2Auto)) {
			listening = false;
			cbbsweep.setSelectedIndex(getTrigger().getSweep());
			listening = true;
		} else if (n.equals(PropertiesItem.NEXT_SINGLE_CHANNEL)) {
			this.setChannel((Integer) evt.getNewValue());
			updateTrVtValueLbl(getChannel());
		} else if (n.equals(PropertiesItem.T_2SweepOnce)) {
			cbbsweep.setSelectedIndex(getTrigger().getSweep());
		}
	}
}