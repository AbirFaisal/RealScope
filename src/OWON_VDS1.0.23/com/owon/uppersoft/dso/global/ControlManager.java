package com.owon.uppersoft.dso.global;

import java.awt.Window;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.owon.uppersoft.dso.control.IDataImporter;
import com.owon.uppersoft.dso.data.LObject;
import com.owon.uppersoft.dso.delegate.DefaultRuntime;
import com.owon.uppersoft.dso.function.DisplayControl;
import com.owon.uppersoft.dso.function.DocsManager;
import com.owon.uppersoft.dso.function.ExportWaveControl;
import com.owon.uppersoft.dso.function.FFTCursorControl;
import com.owon.uppersoft.dso.function.MarkCursorControl;
import com.owon.uppersoft.dso.function.Markable;
import com.owon.uppersoft.dso.function.MarkableProvider;
import com.owon.uppersoft.dso.function.PFRuleManager;
import com.owon.uppersoft.dso.function.PersistentDisplay;
import com.owon.uppersoft.dso.function.PlayerControl;
import com.owon.uppersoft.dso.function.RecordControl;
import com.owon.uppersoft.dso.function.ReferenceWaveControl;
import com.owon.uppersoft.dso.function.ReloadManager;
import com.owon.uppersoft.dso.function.SoftwareControl;
import com.owon.uppersoft.dso.function.measure.MeasureManager;
import com.owon.uppersoft.dso.function.measure.MeasureSnapshot;
import com.owon.uppersoft.dso.function.measure.MeasureWFSupport;
import com.owon.uppersoft.dso.function.record.OfflineChannelsInfo;
import com.owon.uppersoft.dso.function.ref.IReferenceWaveForm;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.mode.control.DeepMemoryControl;
import com.owon.uppersoft.dso.mode.control.FFTControl;
import com.owon.uppersoft.dso.mode.control.SampleControl;
import com.owon.uppersoft.dso.mode.control.SysControl;
import com.owon.uppersoft.dso.mode.control.TimeControl;
import com.owon.uppersoft.dso.model.WaveFormInfoControl;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.model.trigger.TrgTypeDefine;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.model.trigger.TriggerSet;
import com.owon.uppersoft.dso.model.trigger.TriggerUIInfo;
import com.owon.uppersoft.dso.pref.Define;
import com.owon.uppersoft.dso.ref.IRefSource;
import com.owon.uppersoft.dso.source.comm.TrgStatus;
import com.owon.uppersoft.dso.source.comm.detect.PromptPlace;
import com.owon.uppersoft.dso.source.manager.SourceManager;
import com.owon.uppersoft.dso.util.DBG;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.dso.view.TitleStatusLabel;
import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.vds.core.aspect.control.ISupportChannelsNumber;
import com.owon.uppersoft.vds.core.aspect.control.ITimeControl;
import com.owon.uppersoft.vds.core.aspect.help.ILoadPersist;
import com.owon.uppersoft.vds.core.comm.IRuntime;
import com.owon.uppersoft.vds.core.comm.effect.Submitable;
import com.owon.uppersoft.vds.core.control.MathControl;
import com.owon.uppersoft.vds.core.machine.MachineInfo;
import com.owon.uppersoft.vds.core.machine.MachineType;
import com.owon.uppersoft.vds.core.measure.MeasureModel;
import com.owon.uppersoft.vds.core.paint.IPaintOne;
import com.owon.uppersoft.vds.core.paint.PaintContext;
import com.owon.uppersoft.vds.core.pref.Config;
import com.owon.uppersoft.vds.core.zoom.AssitControl;
import com.owon.uppersoft.vds.print.PrinterPreviewControl;
import com.owon.uppersoft.vds.socket.server.ServerControl;
import com.owon.uppersoft.vds.ui.prompt.FadeIOShell;
import com.owon.uppersoft.vds.util.LocalizeCenter;
import com.owon.uppersoft.vds.util.Pref;

public abstract class ControlManager implements ISupportChannelsNumber {

	public boolean isRuntime() {
		return ir.isRuntime();
	}

	private DefaultRuntime ir = new DefaultRuntime(this);

	public IRuntime getIRuntime() {
		return ir;
	}

	public PromptPlace createPromptPlace() {
		return new PromptPlace(this);
	}

	public WaveFormInfoControl getWaveFormInfoControl() {
		return cc.getWaveFormInfoControl();
	}

