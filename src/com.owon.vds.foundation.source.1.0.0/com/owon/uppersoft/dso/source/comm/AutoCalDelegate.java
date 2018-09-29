package com.owon.uppersoft.dso.source.comm;

import java.awt.Window;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import com.owon.uppersoft.dso.global.ControlApps;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.vds.ui.dialog.DefaultProgressObserver;
import com.owon.uppersoft.vds.ui.dialog.ProgressExecutor;
import com.owon.uppersoft.vds.ui.dialog.ProgressObserver;
import com.owon.uppersoft.vds.ui.dialog.ProgressableDialog;

public abstract class AutoCalDelegate implements ProgressExecutor {
	protected Window wnd;
	protected ControlApps ca;
	protected ControlManager cm;

	public AutoCalDelegate(Window wnd, ControlApps ca) {
		this.wnd = wnd;
		this.ca = ca;
		this.cm = ca.getControlManager();
	}

	public boolean prompte = true;

	@Override
	public void execute(final ProgressObserver po) {
		/**
		 * 自校正的过程比较长，占用该线程的时间和usb端口资源
		 */
		ca.getDaemon().addMission(new Runnable() {
			@Override
			public void run() {
				autoCal(po);
			}

			public String toString() {
				return "m_selfcorrect";
			}
		});
	}

	protected abstract void autoCal(ProgressObserver po);

	public void askAutoCalibration() {
		if (!cm.sourceManager.isConnected())
			return;

		if (prompte) {
			int i = JOptionPane.showConfirmDialog(wnd, I18nProvider.bundle()
					.getString("M.Utility.SelfCorrecting.ConfirmTxt"), "",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (i != JOptionPane.YES_OPTION)
				return;
		}

		ca.getOperateBlocker().block();

		if (cm.isRuntimeStop()) {
			ca.interComm.statusRun(false, true);
		}

		final ResourceBundle rb = I18nProvider.bundle();
		String cancelt = rb.getString("Action.Cancel");
		String title = rb.getString("M.Utility.SelfCorrecting");
		String cancelPrompt = rb.getString("Label.Cancel");
		final ProgressableDialog jd = new ProgressableDialog(wnd, true, this,
				title, cancelt, cancelPrompt);
		// 改模态化,自校正时不允许其它操作
		jd.setVisible(true);
	}
}
