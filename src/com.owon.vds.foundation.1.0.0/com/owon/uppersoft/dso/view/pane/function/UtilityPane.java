package com.owon.uppersoft.dso.view.pane.function;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.owon.uppersoft.dso.about.AboutDialog;
import com.owon.uppersoft.dso.function.ExportWaveControl;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.mode.control.SysControl;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.page.IContentPage;
import com.owon.uppersoft.dso.page.function.MachineNetPage;
import com.owon.uppersoft.dso.page.function.ReferenceWavePage;
import com.owon.uppersoft.dso.page.function.UtilityPage;
import com.owon.uppersoft.dso.pref.Define;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.dso.view.TipsWindow;
import com.owon.uppersoft.dso.view.pane.dock.ContentPane;
import com.owon.uppersoft.dso.view.pane.dock.widget.FunctionPanel;
import com.owon.uppersoft.dso.view.pane.dock.widget.ItemPane;
import com.owon.uppersoft.vds.core.machine.MachineType;
import com.owon.uppersoft.vds.core.pref.Config;
import com.owon.uppersoft.vds.print.PrinterPreviewFrame;
import com.owon.uppersoft.vds.ui.widget.custom.CButton;
import com.owon.uppersoft.vds.ui.widget.custom.CComboBox;
import com.owon.uppersoft.vds.util.FileUtil;
import com.owon.uppersoft.vds.util.ui.ListComboBoxModel;

/**
 * UtilityPane，这里面除语言之外的其它CComboBox，需要考虑到语言切换时，跟着切换语言并重新选择已选的Item；
 * 其它FunctionPanel由于不会和语言切换的CComboBox，同时显示，所以忽略了这一动过带来的影响
 * 
 */
public class UtilityPane extends FunctionPanel {
	protected MainWindow mw;

	private boolean SCPIconsole = true;
	private CComboBox cbblocale, cbbexport;
	private CButton pau_expbtn;
	private CComboBox syoccb;
	private Dimension pageDimension = new Dimension(125, 33);

	public UtilityPane(final ControlManager cm, final ContentPane cp,
			final UtilityPage up) {
		super(cm);

		mw = Platform.getMainWindow();
		final Config conf = cm.getConfig();
		final MachineType mt = cm.getMachine();
		/** 语言与皮肤 */
		createInterfaceGroup(conf);

		/** 读写操作 */
		createDataIOGroup(pageDimension, cp, up);

		/** 系统功能帮助 */
		createFunctionGroup(mt, pageDimension, cp, up);

		/** 版本 */
		createSoftGroup(conf);

		/** 多功能口 */
		createMutliIOGroup(mt);

		localizeSelf();

		listening = true;
	}