	public TriggerControl getTriggerControl() {
		return cc.getTriggerControl();
	}

	public TimeControl getTimeControl() {
		return cc.getTimeControl();
	}

	public SampleControl getSampleControl() {
		return cc.getSampleControl();
	}

	public DeepMemoryControl getDeepMemoryControl() {
		return cc.getDeepMemoryControl();
	}

	public SysControl getSysControl() {
		return cc.getSysControl();
	}

	public FFTControl getFFTControl() {
		return cc.getFFTControl();
	}

	public boolean isTrgLevelDisable() {
		return cc.isTrgLevelDisable();
	}

	public String getCurrentSampleRate_Text() {
		return cc.getCurrentSampleRate_Text();
	}

	public BigDecimal getCurrentSampleRateBD_kHz() {
		return cc.getCurrentSampleRateBD_kHz();
	}

	public int getAllChannelsNumber() {
		return cc.getAllChannelsNumber();
	}

	public int getSupportChannelsNumber() {
		return cc.getSupportChannelsNumber();
	}

	public MachineInfo getMachineInfo() {
		return cc.getMachineInfo();
	}

	public MachineType getMachine() {
		return cc.getMachine();
	}

	public int getMachineTypeForSave() {
		return cc.getMachineTypeForSave();
	}

	public CoreControl getCoreControl() {
		return cc;
	}

	public Principle getPrinciple() {
		return principle;
	}

	public boolean isSuitableMachineType(int newLoadID) {
		int id = getMachineTypeForSave();
		return id == newLoadID;
	}

	public boolean isSameSeries(int id) {
		return principle.isSameSeries(id);
	}

	public boolean isBandLimit() {
		String machine = getMachine().name();
		if (machine.startsWith("VDS2062"))
			return sc.isVDS2062Bandlimit();
		else if (machine.startsWith("VDS2064"))
			return sc.isVDS2064Bandlimit();
		else
			return getMachine().bandLimit();
	}

	public String getMachineTypeName(int id) {
		return principle.getMachineTypeName(id);
	}

	public AssitControl getZoomAssctr() {
		return zoomAssctr;
	}

	public OfflineChannelsInfo getPlayChannelsInfo() {
		return playCtrl.getPlayChannelsInfo();
	}

	public boolean isChineseLocale() {
		return Locale.getDefault().getLanguage()
				.equals(Locale.CHINESE.getLanguage());
	}

	public boolean isEnglishLocale() {
		return Locale.getDefault().getLanguage()
				.equals(Locale.ENGLISH.getLanguage());
	}

	public abstract DockControl getDockControl();

	public final MathControl mathControl;
	public final SourceManager sourceManager;
	public final DisplayControl displayControl;
	public final MarkCursorControl mcctr;
	public final FFTCursorControl fftctr;
	public final PrinterPreviewControl ppc;

	public ReferenceWaveControl rwc;

	public MeasureModel measMod;
	private AssitControl zoomAssctr;
	public ExportWaveControl ewc;
	public SoftwareControl sc;

	public PlayerControl playCtrl;
	public RecordControl rc;

	public PropertyChangeSupport pcs;
	public PFRuleManager ruleManager;
	public ReloadManager reloadManager;
	public DocsManager docManager;
	public ServerControl scpiServer;

	private Config conf;

	private CoreControl cc;

	public int circleSerialPort;
	private Principle principle;
	/** 测量管理 */
	private MeasureManager mm;

	/**
	 * @return 测量管理
	 */
	public MeasureManager getMeasureManager() {
		return mm;
	}

	private long measureTimes;

	public void updateMeasure(MeasureWFSupport wfm) {
		if (!mm.ison())
			return;

		long t1 = System.currentTimeMillis();
		long delta = t1 - measureTimes;
		/** 改变后需要等到下一次赋值才能生效 */
		if (delta > 800) {
			// MainWindow mainWindow = getMainWindow();
			// ToolPane tp = mainWindow.getToolPane();

			// long t2, del;
			measure(wfm);
			measureTimes = t1;

			// t2 = System.currentTimeMillis();
			// del = t2 - t1;
			// System.err.println(del + " ms");

			// measureTimes = 0;// controlManager.measureTimes;
		}
		// else {
		// System.err.println("measureTimes: " + measureTimes);
		// int p = Platform.getControlApps().getDaemon().getPeriod();
		// if (p <= 0)
		// p = 50;
		// System.err.println("period: " + p);
		// measureTimes += p;
		// }
	}

