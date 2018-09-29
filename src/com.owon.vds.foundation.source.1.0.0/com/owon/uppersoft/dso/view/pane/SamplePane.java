package com.owon.uppersoft.dso.view.pane;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;

import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.mode.control.ISampleControl;
import com.owon.uppersoft.dso.mode.control.SampleControl;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.view.pane.dock.widget.FunctionPanel;
import com.owon.uppersoft.dso.view.pane.dock.widget.ItemPane;
import com.owon.uppersoft.vds.core.data.MinMax;
import com.owon.uppersoft.vds.ui.widget.CNumberSpinner;
import com.owon.uppersoft.vds.ui.widget.custom.CComboBox;

public class SamplePane extends FunctionPanel {
	private CComboBox samplingcbb;
	private CNumberSpinner avgTimes;

	private ISampleControl sc;

	private boolean listening = false;
	private ItemPane ip2;
	private ItemPane ip1;

	public SamplePane(ControlManager cm, final ISampleControl sc) {
		super(cm);
		this.sc = sc;

		ncgp();
		nrip();
		nlbl("M.Sample.Mode");

		samplingcbb = nccb(SampleControl.SamplingMode);

		ip1 = nrip();
		nlbl("M.Sample.AvgTimes");
		ip2 = nrip();
		nlblt(SampleControl.MinAverageSampleTimes + "<=");

		SpinnerNumberModel avgsnm = new SpinnerNumberModel();
		
		MinMax mm = sc.getAvgTimesRange();
		avgsnm.setMaximum(Long.valueOf(mm.getMax()));
		avgsnm.setMinimum(Long.valueOf(mm.getMin()));
		avgsnm.setStepSize(Long.valueOf(1));
		avgsnm.setValue(Long.valueOf(sc.getAvgTimes()));

		avgTimes = new CNumberSpinner(avgsnm);
		avgTimes.setPreferredSize(new Dimension(100, 30));

		ip.add(avgTimes);
		nlblt("<=" + SampleControl.MaxAverageSampleTimes);
		int midx = sc.getModelIdx();
		showAvg(midx);

		samplingcbb.setSelectedIndex(midx);

		localizeSelf();
		avgTimes.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (!listening)
					return;
				int v = ((Number) avgTimes.getValue()).intValue();
				// System.out.println(avgTimes.getValue());
				sc.c_setAvgTimes(v);
			}
		});
		samplingcbb.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!listening)
					return;
				if (e.getStateChange() != ItemEvent.SELECTED)
					return;

				int idx = samplingcbb.getSelectedIndex();
				sc.c_setModelIdx(idx);
				showAvg(idx);
			}
		});

		listening = true;
	}

	protected void showAvg(int idx) {
		boolean b = (idx == 2);
		ip2.setVisible(b);
		ip1.setVisible(b);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(PropertiesItem.APPLY_SAMPLING)) {
			listening = false;
			int midx = sc.getModelIdx();
			samplingcbb.setSelectedIndex(midx);
			avgTimes.setValue(sc.getAvgTimes());
			showAvg(midx);
			listening = true;
		}
	}
}
