package com.owon.uppersoft.dso.view.pane;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.owon.uppersoft.dso.function.SoftwareControl;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.pref.Define;
import com.owon.uppersoft.dso.source.usb.USBSourceManager;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.dso.view.pane.dock.widget.FunctionPanel;
import com.owon.uppersoft.dso.view.pane.dock.widget.GroupPane;
import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.usb.IDevice;
import com.owon.uppersoft.vds.core.wf.WaveFormInfo;
import com.owon.uppersoft.vds.socket.ScpiPool;
import com.owon.uppersoft.vds.ui.resource.FontCenter;
import com.owon.uppersoft.vds.ui.widget.custom.CButton;
import com.owon.uppersoft.vds.ui.widget.custom.CCheckBox;
import com.owon.uppersoft.vds.ui.widget.custom.CComboBox;
import com.owon.uppersoft.vds.ui.widget.custom.ExcludeButtons;

/**
 * 显示 类型 点，矢量 余辉 1秒，2秒，5秒，无限，关闭 xy显示 开启，关闭 硬件频率计 开启，关闭
 * 
 * @author Matt
 * 
 */
public class DisplayPane extends FunctionPanel {

	private CComboBox persistcbb, xcbb, ycbb;
	private boolean listening = false;
	private CCheckBox xycb;
	private ExcludeButtons ebs;

