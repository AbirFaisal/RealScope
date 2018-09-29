package com.owon.uppersoft.dso.view;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import com.owon.uppersoft.dso.global.ControlApps;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.view.sub.ButtonPane;
import com.owon.uppersoft.dso.view.sub.DetailPane;
import com.owon.uppersoft.dso.view.sub.InfoBlockAction;

public class CKeyAdapter extends KeyAdapter {
	private MainWindow mw;
	private ControlManager cm;

	public CKeyAdapter(MainWindow mw, ControlManager cm) {
		this.mw = mw;
		this.cm = cm;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		ControlApps ca = Platform.getControlApps();
		if (ca == null || ca.getOperateBlocker() == null)
			return;
		boolean b = ca.getOperateBlocker().isBlock();
		// System.err.println("keyPressed" + b);

		if (b || cm.isDuringDMFtech())
			return;
		int cd = e.getKeyCode();
		// System.out.println(cd);
		if (cd == KeyEvent.VK_ENTER
				&& ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)) {
			ca.interComm.autoset();
			return;
		} else if (cd == KeyEvent.VK_T
				&& ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)) {
			cm.pcs.firePropertyChange(ButtonPane.UPDATE_TUNE_BUTTON, null, null);
			return;
		} else if (cd == KeyEvent.VK_L
				&& ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)) {
			cm.pcs.firePropertyChange(
					PropertiesItem.ADMIN_ROOT_PASSWORD_NOTIFY, null, true);
			return;
		}
		// System.err.println(cd);
		switch (cd) {
		case KeyEvent.VK_F1:
			cm.docManager.helpAction();
			break;

		case KeyEvent.VK_F5:
			mw.getTitlePane().askSwitchRS();
			break;

		case KeyEvent.VK_ENTER:
			// autoset
			// mainWindow.getTitlePane().doAutoset();
			break;

		case KeyEvent.VK_Q:
			// ch1 vb-
			changeVB(0, -1);
			break;
		case KeyEvent.VK_A:
			// ch1 vb+
			changeVB(0, 1);
			break;
		case KeyEvent.VK_W:
			// ch2 vb-
			changeVB(1, -1);
			break;
		case KeyEvent.VK_S:
			// ch2 vb+
			changeVB(1, 1);
			break;
		case KeyEvent.VK_E:
			// ch2 vb+
			changeVB(2, -1);
			break;
		case KeyEvent.VK_D:
			// ch2 vb+
			changeVB(2, 1);
			break;
		case KeyEvent.VK_R:
			// ch2 vb+
			changeVB(3, -1);
			break;
		case KeyEvent.VK_F:
			// ch2 vb+
			changeVB(3, 1);
			break;

		// 用page up & down是否更好，数字小键盘考虑不？
		case KeyEvent.VK_LEFT:
			changeTB(-1);
			break;
		case KeyEvent.VK_RIGHT:
			changeTB(1);
			break;
		default:
			inputPWDCode(cd);
			break;
		}
	}

	protected void inputPWDCode(int cd) {
		cm.sc.validatPWDInput(cd);
	}

	protected void changeVB(int chl, int del) {
		int changelimit = cm.getCoreControl().getSupportChannelsNumber() - 1;
		if (chl > changelimit)
			return;
		InfoBlockAction ibd = mw.getToolPane().getInfoPane().get(chl)
				.getInfoBlockDelegate();
		ibd.removeOwnedComboBox();
		ibd.incretSelect(del);
	}

	protected void changeTB(int del) {
		DetailPane dp = mw.getToolPane().getDetailPane();
		dp.removeTBOwnedComboBox();
		dp.nextTimeBase(del);
	}
}