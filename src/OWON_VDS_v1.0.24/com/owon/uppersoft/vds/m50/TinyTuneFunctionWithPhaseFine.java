package com.owon.uppersoft.vds.m50;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.source.comm.effect.SubmitorFactory;
import com.owon.uppersoft.vds.core.tune.Cending;
import com.owon.uppersoft.vds.device.interpret.DeviceAddressTable;
import com.owon.uppersoft.vds.machine.TinyMachine;
import com.owon.uppersoft.vds.source.comm.Submitor2;
import com.owon.vds.tiny.firm.pref.model.ArgType;
import com.owon.vds.tiny.firm.pref.model.PhaseFine;
import com.owon.vds.tiny.firm.pref.model.Register;
import com.owon.vds.tiny.firm.pref.model.TuneModel;
import com.owon.vds.tiny.tune.TinyTuneFunction;
import com.owon.vds.tiny.tune.detail.DefaultCalArgType;

public class TinyTuneFunctionWithPhaseFine extends TinyTuneFunction {
	public TinyTuneFunctionWithPhaseFine(int channelsNumber, int[] vbs) {
		super(channelsNumber, vbs);
	}

	@Override
	public JTabbedPane createTabs(Container panel) {
		JTabbedPane jtp = super.createTabs(panel);

		final Register reg = getTuneModel().getRegister();
		PhaseFine pf = reg.getPhaseFine();
		boolean select = pf.isSelect();
		int value = pf.getAbsValue();
		// System.err.println(value);
		JPanel fine_pane = new JPanel();
		// 相位细调
		fine_pane.add(new JLabel("\u76F8\u4F4D\u7EC6\u8C03"));
		final JToggleButton jtb = new JToggleButton();
		jtb.setSelected(select);
		jtb.setText("1");

		fine_pane.add(jtb);

		final SpinnerNumberModel snm = new SpinnerNumberModel(value, 0,
				PhaseFine.PHASE_FINE_MAX, 1);
		final JSpinner csp = new JSpinner(snm);
		csp.setPreferredSize(new Dimension(80, 28));
		fine_pane.add(csp);
		csp.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				send(reg, jtb.isSelected(), (Integer) csp.getValue());
			}
		});
		jtb.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (jtb.isSelected()) {
					jtb.setText("1");
				} else {
					jtb.setText("0");
				}
				send(reg, jtb.isSelected(), (Integer) csp.getValue());
			}
		});
		panel.add(fine_pane, BorderLayout.SOUTH);

		return jtp;
	}

	protected void send(Register reg, boolean b, int value) {
		int v = reg.setPhaseFineValue(b, (short) value);

		DeviceAddressTable table = ((TinyMachine) Platform.getControlManager()
				.getMachine()).getDeviceAddressTable();
		Submitor2 sb = (Submitor2) SubmitorFactory.reInit();
		sb.sendCMDbyBytes(table.PHASE_FINE, v);
	}

	@Override
	protected TuneModel createTuneModel(int channelsNumber, int vbNum) {
		return new TuneModel(channelsNumber, vbNum) {
			@Override
			protected void fillArgTypeList(List<DefaultCalArgType> cmdts,
					int channelNumber, int vbNum) {
				// 粗调增益
				DefaultCalArgType coarseGain = new DefaultCalArgType(
						ArgType.Gain.ordinal(), Cending.ascending,
						channelNumber, vbNum);
				cmdts.add(coarseGain);

				// 零点步进
				DefaultCalArgType zeroAmplitude = new DefaultCalArgType(
						ArgType.Step.ordinal(), Cending.ascending,
						channelNumber, vbNum);
				cmdts.add(zeroAmplitude);

				// 零点补偿
				DefaultCalArgType zeroCompensation = new DefaultCalArgType(
						ArgType.Compensation.ordinal(), Cending.ascending,
						channelNumber, vbNum);
				cmdts.add(zeroCompensation);
			}
		};
	}
}