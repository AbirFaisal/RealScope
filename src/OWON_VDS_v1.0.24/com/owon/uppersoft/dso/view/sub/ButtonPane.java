package com.owon.uppersoft.dso.view.sub;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.pref.Define;
import com.owon.uppersoft.dso.pref.Style;
import com.owon.uppersoft.dso.source.comm.AbsGetDataRunner;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.view.ChartScreen;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.dso.view.TitlePane;
import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.core.comm.JobQueueDispatcher;
import com.owon.uppersoft.vds.core.paint.ScreenContext;
import com.owon.uppersoft.vds.ui.prompt.FadeIOShell;
import com.owon.uppersoft.vds.ui.prompt.KeepNotice;
import com.owon.uppersoft.vds.ui.resource.SwingResourceManager;
import com.owon.uppersoft.vds.ui.widget.custom.LButton;
import com.owon.uppersoft.vds.util.format.SFormatter;

public class ButtonPane extends JPanel implements Localizable,
		PropertyChangeListener {
	public static final String UPDATE_TUNE_BUTTON = "updateTuneButton";
	public static final String ToolPath = (Define.def.style.path + Style.Toolname);
	public static final String Tool_pPath = (Define.def.style.path + Style.Tool_pname);
	public static final String _3in1Path = (Define.def.style.path + Style._3in1name);
	public static final String _1in1Path = (Define.def.style.path + Style._3in1_pname);
	public static final String factoryPath = (Define.def.style.path + "factoryset.png");
	public static final String factory_pPath = (Define.def.style.path + "factoryset_p.png");
	public static final String exportPath = (Define.def.style.path + "export.png");
	public static final String export_pPath = (Define.def.style.path + "export_p.png");
	public static final int Height = 70;
	private static final int btnW = 37, btnH = 45;
	static int v;
	private MainWindow mw;
	private DataHouse dh;
	private JLabel empty, frmlbl;
	private float fps;
	private int cmdCount, wfs, points;
	private String pkn = "";
	private LButton btn_3in1;
	private LButton toolbtn;
	private LButton factorybtn;
	private LButton pau_expbtn;
	private JButton tunebtn;

	public ButtonPane(final MainWindow mw, final ControlManager cm,
	                  final DataHouse dh) {
		this.mw = mw;
		this.dh = dh;
		setBackground(Color.BLACK);
		FlowLayout fl = new FlowLayout(FlowLayout.RIGHT, 0, 0);
		setLayout(fl);
		//Icon i;

		LButton patchbtn = new LButton("Patch");
		patchbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (dh.getControlManager().isRuntime()) {
					quickPatch();
				} else {
					if (1 == 0) {
						FadeIOShell fio = new FadeIOShell();
						fio.prompt("Please connect to the device first.",
								mw.getWindow());
					} else {
						KeepNotice.notice("message " + (v++));
					}
				}
			}

		});
		patchbtn.setVisible(false);


		tunebtn = new LButton("Tune");
		tunebtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cm.getPrinciple().openTuneDialog(mw.getFrame(), cm.pcs);
			}
		});
		tunebtn.setVisible(false);


		pau_expbtn = new LButton("Export");
		pau_expbtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// pau_expbtn.setEnabled(false);
				// authorizeExport = true;
				cm.ewc.exportDMfile(mw.getFrame());
			}
		});


		factorybtn = new LButton("Reset");
		factorybtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				factorySet();
			}
		});


		toolbtn = new LButton("Menu");
		cm.getDockControl().initialize(mw.getWindow(), cm.pcs, toolbtn);
		toolbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cm.getDockControl().dockDlgOnOff();
			}
		});
		toolbtn.setRolloverIcon(SwingResourceManager.getIcon(TitlePane.class, Tool_pPath));


		btn_3in1 = new LButton("3View");
		btn_3in1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switch_3in1();
			}
		});


		frmlbl = new JLabel();
		//frmlbl.setPreferredSize(new Dimension(350, 25));
		frmlbl.setForeground(Color.white);

		empty = new JLabel();
		empty.setPreferredSize(new Dimension(btnW, 95 - toolbtn.getHeight()));
		//add(empty);

		add(patchbtn);
		add(tunebtn);
		add(factorybtn);
		add(pau_expbtn);
		add(btn_3in1);
		add(toolbtn);
		add(frmlbl);

		/** One-button switch frame number printing */
		onekeyTurnPrint(false);

		updateLabel();
	}

	private void onekeyTurnPrint(boolean dbgbtns) {
		empty.setVisible(!dbgbtns);
		frmlbl.setVisible(dbgbtns);
	}

	private void updateFramesPerSe(float f) {
		fps = f;
		updateLabel();
	}

	private void updateCMDCounter(int count) {
		cmdCount = count;
		updateLabel();
	}

	private void updateFrameCounter(int num, int pts) {
		wfs = num;
		points = pts;
		updateLabel();
	}

	private void updateLabel() {
		frmlbl.setText(SFormatter.dataformat("fps:%.1f,cmd:%d,wfs:%d,pts:%d%s", fps, cmdCount, wfs, points, pkn));
	}

	public void factorySet() {
		int i = JOptionPane.showConfirmDialog(mw.getFrame(), I18nProvider
						.bundle().getString("M.Utility.FactorySet.ConfirmTxt"), "",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (i == JOptionPane.YES_OPTION) {
			dh.controlManager.factorySet();
			factoryViewSet();
		}
	}

	protected void quickPatch() {
	}

	public void factoryViewSet() {
		factory_1in1();
		dh.controlManager.getDockControl().dockDialog2HomePage();
		// Platform.getMainWindow().getToolPane().getButtonPane()
		// .minimizeDock();
	}

	private void factory_1in1() {
		switch_3in1_fft(false);
		// ChartScreen cs = mw.getChartScreen();
		// boolean b = cs.is3in1();
		// if (b)
		// switch_3in1();
	}

	private void switch_3in1() {
		ChartScreen cs = mw.getChartScreen();
		boolean b = cs.is3in1();
		apply_3in1(!b);
	}

	private void apply_3in1(boolean on) {
		ChartScreen cs = mw.getChartScreen();
		boolean b = cs.is3in1();
		if (on != b) {
			b = !b;
			cs.update3in1(b);
			// String p = b ? _3in1Path : _1in1Path;
			// Icon i = SwingResourceManager.getIcon(TitlePane.class, p);
			// btn_3in1.setIcon(i);
			// btn_3in1.setRolloverIcon(SwingResourceManager.getIcon(
			// TitlePane.class, b ? _3in1Path : _1in1Path));
		}
		updateBtn_3in1ToolTip(I18nProvider.bundle());
	}

	/**
	 * Different from other 3in1 switching methods, it can
	 * re-adjust the display interface in the same state of 1in1.
	 */
	public void switch_3in1_fft(boolean ffton) {
		boolean xyon = dh.getWaveFormManager().getXYView().isOn();
		ffton |= xyon;
		ChartScreen cs = mw.getChartScreen();
		cs.update3in1(ffton);
		updateBtn_3in1ToolTip(I18nProvider.bundle());
	}

	public void switch_3in1_xy(boolean xyon) {
		boolean ffton = dh.controlManager.getFFTControl().isFFTon();
		xyon |= ffton;
		apply_3in1(xyon);
	}

	private void updateBtn_3in1ToolTip(ResourceBundle rb) {
		ScreenContext pc = mw.getChartScreen().getPaintContext();
		if (pc.isScreenMode_3())
			btn_3in1.setToolTipText(rb.getString("ToolTip.3in1"));
		else
			btn_3in1.setToolTipText(rb.getString("ToolTip.1in1"));
	}

	@Override
	public void localize(ResourceBundle rb) {
		pau_expbtn.setToolTipText(rb.getString("ToolTip.Save"));
		factorybtn.setToolTipText(rb.getString("ToolTip.Factory"));
		toolbtn.setToolTipText(rb.getString("ToolTip.FuncMenu"));
		updateBtn_3in1ToolTip(rb);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String n = evt.getPropertyName();
		if (n.equals(PropertiesItem.FFT_ON)) {
			pau_expbtn.setEnabled(false);
		} else if (n.equals(PropertiesItem.FFT_OFF)) {
			pau_expbtn.setEnabled(true);
		} else if (n.equals(PropertiesItem.ADMIN_ROOT_PASSWORD_NOTIFY)) {
			boolean open = (Boolean) evt.getNewValue();
			onekeyTurnPrint(open);
			tunebtn.setVisible(open);
		} else if (n.equals(JobQueueDispatcher.UPDATE_CMD_COUNTER)) {
			updateCMDCounter((Integer) evt.getNewValue());
		} else if (n.equals(ButtonPane.UPDATE_TUNE_BUTTON)) {
			tunebtn.setVisible(true);
		} else if (n.equals(PropertiesItem.PAU_EXP_BTN_UPDATE)) {
			boolean v = (Boolean) evt.getNewValue();
			pau_expbtn.setEnabled(v);
		} else if (n.equals(AbsGetDataRunner.UPDATE_FPS)) {
			Float v = (Float) evt.getNewValue();
			updateFramesPerSe(v);
		} else if (n.equals(PropertiesItem.UPDATE_PK)) {
			boolean v = (Boolean) evt.getNewValue();
			String pkmsg = (v ? "pk" : "");

			if (!pkn.equals(pkmsg)) {
				pkn = pkmsg;
				updateLabel();
			}
		} else if (n.equals(PropertiesItem.updateFrameCounter)) {
			int points = (Integer) evt.getNewValue();
			int framecount = (Integer) evt.getOldValue();
			updateFrameCounter(framecount, points);
		} else if (n.equals(PropertiesItem.UPDATE_FACTORY_VIEW)) {
			factoryViewSet();
		}

	}
}