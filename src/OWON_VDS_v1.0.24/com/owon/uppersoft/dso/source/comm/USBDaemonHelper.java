package com.owon.uppersoft.dso.source.comm;

import java.awt.event.MouseEvent;
import java.util.ResourceBundle;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.source.comm.detect.USBLoopChecker;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.vds.core.usb.help.ContextMenuManager;

public class USBDaemonHelper {
	private InfiniteDaemon dmn;
	private ControlManager cm;

	public USBDaemonHelper(InfiniteDaemon dmn, ControlManager cm,
			MainWindow mw, ContextMenuManager ctxMenu) {
		this.dmn = dmn;
		this.cm = cm;
		this.ctxMenu = ctxMenu;
		uc = new USBLoopChecker(cm, cm.sourceManager.getUSBSourceManager(), mw,
				dmn);

		/** Called only if there is usb-related functionality in the program */
		if (cm.reloadManager.isReload())
			setNoDisturb(true);
	}

	public void onNotConnecting() {
		if (!ctxMenu.isOnContextMenu()) {
			uc.checkUSBDevice();
		}
	}

	private USBLoopChecker uc;
	private ContextMenuManager ctxMenu;

	public void onClickStatus(int status_icon_xloc, int btn) {
		int x = status_icon_xloc;

		// Other popup modes
		// JPanel jp = new JPanel();
		// jp.setPreferredSize(new Dimension(100, 100));
		// cs.promptDown(jp, status_icon_xloc);

		ResourceBundle rb = I18nProvider.bundle();
		DefaultUSBCheckHelper uch = new DefaultUSBCheckHelper(dmn, uc);
		if (btn == MouseEvent.BUTTON3) {
			ctxMenu.showContextMenu_RightClick(uch, x, rb);
			return;
		}
		// Connected state
		if (cm.sourceManager.isConnected()) {
			ctxMenu.showContextMenu_Online(uch, x, rb);
		} else {
			// Do-not-disturb state & disconnected state
			// if (noDisturb);
			ctxMenu.showContextMenu_Offline(uch, x, rb);
		}
	}

	public void setNoDisturb(boolean b) {
		uc.setNoDisturb(b);
	}
}