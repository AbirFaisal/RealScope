package com.owon.uppersoft.dso.source.comm;

import static com.owon.uppersoft.dso.util.PropertiesItem.APPLY_CHANNELS;
import static com.owon.uppersoft.dso.util.PropertiesItem.APPLY_DEEPMEMORY;
import static com.owon.uppersoft.dso.util.PropertiesItem.APPLY_SAMPLING;
import static com.owon.uppersoft.dso.util.PropertiesItem.APPLY_TIMEBASE;
import static com.owon.uppersoft.dso.util.PropertiesItem.APPLY_TRIGGER;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ResourceBundle;

import com.owon.uppersoft.dso.data.AbsDataSaver;
import com.owon.uppersoft.dso.global.ControlApps;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.CoreControl;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.mode.control.SysControl;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.source.comm.effect.SubmitorFactory;
import com.owon.uppersoft.dso.util.DBG;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.dso.view.ToolPane;
import com.owon.uppersoft.vds.core.aspect.control.ITimeControl;
import com.owon.uppersoft.vds.core.comm.effect.Submitable;
import com.owon.uppersoft.vds.core.sample.SampleRate;
import com.owon.uppersoft.vds.ui.window.ProgressIndeterminateDialog;
import com.owon.uppersoft.vds.util.FileUtil;

public abstract class AbsInterCommunicator {
	protected DataHouse dh;
	protected MainWindow mw;
	protected ControlApps ca;
	protected ControlManager cm;
	protected CoreControl cc;

	public final static File dmf = new File("tmp", "dm.bin");
	public final static File tmp_dmf = new File("tmp", "temp.bin");
	
	public AbsInterCommunicator(DataHouse dh, MainWindow mw, ControlApps ca) {
		this.dh = dh;
		this.mw = mw;
		this.ca = ca;
		cm = dh.controlManager;
		cc = cm.getCoreControl();
	}

	public ControlManager getControlManager() {
		return cm;
	}

	public void prepare2PersistDMData() {
		dh.releaseDeepMemoryStorage();

		FileUtil.checkPath(dmf);
		/** KNOW 删除旧dmf:若新dmf比原来的内容少，新dmf尾部会有原来dmf的遗留。 */
		dmf.delete();
		if (dmf.exists())
			DBG.errprintln("dmf.exists");
	}

	public abstract void queryALL();

	public abstract void onExport_get();

	public abstract void rebootMachine();

	protected abstract void doSyncFactorySet2Machine();

	/**
	 * 操作时唤起
	 * 
	 * @return 是否支持时间轴的操作(如tb, htp)，并在合适的时候获取深存储
	 */
	public abstract boolean isTimeOperatableNTryGetDM();

	public abstract boolean initMachine(
			PropertyChangeListener propertyChangeListener);

	// protected abstract void doSelfCorrect(ProgressObserver sd);

	public void afterkeepload() {
		// if (!ca.isDMDataGotAlready())
		// persistDMData();
	}

	protected abstract void invokeLater_Autoset(Runnable doneJob);

	protected void dbg(String n) {
		DBG.dbg(n);
	}

	protected void dbgln(String n) {
		DBG.dbgln(n);
	}

	public void syncDetail() {
		syncDetail(null);
	}

	public void syncDetail(Runnable r) {
		Submitable sbm = SubmitorFactory.reInit();
		cm.syncDetail(sbm);
		sbm.applyThen(r);
	}

	public void initDetail() {
		Submitable sbm = SubmitorFactory.reInit();
		cm.initDetail(sbm);
		sbm.apply();
	}

	public void syncFactorySet2Machine() {
		// 判断并让它运行起来,再厂家设置
		if (cm.isRuntimeStop()) {
			statusRun(false, true);
		}

		doSyncFactorySet2Machine();
	}

	public void statusStop(boolean dm) {

		cm.pcs.firePropertyChange(PropertiesItem.OPERATE_STOP, null, null);

		SubmitorFactory.getSubmitable().sendStopThen(new Runnable() {
			@Override
			public void run() {
				mw.updateStatus(TrgStatus.Stop);
				setPreStop(true);
				cm.getDeepMemoryControl().applyFPGADeepIdx();
			}
		});
	}

