package com.owon.uppersoft.dso.view.pane.function;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.owon.uppersoft.dso.function.DisplayControl;
import com.owon.uppersoft.dso.global.ControlApps;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.source.usb.USBSourceManager;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.view.pane.dock.widget.FunctionPanel;
import com.owon.uppersoft.vds.core.usb.IDevice;
import com.owon.uppersoft.vds.ui.widget.custom.CButton;
import com.owon.uppersoft.vds.ui.widget.custom.CComboBox;
import com.owon.uppersoft.vds.ui.widget.custom.CLabel;
import com.owon.uppersoft.vds.util.ui.ContextFileChooser;
import com.owon.uppersoft.vds.util.ui.ListComboBoxModel;

public class TestPane extends FunctionPanel implements Runnable {
	public TestPane(ControlManager cm) {
		super(cm);

		this.usbsm = cm.sourceManager.getUSBSourceManager();
	}

	private USBSourceManager usbsm;

	/** 连接 */
	private class PropertyCL implements PropertyChangeListener {
		private int num = -1, len = -1, range = -1, vl = 0;

		public PropertyCL() {
			jpb.setValue(vl = 0);
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			String pn = evt.getPropertyName();
			if (pn.equals(PropertiesItem.TRANS_FAIL))
				return;

			int v = (Integer) evt.getNewValue();
			if (pn.equals(PropertiesItem.DATA_LEN)) {
				if (range < 0) {
					len = v;
					range = len * num;
					jpb.setMaximum(range);
					cl.setText(range + "");
					jpb.setValue(vl = 0);
				}
			} else if (pn.equals(PropertiesItem.CHL_NUM)) {
				num = v;
			} else if (pn.equals(PropertiesItem.PROGRESS)) {
				jpb.setValue(vl += v);
			}
		}
	};

	/** 连接 */
	private CLabel cl;
	private CComboBox comboBox;
	private CButton refreshButton;
	private CButton connectButton;
	private ControlApps ca;
	private CButton gen;
	private CButton autoGenButton;
	private CButton stopButton;
	private JProgressBar jpb;

	/** 连接 */
	private void createConnectGroup() {
		ncgp();
		nrip();

		comboBox = nccb();
		refreshButton = nbtnt("refresh");
		refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(TestPane.this).start();
			}
		});

		connectButton = nbtnt(usbsm.isConnected() ? ("Action.Disconnect")
				: ("Action.Connect"));
		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!usbsm.isConnected()) {
					IDevice id = (IDevice) comboBox.getSelectedItem();
					if (id != null) {
						if (cm.sourceManager.connectUSB(id))
							connectButton.setText(I18nProvider.bundle()
									.getString("Action.Disconnect"));
					}
				} else {
					cm.sourceManager.disconnectSource();
					connectButton.setText(I18nProvider.bundle().getString(
							"Action.Connect"));
				}
			}
		});

		ca = Platform.getControlApps();

		nrip();
		gen = nbtnt("get");
		gen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread() {
					@Override
					public void run() {
						//controlApps.load();
					}
				}.start();
			}
		});

		autoGenButton = nbtnt("auto get");
		autoGenButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// controlApps.cm.sendRun();
				ca.keepload();
			}
		});

		stopButton = nbtnt("stop");
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// controlApps.cm.sendStop();
				ca.stopkeep();
			}
		});

		nrip();
		final JButton linkBtn = nbtnt("StaDat");
		linkBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// 过时的协议，可能不再适用
				// GetDataRunner gdr = controlApps.getDaemon().getGetDataRunner();
				// gdr.sendCommand(sm, GetDataRunner.sgad, t);
				// controlApps.interComm.receiveALL(cm, sm);
				// gdr.ci.DMem = 'M';
				// sm.acceptDMData(gdr.ci);
				// Platform.getDataHouse().receiveRTData(gdr.ci,
				// DataHouse.RT_Normal);
			}
		});

		final JButton dmBtn = nbtnt("DMem");

		nrip();
		jpb = new JProgressBar();
		ip.add(jpb);
		cl = nlblt("%");
		cl.setPreferredSize(new Dimension(80, 0));

		/** 写法不错 */
		final ContextFileChooser ifc = new ContextFileChooser();

		dmBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dmBtn.setEnabled(false);

				final File f = ifc.save();
				if (f == null) {
					dmBtn.setEnabled(true);
					return;
				}

				new Thread() {
					public void run() {
						// InterCommunicator ic = (InterCommunicator)
						// controlApps.interComm;
						// // Platform.getDataHouse().Run = false;
						// DMInfo ci = DataSaver.saveFileM(controlApps.getDaemon()
						// .getGetDataRunner(), cm, f, new PropertyCL(),
						// ic.getSaverHandler(), cm.sourceManager);
						// if (ci.status == DMInfo.Status_RT_NoDM) {
						//
						// ic.showNoDM();
						// // 关闭可能出现的进度框
						// dmBtn.setEnabled(true);
						// return;
						// }
						//
						// Platform.getDataHouse().receiveRTDMData(ci);
						// // TODO 按钮无法恢复
						// dmBtn.setEnabled(true);
					}
				}.start();
			}
		});

		localizeSelf();
		refreshButton.setEnabled(false);
		comboBox.setEditable(false);
		new Thread(this).start();

	}

	public static final int MaxMeasureTimes = 10;

	/** 连接 */
	@Override
	public void run() {
		List<IDevice> ids = usbsm.refreshUSBPort();
		ListComboBoxModel dcbm = new ListComboBoxModel(ids);
		comboBox.setModel(dcbm);
		comboBox.setEnabled(true);
		refreshButton.setEnabled(true);
	}

	public void addInsideConfig() {
		ncgp();
		nrip();
		nlblt("Times: ");
		final JSpinner js = new JSpinner(new SpinnerNumberModel(
				cm.displayControl.CompositDrawTimes, 1,
				DisplayControl.MaxCompositDrawTimes, 1));
		ip.add(js);

		js.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (!listening)
					return;
				cm.displayControl.CompositDrawTimes = (Integer) js.getValue();
			}
		});

		ncgp();
		nrip();
//		nlblt("MeasureTimes: ");
//		final JSpinner mtjs = new JSpinner(new SpinnerNumberModel(
//				cm.measureTimes2, 1, MaxMeasureTimes, 1));
//		ip.add(mtjs);
//
//		mtjs.addChangeListener(new ChangeListener() {
//			@Override
//			public void stateChanged(ChangeEvent e) {
//				if (!listening)
//					return;
//				cm.measureTimes2 = (Integer) mtjs.getValue();
//			}
//		});

		listening = true;
	}

	private boolean listening = false;
}