	public void measure(MeasureWFSupport wfm) {
		double freLimit = cc.getMachine().getLimitFrequency();
		boolean mayDelayInvalid = mayDelayInvalid();
		mm.measure(wfm, cc.getTriggerControl(), freLimit, mayDelayInvalid,
				cc.getVoltageProvider(), measMod,
				cc.getSupportChannelsNumber(), cc);
	}

	private LocalizeCenter lc = new LocalizeCenter();

	public LocalizeCenter getLocalizeCenter() {
		return lc;
	}

	public String getDivUnit() {
		return lc.getCacheText(LocalizeCenter.LABEL_DIV_UNIT);
	}

	public String getDivUnits() {
		return lc.getCacheText(LocalizeCenter.LABEL_DIV_UNITS);
	}

	public ControlManager(Config conf, final Principle principle,
			final CoreControl cc) {
		this.conf = conf;
		this.principle = principle;

		binIn = geBinaryFileImporter();

		this.cc = cc;

		pcs = cc.getPropertyChangeSupport();

		sc = new SoftwareControl(this);
		Pref p = conf.getSessionProperties();
		int channelsNumber = getSupportChannelsNumber();// conf.getChannelsNumber();

		Define.prepare(p.loadInt("STYLE_TYPE"));

		// 尽量早设置
		DBG.debug = p.loadBoolean("debug");
		// dbgbtns = p.loadBoolean( "dbgbtns");

		sourceManager = getSourceManager(ir);

		PrinterPreviewControl.setupResourceBundleProvider(I18nProvider
				.getResourceBundleProvider());
		ppc = new PrinterPreviewControl(p);
		//
		measMod = new MeasureModel(channelsNumber, p);

		measureTimes = 0;
		mm = new MeasureManager(pcs, measMod);

		rc = createRecordControl(this, p);
		reloadManager = new ReloadManager(this);

		cc.load(p);

		getDockControl().init(this);

		load(p);
		// DBG.errprintln("lp " + sc.getDataPeroid);

		ruleManager = new PFRuleManager(p, channelsNumber);
		mathControl = new MathControl(p);

		zoomAssctr = new AssitControl(cc.getTimeControl(), p,
				cc.getTimeConfProvider(), pcs);

		cc.updateHorTrgPosRange();
		cc.updateHorTrgIdx4View();

		mcctr = new MarkCursorControl(p, channelsNumber, cc.getTimeControl(),
				pcs, new MarkableProvider() {
					@Override
					public Markable get(int idx) {
						return cc.getWaveFormInfo(idx).ci;
					}
				});
		fftctr = new FFTCursorControl(this, p);
		displayControl = new DisplayControl(p, this);
		rwc = new ReferenceWaveControl(p, cc);
		playCtrl = new PlayerControl(this, p);
		ewc = new ExportWaveControl(this);// 用到pcs
		docManager = new DocsManager(this);
		paintContext = new PaintContext() {
			@Override
			public IPaintOne getIPaintOne() {
				return cc.getMachine().getPaintOne();
			}
		};
		paintContext.setColorProvider(displayControl);

		circleSerialPort = p.loadInt("circleSerialPort");
		// DBG.config("circleSerialPort:"+circleSerialPort);
		if (circleSerialPort < 0)
			circleSerialPort = 0;
		scpiServer = new ServerControl(p);

		// reloadPath = p.getProperty("reloadPath", "");
	}

	public abstract IReferenceWaveForm loadRefWF(File refwavfile);

	public abstract boolean singleVideoAlow(TrgTypeDefine etd, int chl,
			TriggerControl trgc);

	protected abstract SourceManager getSourceManager(IRuntime ir);

	public abstract IDataImporter geBinaryFileImporter();

	public abstract IReferenceWaveForm createReferenceWaveForm(
			IRefSource selchl, WaveFormManager wfm);

	protected RecordControl createRecordControl(ControlManager cm, Pref p) {
		return new RecordControl(cm, p);
	}

	public final IDataImporter binIn;

	public PaintContext paintContext;

	public void fire_RefreshMeasureResult() {
		pcs.firePropertyChange(MeasureManager.RefreshMeasureResult, null, null);
	}