	public DisplayPane(final ControlManager cm) {
		super(cm);
		final DataHouse dh = Platform.getDataHouse();
		ebs = initLineBtns();
		ncgp();
		nrip().add(ebs);

		if (cm.isXYModeSupport()) {
			nrip();
			xycb = ncb("M.Display.XYMode");
			xycb.setSelected(cm.displayControl.isXYModeOn());
			WaveFormInfo[] wfis = cm.getCoreControl().getWaveFormInfos();
			xcbb = nccb(wfis);
			nlblt("&");
			ycbb = nccb(wfis);

			xcbb.setSelectedIndex(cm.displayControl.wfx);
			ycbb.setSelectedIndex(cm.displayControl.wfy);

			xcbb.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (!listening)
						return;
					if (e.getStateChange() != ItemEvent.SELECTED)
						return;
					dh.controlManager.displayControl.wfx = xcbb
							.getSelectedIndex();
					updateXY();
				}
			});
			ycbb.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (!listening)
						return;
					if (e.getStateChange() != ItemEvent.SELECTED)
						return;
					dh.controlManager.displayControl.wfy = ycbb
							.getSelectedIndex();
					updateXY();
				}
			});
			xycb.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (!listening)
						return;
					boolean b = xycb.isSelected();
					cm.displayControl.setXYMode(b);
					Platform.getMainWindow().getToolPane().getButtonPane()
							.switch_3in1_xy(b);
					updateXY();
				}
			});
		}

		ncgp();
		nrip();
		nlbl("M.Display.Persistence");
		nrip();
		persistcbb = nccb(cm.displayControl.getPersistenceItems());
		persistcbb.setSelectedIndex(cm.displayControl.getPersistenceIndex());
		CButton clearbtn = nbtn("M.Custom.clear");// (FadeThread)ft.setCover

		ncgp();
		nrip();
		nlbl("Label.GridBrightness");
		nrip();
		final JSlider slider = new JSlider();
		ip.add(slider);
		slider.setMaximum(SoftwareControl.BrightnessLimit);
		slider.setValue(cm.displayControl.getGridBrightness()
				- SoftwareControl.BrightnessRemaining);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int v = slider.getValue() + SoftwareControl.BrightnessRemaining;
				cm.displayControl.changeGridBrightness(v, dh.getMainWindow());
			}
		});

		// addInsideConfig();
		localizeSelf();

		persistcbb.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!listening)
					return;
				if (e.getStateChange() != ItemEvent.SELECTED)
					return;
				int idx = cm.displayControl.setPersistenceIndex(persistcbb
						.getSelectedIndex());
				dh.getPersistentDisplay().fadeThdOn_Off_UI(idx);
			}
		});
		clearbtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cm.resetPersistence();
			}
		});

		//addDebugBtn();

		listening = true;
	}

	private ExcludeButtons initLineBtns() {
		PropertyChangeListener linePcl = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (!listening)
					return;
				String evtType = evt.getPropertyName();
				if (evtType.equals(ExcludeButtons.EXCLUDE)) {
					int sel = (Integer) evt.getNewValue();
					boolean link = sel == 0;
					cm.displayControl.linelink = link;
					// 如果数据来得慢也能及时刷新
					Platform.getMainWindow().updateShow();
				}
			}
		};
		String[] labels = {
				I18nProvider.bundle().getString("M.Display.Vector"),
				I18nProvider.bundle().getString("M.Display.Dots") };
		int sel = cm.displayControl.linelink ? 0 : 1;
		ExcludeButtons ebs = new ExcludeButtons(labels, linePcl, sel, 90, 60,
				FontCenter.getLabelFont());
		// new Font(Font.SANS_SERIF, Font.PLAIN, 14));
		return ebs;
	}

	private void updateXY() {
		Platform.getMainWindow().getChartScreen().updateXYView();
	}

	private LogDialog dlg;
	private GroupPane gp;

	// protected void testPrompt() {
	// Platform.getControlApps().getDaemon().getUSBLoopChecker()
	// .promptUSBPorts(new LinkedList<IDevice>(),
	// new DefaultUSBCheckHelper());
	// }

	protected void testUp() {
		Platform.getMainWindow().promptUp();
	}

	private void addDebugBtn() {
		gp = ncgp();
		/** 进入本页面再输入密码才有效 */

		boolean open = cm.sc.isPWDOpen();
		gp.setVisible(open);
		// if (!open)
		// return;

		nrip_notitle();

		JPanel tune = new JPanel();
		tune.setOpaque(false);
		tune.setPreferredSize(new Dimension(Define.Dock_Width, 135));

		final MainWindow mw = Platform.getMainWindow();
		final JToggleButton logbtn = new JToggleButton();
		logbtn.setText("log");
		logbtn.setSelected(false);
		logbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean b = logbtn.isSelected();
				// System.err.println(b + ", " + dlg);
				if (!b) {
					if (dlg != null) {
						dlg.dispose();
						dlg = null;
					}
				} else {
					dlg = new LogDialog(mw.getFrame(), logbtn);
				}
			}
		});
		gp.add(tune);

		final JButton batchbtn = new JButton();
		batchbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Platform.getControlApps().interComm.initDetail();
			}
		});
		batchbtn.setText("batch");

		JButton autoGenButton = new JButton("autoGet");
		autoGenButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Platform.getControlApps().keepload();
			}
		});

		JButton stopButton = new JButton("stop");
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Platform.getControlApps().stopkeep();
			}
		});

		final JButton connectButton = new JButton("Connect");
		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final USBSourceManager usbsm = cm.sourceManager
						.getUSBSourceManager();
				if (!usbsm.isConnected()) {
					List<IDevice> ids = usbsm.refreshUSBPort();
					if (ids.size() == 0)
						return;
					IDevice id = ids.get(0);
					if (id != null) {
						if (cm.sourceManager.connectUSB(id))
							connectButton.setText(("Disconnect"));
					}
				} else {
					cm.sourceManager.disconnectSource();
					connectButton.setText("Connect");
				}
			}
		});

		JButton tuneButton = new JButton("Tune");
		tuneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cm.getPrinciple().openTuneDialog(mw.getFrame(), cm.pcs);
			}
		});

		final JToggleButton test2Button = new JToggleButton("t2");
		test2Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// ChartScreen cs = mw.getChartScreen();
				if (test2Button.isSelected()) {
					// cs.removeMouseGesture();
					// testPrompt();
				} else
					// cs.addMouseGesture();
					testUp();
			}
		});

		final JButton elapsedTool = new JButton("FadeCost");
		elapsedTool.addActionListener(new ActionListener() {
			long t0;
			boolean fadedCostTimeVisible = false;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!fadedCostTimeVisible) {
					setChannelCoupling(0, "GND");
					t0 = System.currentTimeMillis();
					elapsedTool.setText("timing...");
					fadedCostTimeVisible = true;
				} else {
					long end = System.currentTimeMillis();
					long del = end - t0;
					elapsedTool.setText("elapsed:" + del);
					fadedCostTimeVisible = false;
					setChannelCoupling(0, "AC");
				}
			}
		});

		tune.add(tuneButton);
		tune.add(logbtn);

		tune.add(stopButton);
		tune.add(autoGenButton);
		tune.add(connectButton);

		tune.add(batchbtn);
		// tune.add(test2Button);
		tune.add(elapsedTool);

		createHideContent(mw.getWindow(), tune, cm);
	}

	protected void createHideContent(Window wnd, JPanel tune, ControlManager cm) {
	}

	private String setChannelCoupling(int chidx, String rearcmd) {
		int cpidx = -1;
		String[] coupArr = ChannelInfo.COUPLINGCHARS;
		for (int i = 0; i < coupArr.length; i++) {
			if (rearcmd.equalsIgnoreCase(coupArr[i]))
				cpidx = i;
		}

		WaveForm wf = cm.getWaveFormInfoControl().getWaveForm(chidx);
		if (wf == null || cpidx < 0)
			return ScpiPool.ErrCh;
		wf.wfi.ci.c_setCoupling(cpidx);
		cm.pcs.firePropertyChange(PropertiesItem.COUPLING_OPTION, null, null);
		Platform.getMainWindow().updateDefaultAll();
		return ScpiPool.Success;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String n = evt.getPropertyName();
		if (n.equals(PropertiesItem.ADMIN_ROOT_PASSWORD_NOTIFY)) {
			boolean open = (Boolean) evt.getNewValue();
			gp.setVisible(open);
		} else if (n.equals(PropertiesItem.UPDATE_PERSISTENCE_INDEX)) {
			listening = false;
			int idx = cm.displayControl.getPersistenceIndex();
			persistcbb.setSelectedIndex(idx);
			listening = true;
		} else if (n.equals(PropertiesItem.UPDATE_LINELINK)) {
			listening = false;
			int idx = cm.displayControl.linelink ? 0 : 1;
			ebs.setSelected(idx);
			listening = true;
		}

	}
}
