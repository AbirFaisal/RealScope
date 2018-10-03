package com.owon.uppersoft.dso.global;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

import com.owon.uppersoft.dso.function.PersistentDisplay;
import com.owon.uppersoft.dso.function.record.OfflineChannelsInfo;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.machine.aspect.IMultiReceiver;
import com.owon.uppersoft.dso.mode.control.SysControl;
import com.owon.uppersoft.dso.model.ChartDecorater;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.core.aspect.Paintable;
import com.owon.uppersoft.vds.core.data.CByteArrayInputStream;
import com.owon.uppersoft.vds.core.paint.ILazy;
import com.owon.uppersoft.vds.core.pref.Config;
import com.owon.uppersoft.vds.core.wf.dm.DMInfo;
import com.owon.uppersoft.vds.core.wf.rt.ChannelsTransportInfo;
import com.owon.uppersoft.vds.ui.resource.FontCenter;

/**
 * DataHouse，database
 * 
 */
public abstract class DataHouse implements Localizable, ILazy, Paintable {
	public static final int Offline_Unload = 0;
	public static final int Offline_Normal = 1;
	public static final int Offline_DM = 2;
	public static final int RT_Normal = 11;
	public static final int RT_DM = 12;
	public static final int RC_Play = 21;
	
	public static double xRate=1,yRate=1;

	public MainWindow getMainWindow() {
		return workBench.getMainWindow();
	}

	public ControlApps getControlApps() {
		return workBench.getControlApps();
	}

	public boolean allowLazyRepaint() {
		return isRuntime() || status == Offline_Unload;
	}

	public boolean isRuntime() {
		return controlManager.isRuntime();
	}

	/**
	 * This value indicates the state of the internal load data,
	 * if the data is not loaded, it remains in the previous state.
	 */
	private int status = Offline_Unload;
	private boolean DataComplete = false;

	/**
	 * The vertical transformation of the waveform inside the screen,
	 * supported after stopping, does not need to take deep storage,
	 * and record the transformation information for loading deep storage.
	 * 
	 * @return
	 */
	public boolean allowTransformScreenWaveFormVertical() {
		return !isRuntime() && (status == RT_Normal)
				&& controlManager.isRecentRunThenStop();
	}

	public boolean isDataComplete() {
		return DataComplete;
	}

	public void dataComplete() {
		DataComplete = true;
	}

	public void dataNotComplete() {
		DataComplete = false;
	}

	public void setStatus(int sta) {
		status = sta;
	}

	public int getStatus() {
		return status;
	}

	public boolean isPlayRecord() {
		return status == RC_Play;
	}

	public boolean isParaAsync() {
		return status == Offline_DM || status == Offline_Normal;
	}

	public boolean isDMLoad() {
		return status == Offline_DM || status == RT_DM;
	}

	public boolean isRTWhenLoad() {
		return status == RT_Normal || status == RT_DM;
	}

	/**
	 * Consider a call in some cases to ensure that the value does not
	 * retain the state of the DM and causes a logic error when it
	 * starts running but does not acquire data.
	 */
	public void admiteRT() {
		status = RT_Normal;
	}

	/**
	 * coordinate：yb - adc
	 * 
	 * @return
	 */
	public int getADC_yb() {
		return controlManager.paintContext.getHcenter();
	}

	private CByteArrayInputStream cba;

	public void setupDeepMemoryStorage(File file) {
		releaseDeepMemoryStorage();
		cba = new CByteArrayInputStream(file);
	}

	/**
	 * Usually the release of the previous load file is released when offline loading
	 * 
	 */
	public void releaseDeepMemoryStorage() {
		if (cba != null) {
			cba.dispose();
		}
	}

	public CByteArrayInputStream getDeepMemoryStorage() {
		return cba;
	}

	private WorkBench workBench;
	/** Waveform management */
	private WaveFormManager waveFormManager;

	public final ControlManager controlManager;
	protected CoreControl coreControl;
	private IMultiReceiver iMultiReceiver;

	public boolean isOptimizeDragCommandSend() {
		return controlManager.sourceManager.isNETConnect();
	}