	/**
	 * 初始同步，在任何模式下都发完整信息进行匹配
	 * 
	 * @param bbuf
	 */
	public void initDetail(Submitable sbm) {
		cc.initDetail(sbm);
	}

	/**
	 * 在运行时同步，可在切入fft时只发必要信息
	 * 
	 * @param bbuf
	 */
	public void syncDetail(Submitable sbm) {
		cc.syncDetail(sbm);
	}

	/**
	 * 数据点：min,max,min,max 可在改变采样率时判断，减少测试次数，但测试内容比较简单，这里就不优化了
	 * 
	 * 但是界面下方的采样率显示区不需要针对打开峰值检测而更改为实际的采样率
	 * 
	 * @return 峰值检测实际是否开启
	 */
	public boolean isPeakDetectWork() {
		return cc.isPeakDetectWork();
	}

	public void persist(Pref p) {
		cc.persist(p);
		ruleManager.persist(p);
		mathControl.persist(p);
		displayControl.persist(p);

		for (ILoadPersist lp : lpList) {
			lp.persist(p);
		}

		p.persistInt("STYLE_TYPE", Define.def.STYLE_TYPE);
		mcctr.persist(p);
		fftctr.persist(p);
		ppc.persist(p);
		measMod.persist(p);
		rc.persist(p);
		getZoomAssctr().persist(p);
		rwc.persist(p);
		playCtrl.persist(p);
		reloadManager.persist(p);
		scpiServer.persist(p);

		if (getZoomAssctr().isonZoom()) {
			p.persistInt(ITimeControl.TIMEBASE_INDEX, getZoomAssctr().mtbIdx);
			p.persistInt(ITimeControl.HOR_TRG_POS, getZoomAssctr().mhtp);
		}

		// p.setProperty("reloadPath", reloadManager.reloadPath);
		// p
		// .persistInt( "reloadStatus", reloadManager.ReloadStatus);

		p.persistBoolean("debug", DBG.debug);
		// p.persistBoolean( "dbgbtns", dbgbtns);
		p.persistInt(PropertiesItem.LOGTYPE, DBG.cmddebug);

		p.persistInt("ComputeFreqTimes", computeFreqTimes);
		// p.persistInt( "MeasureTimes", measureTimes2);
		p.persistInt("maxFailureTime", maxFailureTime);

		p.setProperty("OpenfilePath", openfilePath);
		p.setProperty("SaveimgPath", saveimgPath);
		p.setProperty("ExportPath", exportPath);
		p.persistBoolean("TipsWindowShow", istipsWindowShow);
	}

	protected void load(Pref p) {
		for (ILoadPersist lp : lpList) {
			// System.out.println(lp);
			lp.load(p);
		}

		reloadManager.load(p);
		// ReloadStatus = p.loadInt( "reloadStatus");

		computeFreqTimes = p.loadInt("ComputeFreqTimes");
		if (computeFreqTimes <= 0)
			computeFreqTimes = 5;

		// measureTimes2 = p.loadInt( "MeasureTimes");
		// if (measureTimes2 <= 0)
		// measureTimes2 = 20;

		maxFailureTime = p.loadInt("maxFailureTime");
		if (maxFailureTime < 0)
			maxFailureTime = 20;

		openfilePath = p.getProperty("OpenfilePath", "");
		saveimgPath = p.getProperty("SaveimgPath", "");
		exportPath = p.getProperty("ExportPath", "");
		istipsWindowShow = p.loadBoolean("TipsWindowShow");
	}

	public int computeFreqTimes;
	// public int measureTimes2 = 5;
	public int measureSnapshotIdx = 0;
	public int maxFailureTime = 20;

	/** 此部分为软件内部使用的变量 */
	public String openfilePath, saveimgPath, exportPath;
	public boolean istipsWindowShow;

	// public static boolean dbgbtns = false;

	public void changeKeepget(boolean kg) {
		fireRecord_PlayUpdate(kg);
		pcs.firePropertyChange(PropertiesItem.KEEPGET, !kg, kg);

		WaveFormManager wfm = Platform.getDataHouse().getWaveFormManager();
		wfm.resetVbmulti();
		if (!kg)
			wfm.saveFirstLoadPos0();
	}

