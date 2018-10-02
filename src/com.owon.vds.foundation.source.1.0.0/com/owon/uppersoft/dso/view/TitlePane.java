package com.owon.uppersoft.dso.view;

import static com.owon.uppersoft.dso.pref.Define.def;
import static com.owon.uppersoft.vds.ui.resource.ResourceCenter.ImageDirectory;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.mode.control.TimeControl;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.pref.Define;
import com.owon.uppersoft.dso.pref.Style;
import com.owon.uppersoft.dso.source.comm.AbsInterCommunicator;
import com.owon.uppersoft.dso.source.comm.TrgStatus;
import com.owon.uppersoft.dso.source.comm.effect.SubmitorFactory;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.core.aspect.control.TimeConfProvider;
import com.owon.uppersoft.vds.core.pref.StaticPref;
import com.owon.uppersoft.vds.ui.layout.OneRowLayout;
import com.owon.uppersoft.vds.ui.paint.LineDrawTool;
import com.owon.uppersoft.vds.ui.resource.SwingResourceManager;
import com.owon.uppersoft.vds.ui.widget.custom.CLButton;
import com.owon.uppersoft.vds.ui.widget.custom.LButton;
import com.owon.uppersoft.vds.ui.window.ComponentMover;

/**
 * TitlePane，标题栏
 * 
 */
