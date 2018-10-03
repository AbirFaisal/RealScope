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
 * 
 * In the refactoring migration process of vds_tiny to android, how to structure and iterate,
 * the problem of terminal mobile phone debugging, let us choose to make changes based on a basis,
 * convenient for debugging and porting, but this requires the original project The changes are controllable and do not affect the original release cycle; you need to constantly find a more comprehensive understanding and clear framework, and then reconstruct the corresponding sub-modules
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

		/** Load model category */
		String productParam = p.getProperty("productParam", "VDS3102ONE")
				.toUpperCase();

		final PrincipleTiny pt = new PrincipleTiny(productParam);

		cc = new CoreControlTiny(conf, pt);
		ctrlMgr = new ControlManagerTiny(conf, pt, cc);

		/** Dm precedes mf, but can delay initialization */
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
						return new WFTimeScopeControl(controlManager) {
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

		UIUtil.modifyui(); //TODO Get rid of this

		FontCenter.updateFont();
		mw = new MainWindow(this, dh) {

			@Override
			protected ToolPane createToolPane(Dimension sz, ControlManager cm) {
				return new ToolPane(this, sz, cm) {
					@Override
					protected ButtonPane createButtonPane(MainWindow mw, ControlManager cm) {
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