	private void fireRecord_PlayUpdate(boolean rt) {
		boolean stop = !rt;
		boolean rr = rc.isRecording(), pr = playCtrl.isPlaying();
		if (rr && stop) {
			// 录制时停止要数据，因录制在原线程的获取数据之后，而数据不再获取，这里快速模拟一次结束录制关闭文件
			rc.forceStop();
		}
		if (pr && rt) {
			// 回放时开始要数据，则简单关闭回复即可
		}
	}

	public List<ILoadPersist> lpList = new LinkedList<ILoadPersist>();

	public void factorySet() {
		Pref p = conf.getFactoryProperties();
		cc.factoryset(p);
		load(p);

		int channelsNumber = getSupportChannelsNumber();
		ruleManager.load(p, channelsNumber);

		getZoomAssctr().factoryload(p);

		// updateHorTrgRange();
		cc.updateHorTrgIdx4View();

		mathControl.load(p);

		fftctr.load(p);
		displayControl.loadFactorySet(p);

		rwc.factoryload(p);
		mcctr.load(p, channelsNumber);
		rc.load(p);

		Platform.getDataHouse().getPersistentDisplay()
				.fadeThdOn_Off_UI(displayControl.getPersistenceIndex());
		MainWindow mw = Platform.getMainWindow();
		MeasureSnapshot.closeMeasureSnapshot();
		fireFFTonoff2EnableMainWindow();
		mw.updateDefaultAll();
		/** 发送到下位机 */
		Platform.getControlApps().interComm.syncFactorySet2Machine();
		// fire_RefreshMeasureResult();
	}

	public void fireFFTonoff2EnableMainWindow() {
		boolean on = getFFTControl().isFFTon();
		pcs.firePropertyChange(on ? PropertiesItem.FFT_ON
				: PropertiesItem.FFT_OFF, null, null);
	}

	public void reduceFrame() {
		DataHouse dh = Platform.getDataHouse();
		if (ir.isRuntime())
			return;
		dh.getWaveFormManager().reduceFrame();
		Platform.getMainWindow().updateShow();
	}

	public void onRelease(File confIni) {
		ir.setExit();
		conf.persist(confIni, new ILoadPersist() {

			@Override
			public void persist(Pref p) {
				ControlManager.this.persist(p);
			}

			@Override
			public void load(Pref p) {
			}
		});
	}

	private boolean pau_expEnable = true;
	private boolean duringDMFtech = false;

	public boolean isPau_expEnable() {
		return pau_expEnable;
	}

	public void updateExportbtnEnable(boolean able) {
		pcs.firePropertyChange(PropertiesItem.PAU_EXP_BTN_UPDATE, null, able);
		pau_expEnable = able;
	}

	public void duringDMFetch(boolean b) {
		duringDMFtech = b;
		pcs.firePropertyChange(PropertiesItem.DURINGDMFETCH, null, b);
	}

	public boolean isDuringDMFtech() {
		return duringDMFtech;
	}

	public SoftwareControl getSoftwareControl() {
		return sc;
	}

	public boolean mayDelayInvalid() {
		return cc.mayDelayInvalid();
	}

	// public boolean shouldSkipBeforeFrames() {
	// return ruleManager.isChecking() || cc.shouldSkipBeforeFrames();
	// }

	/**
	 * @return 配置
	 */
	public Config getConfig() {
		return conf;
	}

	public boolean isXYModeSupport() {
		return getAllChannelsNumber() > 1;
	}

	/**
	 * 根据运行时的情况来使用频率计
	 * 
	 * @return
	 */
	public String getFreqLabel(ChannelInfo ci) {
		boolean b = ci.getFreqtxt() == null || !ci.isOn() || ci.isGround();
		if (b)
			return "?";
		TriggerControl trgc = getTriggerControl();
		TriggerSet ts = trgc.getTriggerSetOrNull(ci.getNumber());
		if (ts != null) {
			if (ts.getTrigger() == ts.video)
				return "?";
		}

		if (trgc.isSingleTrg()) {
			TriggerUIInfo tui = trgc.getTriggerUIInfo();
			if (tui.getCurrentChannel() != ci.getNumber()) {
				return "?";
			}
		}

		b = !TrgStatus.isFreqShowableOnTrgStatus(curTrgStatus);
		if (b)
			return "<2Hz";

		return ci.getFreqtxt();
	}

	/** 运行实时的触发状态 */
	public int curTrgStatus;

	public List<LObject> getSyncInOuts() {
		return SysControl.syncs;
	}

