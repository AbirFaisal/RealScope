package com.owon.uppersoft.dso.view.pane.function;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.mode.control.SysControl;
import com.owon.uppersoft.dso.page.IContentPage;
import com.owon.uppersoft.dso.page.function.MachineNetPage;
import com.owon.uppersoft.dso.page.function.MachineSettingPage;
import com.owon.uppersoft.dso.pref.Define;
import com.owon.uppersoft.dso.source.comm.AbsInterCommunicator;
import com.owon.uppersoft.dso.util.DBG;
import com.owon.uppersoft.dso.view.pane.dock.ContentPane;
import com.owon.uppersoft.dso.view.pane.dock.widget.FunctionPanel;
import com.owon.uppersoft.vds.ui.widget.IPPane;
import com.owon.uppersoft.vds.ui.widget.custom.CButton;

public class MachineNetPane extends FunctionPanel {
	// private final MachineSettingPage msp;
	private IPPane iptfs;
	private JTextField ptf;
	private SysControl sc;

	/**
	 * 
	 */
	public MachineNetPane(final ControlManager cm, final ContentPane cp,
			final MachineNetPage mnp) {
		super(cm);
		// msp = (MachineSettingPage) ;
		int tipsHeight = 40;

		ncgp();
		nrip();
		nlbl("M.Utility.MachineNet.TipsTitle");
		JPanel jp = nrip();
		String tips = I18nProvider.bundle().getString(
				"M.Utility.MachineNet.Tips");
		JTextArea lb = new JTextArea(tips);
		// addLocalizable(lb);
		lb.setBackground(Define.def.style.CO_DockContainer);
		lb.setForeground(Color.WHITE);
		lb.setFont(Define.def.alphafont);
		lb.setPreferredSize(new Dimension(Define.def.DockFrameWidth, tipsHeight));

		lb.setLineWrap(true);// 换行
		lb.setWrapStyleWord(true);// 单词本行放不下，自动放下一行
		lb.setHighlighter(null);
		lb.setEditable(false);
		lb.setBorder(null);

		jp.add(lb);

		nrip().setLayout(new FlowLayout(FlowLayout.RIGHT));
		CButton cb = nbtn("Action.OK");
		cb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cp.applyContent(mnp.getContentID(), MachineSettingPage.Name,
						IContentPage.Forward);
			}
		});

		sc = cm.getSysControl();

		ncgp();
		nrip();
		nlbl("Action.Connect");
		nrip();
		iptfs = nip4net("M.Utility.MachineNet.IP", sc.link_ip_address);// sc.ipaddress
		nrip();
		nlbl("M.Utility.MachineNet.Port")
				.setPreferredSize(new Dimension(79, 0));
		ptf = ntf(String.valueOf(sc.link_port));// sc.port
		ptf.setPreferredSize(new Dimension(60, 30));

		nrip();
		final CButton conbtn = nbtn("Action.Connect");
		conbtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveLink();
				link(sc.link_ip_address, sc.link_port);

				conbtn.setEnabled(false);
			}
		});

		final CButton disconbtn = nbtn("Action.Disconnect");
		disconbtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dislink();

				conbtn.setEnabled(true);
			}
		});

		nrip();
		nlbld("M.Utility.MachineNet.PowerTips").setPreferredSize(
				new Dimension(Define.def.DockFrameWidth, 110));

		localizeSelf();
	}

	@Override
	public void beforeLeave() {
		saveLink();
	}

	private void saveLink() {
		iptfs.saveIP2Array(sc.link_ip_address);
		int pt = 0;
		try {
			pt = Integer.parseInt(ptf.getText());
		} catch (Exception ex) {
		}
		sc.link_port = pt;
	}

	private void link(final byte[] ip, final int pt) {
		final AbsInterCommunicator interComm = Platform.getControlApps().interComm;

		new Thread() {
			@Override
			public void run() {
				cm.sourceManager.connectNet(ip, pt);
				interComm.initMachine(new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						DBG.outprintln(evt.getPropertyName() + ": "
								+ evt.getNewValue());
					}
				});
			}
		}.start();
	}

	private void dislink() {
		Platform.getControlApps().releaseConnect();
	}
}
