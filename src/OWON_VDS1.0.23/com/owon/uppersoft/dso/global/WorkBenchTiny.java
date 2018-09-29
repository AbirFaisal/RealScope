package com.owon.uppersoft.dso.global;

import java.awt.Dimension;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.owon.uppersoft.dso.machine.aspect.IMultiReceiver;
import com.owon.uppersoft.dso.machine.aspect.IMultiWFManager;
import com.owon.uppersoft.dso.model.WFTimeScopeControl;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.dso.view.ToolPane;
import com.owon.uppersoft.dso.view.sub.ButtonPane;
import com.owon.uppersoft.dso.wf.common.MultiReceiver;
import com.owon.uppersoft.dso.wf.common.MultiWFManager;
import com.owon.uppersoft.vds.core.pref.Config;
import com.owon.uppersoft.vds.machine.PrincipleTiny;
import com.owon.uppersoft.vds.machine.TinyMachine;
import com.owon.uppersoft.vds.ui.resource.FontCenter;
import com.owon.uppersoft.vds.util.ui.UIUtil;
import com.owon.vds.tiny.firm.pref.model.Register;
import com.owon.vds.tiny.tune.TinyTuneFunction;

/**
 * Workbench, connecting all major functional modules
 * <p>
 * In the refactoring migration process of vds_tiny to android, how to structure and iterate,
 * the problem of mobile phone debugging of the terminal, let us choose to make changes based on a basis,
 * convenient for debugging and porting. However, this requires changes to the original project to be
 * controllable, without affecting the original release cycle; it is necessary to constantly find a more
 * comprehensive understanding and clear framework, and then reconstruct the corresponding sub-module
 * <p>
 * The model is as expected, and then from the very beginning, starting with the userInterfaceThread control and extensions
 */
public class WorkBenchTiny implements WorkBench {

	public static final String SCOPEINFOR = "/com/owon/uppersoft/dso/pref/scopeInfo.ini";
	public static final String PRODUCT_URL_NAME = "com.owon.uppersoft.dso";
	public static final String CONFIGURATION_DIR = "configuration";
	public static final String PREFERENCE_FILENAME = "prefrences.properties";
	public static final String defaultSystemPrefrences = "/com/owon/uppersoft/dso/pref/default.ini";
	protected MainWindow mainWindow;
	protected DataHouse dataHouse;
	protected ControlApps controlApps;
	protected ControlManager controlManager;
	private Thread userInterfaceThread;
	private File confIniFile;
	private CoreControl coreControl;

	public WorkBenchTiny() {
		confIniFile = getConfigInIFile();
		Config conf = ConfigFactoryTiny.createConfig(confIniFile,
				getDefaultInIStream(), getStaticPrefStream());

		Properties p = conf.getSessionProperties();
		// channelsNumber = conf.getChannelsNumber();

		/** 装入机型类别 */
		String productParam = p.getProperty("productParam", "VDS3102ONE")
				.toUpperCase();

		final PrincipleTiny pt = new PrincipleTiny(productParam);

		coreControl = new CoreControlTiny(conf, pt);
		controlManager = new ControlManagerTiny(conf, pt, coreControl);

		/** dm先于mf，但可以把初始化工作延后 */
		dataHouse = new DataHouse(controlManager, this) {
			@Override
			public IMultiReceiver createMultiReceiver(ControlManager cm, WaveFormManager wfm) {
				return new MultiReceiver(cm, wfm);
			}

			@Override
			protected WaveFormManager createWaveFormManager() {

				return new WaveFormManager(this) {
					@Override
					public IMultiWFManager createMultiWFManager(ControlManager cm) {
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
					userInterfaceThread = Thread.currentThread();
				}
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		// System.out.println(mainWindow);
		controlApps = new ControlAppsTiny(dataHouse, mainWindow);
		conf.releaseSession();
	}

	public void join() {
		try {
			userInterfaceThread.join();
			System.out.println("out_ui.join");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public ControlApps getControlApps() {
		return controlApps;
	}

	public MainWindow getMainWindow() {
		return mainWindow;
	}

	@Override
	public DataHouse getDataHouse() {
		return dataHouse;
	}

	public ControlManager getControlManager() {
		return controlManager;
	}

	private void showMainFrm(final PrincipleTiny pt) {
		UIUtil.modifyui();
		FontCenter.updateFont();
		mainWindow = new MainWindow(this, dataHouse) {

			@Override
			protected ToolPane createToolPane(Dimension sz, ControlManager cm) {
				return new ToolPane(this, sz, cm) {
					@Override
					protected ButtonPane createButtonPane(MainWindow mw,
					                                      ControlManager cm) {
						return new ButtonPane(mw, cm, dataHouse) {
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
		mainWindow.show();
	}

	public void exit() {
		controlManager.onRelease(confIniFile);
		controlApps.onExit();
	}

	public InputStream getDefaultInIStream() {
		return WorkBenchTiny.class.getResourceAsStream(SCOPEINFOR);
	}

	public InputStream getStaticPrefStream() {
		return WorkBenchTiny.class.getResourceAsStream(defaultSystemPrefrences);
	}

	public File getConfigInIFile() {
		File confDir = new File(CONFIGURATION_DIR);
		confDir.mkdirs();
		File file = new File(confDir, PREFERENCE_FILENAME);
		return file;
	}

}
