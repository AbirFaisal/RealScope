package com.owon.uppersoft.dso.view.trigger;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.ResourceBundle;

import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.owon.uppersoft.dso.data.LObject;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.model.trigger.AbsTrigger;
import com.owon.uppersoft.dso.model.trigger.SlopeTrigger;
import com.owon.uppersoft.dso.model.trigger.TrgCheckType;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.model.trigger.TriggerDefine;
import com.owon.uppersoft.dso.model.trigger.common.Thredshold;
import com.owon.uppersoft.dso.model.trigger.condition.TrgConditionSpinnerModel;
import com.owon.uppersoft.dso.model.trigger.helper.ETV_Holdoff;
import com.owon.uppersoft.dso.model.trigger.helper.LabelValueUnitContactor;
import com.owon.uppersoft.dso.model.trigger.holdoff.HoldoffSpinnerModel;
import com.owon.uppersoft.dso.source.comm.effect.SubmitorFactory;
import com.owon.uppersoft.dso.source.pack.VTPatchable;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.dso.widget.custom.effect.LabelValueUnitPane;
import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.comm.effect.Submitable;
import com.owon.uppersoft.vds.core.trigger.help.EnumNValue;
import com.owon.uppersoft.vds.data.Point;
import com.owon.uppersoft.vds.ui.layout.OneRowLayout;
import com.owon.uppersoft.vds.ui.slider.SliderAdapter;
import com.owon.uppersoft.vds.ui.slider.SliderBarLocation;
import com.owon.uppersoft.vds.ui.slider.SliderDelegate;
import com.owon.uppersoft.vds.ui.slider.SymmetrySliderBar;
import com.owon.uppersoft.vds.ui.widget.custom.CComboBox;
import com.owon.uppersoft.vds.ui.widget.custom.CLabel;
import com.owon.uppersoft.vds.util.format.UnitConversionUtil;

