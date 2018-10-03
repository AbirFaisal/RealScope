package com.owon.uppersoft.vds.source.comm;

import static com.owon.uppersoft.dso.util.PropertiesItem.MachineInformation;
import static com.owon.uppersoft.dso.util.PropertiesItem.STATUS;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ResourceBundle;

import com.owon.uppersoft.dso.data.AbsDataSaver;
import com.owon.uppersoft.dso.function.SoftwareControl;
import com.owon.uppersoft.dso.global.ControlApps;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.model.DataSaverTiny;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.source.comm.AbsInterCommunicator;
import com.owon.uppersoft.dso.source.comm.TrgStatus;
import com.owon.uppersoft.dso.source.comm.detect.PromptPlace;
import com.owon.uppersoft.dso.source.comm.effect.SubmitorFactory;
import com.owon.uppersoft.dso.source.manager.SourceManager;
import com.owon.uppersoft.dso.source.usb.USBSourceManager;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.dso.view.ToolPane;
import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.vds.auto.AutosetRunner2;
import com.owon.uppersoft.vds.core.comm.ICommunicateManager;
import com.owon.uppersoft.vds.core.pref.StaticPref;
import com.owon.uppersoft.vds.core.wf.dm.DMInfo;
import com.owon.uppersoft.vds.device.interpret.DeviceAddressTable;
import com.owon.uppersoft.vds.machine.PrincipleTiny;
import com.owon.uppersoft.vds.machine.TinyMachine;
import com.owon.uppersoft.vds.source.comm.data.ChannelsTransportInfo_Tiny;
import com.owon.uppersoft.vds.source.comm.data.GetDataRunner2;
import com.owon.uppersoft.vds.source.comm.data.TinyDMSourceManager;
import com.owon.uppersoft.vds.source.comm.data.ValueFeeder;
import com.owon.uppersoft.vds.ui.prompt.FadeIOShell;
import com.owon.vds.calibration.BaselineCalDelegateTiny;
import com.owon.vds.firm.protocol.AddressAttachCommand;
import com.owon.vds.tiny.firm.DeviceFlashCommunicator;
import com.owon.vds.tiny.firm.FPGADownloader;
import com.owon.vds.tiny.firm.pref.PrefControl;
import com.owon.vds.tiny.firm.pref.PrefSync;
import com.owon.vds.tiny.firm.pref.model.Register;

public class InterCommTiny extends AbsInterCommunicator {

	private static final String NO_FOUND_DEVICE_PREFERENCE = "Tiny.Prompt.Comm.NoFoundDevicePreference";
	private static final String FOUND_DEVICE_PREFERENCE = "Tiny.Prompt.Comm.FoundDevicePreference";

	public static final String NEW_DATA_RECEIVE = "NewDataReceive";

	@Override
	public void rebootMachine() {
	}

	public ToolPane getToolPane() {
		return getMainWindow().getToolPane();
	}

	public ControlManager getControlManager() {
		return cm;
	}

	public WaveFormManager getWaveFormManager() {
		return dh.getWaveFormManager();
	}

	@Override
	protected void invokeLater_Autoset(final Runnable doneJob) {
		AutosetRunner2 ar = new AutosetRunner2(doneJob, this);
		// AutosetRunner ar = new AutosetRunner(doneJob, this);
		// new Thread(ar).start();
		ar.getReady();
	}

	public TinyMachine getTinyMachine() {
		return (TinyMachine) dh.getControlManager().getMachine();
	}

	/**
	 * Arouse during operation
	 * 
	 * @return Whether to support timeline operations (such as tb, htp) and get deep storage when appropriate
	 */
	public boolean isTimeOperatableNTryGetDM() {
		if (cm.isRuntime())
			return true;

		if (!sm.isConnected())
			return true;
		if (!cm.isRuntimeStop())
			return false;
		boolean isPlaying = cm.playCtrl.confirmGoOnPlaying();
		if (isPlaying)
			return true;

		/** For example, in the case of normal triggering, it stops from the Ready state and cannot obtain deep storage data. */
		if (cc.getTriggerControl().isSweepNormal()
				&& getChannelsTransportInfo().getFrameCount() == 0)
			return true;

		boolean availableDM = ca.isDMAvailable();
		// System.err.println("whenOperation:" + dh.isDMDataGotAlready());
		if (!availableDM)
			return true;

		if (!ca.isDMDataGotAlready()) {
			persistDMData();
			return true;
		} else
			return true;
	}

