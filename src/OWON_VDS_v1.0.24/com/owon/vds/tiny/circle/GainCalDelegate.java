package com.owon.vds.tiny.circle;

import java.awt.Window;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import com.owon.uppersoft.dso.global.ControlApps;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.vds.machine.PrincipleTiny;
import com.owon.uppersoft.vds.machine.TinyMachine;
import com.owon.uppersoft.vds.source.comm.InterCommTiny;
import com.owon.uppersoft.vds.ui.dialog.ProgressExecutor;
import com.owon.uppersoft.vds.ui.dialog.ProgressObserver;
import com.owon.uppersoft.vds.ui.dialog.ProgressableDialog;
import com.owon.vds.calibration.stuff.ArgCreator;
import com.owon.vds.tiny.firm.pref.PrefControl;

public class GainCalDelegate implements ProgressExecutor {
	protected Window wnd;
	protected ControlApps ca;
	protected ControlManager cm;

	private TinyMachine tm;
	private PrincipleTiny pt;

	public GainCalDelegate(Window wnd, ControlApps ca, TinyMachine tm,
			PrincipleTiny pt) {
		this.wnd = wnd;
		this.ca = ca;
		this.cm = ca.getControlManager();

		this.tm = tm;
		this.pt = pt;
	}

	public Runnable nextJob;

	private GainCalManager acm;

	protected void circleCal(ProgressObserver po) {
		PrefControl pc = pt.getPrefControl();
		ArgCreator ac = tm.getArgCreator(pt.getCalArgTypeProvider());
		acm = new GainCalManager(cm, (InterCommTiny) ca.interComm,
				Platform.getDataHouse(), agp) {
			@Override
			public void onFinished(ProgressObserver sd) {
				super.onFinished(sd);

				if (nextJob != null)
					nextJob.run();
			}
		};
		acm.getReady(po, ac, pc);
	}

	public void cancel(Runnable afterCancel) {
		if (acm != null) {
			acm.cancel(afterCancel);
			acm = null;
		}
	}

	public void execute(final ProgressObserver po) {
		/**
		 * 自校正的过程比较长，占用该线程的时间和usb端口资源
		 */
		ca.getDaemon().addMission(new Runnable() {
			@Override
			public void run() {
				circleCal(po);
			}

			public String toString() {
				return "m_circle";
			}
		});
	}

	private AGPControl agp;
	public boolean prompte;

	public void askCircleCalibration(AGPControl ap) {
		if (agp != null)
			agp.finish();

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

		this.agp = ap;

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