	public DataHouse(ControlManager cm, WorkBench workBench) {
		this.workBench = workBench;
		controlManager = cm;
		coreControl = cm.getCoreControl();
		cm.paintContext.setLazy(this);

		waveFormManager = createWaveFormManager();
		chartDecorater = new ChartDecorater(cm, workBench, this);
		coreControl.getFFTControl().setWaveFormManager(waveFormManager);
		iMultiReceiver = createMultiReceiver(cm, waveFormManager);

		cm.pcs.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				String pn = evt.getPropertyName();
				if (pn.equals(PropertiesItem.CHARTSCREEN_UPDATESHOW)) {
					getMainWindow().updateShow();
				} else if (pn.equals(PropertiesItem.SYNCOUTPUTCHANGE)) {
					int old = (Integer) evt.getOldValue();
					// int newidx = (Integer) evt.getNewValue();

					if (old == SysControl.SYNOUT_TrgIn) {
						/** Prompt to unplug the external trigger source */
						// FadeIOShell fio = new FadeIOShell();
						// fio.prompt("", getMainWindow().getWindow());
					}
				}
			}
		});

		localize(I18nProvider.bundle());
	}

	protected abstract IMultiReceiver createMultiReceiver(ControlManager cm,
			WaveFormManager wfm);

	protected abstract WaveFormManager createWaveFormManager();

	public void localize(ResourceBundle rb) {
		divUnit = rb.getString("Label.DivUnit");
		if (controlManager.isEnglishLocale())
			divUnits = divUnit + "s";
		else
			divUnits = divUnit;
	}

	public boolean isLineLink() {
		return controlManager.displayControl.linelink;
	}

	@Override
	public void re_paint() {
		getMainWindow().re_paint();
	}

	public void adjustLocale_LocalizeWindow(Config conf, int idx) {
		// Config conf = dh.getControlManager().getConfig();
		conf.setLocaleIndex(idx);
		// Locale lold = Locale.getDefault();
		Locale lnew = conf.getLocales().get(idx).getLocale();
		// String lan = Locale.CHINA.getLanguage();
		ResourceBundle rb = I18nProvider.updateLocale(lnew);
		/** Will refresh each show, including the sidebar */

		FontCenter.updateFont();
		controlManager.getLocalizeCenter().reLocalize(rb);
	}

	/**
	 * @return Workbench
	 */
	public WorkBench getWorkBench() {
		return workBench;
	}

	/**
	 * @return Waveform management
	 */
	public WaveFormManager getWaveFormManager() {
		return waveFormManager;
	}

	/**
	 * This is different from other triggers that are disabled
	 * after entering the slow sweep and needs to be run when
	 * offline deep storage is available.
	 * 
	 * @return
	 */
	public boolean isHorTrgPosFrozen() {
		return (coreControl.isRunMode_slowMove() && isRuntime())
				|| coreControl.getFFTControl().isFFTon();
	}

	public String divUnit, divUnits;

	/**
	 * Loading data offline, currently only recording files with offline_Normal status
	 * 
	 * @param ci
	 */
	public void receiveOfflineNormalData(OfflineChannelsInfo ci) {
		iMultiReceiver.receiveOfflineData(ci, this, Offline_Normal);
		waveFormManager.retainClosedWaveForms();
	}

	public void receiveOfflineVideoData(OfflineChannelsInfo ci) {
		iMultiReceiver.receiveOfflineData(ci, this, RC_Play);
		waveFormManager.retainClosedWaveForms();
	}

	/**
	 * Load deep storage file data offline
	 * 
	 * @param ci
	 */
	public void receiveOfflineDMData(DMInfo ci) {
		iMultiReceiver.receiveOfflineDMData(ci, this);
		waveFormManager.retainClosedWaveForms();
	}

	/**
	 * Loading data at runtime
	 * 
	 * @param ci
	 */
	public void receiveRTData(ChannelsTransportInfo ci) {
		iMultiReceiver.receiveRTData(ci, this);
	}

	/**
	 * Loading deep storage data at runtime
	 *
	 * @param ci
	 */
	public void receiveRTDMData(DMInfo ci) {
		iMultiReceiver.receiveRTDMData(ci, this);
	}

	public ControlManager getControlManager() {
		return controlManager;
	}

	private ChartDecorater chartDecorater;

	public ChartDecorater getGlobalDecorater() {
		return chartDecorater;
	}

	public void resetPersistence() {
		controlManager.resetPersistence();
	}

	public void set3in1(boolean b) {
		chartDecorater.getPersistentDisplay().resetPersistBufferImage();
	}

	public void closePersistence() {
		getPersistentDisplay().destroyFadeThread();
	}

	public PersistentDisplay getPersistentDisplay() {
		return chartDecorater.getPersistentDisplay();
	}

}