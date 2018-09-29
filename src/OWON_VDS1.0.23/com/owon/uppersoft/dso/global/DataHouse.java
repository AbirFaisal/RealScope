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
 * DataHouse，数据仓库
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
		return wb.getMainWindow();
	}

	public ControlApps getControlApps() {
		return wb.getControlApps();
	}

	public boolean allowLazyRepaint() {
		return isRuntime() || status == Offline_Unload;
	}

	private boolean isRuntime() {
		return controlManager.isRuntime();
	}

	/**
	 * 该值指示了内部载入数据的状态，如果因为数据无法载入，则仍保持着上一次的状态
	 */
	private int status = Offline_Unload;
	private boolean DataComplete = false;

	/**
	 * 屏幕内波形的垂直变换，在停止后支持，不需要拿深存储，并记录变换信息用于载入深存储使用
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
	 * 考虑在某些情况下调用以保证在刚开始运行但未获取数据时该值不会保留DM的状态而导致逻辑错误
	 */
	public void admiteRT() {
		status = RT_Normal;
	}

	/**
	 * 坐标：yb - adc
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
	 * 一般为离线载入时释放前一次载入文件的占用
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

	private WorkBench wb;
	/** 波形管理 */
	private WaveFormManager wfm;

	public final ControlManager controlManager;
	protected CoreControl cc;
	private IMultiReceiver mr;

	public boolean isOptimizeDragCommandSend() {
		return controlManager.sourceManager.isNETConnect();
	}

	public DataHouse(ControlManager cm, WorkBench wb) {
		this.wb = wb;
		controlManager = cm;
		cc = cm.getCoreControl();
		cm.paintContext.setLazy(this);

		wfm = createWaveFormManager();
		gd = new ChartDecorater(cm, wb, this);
		cc.getFFTControl().setWaveFormManager(wfm);
		mr = createMultiReceiver(cm, wfm);

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
						/** 提示拔掉外部触发信源 */
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
			divUnits = divUnit + 's';
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
		// Config conf = dataHouse.getControlManager().getConfig();
		conf.setLocaleIndex(idx);
		// Locale lold = Locale.getDefault();
		Locale lnew = conf.getLocales().get(idx).getLocale();
		// String lan = Locale.CHINA.getLanguage();
		ResourceBundle rb = I18nProvider.updateLocale(lnew);
		/** 会刷新各个节目，包括侧边栏 */

		FontCenter.updateFont();
		controlManager.getLocalizeCenter().reLocalize(rb);
	}

	/**
	 * @return 工作台
	 */
	public WorkBench getWorkBench() {
		return wb;
	}

	/**
	 * @return 波形管理
	 */
	public WaveFormManager getWaveFormManager() {
		return wfm;
	}

	/**
	 * 这和其它进入慢扫后禁用的触发内容不同，需要运行在离线载入深存储的时候可用
	 * 
	 * @return
	 */
	public boolean isHorTrgPosFrozen() {
		return (cc.isRunMode_slowMove() && isRuntime())
				|| cc.getFFTControl().isFFTon();
	}

	public String divUnit, divUnits;

	/**
	 * 离线载入数据，目前仅录制文件用offline_Normal状态
	 * 
	 * @param ci
	 */
	public void receiveOfflineNormalData(OfflineChannelsInfo ci) {
		mr.receiveOfflineData(ci, this, Offline_Normal);
		getWaveFormManager().retainClosedWaveForms();
	}

	public void receiveOfflineVideoData(OfflineChannelsInfo ci) {
		mr.receiveOfflineData(ci, this, RC_Play);
		getWaveFormManager().retainClosedWaveForms();
	}

	/**
	 * 离线载入深存储文件数据
	 * 
	 * @param ci
	 */
	public void receiveOfflineDMData(DMInfo ci) {
		mr.receiveOfflineDMData(ci, this);
		getWaveFormManager().retainClosedWaveForms();
	}

	/**
	 * 运行时载入数据
	 * 
	 * @param ci
	 */
	public void receiveRTData(ChannelsTransportInfo ci) {
		mr.receiveRTData(ci, this);
	}

	/**
	 * 运行时载入深存储数据
	 * 
	 * @param ci
	 */
	public void receiveRTDMData(DMInfo ci) {
		mr.receiveRTDMData(ci, this);
	}

	public ControlManager getControlManager() {
		return controlManager;
	}

	private ChartDecorater gd;

	public ChartDecorater getGlobalDecorater() {
		return gd;
	}

	public void resetPersistence() {
		controlManager.resetPersistence();
	}

	public void set3in1(boolean b) {
		gd.getPersistentDisplay().resetPersistBufferImage();
	}

	public void closePersistence() {
		getPersistentDisplay().destroyFadeThread();
	}

	public PersistentDisplay getPersistentDisplay() {
		return gd.getPersistentDisplay();
	}

}