	private SourceManagerTiny sm;
	private PrincipleTiny pp;

	public InterCommTiny(DataHouse dh, MainWindow mw, ControlApps ca,
			SourceManagerTiny smt, PrincipleTiny pp) {
		super(dh, mw, ca);
		sm = smt;
		this.pp = pp;

		// controlManager.sc.quickPass();

		data_receive_broadcast = new PropertyChangeSupport(this);
	}

	public PropertyChangeSupport getDataReceiveBroadcast() {
		return data_receive_broadcast;
	}

	private PropertyChangeSupport data_receive_broadcast;

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		data_receive_broadcast.addPropertyChangeListener(listener);
	}

	public void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		data_receive_broadcast.firePropertyChange(propertyName, oldValue,
				newValue);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		data_receive_broadcast.removePropertyChangeListener(listener);
	}

	public Submitor2 reinitSubmitor2() {
		return (Submitor2) SubmitorFactory.reInit();
	}

	@Override
	public void onExport_get() {
		/** Deep storage, export */
		boolean acquired = getDMDataBackgroundWhenAsk();
		if (!acquired)
			// controlManager.pcs.firePropertyChange(ExportWaveControl.AFTER_EXPORT, null,
			// true);
			cm.ewc.setAuthorizeExport(false);
	}

	/**
	 * Proactive request
	 */
	private boolean getDMDataBackgroundWhenAsk() {
		// System.err.println("whenask:" + dh.isDMDataGotAlready());
		if (sm.isConnected()) {

			if (!ca.isDMDataGotAlready()) {
				persistDMData();
				return true;
			}

		} else {
			FadeIOShell fs = new FadeIOShell();
			String txt = I18nProvider.bundle().getString(
					"M.Utility.ExportWave.Confirm");
			fs.prompt(txt, getMainWindow().getFrame());
		}
		return false;
	}

	protected MainWindow getMainWindow() {
		return Platform.getMainWindow();
	}

	private void persistDMData() {
		prepare2PersistDMData();
		DMInfo ci = new DataSaverTiny().saveFileM(cm, dmf,
				new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
					}
				}, new TinyDMSourceManager(getChannelsTransportInfo()));

		dh.receiveRTDMData(ci);
		ca.setDMDataGotAlready(true);
		/** Stop and get DM data before exporting waveforms */
		cm.pcs.firePropertyChange(PropertiesItem.AFTER_GOT_DM_DATA, null,
				dh.getWaveFormManager());
	}

	private ChannelsTransportInfo_Tiny getChannelsTransportInfo() {
		return (ChannelsTransportInfo_Tiny) Platform.getControlApps()
				.getDaemon().getChannelsTransportInfo();
	}

	private boolean askDM;

	public void statusStop(boolean dm) {
		askDM = dm;
		super.statusStop(dm);
	}

	@Override
	public void afterkeepload() {
		if (!ca.isDMDataGotAlready() && askDM) {
			persistDMData();
		}
	}

	@Override
	public void queryALL() {
	}

	@Override
	public void doSyncFactorySet2Machine() {
		Submitor2 sb = reinitSubmitor2();
		PrefControl pc = pp.getPrefControl();
		// Factory settings override current settings
		pc.rollFactory2Device();
		syncFlash();

		// Factory settings override current settings...
		cm.initDetail(sb);
		sb.apply();
	}

	protected ICommunicateManager getICommunicateManager() {
		return sm;
	}

	public void syncFlash() {
		PrefControl pc = pp.getPrefControl();
		new PrefSync().syncFlash(pc, ca.getJobUnitDealer());
	}

	@Override
	public void initDetail() {
		Submitor2 sbm = reinitSubmitor2();

		cm.initDetail(sbm);
		sbm.apply();
	}

	protected String getMachineType() {
		if (sm.isUSBConnect()) {
			USBSourceManager usbsm = ((SourceManager) sm).getUSBSourceManager();
			String sn = usbsm.getUsbs().getId().getSerialNumber();
			return sn;
		} else {// if (sm.isNetConnected())
			return "VDS1022";
		}
	}

	protected String getMachineText() {
		if (sm.isUSBConnect()) {
			USBSourceManager usbsm = ((SourceManager) sm).getUSBSourceManager();
			String sn = usbsm.getUsbs().getId().toString();
			return sn;
		} else {// if (sm.isNetConnected())
			return "?";
		}
	}

	public boolean initMachine(PropertyChangeListener pcl) {
		Submitor2 sbm = reinitSubmitor2();
		DeviceAddressTable table = getTinyMachine().getDeviceAddressTable();

		SoftwareControl sc = cm.getSoftwareControl();

		String sn = getMachineType();
		System.out.println("sn: " + sn);

		sc.machine_name = getTinyMachine().name();

		if (!sc.machine_name.equalsIgnoreCase(sn)) {
			cm.reloadManager.restartInLinking(sn);
			return false;
		}

		sc.setMachineHeader("SPB" + sc.machine_name);
		mw.updateStatus(TrgStatus.Detect);
		pcl.propertyChange(new PropertyChangeEvent(this, STATUS, null,
				I18nProvider.bundle().getString("Label.detect")));

		mw.updateStatus(TrgStatus.Initialize);
		pcl.propertyChange(new PropertyChangeEvent(this, STATUS, null,
				I18nProvider.bundle().getString("Label.initialize") + ": "));
		// + getMachineText() 避免出现usb描述符

		StaticPref sp = cm.getConfig().getStaticPref();
		if (sc.machine_name.equalsIgnoreCase("VDS1021")) {
			sp.setProductId(getMachineText().substring(0, 3));
		}

		pcl.propertyChange(new PropertyChangeEvent(this, MachineInformation,
				null, null));

		boolean success;

		ICommunicateManager sm = getICommunicateManager();

		FPGADownloader fg = new FPGADownloader();
		/** 识别fpga运行状态：<0出错，==0未下载，>0已下载 */
		int downloadFlag = fg.queryFPGADownloaded(sm);
		if (downloadFlag < 0) {
			pcl.propertyChange(new PropertyChangeEvent(this, STATUS, null,
					"fpga download status query err response."));
			return false;
		}

		/** 读取flash中的本机参数配置 */
		boolean fpgaNotDownload = (downloadFlag == 0);

		/**
		 * 这里必须先读取flash_txt，从中得到设备的机型和版本，据此下载对应的fpga
		 */
		DeviceFlashCommunicator dflash = new DeviceFlashCommunicator();
		byte[] devPref = dflash.fetchPrefernce(sm, fpgaNotDownload);
		PrefControl pc = pp.getPrefControl();
		boolean loadPref = pc.loadSyncImageFromDevice(devPref);

		Register reg = pp.getTuneFunction().getTuneModel().getRegister();
		sc.setBoardVersion(reg.version);
		sc.setBoardSeries(reg.serialNumber);

		cm.getConfig().updateLocales(reg.getSelectedLocaleObject());
		cm.pcs.firePropertyChange(PropertiesItem.UPDATE_TXT_LOCALES, null, null);

		String key;
		FadeIOShell fs = new FadeIOShell();
		ResourceBundle rb = I18nProvider.bundle();
		if (loadPref) {
			key = FOUND_DEVICE_PREFERENCE;
			fs.prompt(rb.getString(key), mw.getWindow(), 800);
		} else {
			key = NO_FOUND_DEVICE_PREFERENCE;
			fs.prompt(rb.getString(key), mw.getWindow(), 2000);
			// return false;
		}
		// pc.propertyChange(new PropertyChangeEvent(this, STATUS, null, msg));

		if (fpgaNotDownload) {
			// 这里放各自机型的文件夹用小写命名
			File fpgafile = fg.checkFPGAAvailable(sn.toLowerCase());
			if (fpgafile == null) {
				pcl.propertyChange(new PropertyChangeEvent(this,
						FPGADownloader.FPD_NOFILE, null, null));
				return false;
			}

			/** 下载fpga */
			success = fg.downloadFPGA(pcl, sm, fpgafile);
			if (!success) {
				pcl.propertyChange(new PropertyChangeEvent(this, STATUS, null,
						"fpga download fail."));
				return false;
			}

			initializeDevice(pcl, reg);
		}

		if (getTinyMachine().name().equals("VDS2052")) {
			sbm.d_chl_vb(0, 9, null);
			sbm.d_chl_vb(1, 9, null);

			// 在循环之前确保指令发送完毕

			// adc复位
			sbm.sendCMD(new AddressAttachCommand("reset_adc_0x22", 0x22, 1), 1);// clock
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			sbm.sendCMD(new AddressAttachCommand("reset_adc_0x21", 0x21, 1), 1);// adc
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				//
				sendCmd(sbm, 0x000020);
				sendCmd(sbm, 0x007201);
				sendCmd(sbm, 0x007341);
				sendCmd(sbm, 0x007400);
				// sendCmd(sbm, 0x002080);
				// sendCmd(sbm, 0x002180);
				// sendCmd(sbm, 0x002208);
				// sendCmd(sbm, 0x002380);
				// sendCmd(sbm, 0x002480);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			ca.getJobQueueDispatcher().dealQueue();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		ChannelInfo ci = dh.getWaveFormManager().getWaveForm(1).wfi.ci;
		boolean b = ci.isOn();
		if (getTinyMachine().name().equals("VDS2052")) {
			// vds2052专用复位adc特别是ch2以防干扰
			ci.c_setOn(true);
			// customInit();
		}

		initDetail();
		ca.getJobQueueDispatcher().dealQueue();

		if (getTinyMachine().name().equals("VDS2052")) {

			// ci.setVoltbaseIndex( 9, true,true);
			ci.c_setOn(b);
			// resumeCustom();

			pcl.propertyChange(new PropertyChangeEvent(this, STATUS, null,
					"initializing..."));
			int fakeTime = 5000, t = 0, fakeStepTime = 200;
			pcl.propertyChange(new PropertyChangeEvent(this,
					PromptPlace.ProgressStart, null, fakeTime));
			while (t < fakeTime) {
				pcl.propertyChange(new PropertyChangeEvent(this,
						PromptPlace.ProgressIncrease, null, fakeStepTime));
				try {
					Thread.sleep(fakeStepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				t += fakeStepTime;
			}
			pcl.propertyChange(new PropertyChangeEvent(this,
					PromptPlace.ProgressDone, null, null));

			sbm.sendCMDbyBytes(table.RE_COLLECT, 1, null);

			ca.getJobQueueDispatcher().dealQueue();
		}

		mw.updateStatus(TrgStatus.Auto);
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		ca.keepload();
		if (getTinyMachine().name().equals("VDS2052")) {
			try {
				sendCmd(sbm, 0x002208);
				sendCmd(sbm, 0x002380);
				sendCmd(sbm, 0x002480);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	private void sendCmd(Submitor2 sbm, int value) throws InterruptedException {
		Thread.sleep(500);
		sbm.sendCMD(new AddressAttachCommand("", 0x14, 3), value);

	}

	protected void initializeDevice(PropertyChangeListener pcl, Register reg) {
		DeviceAddressTable table = getTinyMachine().getDeviceAddressTable();
		Submitor2 sbm = reinitSubmitor2();

		// 各机型通用
		sbm.sendCMDbyBytes(table.PHASE_FINE, reg.getPhaseFine().getWrValue(),
				null);

		if (getTinyMachine().name().equals("VDS2052")) {
			// vds2052处理adc的移动干扰，并延时等待adc工作正常
			// sbm.sendCMD(table.channel_set[0], 0xff);
			// sbm.sendCMD(table.channel_set[1], 0xff);
			// sbm.sendCMD(table.volt_gain[0], 1);
			// sbm.sendCMD(table.volt_gain[1], 1);

			// ca.getJobQueueDispatcher().dealQueue();
		}

	}

	/** 以下来个方法产生界面阻塞，可考虑引入全局可用的阻塞机制，方便按键指令得到屏蔽 */
	// public void selfCalibration() {
	// super.selfCalibration();
	// }
	// public void autoset() {
	// super.autoset();
	// }
	public AbsDataSaver createDatasaver() {
		return new DataSaverTiny();
	}

	public ValueFeeder getValueFeeder() {
		return ((GetDataRunner2) Platform.getControlApps().getDaemon()
				.getAbsGetDataRunner()).getValueFeeder();
	}

	public void selfCalibration() {
		new BaselineCalDelegateTiny(mw.getWindow(), ca, getTinyMachine(), pp)
				.askAutoCalibration();
	}
}