	private void createMutliIOGroup(final MachineType mt) {
		if (mt.isMultiIOSupport()) {
			ncgp();
			nrip();
			nlbl("M.Utility.SYNO.Name");// .setPreferredSize(new Dimension(110,
			// 28))
			nrip();

			/** 这里理论上在tiny<->smart切换时需要更新combobox */

			syoccb = nccb(cm.getSyncInOuts());
			// syoccb.setPreferredSize(new Dimension(138, 31));
			syoccb.setSelectedIndex(cm.getSysControl().getSyncOutput());
			syoccb.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (!listening)
						return;
					int sycidx = syoccb.getSelectedIndex();

					cm.getSysControl().c_setSyncOut(sycidx);
					TriggerControl tc = cm.getTriggerControl();
					if (sycidx != SysControl.SYNOUT_TrgIn) {
						if (tc.isSingleTrg()
								&& tc.isExtTrg(tc.getSingleTrgChannel())) {
							/** 离开外部触发，自动把通道设到ch1 */
							tc.setSingleChannel(0);
							mw.getToolPane().getTrgInfoPane().updateInfos(tc);
							tc.doSubmit();
						}
					} else {
						/** 但是选择外部触发的时候，并没有把单一触发通道选择到ext */
					}
					mw.re_paint();
				}
			});

			if (SCPIconsole) {
				CButton servercb = nbtn("M.Utility.Console");
				servercb.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						cm.scpiServer.startSCPIConsole();
					}
				});
			}
		} else
			syoccb = null;
	}

	private void createSoftGroup(final Config conf) {
		ncgp();
		nrip();

		CButton tipsShow = nbtn("M.TipsWindow.Name");
		tipsShow.setPreferredSize(pageDimension);
		CButton aboutbtn = nbtn("About.Name");
		aboutbtn.setPreferredSize(pageDimension);

		aboutbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AboutDialog.handleAboutInstance(conf, cm);
			}
		});

		tipsShow.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				TipsWindow.onoffTipsWin(mw, cm);
			}
		});
	}

	protected void createFunctionGroup(final MachineType mt, Dimension s,
			final ContentPane cp, final UtilityPage up) {
		ncgp();
		nrip();
		CButton correctSelfbtn = nbtn("Action.SelfCorrect");
		correctSelfbtn.setPreferredSize(s);
		CButton factSetbtn = nbtn("Action.FactorySet");
		factSetbtn.setPreferredSize(s);

		nrip();
		CButton helpbtn = nbtn("Action.Help");
		helpbtn.setPreferredSize(s);

		CButton webbt = nbtn("M.Utility.MachineNet");
		webbt.setPreferredSize(s);

		helpbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cm.docManager.helpAction();
			}
		});

		correctSelfbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				askAutoCal();
			}
		});

		factSetbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				factorySet();
			}
		});

		webbt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cp.applyContent(up.getContentID(), MachineNetPage.Name,
						IContentPage.Forward);
			}
		});

		webbt.setVisible(mt.isSupportNetwork());
	}

	protected void askAutoCal() {
	}

	private void createDataIOGroup(Dimension s, final ContentPane cp,
			final UtilityPage up) {
		ncgp();
		nrip();
		final JButton loadBtn = nbtn("Action.Open");
		loadBtn.setPreferredSize(s);
		loadBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadBinFile();
			}
		});
		CButton previewbtn = nbtn("Print.Preview");
		previewbtn.setPreferredSize(s);
		nrip();
		CButton saveimgbtn = nbtn("Action.SavaIMG");
		saveimgbtn.setPreferredSize(s);
		CButton referencebtn = nbtn("M.Utility.ReferenceWave.Name");
		referencebtn.setPreferredSize(s);
		nrip();
		cbbexport = nccb(ExportWaveControl.exportFilters);
		pau_expbtn = nbtn("Action.ExportWave");

		previewbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PrinterPreviewFrame.handlePrinterPreview(mw, cm);
			}
		});

		// 默认进入该界面时，是可用的；因为唯一的情况下都有进度条阻塞界面不会让用户可点击
		pau_expbtn.setEnabled(cm.isPau_expEnable());
		pau_expbtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// pau_expbtn.setEnabled(false);
				// authorizeExport = true;
				cm.ewc.setExpTypidx(cbbexport.getSelectedIndex());
				cm.ewc.exportDMfile(mw.getFrame());
			}
		});
		cbbexport.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (ItemEvent.SELECTED != e.getStateChange())
					return;
				cm.ewc.setExpTypidx(cbbexport.getSelectedIndex());
			}
		});

		saveimgbtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cm.ewc.getDataOutput().exportWaveAsPicture(UtilityPane.this,
						mw.getChartScreen());
			}
		});
		referencebtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cp.applyContent(up.getContentID(), ReferenceWavePage.Name,
						IContentPage.Forward);
			}
		});
	}

	private void createInterfaceGroup(final Config conf) {
		ncgp();
		nrip();
		nlbl("M.Locale.Name");

		cbblocale = nccb(conf.getLocales());

		nrip();
		nlbl("M.Skin.Label");

		final CComboBox cbbskin = nccb(UtilityPage.skintype, 115, 31);

		final ItemPane ipwarn = nrip();
		ipwarn.setVisible(false);
		nlbl("M.Skin.Labelwarn");
		CButton btres = nbtn("M.Skin.Restart");

		cbblocale.setSelectedIndex(conf.getLocaleIndex());
		cbblocale.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() != ItemEvent.SELECTED)
					return;

				executeLocalize(conf);
			}
		});

		cbbskin.setSelectedIndex(Define.def.STYLE_TYPE);
		cbbskin.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() != ItemEvent.SELECTED)
					return;
				int idx = cbbskin.getSelectedIndex();
				if (Define.def.STYLE_TYPE == idx) {
					return;
				} else {
					Define.def.STYLE_TYPE = idx;
					ipwarn.setVisible(true);
				}
			}
		});

		btres.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mw.getFrame().dispose();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						try {
							Runtime.getRuntime().exec("launcher");
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				});
			}
		});
	}

	private void loadBinFile() {
		JFileChooser jfc = new JFileChooser();
		jfc.setCurrentDirectory(new File(cm.openfilePath));
		int reval = jfc.showOpenDialog(UtilityPane.this);
		if (reval == JFileChooser.APPROVE_OPTION) {
			File f = jfc.getSelectedFile();
			if (f == null) {
				return;
			}
			DataHouse dh = Platform.getDataHouse();
			boolean success = cm.binIn.openfile(dh, f);
			if (success) {
				cm.openfilePath = FileUtil.getFileCanonicalPath(jfc
						.getCurrentDirectory());
			} else {
				if (!cm.isRuntime()) {
					String mes = I18nProvider.bundle().getString(
							"M.Utility.LoadInvalid");
					String title = I18nProvider.bundle().getString(
							"M.Utility.Name");
					int rsp = JOptionPane.showConfirmDialog(Platform
							.getMainWindow().getFrame(), mes, title,
							JOptionPane.YES_NO_OPTION,
							JOptionPane.ERROR_MESSAGE);
					if (rsp == JOptionPane.YES_OPTION) {
						loadBinFile();
					}
				}
			}
		}
	}

	public void factorySet() {
		mw.getToolPane().getButtonPane().factorySet();
	}

	private boolean listening = false;

	protected void executeLocalize(Config conf) {
		int idx = cbblocale.getSelectedIndex();
		Platform.getDataHouse().adjustLocale_LocalizeWindow(conf, idx);
	}

	@Override
	public void localize(ResourceBundle rb) {
		/** 父类会localize其它控件 */
		if (syoccb != null)
			syoccb.repaint();

		cbbexport.repaint();
		super.localize(rb);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String n = evt.getPropertyName();
		if (n.equals(PropertiesItem.PAU_EXP_BTN_UPDATE)) {
			boolean v = (Boolean) evt.getNewValue();
			pau_expbtn.setEnabled(v);
		} else if (n.equals(PropertiesItem.UPDATE_TXT_LOCALES)) {
			cbblocale.setModel(new DefaultComboBoxModel(cm.getConfig()
					.getLocales().toArray()));
			cbblocale.setSelectedIndex(cm.getConfig().getLocaleIndex());
		} else if (n.equals(PropertiesItem.MACHINETYPE_CHANGE)) {
			if (syoccb != null) {
				listening = false;
				syoccb.setModel(new ListComboBoxModel(cm.getSyncInOuts()));
				listening = true;
			}
		} else if (n.equals(PropertiesItem.SYNCOUTPUTCHANGE)) {
			if (syoccb != null) {
				listening = false;
				syoccb.setSelectedIndex(cm.getSysControl().getSyncOutput());
				syoccb.repaint();
				listening = true;
			}
		}

		// if (n.equalsIgnoreCase(PropertiesItem.AFTER_GOT_DM_DATA)) {
		// boolean v = (Boolean) evt.getNewValue();
		// cm.ewc.lastExportWave(cm.ewc.expOutFile, wfm);
		// }else if (n.equals(PropertiesItem.RSSTATUS_CHANGE)) {
		// // TitlePane.enableBtns(),enableBtns
		// // boolean v = (Boolean) evt.getNewValue();
		// // pau_expbtn.setEnabled(v);
		// } else if (n.equals(ExportWaveControl.AFTER_EXPORT)) {
		// // pau_expbtn.setEnabled(true);
		// // authorizeExport = false;
		// }

	}
}
