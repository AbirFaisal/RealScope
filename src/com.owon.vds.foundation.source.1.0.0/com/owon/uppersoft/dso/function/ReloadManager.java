package com.owon.uppersoft.dso.function;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.page.function.RecordPage;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.vds.util.Pref;

public class ReloadManager {
	public static final int NoUse = 0, Record_Reload = 1, DM_Reload = 2;
	public int ReloadStatus = NoUse;
	public String reloadPath;
	private ControlManager cm;

	public ReloadManager(ControlManager cm) {
		this.cm = cm;
	}

	public boolean isReloadDM() {
		return ReloadStatus == DM_Reload;
	}

	public boolean isReload() {
		return ReloadStatus != NoUse;
	}

	public void setReloadStatus(int status) {
		ReloadStatus = status;
	}

	public void releaseReloadFlag() {
		ReloadStatus = NoUse;
	}

	public int prepareReloadPlayFile(int machID) {
		// if (!cm.isSameSeries(machID))
		// return -1;
		ReloadStatus = Record_Reload;
		restartWhenMismatching(machID);
		return 1;
	}

	public void prepareReloadDM(int machID, String path) {
		// if (cm.isSameSeries(machID)) {
		ReloadStatus = DM_Reload;
		reloadPath = path;
		restartWhenMismatching(machID);
		// }
	}

	public void reload() {
		switch (ReloadStatus) {
		case NoUse:
			break;
		case DM_Reload:
			File rlf = new File(reloadPath);
			if (rlf != null) {
				cm.binIn.openfile(Platform.getDataHouse(), rlf);
			}
			break;
		case Record_Reload:
			reloadPlayFile();
			break;
		}
	}

	public void restartInLinking(String machine_name) {
		int machID = cm.getPrinciple().getMachineID(machine_name);
		// if (isSameSeries(machID)) {
		restartWhenMismatching(machID);
		// } else {
		// String msn = I18nProvider.bundle().getString("Label.LinkMismatch")
		// + " \"" + machine_name + "\"";
		// JOptionPane.showMessageDialog(Platform.getMainWindow().getFrame(),
		// msn);
		// }
	}

	public void restartWhenMismatching(final int machID) {
		String msn = I18nProvider.bundle().getString("Label.Mismatch");
		int re = JOptionPane.showConfirmDialog(Platform.getMainWindow()
				.getFrame(), msn, null, JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (re == JOptionPane.YES_OPTION) {// false &&
			new Thread() {
				@Override
				public void run() {
					cm.getCoreControl().setProductParam(
							cm.getMachineTypeName(machID));
					cm.sourceManager.disconnectSource();
					Platform.getDataHouse().releaseDeepMemoryStorage();
					Platform.getMainWindow().getFrame().dispose();
					// mw.onDispose();//该工作重复，已监听MainFrame的关闭，关闭会触发这句
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					try {
						Runtime.getRuntime().exec(
								cm.getConfig().getStaticPref().getLauncher());
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.exit(0);
					// Platform.relaunch();
				}
			}.start();
		}
		// coreControl.machineChange(machine_name + "ONE");
	}

	public void load(Pref p) {
		reloadPath = p.getProperty("reloadPath", "");
		ReloadStatus = p.loadInt("reloadStatus");
	}

	public void persist(Pref p) {
		p.setProperty("reloadPath", reloadPath);
		p.persistInt("reloadStatus", ReloadStatus);
	}

	public void reloadPlayFile() {
		cm.getDockControl().dockDialogQuickOpenHide(RecordPage.Name);
		cm.pcs.firePropertyChange(PropertiesItem.SWITCH_PLAYPANE, null, null);
	}
}
