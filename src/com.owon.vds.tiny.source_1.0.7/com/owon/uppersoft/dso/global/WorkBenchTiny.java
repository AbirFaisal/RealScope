package com.owon.uppersoft.dso.global;

import java.awt.Dimension;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.owon.uppersoft.dso.machine.aspect.IMultiReceiver;
import com.owon.uppersoft.dso.machine.aspect.IMultiWFManager;
import com.owon.uppersoft.dso.model.WFTimeScopeControl;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.dso.view.TitlePane;
import com.owon.uppersoft.dso.view.ToolPane;
import com.owon.uppersoft.dso.view.sub.ButtonPane;
import com.owon.uppersoft.dso.wf.common.MultiReceiver;
import com.owon.uppersoft.dso.wf.common.MultiWFManager;
import com.owon.uppersoft.vds.core.pref.Config;
import com.owon.uppersoft.vds.machine.PrincipleTiny;
import com.owon.uppersoft.vds.machine.TinyMachine;
import com.owon.uppersoft.vds.ui.resource.FontCenter;
import com.owon.uppersoft.vds.ui.resource.SwingResourceManager;
import com.owon.uppersoft.vds.util.ui.UIUtil;
import com.owon.vds.tiny.firm.pref.model.Register;
import com.owon.vds.tiny.tune.TinyTuneFunction;

/**
 * 工作台，连接各个主要功能模块
 * 
 * 在通往android的vds_tiny的重构移植过程中，涉及如何架构和迭代，终端的手机调试存在的问题，让我们选择了基于一个基础进行更改，方便调试和移植，
 * 但这需要对原有项目的改动在可控的，不影响原有发布周期的情况下；需要不断找到更全面的理解和清晰的构架，再去重构对应的子模块
 * 
 * 模型一如已经想过的更改，然后从最末端开始，从ui控制和扩展插件开始
 */
public class WorkBenchTiny implements WorkBench {

	protected MainWindow mw;
	protected DataHouse dh;
	protected ControlApps ca;

	private Thread ui;

	public void join() {
		try {
			ui.join();
			System.out.println("out_ui.join");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public ControlApps getControlApps() {
		return ca;
	}

	public MainWindow getMainWindow() {
		return mw;
	}

	@Override
	public DataHouse getDataHouse() {
		return dh;
	}

	public ControlManager getControlManager() {
		return ctrlMgr;
	}

	protected ControlManager ctrlMgr;

	private File confIni;
	private CoreControl cc;

	public WorkBenchTiny() {
		confIni = getConfigInIFile();
		Config conf = ConfigFactoryTiny.createConfig(confIni,
				getDefaultInIStream(), getStaticPrefStream());

		Properties p = conf.getSessionProperties();
		// channelsNumber = conf.getChannelsNumber();

		/** 装入机型类别 */
		String productParam = p.getProperty("productParam", "VDS3102ONE")
				.toUpperCase();

		final PrincipleTiny pt = new PrincipleTiny(productParam);

		cc = new CoreControlTiny(conf, pt);
		ctrlMgr = new ControlManagerTiny(conf, pt, cc);

		/** dm先于mf，但可以把初始化工作延后 */
		dh = new DataHouse(ctrlMgr, this) {
			@Override
			public IMultiReceiver createMultiReceiver(ControlManager cm,
					WaveFormManager wfm) {
				return new MultiReceiver(cm, wfm);
			}

			@Override
			protected WaveFormManager createWaveFormManager() {
				return new WaveFormManager(this) {
					@Override
					public IMultiWFManager createMultiWFManager(
							ControlManager cm) {
						return new MultiWFManager(cm);
					}

					@Override
					protected WFTimeScopeControl createWFTimeScopeControl() {
						return new WFTimeScopeControl(cm) {
							@Override
							protected int getPKType(boolean dm, int drawMode) {
								return ((TinyMachine) (cm.getMachine()))
										.getPKType(drawMode);
							}
						};
					}
				};
			}
		};
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					showMainFrm(pt);
					ui = Thread.currentThread();
				}
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		// System.out.println(mw);
		ca = new ControlAppsTiny(dh, mw);
		conf.releaseSession();
	}

	private void showMainFrm(final PrincipleTiny pt) {
		UIUtil.modifyui();
		FontCenter.updateFont();
		mw = new MainWindow(this, dh) {

			@Override
			protected ToolPane createToolPane(Dimension sz, ControlManager cm) {
				return new ToolPane(this, sz, cm) {
					@Override
					protected ButtonPane createButtonPane(MainWindow mw,
							ControlManager cm) {
						return new ButtonPane(mw, cm, dh) {
							@Override
							public void quickPatch() {
								TinyTuneFunction tune = pt.getTuneFunction();
								Register reg = tune.getTuneModel()
										.getRegister();
								reg.enableLanguage("pt_BR");
								tune.getTinyTuneDelegate().writeFactoryNSync();

								String message = "Patch Done! Notice: this software is only for PATCH VDS1022 but not an application of software for VDS";
								String title = "";
								JOptionPane.showMessageDialog(Platform
										.getMainWindow().getWindow(), message,
										title, JOptionPane.INFORMATION_MESSAGE);
							}
						};
					}
				};
			}
		};
		mw.show();
	}

	public void exit() {
		ctrlMgr.onRelease(confIni);
		ca.onExit();
	}

	public static final String SCOPEINFOR = "/com/owon/uppersoft/dso/pref/scopeInfo.ini";
	public static final String PRODUCT_URL_NAME = "com.owon.uppersoft.dso";

	public static final String CONFIGURATION_DIR = "configuration"
			+ File.separator + PRODUCT_URL_NAME;

	public static final String PREFERENCE_FILENAME = "pref.properties";
	public static final String sp = "/com/owon/uppersoft/dso/pref/default.ini";

	public InputStream getDefaultInIStream() {
		return WorkBenchTiny.class.getResourceAsStream(SCOPEINFOR);
	}

	public InputStream getStaticPrefStream() {
		return WorkBenchTiny.class.getResourceAsStream(sp);
	}

	public File getConfigInIFile() {
		File confDir = new File(CONFIGURATION_DIR);
		confDir.mkdirs();
		File file = new File(confDir, PREFERENCE_FILENAME);
		return file;
	}

}