public class TitlePane extends JPanel implements Localizable, ITitleStatus,
		PropertyChangeListener {

	public static final String LogoPath = ImageDirectory + "logo.png";
	public static final String MinimizePath = (Define.def.style.path + Style.Minimizename);
	public static final String Minimize_pPath = (Define.def.style.path + Style.Minimize_pname);
	public static final String RestorePath = ImageDirectory + "restore.gif";
	public static final String ClosePath = (Define.def.style.path + Style.Closename);
	public static final String Close_pPath = (Define.def.style.path + Style.Close_pname);

	public static final String RunPath = ImageDirectory + "run.png";
	public static final String StopPath = ImageDirectory + "stop.png";

	//public static final String AutosetPath = ImageDirectory + "autoset_u.png";
	//public static final String Autoset_pPath = ImageDirectory + "autoset_p.png";
	//public static final String SinglePath = ImageDirectory + "single.png";
	//public static final String Single_pPath = ImageDirectory + "single_p.png";

	/**
	 * 
	 */
	private static final long serialVersionUID = -471832215692573020L;
	private MainWindow mw;
	private DataHouse dh;
	private Dimension iconsize = new Dimension(32, 24);
	private StorageView view;
	private TitleStatusLabel statuslbl;

	private RSLButton btnRS;
	// private LButton btnOption;
	private LButton btnAutoset, btnSingle;
	private boolean run;
	//private ImageIcon ri;
	//private ImageIcon si;

	/**
	 * @return 存储视图
	 */
	public StorageView getStorageView() {
		return view;
	}

	private ControlManager cm;

	public TitlePane(final MainWindow mw, Dimension sz) {
		this.mw = mw;
		dh = mw.getDataHouse();
		cm = dh.controlManager;

		setPreferredSize(sz);

		setOpaque(false);
		setLayout(new BorderLayout());

		JPanel leftPane = createLeftPane();
		add(leftPane, BorderLayout.WEST);

		view = new StorageView(dh);
		add(view, BorderLayout.CENTER);

		JPanel rightPane = createRightPane();
		add(rightPane, BorderLayout.EAST);

		new ComponentMover(mw.getFrame(), this);
		I18nProvider.LocalizeSelf(this);
		cm.pcs.addPropertyChangeListener(this);
	}

	private JPanel createLeftPane() {
		final JPanel operatePane = new JPanel();
		operatePane.setOpaque(false);
		operatePane.setLayout(new OneRowLayout(5));
		final JLabel iconlbl = new JLabel();

		int status_icon_xloc = 10;

		StaticPref sp = cm.getConfig().getStaticPref();

		Icon i = sp.getLogoIcon(sp);
		/** 从配置中取logo地址，获取成功则使用 */
		if (i != null) {
			iconlbl.setIcon(i);
			iconlbl.setPreferredSize(new Dimension(i.getIconWidth(), i
					.getIconHeight()));

			status_icon_xloc += i.getIconWidth();

			operatePane.add(iconlbl);
		}

		statuslbl = new TitleStatusLabel(status_icon_xloc);
		cm.bufferTitleStatus(statuslbl);

		operatePane.add(statuslbl);
		updateTrgStatus(TrgStatus.Offline);

		return operatePane;
	}

	public void exposeTrgStatus() {
		statuslbl.repaint();
	}

	public void updateTrgStatus(int c, TrgStatus ts) {
		statuslbl.updateTrgStatus(c, ts);
	}

	public void setTempStatus(boolean b) {
		statuslbl.setTempStatus(b);
	}

	public void updateTrgStatus(TrgStatus ts) {
		updateTrgStatus(0, ts);
	}

	public TrgStatus getTrgStatus() {
		return statuslbl.getTrgStatus();
	}

	// private void startStopStatus() {

	// btnOption.setEnabled(false);

	// /** 当RS按钮Unable则"Pause&Export"不能点 */
	// cm.pcs.firePropertyChange(
	// PropertiesItem.RSSTATUS_CHANGE, null, false);
	// }

	public void confirmStopStatus() {
		enableBtns();
	}

	public void enableBtns() {
		btnRS.setEnabled(true);
	}

	public void enableAllButtons(boolean b) {
		btnRS.setEnabled(b);
		btnAutoset.setEnabled(b);
		if (!b)
			btnSingle.setEnabled(false);
		else
			updateBtnSingle();
		cm.updateExportbtnEnable(b);
	}

	private void askStop(boolean dm) {
		getAbsInterCommunicator().statusStop(dm);
	}

	private void askRun(boolean syncChannel) {
		getAbsInterCommunicator().statusRun(syncChannel, true);
	}

	public void askSweepOnce() {
		dh.getControlApps().keepload();
		run = true;
		btnRS.setText("Stop");
		//btnRS.setIcon(si);
		//btnRS.setRolloverIcon(LineDrawTool.getRolloverIcon(si));

		TriggerControl trgc = cm.getTriggerControl();
		trgc.trySweepOnce();
	}

	/**
	 * 返回是否run
	 */
	public boolean switchRS() {
		if (run) {
			askStop(false);// true
			dh.getWaveFormManager().retainClosedWaveForms();
			return false;
		} else {
			boolean keepPlay = cm.playCtrl.confirmGoOnPlaying();
			TimeControl tc = cm.getTimeControl();
			tc.dosubmit();
			if (keepPlay)
				return false;

			// 当录制结束设置loadPos0返回给RT状态使用
			if (dh.isParaAsync())
				cm.initDetail(SubmitorFactory.reInit());
			askRun(true);

			return true;
		}
	}

	/**
	 * 返回是否run
	 */
	public void switchSingle_Run() {
		if (run) {
			askSweepOnce();
		} else {
			boolean keepPlay = cm.playCtrl.confirmGoOnPlaying();
			if (keepPlay)
				return;

			// 当录制结束设置loadPos0返回给RT状态使用
			if (dh.isParaAsync())
				cm.initDetail(SubmitorFactory.reInit());
			askRun(true);
		}
	}

	public void doAutoset() {
		getAbsInterCommunicator().autoset();
	}

	private AbsInterCommunicator getAbsInterCommunicator() {
		return Platform.getControlApps().interComm;
	}

	public void askSwitchRS() {
		btnRS.askSwitchRS();
	}

	private JPanel createRightPane() {
		final JPanel operatePane = new JPanel();
		operatePane.setOpaque(false);
		operatePane.setLayout(new OneRowLayout());

		//ImageIcon i;
		btnAutoset = new LButton("Auto");
		//i = SwingResourceManager.getIcon(TitlePane.class, AutosetPath);
		//btnAutoset.setIcon(i);
		//btnAutoset.setRolloverIcon(LineDrawTool.getRolloverIcon(i));
		//i = SwingResourceManager.getIcon(TitlePane.class, Autoset_pPath);
		//btnAutoset.setPressedIcon(i);
		operatePane.add(btnAutoset);
		btnAutoset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doAutoset();
			}

		});
		//btnAutoset.setPreferredSize(new Dimension(35, 32));




		btnRS = new RSLButton(this);
		btnRS.setText("Stop");
		//si = SwingResourceManager.getIcon(TitlePane.class, StopPath);
		//ri = SwingResourceManager.getIcon(TitlePane.class, RunPath);
		btnRS.setEnabled(false);
		//btnRS.setIcon(si);


		run = true;
		//btnRS.setPreferredSize(new Dimension(30, 32));




		/** 单次触发的快捷操作 */
		btnSingle = new LButton("Single");
		//i = SwingResourceManager.getIcon(TitlePane.class, SinglePath);
		//btnSingle.setIcon(i);
		//btnSingle.setRolloverIcon(LineDrawTool.getRolloverIcon(i));
		//i = SwingResourceManager.getIcon(TitlePane.class, Single_pPath);
		//btnSingle.setPressedIcon(i);

		btnSingle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				askSweepOnce();
			}
		});
		//btnSingle.setPreferredSize(new Dimension(30, 32));
		updateBtnSingle();



		final LButton btnMin = new LButton("Min");
		btnMin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mw.minSize();
			}
		});
		//i = SwingResourceManager.getIcon(TitlePane.class, MinimizePath);
		//btnMin.setIcon(i);
		// i = SwingResourceManager.getIcon(TitlePane.class, Minimize_pPath);
		//btnMin.setRolloverIcon(SwingResourceManager.getIcon(TitlePane.class, Minimize_pPath));
		//btnMin.setPressedIcon(i);
		//btnMin.setPreferredSize(iconsize);


		final LButton btnClose = new LButton("X");
		//btnClose.setPreferredSize(iconsize);
		//i = SwingResourceManager.getIcon(TitlePane.class, ClosePath);
		//btnClose.setIcon(i);
		// i = SwingResourceManager.getIcon(TitlePane.class, Close_pPath);
		//btnClose.setRolloverIcon(SwingResourceManager.getIcon(TitlePane.class, Close_pPath));
		//btnClose.setPressedIcon(i);
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mw.getFrame().dispose();
			}
		});

		operatePane.add(btnRS);
		operatePane.add(btnSingle);
		// operatePane.add(btnMin);
		// operatePane.add(btnClose);

		return operatePane;
	}

	public void applyStop() {
		run = false;
		//btnRS.setIcon(ri);
		btnRS.setText("Run");
		//btnRS.setRolloverIcon(LineDrawTool.getRolloverIcon(ri));
	}

	public void updateView() {
		view.repaint();
		updateBtnSingle();
	}

	protected void createbtnMax() {
		final LButton btnMax = new LButton("Max");
		btnMax.setBackground(def.CO_TITLE);
		btnMax.setPreferredSize(iconsize);

		btnMax.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mw.exchangeSize();
			}
		});
		//btnMax.setIcon(SwingResourceManager.getIcon(TitlePane.class, RestorePath));
	}

	@Override
	public void localize(ResourceBundle rb) {
		view.localize(rb);

		statuslbl.localize(rb);
		btnAutoset.setToolTipText(rb.getString("ToolTip.AutoSet"));
		btnSingle.setToolTipText(rb.getString("ToolTip.SingleTrig"));
		btnRS.setToolTipText(rb.getString("ToolTip.RS"));
		// btnOption.setToolTipText(rb.getString("ToolTip.OptionAs"));
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String pn = evt.getPropertyName();
		if (pn.equals(PropertiesItem.FFT_ON)) {
			updateBtnSingle();
		} else if (pn.equals(PropertiesItem.FFT_OFF)) {
			updateBtnSingle();
		} else if (pn.equals(PropertiesItem.SWITCH_SLOWMOVE)) {
			updateBtnSingle();
		} else if (pn.equals(PropertiesItem.OPERATE_RUN)) {
			run = true;
			btnRS.setText("Stop");
			//btnRS.setIcon(si);
			//btnRS.setRolloverIcon(LineDrawTool.getRolloverIcon(si));
		} else if (pn.equals(PropertiesItem.OPERATE_STOP)) {
			run = false;
			btnRS.setEnabled(false);
			btnRS.setText("Run");
			//btnRS.setIcon(ri);
			//btnRS.setRolloverIcon(LineDrawTool.getRolloverIcon(ri));
		} else if (pn.equals(PropertiesItem.START_AUTOSET)) {
			statuslbl.setTempStatus(true);
			enableAllButtons(false);
		} else if (pn.equals(PropertiesItem.STOP_AUTOSET)) {
			setTempStatus(false);
			enableAllButtons(true);
			updateView();
		}
	}

	public void updateBtnSingle() {
		// b为false时必定设灰,b为true时,按钮不一定可用,当不为单触的情况也设灰
		if (!cm.getTriggerControl().isSingleTrg()) {
			btnSingle.setEnabled(false);
			return;
		}
		if (cm.getFFTControl().isFFTon()) {
			btnSingle.setEnabled(false);
			return;
		}
		if (cm.getCoreControl().isRunMode_slowMove()) {
			btnSingle.setEnabled(false);
			return;
		}
		btnSingle.setEnabled(true);
	}
}