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
		/** KNOW Delete the old dmf: if the new dmf has less content than before, the tail of the new dmf will contain leftovers of the old dmf. */
		dmf.delete();
		if (dmf.exists())
			DBG.errprintln("dmf.exists");
	}

	public abstract void queryALL();

	public abstract void onExport_get();

	public abstract void rebootMachine();

	protected abstract void doSyncFactorySet2Machine();

	/**
	 * Invoked on operation
	 * 
	 * @return whether time-axis operations (such as tb, htp) are supported, and fetch deep memory at the appropriate time
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
		// Check and get it running, then apply the factory settings
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
		// /** Start running, set so that waveforms cannot be exported */
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
	 *            1 = pass, 0 = fail
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

		/** The two below are only reserved KVO */
		pcs.firePropertyChange(APPLY_TIMEBASE, null, null);
		pcs.firePropertyChange(APPLY_DEEPMEMORY, null, null);
		pcs.firePropertyChange(ITimeControl.onTimebaseUpdated, null, null);

		/** After auto setting completes, also update the sampling rate accordingly and update the panel */
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

		// Open the modal dialog
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
				pid.close();// Close the modal dialog

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