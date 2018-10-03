package com.owon.uppersoft.dso.view.pane.function;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.mode.control.SysControl;
import com.owon.uppersoft.dso.page.function.MachineSettingPage;
import com.owon.uppersoft.dso.source.comm.AbsInterCommunicator;
import com.owon.uppersoft.dso.source.manager.SourceManager;
import com.owon.uppersoft.dso.util.DBG;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.view.pane.dock.widget.FunctionPanel;
import com.owon.uppersoft.vds.ui.widget.IPPane;
import com.owon.uppersoft.vds.ui.widget.custom.NLabel;

public class MachineSettingPane extends FunctionPanel {
	private MachineSettingPage msp;
	private SysControl sc;
	private JTextField ptf, mactf;
	private IPPane iptfs;
	private IPPane smtfs;
	private IPPane gwtfs;
	private NLabel macdiplaylbl;

	public MachineSettingPane(ControlManager cm, MachineSettingPage msp) {
		super(cm);
		this.msp = msp;

		// ncgp();
		// nrip();
		// nlbl("M.Utility.MachineSetting.TypeSelect");
		// nrip();
		// USBSourceManager usbsm = controlManager.sourceManager.getUSBSourceManager();
		// nccb(usbsm.refreshUSBPort());
		// nbtn("M.Utility.MachineNet.Refresh");

		sc = cm.getSysControl();
		ncgp();

		// IP Address
		nrip();
		iptfs = nip4net("M.Utility.MachineNet.IP", sc.ipaddress);
		// SubNet Mask;
		nrip();
		smtfs = nip4net("M.Utility.MachineNet.SM", sc.smaddress);
		// Gateway
		nrip();
		gwtfs = nip4net("M.Utility.MachineNet.GW", sc.gwaddress);
		// Port
		nrip();
		nlbl("M.Utility.MachineNet.Port")
				.setPreferredSize(new Dimension(79, 0));
		ptf = ntf(String.valueOf(sc.port));
		ptf.setPreferredSize(new Dimension(60, 30));
		nlblt("(0~65535)");

		nrip();
		nlbl("M.Utility.MachineSetting.Mac").setPreferredSize(
				new Dimension(80, 0));
		mactf = ntf(sc.getMac());
		mactf.setPreferredSize(new Dimension(110, 30));

		mactf.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				sc.isMacvalid(mactf.getText());
			}
		});

		// nrip();
		// nlbl("M.Utility.MachineSetting.Macshow").setPreferredSize(
		// new Dimension(85, 30));
		// macdiplaylbl = nnnlbl(sc.getMac());
		// macdiplaylbl.setPreferredSize(new Dimension(120, 30));

		nrip().setLayout(new FlowLayout(FlowLayout.RIGHT));
		// nbtn("Action.OK").addActionListener(new ActionListener() {
		// @Override
		// public void actionPerformed(ActionEvent e) {
		// okAction();
		// }
		// });
		// nbtn("M.Utility.MachineNet.Reboot").addActionListener(
		// new ActionListener() {
		// @Override
		// public void actionPerformed(ActionEvent e) {
		// restartAction();
		// }
		// });
		nbtn("M.Utility.MachineNet.Rework").addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {

						new Thread() {
							@Override
							public void run() {
								showTips();
								AbsInterCommunicator interComm = Platform
										.getControlApps().interComm;
								okAction();

								try {
									Thread.sleep(2000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}

								interComm.rebootMachine();
								// 断开自动获取
								Platform.getControlApps().releaseConnect();

								/** 这里断开后不再自动连接网络，因为usb要改为供电线，无法确保用户已经拔掉再插上 */
								// try {
								// Thread.sleep(2000);
								// } catch (InterruptedException e) {
								// e.printStackTrace();
								// }
								// interComm.connectAfterReboot();
							}
						}.start();

					}
				});

		localizeSelf();
		updateMacDisplay();
	}

	protected void saveSetting() {
		iptfs.saveIP2Array(sc.ipaddress);
		smtfs.saveIP2Array(sc.smaddress);
		gwtfs.saveIP2Array(sc.gwaddress);

		int pt = 0;
		try {
			pt = Integer.parseInt(ptf.getText());
		} catch (Exception ex) {
		}
		sc.port = pt;
		sc.saveMac(mactf.getText());
	}

	@Override
	public void beforeLeave() {
		saveSetting();
	}

	private void okAction() {
		// JTextField[] iptfs, JTextField[] smtfs, JTextField[] gwtfs

		// 设置IP等信息到指定型号示波器
		saveSetting();
		cm.getSysControl().c_network();
		// InterCommunicator interComm = Platform.getControlApps().interComm;
		// interComm.setNetWork();
		// interComm.reboot();
		DBG.outprintln("OK");
	}

	private void showTips() {
		String m1;
		SourceManager sm = Platform.getControlManager().sourceManager;
		if (sm.isNETConnect())
			m1 = I18nProvider.bundle().getString(
					"M.Utility.MachineNet.ReworkNetTips");
		else
			// if(sm.isUSBConnect())
			m1 = I18nProvider.bundle().getString(
					"M.Utility.MachineNet.ReworkUsbTips");
		JOptionPane.showMessageDialog(Platform.getMainWindow().getFrame(), m1);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String pItem = evt.getPropertyName();
		if (pItem.equals(PropertiesItem.UPDATE_MAC_DISPLAY)) {
			mactf.setText(sc.getMac());
		}
	}

	public void updateMacDisplay() {
		AbsInterCommunicator ic = Platform.getControlApps().interComm;
		ic.queryALL();
	}

}