public class SlopePane extends TriggerLoaderPane implements ItemListener,
		ChangeListener, SliderBarLocation {
	private CComboBox cbbsweep;
	private CComboBox cbbscondition;

	private TrgConditionSpinnerModel conspin;
	private HoldoffSpinnerModel hospin;

	private LabelValueUnitPane hovup;

	public void loadTrigger(int chl) {
		this.setChannel(chl);
		listening = false;
		SlopeTrigger st = getTrigger();
		cbbsweep.setSelectedIndex(st.getSweep());
		cbbscondition.setSelectedIndex(st.condition);

		conspin.setValue(st.trgcondition);
		conlvuc.updateStepIndex(st.trgcondition.getStepIndex());

		hospin.setValue(st.etvho);
		holvuc.updateStepIndex(st.etvho.getStepIndex());

		uptxt.setText(getUppestLabel(st));
		lowtxt.setText(getLowestLabel(st));

		if (getTriggerPane().getTriggerControl().isSweepEnable()) {
			cbbsweep.setSelectedIndex(st.getSweep());
			cbbsweep.setEnabled(true);
		} else {
			cbbsweep.setSelectedIndex(st.setSweep(0));
			cbbsweep.setEnabled(false);
		}

		listening = true;
		/** 构造时会计算一次摆率，显示出来 */
		updateShakeRate();
	}

	public AbsTrigger submitTrigger() {
		SlopeTrigger st = getTrigger();
		st.condition = cbbscondition.getSelectedIndex();
		st.trgcondition.set((EnumNValue) conspin.getValue());
		st.setSweep(cbbsweep.getSelectedIndex());
		st.etvho.set((EnumNValue) hospin.getValue());
		return st;
	}

	public SlopeTrigger getTrigger() {
		return getTriggerPane().curTrgSet().slope;
	}

	private String getUppestLabel(SlopeTrigger st) {
		ChannelInfo ci = Platform.getDataHouse().getWaveFormManager()
				.getWaveForm(getChannel()).wfi.ci;
		int voltbase = ci.getVoltageLabel().getValue();
		int pos0 = ci.getPos0();
		String txt = st.getUppestLabel(ci.isInverse(), voltbase, pos0);

		return "<html><font><u>" + txt + "<u></font></html>";
	}

	private String getLowestLabel(SlopeTrigger st) {
		ChannelInfo ci = Platform.getDataHouse().getWaveFormManager()
				.getWaveForm(getChannel()).wfi.ci;
		int voltbase = ci.getVoltageLabel().getValue();
		int pos0 = ci.getPos0();
		String txt = st.getLowestLabel(ci.isInverse(), voltbase, pos0);

		return "<html><font><u>" + txt + "<u></font></html>";
	}

	private void updateUpLowOnReverse() {
		SlopeTrigger st = getTrigger();
		uptxt.setText(getUppestLabel(st));
		lowtxt.setText(getLowestLabel(st));

		ChannelInfo ci = Platform.getDataHouse().getWaveFormManager()
				.getWaveForm(getChannel()).wfi.ci;
		ResourceBundle rb = I18nProvider.bundle();

		// 根据反相情况，更改上下限文本
		if (ci.isInverse()) {
			lowlbl.setText(rb.getString("M.Trg.ThredsholdUp") + ": ");
			uplbl.setText(rb.getString("M.Trg.ThredsholdLow") + ": ");
		} else {
			uplbl.setText(rb.getString("M.Trg.ThredsholdUp") + ": ");
			lowlbl.setText(rb.getString("M.Trg.ThredsholdLow") + ": ");
		}

		updateShakeRate();
	}

	/**
	 * 上下限电压差值 dvolt,时间值 secs, 摆率 srv
	 */
	public void updateShakeRate() {
		ChannelInfo ci = Platform.getDataHouse().getWaveFormManager()
				.getWaveForm(getChannel()).wfi.ci;
		int voltbase = ci.getVoltageLabel().getValue();
		SlopeTrigger st = getTrigger();
		/** dvolt */
		/** KNOW 没减去POS0，因为将取上下限差值，算得电压差值 */
		Thredshold ts = st.getThredshold();
		double dvolt = (ts.c_getUppest() - ts.c_getLowest())
				* (double) voltbase / GDefine.PIXELS_PER_DIV;
		/** secs */
		String secsunit = "ns";
		double secs = st.trgcondition.toInt() * 10;
		/** srv */
		double srv = dvolt / secs;
		String sr = UnitConversionUtil.getSimplifiedVoltLabel_mV(srv) + "/"
				+ secsunit;
		slratetxt.setText(sr);
	}

	@Override
	public Point getSliderBarLocation(int x, int y, int xs, int ys, Component cp) {
		int w = cp.getWidth();

		x = w + xs - x - 20;
		y = ys - y - sliderheight + 20;
		return new Point(x, y);
	}

	private void operationThredshold(MouseEvent e, int v,
			final TrgCheckType type) {
		final int chl = getChannel();
		final int x = e.getX();
		final int y = e.getY();
		Point p = getSliderBarLocation(x, y, e.getXOnScreen(),
				e.getYOnScreen(), e.getComponent());

		int hr = SlopeTrigger.ThredsholdHalfRange;
		final WaveFormManager wfm = Platform.getDataHouse()
				.getWaveFormManager();
		final TriggerControl trgc = getTriggerPane().getTriggerControl();
		final WaveForm wf = wfm.getWaveForm(chl);
		final ChannelInfo ci = wf.wfi.ci;
		int defV = ci.getPos0byRange(hr);
		// 附加反相
		v = ci.getInverseValue(v);

		SymmetrySliderBar.createSymmetrySliderFrame(Platform.getMainWindow()
				.getFrame(), p.x, p.y, hr, defV, hr - v, true,
				Color.LIGHT_GRAY, SliderDelegate.BtnStatusBoth,
				new SliderAdapter() {
					public void valueChanged(int oldV, int newV) {
						int hr = SlopeTrigger.ThredsholdHalfRange;
						int v = newV;
						v = hr - v;
						SlopeTrigger st = getTrigger();

						// 过滤反相
						v = ci.getInverseValue(v);
						VTPatchable vtp = trgc.getVTPatchable();
						boolean changed = st.c_setUppestOrLowest(type, v);
						if (changed) {
							vtp.submiteUpper_Lower(chl, st, type);
						}
						updateLowUpOnChange(st);
					}

					public void on50percent() {
						// System.err.println("on50percent");
						int v = wf.getTrg50Percent();
						// System.err.println("on50: " + v);
						if (v != Integer.MAX_VALUE) {
							SlopeTrigger st = getTrigger();
							VTPatchable vtp = trgc.getVTPatchable();
							boolean changed = st.c_setUppestOrLowest(type, v);
							if (changed) {
								vtp.submiteUpper_Lower(chl, st, type);
							}
							updateLowUpOnChange(st);
						}
					}

					public void onDispose() {
						Platform.getMainWindow().update_DoneUppLow();
					}

					public void actionOff() {
						// Platform.getDataHouse().controlManager
						// .getJobUnitSyncronizer2().endSync();
					}

				}, I18nProvider.bundle());

		Platform.getMainWindow().update_ChangeUppLow(chl, type);
	}

	private void updateLowUpOnChange(SlopeTrigger st) {
		uptxt.setText(getUppestLabel(st));
		lowtxt.setText(getLowestLabel(st));
		/** 摆率 */
		updateShakeRate();
		Platform.getMainWindow().getChartScreen().re_paint();
	}

	public SlopePane(TriggerPane trp) {
		super(trp);
		SlopeTrigger st = getTrigger();

		nrip(new OneRowLayout(new Insets(3, 3, 3, 3), 1));
		cbbscondition = new CComboBox(
				LObject.getLObjects(TriggerDefine.CONDITIONS_SLOPE));

		lblcdt = new CLabel();
		ip.add(lblcdt);
		ip.add(cbbscondition);

		nrip();

		conspin = new TrgConditionSpinnerModel(st.trgcondition);
		DefaultComboBoxModel stepmodel1 = new DefaultComboBoxModel(
				TriggerDefine.CONDITION_STEP_TIME_UNIT);
		convup = new LabelValueUnitPane("", conspin, stepmodel1);
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
		convup.spinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				/** 摆率 */
				updateShakeRate();
			}

		});

		ip.add(convup);

		nrip();
		uplbl = new CLabel();
		// uplbl.setPreferredSize(new Dimension(100, 25));
		uptxt = new CLabel();
		uptxt.setPreferredSize(new Dimension(100, 25));
		uptxt.setText(getUppestLabel(st));
		ip.add(uplbl);
		ip.add(uptxt);
		uptxt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		uptxt.addMouseListener(new MouseAdapter() {

			/** 阈值目前没有双击到50%的功能 */
			@Override
			public void mousePressed(MouseEvent e) {
				int v = getTrigger().c_getUppest();
				operationThredshold(e, v, TrgCheckType.UppOver);
			}
		});

		nrip();
		lowlbl = new CLabel();
		// lowlbl.setPreferredSize(new Dimension(100, 25));
		lowtxt = new CLabel();
		lowtxt.setText(getLowestLabel(st));
		lowtxt.setPreferredSize(new Dimension(100, 25));
		ip.add(lowlbl);
		ip.add(lowtxt);
		lowtxt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lowtxt.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				int v = getTrigger().c_getLowest();
				operationThredshold(e, v, TrgCheckType.LowOver);
			}
		});

		nrip();
		slratelbl = new CLabel();
		ip.add(slratelbl);
		slratetxt = new CLabel();
		ip.add(slratetxt);

		nrip();
		sweeplbl = new CLabel();
		ip.add(sweeplbl);

		cbbsweep = new CComboBox(LObject.getLObjects(TriggerDefine.SWEEP));
		ip.add(cbbsweep);

		ETV_Holdoff etvho = st.etvho;
		hospin = new HoldoffSpinnerModel(etvho);
		DefaultComboBoxModel stepmodel = new DefaultComboBoxModel(
				TriggerDefine.HOLDOFF_STEP_TIME_UNIT);
		hovup = new LabelValueUnitPane("", hospin, stepmodel);
		holvuc = new LabelValueUnitContactor(new HoldoffValueChange(),
				hovup.comboBox, hovup.spinner);

		nrip().add(hovup);

		/** 先设置模型，更新语言，然后添加事件监听器 */
		addListeners();
	}

	protected void addListeners() {
		cbbsweep.addItemListener(this);
		cbbscondition.addItemListener(this);
		listening = true;
	}

	private boolean listening = false;
	private CLabel sweeplbl;
	private CLabel uptxt;
	private CLabel lowtxt;
	private CLabel lowlbl;
	private CLabel uplbl;
	private CLabel slratelbl;
	private CLabel slratetxt;
	private LabelValueUnitContactor holvuc;
	private LabelValueUnitContactor conlvuc;
	private CLabel lblcdt;
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
			bodySend();
			return;
		}
		if (e.getSource() == cbbscondition) {
			int idx = cbbscondition.getSelectedIndex();
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
		lblcdt.setText(rb.getString("M.Trg.S.Condition") + ": ");
		slratelbl.setText(rb.getString("M.Trg.SlewRate") + ": ");
		sweeplbl.setText(rb.getString("M.Trg.TrigMode") + ": ");
		hovup.setLabel(rb.getString("M.Trg.Holdoff") + ": ");
		uplbl.setText(rb.getString("M.Trg.ThredsholdUp") + ": ");
		lowlbl.setText(rb.getString("M.Trg.ThredsholdLow") + ": ");

		boolean tmp = listening;
		listening = false;

		cbbscondition.setSelectedItem(cbbscondition.getSelectedItem());
		cbbscondition.repaint();

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
		if (n.equals(PropertiesItem.UPDATE_UPP_LOW)) {
			int chl = (Integer) evt.getNewValue();
			int idx = getChannel();
			if (chl == idx)
				updateUpLowOnReverse();
		} else if (n.equals(PropertiesItem.NEXT_STATUS)) {
			listening = false;
			cbbscondition.setSelectedIndex(getTrigger().condition);
			listening = true;
		} else if (n.equals(PropertiesItem.T_SweepOnce2Auto)) {
			listening = false;
			cbbsweep.setSelectedIndex(getTrigger().getSweep());
			listening = true;
		} else if (n.equals(PropertiesItem.NEXT_SINGLE_CHANNEL)) {
			this.setChannel((Integer) evt.getNewValue());
			updateUpLowOnReverse();
		} else if (n.equals(PropertiesItem.T_2SweepOnce)) {
			cbbsweep.setSelectedIndex(getTrigger().getSweep());
		}
	}
}