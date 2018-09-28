package com.owon.uppersoft.dso.view.trigger;

import java.awt.Cursor;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.util.ResourceBundle;

import javax.swing.DefaultComboBoxModel;

import com.owon.uppersoft.dso.data.LObject;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.model.trigger.AbsTrigger;
import com.owon.uppersoft.dso.model.trigger.EdgeTrigger;
import com.owon.uppersoft.dso.model.trigger.TriggerDefine;
import com.owon.uppersoft.dso.model.trigger.helper.ETV_Holdoff;
import com.owon.uppersoft.dso.model.trigger.helper.LabelValueUnitContactor;
import com.owon.uppersoft.dso.model.trigger.holdoff.HoldoffSpinnerModel;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.dso.widget.custom.effect.LabelValueUnitPane;
import com.owon.uppersoft.vds.core.trigger.help.EnumNValue;
import com.owon.uppersoft.vds.ui.widget.custom.CComboBox;
import com.owon.uppersoft.vds.ui.widget.custom.CLabel;

public class EdgePane extends TriggerLoaderPane implements ItemListener {

	private CComboBox cbbsweep;
	private CComboBox cbbcoupling;
	private CComboBox cbbrnf;

	private HoldoffSpinnerModel hospin;
	private LabelValueUnitPane vup;

	public void updateTrVtValueLbl(int chl) {
		boolean ext = getTriggerPane().getTriggerControl().isExtTrg(chl);
		trvtValueLbl.setVisible(!ext);
		if (ext) {
			trvtValueLbl.setText("EX");
		} else {
			WaveFormManager wfm = Platform.getDataHouse().getWaveFormManager();
			ChannelInfo ci = wfm.getWaveForm(chl).wfi.ci;
			int voltbase = ci.getVoltageLabel().getValue();
			int pos0 = ci.getPos0();

			String txt = getTrigger().getLabelText(ci.isInverse(), voltbase,
					pos0);
			txt = "<html><font><u>" + txt + "<u></font></html>";
			trvtValueLbl.setText(txt);
		}
	}

	public void loadTrigger(int chl) {
		this.setChannel(chl);
		updateTrVtValueLbl(chl);

		listening = false;
		EdgeTrigger et = getTrigger();
		cbbrnf.setSelectedIndex(et.raisefall);
		cbbcoupling.setSelectedIndex(et.coupling);
		cbbsweep.setSelectedIndex(et.getSweep());

		hospin.setValue(et.etvho);
		lvuc.updateStepIndex(et.etvho.getStepIndex());

		if (getTriggerPane().getTriggerControl().isSweepEnable()) {
			cbbsweep.setSelectedIndex(et.getSweep());
			cbbsweep.setEnabled(true);
		} else {
			cbbsweep.setSelectedIndex(et.setSweep(0));
			cbbsweep.setEnabled(false);
		}

		listening = true;
	}

	public AbsTrigger submitTrigger() {
		EdgeTrigger et = getTrigger();
		et.raisefall = cbbrnf.getSelectedIndex();
		et.setSweep(cbbsweep.getSelectedIndex());
		et.coupling = cbbcoupling.getSelectedIndex();
		et.etvho.set((EnumNValue) hospin.getValue());
		return et;
	}

	public EdgeTrigger getTrigger() {
		return getTriggerPane().curTrgSet().edge;
	}

	public EdgePane(TriggerPane trp) {
		super(trp);

		ETV_Holdoff etvho = getTrigger().etvho;
		hospin = new HoldoffSpinnerModel(etvho);

		nrip();

		couplinglbl = new CLabel();
		// ip.add(couplinglbl);
		cbbcoupling = new CComboBox();
		// ip.add(cbbcoupling);

		cbbrnf = new CComboBox();
		ip.add(cbbrnf);

		nrip();
		trvtLbl = new CLabel();
		trvtValueLbl = new CLabel();
		// trvtValueLbl.setPreferredSize(new Dimension(200, 20));
		ip.add(trvtLbl);
		ip.add(trvtValueLbl);
		trvtValueLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		voltLevelMouseAdapter = new VoltLevelMouseAdapter(this,
				getTriggerPane().getTriggerControl(),
				EdgeTrigger.VoltSenseHalfRange);
		trvtValueLbl.addMouseListener(voltLevelMouseAdapter);

		nrip();

		sweepLbl = new CLabel();
		ip.add(sweepLbl);

		cbbsweep = new CComboBox();
		ip.add(cbbsweep);

		DefaultComboBoxModel stepmodel = new DefaultComboBoxModel(
				TriggerDefine.HOLDOFF_STEP_TIME_UNIT);
		vup = new LabelValueUnitPane("", hospin, stepmodel);
		lvuc = new LabelValueUnitContactor(new HoldoffValueChange(),
				vup.comboBox, vup.spinner);

		// DefaultEditor de = (DefaultEditor) vup.spinner.getEditor();
		// JFormattedTextField tf = de.getTextField();
		// System.out.println(tf.getFormatter());

		nrip().add(vup);

		/** 先设置模型，更新语言，然后添加事件监听器 */
		cbbcoupling.setModel(new DefaultComboBoxModel(TriggerDefine.COUPLING));
		cbbrnf.setModel(new DefaultComboBoxModel(EdgeTrigger.RiseFall));
		cbbsweep.setModel(new DefaultComboBoxModel(LObject
				.getLObjects(TriggerDefine.SWEEP)));

		addListeners();
	}

	protected void addListeners() {
		// spinner_se.addChangeListener(this);
		cbbsweep.addItemListener(this);
		cbbcoupling.addItemListener(this);
		cbbrnf.addItemListener(this);
		listening = true;
	}

	private boolean listening = false;
	private CLabel couplinglbl;
	private CLabel sweepLbl;
	private CLabel trvtLbl, trvtValueLbl;
	private LabelValueUnitContactor lvuc;
	private VoltLevelMouseAdapter voltLevelMouseAdapter;

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (!listening)
			return;
		if (e.getStateChange() != ItemEvent.SELECTED)
			return;

		if (e.getSource() == cbbrnf) {
			getTrigger().raisefall = cbbrnf.getSelectedIndex();
			bodySend();
			getTriggerPane().updateTrgVoltPane();
			return;
		}
		if (e.getSource() == cbbcoupling) {
			int idx = cbbcoupling.getSelectedIndex();
			getTrigger().coupling = idx;
			bodySend();
			return;
		}
		if (e.getSource() == cbbsweep) {
			int idx = cbbsweep.getSelectedIndex();
			getTrigger().c_setSweep(idx, getTriggerPane().getTriggerControl());
			bodySend();
			return;
		}
	}

	@Override
	public void localize(ResourceBundle rb) {
		trvtLbl.setText(rb.getString("M.Trg.TrgVolt") + ": ");
		couplinglbl.setText(rb.getString("M.Trg.Coupling") + ": ");
		sweepLbl.setText(rb.getString("M.Trg.TrigMode") + ": ");
		vup.setLabel(rb.getString("M.Trg.Holdoff") + ": ");

		boolean tmp = listening;
		listening = false;
		cbbrnf.setSelectedItem(cbbrnf.getSelectedObjects());
		cbbsweep.setSelectedItem(cbbsweep.getSelectedObjects());
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
			cbbrnf.setSelectedIndex(getTrigger().raisefall);
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