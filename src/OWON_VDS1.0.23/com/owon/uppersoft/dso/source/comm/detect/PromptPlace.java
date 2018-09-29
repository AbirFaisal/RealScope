package com.owon.uppersoft.dso.source.comm.detect;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.owon.uppersoft.dso.function.SoftwareControl;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.util.PropertiesItem;

public class PromptPlace extends JPanel implements PropertyChangeListener {
	public static final String ProgressStart = "ProgressStart",
			ProgressIncrease = "ProgressIncrease",
			ProgressDone = "ProgressDone";

	protected JLabel lbl;
	protected JTextArea infoarea;

	protected ControlManager cm;

	public PromptPlace(ControlManager cm) {
		this.cm = cm;
		// setLayout(new BorderLayout(0, 0));
		setLayout(null);
		lbl = new JLabel();
		lbl.setBounds(0, 0, 350, 25);
		infoarea = new JTextArea();
		infoarea.setBounds(0, 25, 350, 100);
		infoarea.setBackground(getBackground());
		infoarea.setEditable(false);
		infoarea.setLineWrap(true);
		infoarea.setWrapStyleWord(true);

		add(lbl);
		add(infoarea);

		// 开始连接
		infoarea.setText(I18nProvider.bundle().getString("Label.connect"));

		setPreferredSize(new Dimension(350, 150));
	}

	protected String machineInformation() {
		StringBuilder initinfo = new StringBuilder();
		final SoftwareControl sc = cm.getSoftwareControl();
		initinfo.append("version: " + sc.getBoardVersion() + "series: "
				+ sc.getBoardSeries() + ":\r\n");
		return initinfo.toString();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String pn = evt.getPropertyName();
		if (pn.equals(PropertiesItem.STATUS)) {
			lbl.setText(evt.getNewValue().toString());
		} else if (pn.equals(PropertiesItem.MachineInformation)) {
			infoarea.setText("");//machineInformation().toString();
		}
	}
}