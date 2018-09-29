package com.owon.uppersoft.dso.source.comm.detect;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.source.comm.TrgStatus;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.vds.core.usb.IDevice;

public class InitLinkRunner implements Runnable {
	private IDevice id;
	private MainWindow mw;
	private ControlManager cm;

	// public boolean isinitLink=false;

	public InitLinkRunner(MainWindow mw, ControlManager cm) {
		this.mw = mw;
		this.cm = cm;
	}

	public void setDevice(IDevice id) {
		this.id = id;
	}

	public String toString() {
		return "m_initlink";
	}

	public void run() {
		if (!cm.sourceManager.isConnected()) {
			initLink(id, cm, mw);
		}

		// isinitLink=true;
	}

	private void initLink(IDevice id, final ControlManager cm,
			final MainWindow mw) {
		mw.updateStatus(TrgStatus.Linking);
		PromptPlace pp = cm.createPromptPlace();
		// final JLabel lbl2 = new JLabel("fdsfsd2");
		// lbl2.setBorder(new LineBorder(Color.BLACK, 1));
		// lbl2.setPreferredSize(new Dimension(200, 100));

		mw.prompt(pp, null);

		// 加屏蔽可单独测试弹出提示
		// if (true)return;

		// 连接成功&失败
		boolean b = cm.sourceManager.connectUSB(id);
		if (!b) {
			// USB连接失败的提示?
			mw.updateStatus(TrgStatus.Offline);
			return;
		}

		// 成功连接，开始初始化等设置
		mw.updateStatus(TrgStatus.Connect);

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Platform.getControlApps().interComm.initMachine(pp);// (pp);

		// try {
		// Thread.sleep(100);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// mainWindow.promptClose();
	}

	
}