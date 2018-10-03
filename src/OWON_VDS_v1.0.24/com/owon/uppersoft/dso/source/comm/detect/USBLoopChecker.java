package com.owon.uppersoft.dso.source.comm.detect;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.source.comm.InfiniteDaemon;
import com.owon.uppersoft.dso.source.comm.TrgStatus;
import com.owon.uppersoft.dso.source.usb.USBSourceManager;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.vds.core.usb.IDevice;
import com.owon.uppersoft.vds.ui.prompt.PromptDialog;

public class USBLoopChecker {
	public static final int TimeBetweenTwiceCheckUSB = 500;

	private CheckUSBPortsPane cup = null;
	private boolean noDisturb;
	private boolean noCheck = false;

	private MainWindow mw;
	private ControlManager cm;
	private InfiniteDaemon dae;
	private USBSourceManager usbsm;

	public USBLoopChecker(ControlManager cm, USBSourceManager usbsm,
			MainWindow mw, InfiniteDaemon dae) {
		this.mw = mw;
		this.cm = cm;
		this.dae = dae;
		this.usbsm = usbsm;

		noDisturb = false;

		boolean drvInstalled = usbsm.init_check_usbdrv();
		if (!drvInstalled) {
			runInstallBat();

			// drvInstalled = sm.getUSBSourceManager().init_check_usbdrv();
		}
	}

	/**
	 * 
	 */
	public void runInstallBat() {
		String cmd = cm.getConfig().getStaticPref()
				.getReinstallUSBDriverCommand();
		try {
			Process p = Runtime.getRuntime().exec(cmd, null,
					new File(USBSourceManager.USBDrvDir));
			/** 不调用下面的，不然会报错，本地代码jni错误 */
			// p.exitValue();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public void setNoDisturb(boolean b) {
		noDisturb = b;
	}

	public void setNoCheck(boolean b) {
		noCheck = b;
	}

	public boolean isNoCheck() {
		return noCheck;
	}

	private List<IDevice> ids;

	public List<IDevice> getDeviceList() {
		return ids;
	}

	/**
	 * 只在无连接的情况下进入
	 * 
	 */
	public void checkUSBDevice() {
		if (noCheck)
			return;
		try {
			Thread.sleep(TimeBetweenTwiceCheckUSB);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		ids = usbsm.refreshUSBPort();
		if (ids == null) {
			// 用返回null来判断usb驱动调用异常
			mw.updateStatus(TrgStatus.USBDrvErr);
			return;
		}

		int len = ids.size();

		if (cup != null) {
			cup.updateList(ids);
			return;
		}

		// 免打扰的模式下，把可用端口数加负号传进去，需要显示的时候重置为取绝对值
		mw.updateStatus(noDisturb ? len : -len, len > 0 ? TrgStatus.USBFound
				: TrgStatus.Offline);

		if (len <= 0) {
			return;
		}

		// 免打扰模式，状态只是在左上角显示更新
		if (noDisturb) {
			return;
		}

		if (!cm.isExit()) {
			promptUSBPorts(ids, this);
		}
	}

	public void promptUSBPorts(List<IDevice> ids, final USBLoopChecker uc) {
		cup = new CheckUSBPortsPane(ids, uc) {
			@Override
			public void clickToConnect(IDevice id) {
				/** 点击端口开始连接后，通过监听器传到这里，然后占用InfiniteDaemon线程进行连接 */
				dae.initLink(id);
			}
		};
		cup.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				String pn = evt.getPropertyName();
				if (pn.equals(PromptDialog.LastPromptDispose)) {
					cup = null;
					mw.getFrame().toFront();
				}
			}
		});

		mw.prompt(cup, new Runnable() {
			@Override
			public void run() {
				defaultInitLink();
			}
		});
	}

	public void defaultInitLink() {
		// System.err.println("ids: " + ids.size());
		if (ids != null && ids.size() == 1) {
			dae.initLink(ids.get(0));
		}
	}

	/**
	 * 不显示检测结果
	 */
	public void ignoreCheckResult() {
		noDisturb = true;
		mw.getITitleStatus().exposeTrgStatus();
		mw.promptUp();
	}
}