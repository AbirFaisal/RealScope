package com.owon.uppersoft.vds.core.usb.help;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.owon.uppersoft.vds.core.usb.IDevice;

public class ContextMenuManager implements PopupMenuListener {
	private static final String ACTION_DISCONNECT = "Action.disconnect";
	private static final String ACTION_CONNECT_LAN = "Action.connectLAN";
	private static final String ACTION_INSTALL_USBDRV = "Action.installUSBDRV";
	private static final String ACTION_NO_USB_FOUND = "Action.noUSBFound";
	private static final String ACTION_CONNECT_USB = "Action.connectUSB";
	private static final String ACTION_USB_SLEEP = "Label.UsbSleep";
	private static final String ACTION_USB_WAKE = "Label.UsbWake";

	private JPopupMenu ctxMenu;
	private Component c;
	private boolean onContextMenu = false;

	public ContextMenuManager(JPopupMenu m, Component com) {
		ctxMenu = m;
		c = com;
		ctxMenu.addPopupMenuListener(this);
	}

	public boolean isOnContextMenu() {
		return onContextMenu;
	}

	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		onContextMenu = false;
	}

	@Override
	public void popupMenuCanceled(PopupMenuEvent e) {
	}

	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		onContextMenu = true;
	}

	public void showContextMenu_Offline(final IUSBCheckHelper uch, int x,
			ResourceBundle rb) {
		final List<IDevice> ids = uch.getDeviceList();
		ctxMenu.removeAll();
		final boolean b = !(ids == null || ids.size() == 0);

		JMenu subm = new JMenu(rb.getString(b ? ACTION_CONNECT_USB
				: ACTION_NO_USB_FOUND));
		if (b) {
			for (final IDevice id : ids) {
				String bcd = Integer.toHexString(id.getUsb_Device()
						.getDescriptor().getBcdUSB());
				String name = id.toString() + ", bcd: 0x" + bcd;

				JMenuItem jmi = new JMenuItem(name);
				jmi.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (b)
							uch.askConnectUSB(id);
					}
				});
				subm.add(jmi);
			}
		}
		ctxMenu.add(subm);
		JMenuItem jmi = new JMenuItem(rb.getString(ACTION_INSTALL_USBDRV));
		jmi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				uch.runInstallBat();
			}
		});
		ctxMenu.add(jmi);
		jmi = new JMenuItem(rb.getString(ACTION_CONNECT_LAN));
		jmi.setEnabled(false);
		jmi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		});
		ctxMenu.add(jmi);
		ctxMenu.show(c, x, 0);
	}

	public void showContextMenu_Online(final IUSBCheckHelper uch, int x,
			ResourceBundle rb) {
		ctxMenu.removeAll();
		JMenuItem jmi = new JMenuItem(rb.getString(ACTION_DISCONNECT));
		jmi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				uch.askDisconnect();
			}
		});
		ctxMenu.add(jmi);

		jmi = new JMenuItem(rb.getString(ACTION_INSTALL_USBDRV));
		jmi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				uch.runInstallBat();
			}
		});
		ctxMenu.add(jmi);
		ctxMenu.show(c, x, 0);
	}

	public void showContextMenu_RightClick(final IUSBCheckHelper uch, int x,
			ResourceBundle rb) {
		ctxMenu.removeAll();
		final boolean b = uch.isNoCheck();//isSleep
		final JMenuItem jmi = new JMenuItem(b ? rb.getString(ACTION_USB_WAKE)
				: rb.getString(ACTION_USB_SLEEP));
		jmi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				uch.setNoCheck(!b);
				if (!b)
					uch.askDisconnect();
			}
		});
		ctxMenu.add(jmi);
		ctxMenu.show(c, x, 0);
		// System.out.println(jmi.getText() + "   noCheck?  "
		// + USBLoopChecker.noCheck);
	}

	public void closeContextMenu() {
		ctxMenu.setVisible(false);
	}
}