	public boolean isExit() {
		return ir.isExit();
	}

	public boolean isKeepGet() {
		return ir.isKeepGet();
	}

	public boolean isRuntimeStop() {
		return ir.isRuntimeStop();
	}

	public boolean isRecentRunThenStop() {
		return ir.isRecentRunThenStop();
	}

	public boolean isTimeOperatable() {
		if (getFFTControl().isFFTon() || getZoomAssctr().isonAssistSet())
			return false;
		return isRuntime() || isRuntimeStop() || !sourceManager.isConnected();
	}

	public boolean is50percentAvailable() {
		return isRuntime() || isRecentRunThenStop();
	}

	/**
	 * 屏幕内波形的变换，原来是非运行时，后来关闭了，再后来开启在运行时正常触发情况下
	 * 
	 * @return
	 */
	public boolean allowTransformScreenWaveForm() {
		TriggerControl tc = getTriggerControl();
		return isRuntime() && (tc.isSweepNormal() || tc.isSweepOnce());
	}

	/**
	 * 屏幕内波形的变换，原来是非运行时，后来关闭了，再后来开启在运行时正常触发情况下
	 * 
	 * @return
	 */
	public boolean allowTransformScreenWaveForm_Ready() {
		return allowTransformScreenWaveForm();
		// && (Platform.getMainWindow().getTitlePane().getTrgStatus() ==
		// TrgStatus.Ready);
	}

	public void resetPersistence() {
		pcs.firePropertyChange(PersistentDisplay.PERSISTENCE_RESET, null, null);
	}

	/**
	 * 用于在指令更新后添加清空余辉，后续不放在Submitor里调用
	 * 
	 * @return
	 */
	public Runnable getResetPersistenceRunnable() {
		return new Runnable() {
			@Override
			public void run() {
				resetPersistence();
			}
		};
	}

	public void notifyShouldEnableTrg() {
		getTimeControl().setShouldEnableTrgByJudgeIsSlowMove();
		String propertyName = getTriggerControl().isTrgEnable() ? PropertiesItem.SWITCH_NormalMOVE
				: PropertiesItem.SWITCH_SLOWMOVE;
		pcs.firePropertyChange(propertyName, null, null);
	}

	public void updateChannelVoltValueEverywhere(int chl) {
		ChannelInfo ci = getWaveFormInfoControl().getWaveFormChannelInfo(chl);
		measure(Platform.getDataHouse().getWaveFormManager());// updatevaluepane
		mcctr.computeYValues(ci.getPos0(), ci.getVoltValue());// updateMarkMeasue
		fire_RefreshMeasureResult();// updateValuePane
		// updateInfoBlocks
		// int chl = ci.getNumber();
		// mainWindow.getToolPane().getInfoPane().updateVolt(chl);
		pcs.firePropertyChange(PropertiesItem.UPDATE_CHLVOLT, null, chl);
		// chartScreen.re_paint
		// updateTriggerInofPane
		// fireTriggerPane
		pcs.firePropertyChange(PropertiesItem.REPAINT_CHARTSCREEN, null, null);
		pcs.firePropertyChange(PropertiesItem.UPDATE_VOLTSENSE, -1, chl);
	}

	public boolean shouldAdjustHorTrgPos() {
		Window parent = Platform.getMainWindow().getWindow();
		if (getZoomAssctr().isonAssistSet()) {
			FadeIOShell pv = new FadeIOShell();
			pv.prompt(I18nProvider.bundle().getString("M.Zoom.Warn"), parent);
			return false;
		}

		boolean operatable = Platform.getControlApps().interComm
				.isTimeOperatableNTryGetDM();// 如果连接暂停，调动htp,波形将变动，需要载入DM
		if (!operatable)
			return false;

		/** 冻结在后，否则无法在停下来后要慢扫的深存储数据 */
		if (Platform.getDataHouse().isHorTrgPosFrozen()) {
			FadeIOShell fs = new FadeIOShell();
			fs.prompt(I18nProvider.bundle().getString("Label.SlowMoveWarn"),
					parent);
			return false;
		}
		return true;

	}

	private TitleStatusLabel statuslbl;

	public void bufferTitleStatus(TitleStatusLabel statuslbl) {
		this.statuslbl = statuslbl;
	}

	public String getTitleStatusLabel() {
		if (statuslbl != null)
			return statuslbl.getText();
		return "?";
	}
}