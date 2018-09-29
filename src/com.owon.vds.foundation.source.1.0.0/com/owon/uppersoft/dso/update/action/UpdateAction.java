package com.owon.uppersoft.dso.update.action;

import java.awt.Window;
import java.util.ResourceBundle;

import javax.swing.SwingUtilities;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.core.update.CheckUpdateFrame;
import com.owon.uppersoft.vds.core.update.IUpdateAction;
import com.owon.uppersoft.vds.core.update.UpdateRuntime;

public class UpdateAction implements Runnable, IUpdateAction, Localizable {

	private static UpdateAction updateAct;

	public static UpdateAction getUpdateAction() {
		return updateAct;
	}

	public static void handleUpdateAction(ControlManager cm, Window frame) {
		updateAct = new UpdateAction(cm, frame);
		cm.getLocalizeCenter().addLocalizable(updateAct);
	}

	private UpdateContent uc;
	private UpdateRuntime ur;

	public UpdateAction(ControlManager cm, Window wnd) {
		uc = new UpdateContent(this, cm, wnd, cm.getPrinciple()
				.getConfigurationDirectory());
	}

	public void release() {
		ur = null;
	}

	@Override
	public void run() {
		if (ur != null)
			return;

		ur = new UpdateRuntime(uc);
		/**
		 * 清理任务不再在启动程序时进行，因自动更新之后使用新的进程打开程序，
		 * 
		 * 无法确保和旧程序进程的关闭同步，可能导致旧版本的jar包无法被删除
		 */
		ur.clearUp();
		ur.checkUpdate();
	}

	public void updateAction() {
		if (ur == null) {
			SwingUtilities.invokeLater(this);
		} else {
			ur.getCheckUpdateFrame().toFront();
		}
	}

	public void upgradeLocalize(ResourceBundle rb) {
		if (ur == null)
			return;
		CheckUpdateFrame chUpFrame = ur.getCheckUpdateFrame();
		if (chUpFrame != null) {
			chUpFrame.localize();
			if (chUpFrame.getUpdateFrame() != null)
				chUpFrame.getUpdateFrame().localize(rb);
		}
	}

	@Override
	public void localize(ResourceBundle rb) {
		if (updateAct != null)
			updateAct.upgradeLocalize(rb);
	}

}