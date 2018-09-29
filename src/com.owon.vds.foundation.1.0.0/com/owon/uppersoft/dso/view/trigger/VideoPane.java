package com.owon.uppersoft.dso.view.trigger;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.util.ResourceBundle;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.owon.uppersoft.dso.model.trigger.AbsTrigger;
import com.owon.uppersoft.dso.model.trigger.TriggerDefine;
import com.owon.uppersoft.dso.model.trigger.VideoTrigger;
import com.owon.uppersoft.dso.model.trigger.helper.ETV_Holdoff;
import com.owon.uppersoft.dso.model.trigger.helper.LabelValueUnitContactor;
import com.owon.uppersoft.dso.model.trigger.holdoff.HoldoffSpinnerModel;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.widget.custom.effect.LabelValueUnitPane;
import com.owon.uppersoft.vds.core.trigger.help.EnumNValue;
import com.owon.uppersoft.vds.ui.widget.CNumberSpinner;
import com.owon.uppersoft.vds.ui.widget.custom.CComboBox;
import com.owon.uppersoft.vds.ui.widget.custom.CLabel;

public class VideoPane extends TriggerLoaderPane implements ItemListener,
		ChangeListener {

	private JSpinner spinner_sync;
	private CComboBox cbbsync;
	private CComboBox cbbmodule;

	private SpinnerNumberModel syncspin;
	private HoldoffSpinnerModel hospin;

	private LabelValueUnitPane vup;

	public void loadTrigger(int chl) {
		this.setChannel(chl);
		listening = false;
		VideoTrigger vt = getTrigger();
		cbbmodule.setSelectedIndex(vt.module);
		resetMax(vt);
		cbbsync.setSelectedIndex(vt.sync);

		spinner_sync.setEnabled(vt.isSyncValueEnable());

		hospin.setValue(vt.etvho);

		lvuc.updateStepIndex(vt.etvho.getStepIndex());

		syncspin.setValue(new Long(vt.syncValue));
		listening = true;
	}

	public AbsTrigger submitTrigger() {
		VideoTrigger vt = getTrigger();
		vt.etvho.set((EnumNValue) hospin.getValue());
		vt.module = cbbmodule.getSelectedIndex();
		vt.sync = cbbsync.getSelectedIndex();
		vt.syncValue = ((Number) syncspin.getValue()).intValue();
		return vt;
	}

	public VideoTrigger getTrigger() {
		return getTriggerPane().curTrgSet().video;
	}

	private CLabel cl;

	public VideoPane(TriggerPane trp) {
		super(trp);
		nrip();
		cbbmodule = new CComboBox();
		ip.add(cbbmodule);
		syncspin = new SpinnerNumberModel() {
			@Override
			public Object getNextValue() {
				Object o = super.getNextValue();

				if (o != null)
					return o;
				return getMinimum();
			}

			@Override
			public Object getPreviousValue() {
				Object o = super.getPreviousValue();
				if (o != null)
					return o;

				return getMaximum();
			}

		};
		syncspin.setStepSize(new Long(1));
		syncspin.setMaximum(new Long(525));
		syncspin.setMinimum(new Long(1));

		ETV_Holdoff etvho = getTrigger().etvho;
		hospin = new HoldoffSpinnerModel(etvho);

		nrip();
		cbbsync = new CComboBox();
		ip.add(cbbsync);

		spinner_sync = new CNumberSpinner(syncspin);
		new SpinnerMouseWheelSupport(spinner_sync);
		spinner_sync.setPreferredSize(new Dimension(60, 25));
		ip.add(spinner_sync);

		cl = new CLabel();
		cl.setPreferredSize(new Dimension(80, 25));
		ip.add(cl);

		DefaultComboBoxModel stepmodel = new DefaultComboBoxModel(
				TriggerDefine.HOLDOFF_STEP_TIME_UNIT);
		vup = new LabelValueUnitPane("", hospin, stepmodel);
		lvuc = new LabelValueUnitContactor(new HoldoffValueChange(),
				vup.comboBox, vup.spinner);
		nrip().add(vup);

		/** 先设置模型，更新语言，然后添加事件监听器 */
		cbbsync.setModel(new DefaultComboBoxModel(VideoTrigger.SYNC));
		cbbmodule.setModel(new DefaultComboBoxModel(VideoTrigger.MODULE));

		addListeners();
	}

	protected void addListeners() {
		spinner_sync.addChangeListener(this);
		cbbsync.addItemListener(this);
		cbbmodule.addItemListener(this);
		listening = true;
	}

	private boolean listening = false;
	private LabelValueUnitContactor lvuc;

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (!listening)
			return;
		if (e.getStateChange() != ItemEvent.SELECTED)
			return;

		if (e.getSource() == cbbsync) {
			int idx = cbbsync.getSelectedIndex();
			VideoTrigger vt = getTrigger();
			vt.sync = idx;
			spinner_sync.setEnabled(vt.isSyncValueEnable());
			bodySend();
			getTriggerPane().updateTrgVoltPane();
			return;
		}
		if (e.getSource() == cbbmodule) {
			int idx = cbbmodule.getSelectedIndex();
			VideoTrigger vt = getTrigger();
			vt.module = idx;
			resetMax(vt);
			bodySend();
			return;
		}
	}

	protected void resetMax(VideoTrigger vt) {
		int module = vt.module;
		// int max = vt.getMax(module);
		if (module != 0) {
			syncspin.setMaximum(Long.valueOf(625));
			cl.setText(" <= 625");
		} else {
			syncspin.setMaximum(Long.valueOf(525));
			cl.setText(" <= 525");
			int v = ((Number) syncspin.getValue()).intValue();
			if (v > 525) {
				syncspin.setValue(Long.valueOf(525));
			} else {
			}
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (!listening)
			return;

		if (e.getSource() == spinner_sync) {
			VideoTrigger vt = getTrigger();
			int v = ((Number) syncspin.getValue()).intValue();

			// System.out.println("commit: " + v);

			if (vt.syncValue != v) {
				vt.syncValue = v;
				bodySend();
			}

			getTriggerPane().updateTrgVoltPane();
			return;
		}
	}

	@Override
	public void localize(ResourceBundle rb) {
		vup.setLabel(rb.getString("M.Trg.Holdoff") + ": ");

		boolean tmp = listening;
		listening = false;

		cbbsync.setSelectedItem(cbbsync.getSelectedItem());
		cbbsync.repaint();

		cbbmodule.setSelectedItem(cbbmodule.getSelectedItem());
		listening = tmp;
	}

	@Override
	public void bodySend() {
		getTriggerPane().bodySend();
	}

	@Override
	public void fireProperty(PropertyChangeEvent evt) {
		String n = evt.getPropertyName();
		if (n.equals(PropertiesItem.NEXT_STATUS)) {
			listening = false;
			VideoTrigger vt = getTrigger();
			cbbsync.setSelectedIndex(vt.sync);
			spinner_sync.setEnabled(vt.isSyncValueEnable());
			spinner_sync.setValue(new Long(vt.syncValue));
			listening = true;
			return;
		} else if (n.equals(PropertiesItem.UPDATE_VIDEOMOD)) {
			listening = false;
			cbbmodule.setSelectedIndex(getTrigger().module);
			listening = true;
		}
	}

	class SpinnerMouseWheelSupport implements MouseWheelListener {
		private JSpinner js;

		public SpinnerMouseWheelSupport(JSpinner jcb) {
			this.js = jcb;
			jcb.addMouseWheelListener(this);
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (!js.isFocusable())
				return;
			int i = e.getWheelRotation();
			if (i < 0) {
				js.setValue(js.getNextValue());
			} else {
				js.setValue(js.getPreviousValue());
			}
		}

	};
}