	public void statusRun(boolean syncChannel, boolean resumeAuto) {
		cm.pcs.firePropertyChange(PropertiesItem.OPERATE_RUN, null, null);

		TriggerControl trgc = cc.getTriggerControl();
		if (resumeAuto && trgc.resumeAuto()) {
			cm.pcs.firePropertyChange(PropertiesItem.T_SweepOnce2Auto, null,
					null);
		}

		ca.setDMDataGotAlready(false);
		// /** 开始运行，设定成不能导出波形 */
		// controlManager.pcs.firePropertyChange(PropertiesItem.AFTER_GOT_DM_DATA, null,
		// false);
		SubmitorFactory.getSubmitable().sendRun();
		if (syncChannel) {
			syncChannels();
		}
		ca.keepload();
	}

	protected void syncChannels() {
		Submitable sbm = SubmitorFactory.reInit();
		cc.getWaveFormInfoControl().selfSubmit(sbm);
		sbm.apply();
	}

	/** pre stop flag */
	private boolean preStop = false;

	public void setPreStop(boolean preStop) {
		this.preStop = preStop;
	}

	public boolean isPreStop() {
		return preStop;
	}

	/**
	 * @param pf
	 *            1通过，0失败
	 */
	public void sync_pf(int pf) {
		final SysControl sys = cc.getSysControl();
		if (sys.getSyncOutput() != SysControl.SYNOUT_PF)
			return;

		SubmitorFactory.getSubmitable().sync_pf(pf);
	}

	private void updateForAutoSet() {
		PropertyChangeSupport pcs = cm.pcs;
		pcs.firePropertyChange(APPLY_SAMPLING, null, null);

		cm.resetPersistence();

		ToolPane tp = mw.getToolPane();
		pcs.firePropertyChange(APPLY_TRIGGER, null, null);
		tp.updateTrgVolt();

		pcs.firePropertyChange(APPLY_CHANNELS, null, null);
		tp.updateChannels();

		/** 下面两种只是预留的KVO */
		pcs.firePropertyChange(APPLY_TIMEBASE, null, null);
		pcs.firePropertyChange(APPLY_DEEPMEMORY, null, null);
		pcs.firePropertyChange(ITimeControl.onTimebaseUpdated, null, null);

		/** 自动设置完成后也要相应更新采样率，并更新面板 */
		cc.updateCurrentSampleRate();
		pcs.firePropertyChange(SampleRate.sampleRateUpdated, null, null);
		cm.notifyShouldEnableTrg();
		cm.updateExportbtnEnable(true);

		mw.updateShow();
		mw.re_paint();
	}

	private boolean isAutosetting = false;

	public boolean autoset() {
		if (isAutosetting)
			return false;
		if (!cm.sourceManager.isConnected())
			return false;

		isAutosetting = true;
		ca.getOperateBlocker().block();
		cm.pcs.firePropertyChange(PropertiesItem.START_AUTOSET, null, null);

		if (cm.isRuntimeStop()) {
			statusRun(false, true);
		}

		// 开启模态框
		ResourceBundle rb = I18nProvider.bundle();
		String cancelt = rb.getString("Action.Cancel");
		String cancelPrompt = rb.getString("Label.Cancel");
		String title = rb.getString("M.AutoSet");
		final ProgressIndeterminateDialog pid = new ProgressIndeterminateDialog(
				mw.getFrame(), true, title, cancelt, cancelPrompt);
		pid.startIndeterminateDlg();

		final Runnable r = new Runnable() {
			@Override
			public void run() {
				updateForAutoSet();
				pid.close();// 关闭模态框

				cm.pcs.firePropertyChange(PropertiesItem.STOP_AUTOSET, null,
						null);
				ca.getOperateBlocker().kickThrough();
				isAutosetting = false;
			}
		};

		invokeLater_Autoset(r);
		return true;
	}

	public abstract AbsDataSaver createDatasaver();

	public boolean tryToPersistDMData() {
		return false;
	}
	
}