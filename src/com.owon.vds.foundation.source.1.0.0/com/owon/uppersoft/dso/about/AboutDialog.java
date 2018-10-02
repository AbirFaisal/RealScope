package com.owon.uppersoft.dso.about;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.pref.Define;
//import com.owon.uppersoft.dso.update.action.UpdateAction;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.dso.view.TitlePane;
import com.owon.uppersoft.dso.view.pane.dock.widget.TitleBar;
import com.owon.uppersoft.vds.core.aspect.IBoard;
import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.core.pref.Config;
import com.owon.uppersoft.vds.core.pref.StaticPref;
//import com.owon.uppersoft.vds.core.update.IUpdateAction;
import com.owon.uppersoft.vds.ui.layout.OneColumnLayout;
import com.owon.uppersoft.vds.ui.layout.OneRowLayout;
import com.owon.uppersoft.vds.ui.resource.SwingResourceManager;
import com.owon.uppersoft.vds.ui.widget.custom.CButton;
import com.owon.uppersoft.vds.ui.widget.custom.CLabel;
import com.owon.uppersoft.vds.ui.widget.custom.LButton;
import com.owon.uppersoft.vds.ui.window.ComponentMover;
//import com.sun.awt.AWTUtilities;

public class AboutDialog implements Localizable {
	private static final int outorc = 25, inorc = 24, orcw = 15, orch = 15;
	private static final int fw = 550, fh = 150 + 40 + 40;
	private JDialog frame;
	private StaticPref pre;
	private CLabel title;
	//private CButton updatebtn;
	private JLabel version, machine, copyright, weblb;
	private IBoard sc;
	private JLabel vslbl;

	private static AboutDialog instance;

	public static void handleAboutInstance(Config conf, ControlManager cm) {
		if (instance == null || !instance.frame.isDisplayable()) {
			instance = new AboutDialog(Platform.getMainWindow(), conf, cm);
			cm.getLocalizeCenter().addLocalizable(instance);
		} else {
			instance.frame.toFront();
		}
	}

	public JDialog getAboutDlg() {
		return frame;
	}

	//private IUpdateAction updateAct;

	public AboutDialog(MainWindow mw, Config cf, ControlManager cm) {
		//this.updateAct = UpdateAction.getUpdateAction();
		Window owner = mw.getFrame();
		pre = cf.getStaticPref();
		initialize(owner, cm);
		new ComponentMover(frame, frame);// 拖拽特定区域移动窗口
		frame.setUndecorated(true);// 去除边框

		frame.setResizable(false);
		frame.setVisible(true);
		// WindowUtil.ShapeWindow(frame, Define.def.WND_SHAPE_ARC_2);//
		//The border is rounded, and the second part is rounded.
		//AWTUtilities.setWindowOpaque(frame, false);
		frame.setOpacity(1f);
	}

	private void initialize(Window owner, final ControlManager cm) {
		ResourceBundle rb = I18nProvider.bundle();
		Font ft = Define.def.snapshotfont_en;
		// if (I18nProvider.locale().getLanguage().equals(
		// Locale.CHINESE.getLanguage())) {
		// ft = Define.def.snapshotfont;
		// }
		frame = new JDialog(owner);// , JDialog.DEFAULT_MODALITY_TYPE
		owner.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				close();
			}
		});
		JPanel cp = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				int w = getWidth(), h = getHeight();
				Graphics2D g2d = (Graphics2D) g;
				g2d.setColor(Define.def.CO_DockBorder);
				g2d.setStroke(Define.def.Stroke2);
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.drawRoundRect(1, 1, w - 3, h - 3, outorc, outorc);
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_OFF);
			}
		};
		cp.setLayout(new BorderLayout(0, 0));
		frame.setContentPane(cp);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JPanel tp = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				int w = getWidth(), h = getHeight();
				Graphics2D g2d = (Graphics2D) g;
				g2d.setPaint(TitleBar.GradientTexturePaint);
				g2d.fillRect(2, h - orch, orcw, orch);
				g2d.fillRect(w - 17, h - orch, orcw, orch);
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.fillRoundRect(2, 2, w - 4, h - 2, inorc, inorc);
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_OFF);
			}
		};
		tp.setLayout(new OneRowLayout(new Insets(5, 5, 5, 5), 0));
		new ComponentMover(frame, tp);
		title = new CLabel();
		title.setPreferredSize(new Dimension(fw - 50, 30));

		LButton closecb = new LButton();
		ImageIcon close = SwingResourceManager.getIcon(TitlePane.class,
				TitlePane.ClosePath);
		closecb.setIcon(close);
		close = SwingResourceManager.getIcon(TitlePane.class,
				TitlePane.Close_pPath);
		closecb.setPressedIcon(close);
		closecb.setPreferredSize(new Dimension(38, 24));
		closecb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});

		tp.add(title);
		tp.add(closecb);

		JPanel gp = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				int w = getWidth(), h = getHeight();
				Graphics2D g2d = (Graphics2D) g;
				g2d.setColor(Define.def.style.CO_DockContainer);
				g2d.fillRect(2, 0, orcw, orch);
				g2d.fillRect(w - 17, 0, orcw, orch);
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.fillRoundRect(2, 0, w - 4, h - 2, inorc, inorc);
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_OFF);
			}
		};
		gp.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));

		JPanel leftPane = new JPanel();
		leftPane.setOpaque(false);
		leftPane.setLayout(new OneColumnLayout());
		leftPane.setPreferredSize(new Dimension(fw, 90));// / 2
		// JPanel rightPane = new JPanel();
		// rightPane.setOpaque(false);
		// rightPane.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		// rightPane.setPreferredSize(new Dimension(fw / 2 - 60, 90));

		sc = cm.getSoftwareControl();
		Dimension lbsz = new Dimension(fw / 2, 25);
		/** 软件版本信息 */
		version = new JLabel();
		version.setFont(ft);
		version.setPreferredSize(lbsz);
		version.setForeground(Color.WHITE);
		leftPane.add(version);

		vslbl = new JLabel();
		vslbl.setFont(ft);
		vslbl.setPreferredSize(lbsz);
		vslbl.setForeground(Color.WHITE);
		leftPane.add(vslbl);

		/** 机器信息 */
		// machine = new JLabel();
		// machine.setFont(ft);
		// machine.setPreferredSize(lbsz);
		// machine.setForeground(Color.WHITE);
		// rightPane.add(machine);

		// boolean isMachCon = cm.sourceManager.isConnected();// true;//
		//
		// String ver_bios, ver_os, ver_fpga;
		// if (isMachCon) {
		// ver_bios = "    " + sc.ver_bios;
		// ver_os = "    " + sc.ver_os;
		// ver_fpga = "    " + sc.ver_fpga;
		// } else {
		// ver_bios = "    ";// + "None";
		// ver_os = null;
		// ver_fpga = null;
		// }
		// String[] machInfos = { ver_bios, ver_os, ver_fpga };
		// for (int i = 0; i < machInfos.length; i++) {
		// JLabel lb = new JLabel(machInfos[i]);
		// lb.setFont(ft);
		// lb.setPreferredSize(lbsz);
		// lb.setForeground(Color.WHITE);
		// rightPane.add(lb);
		// }

		// copyright = new JLabel();
		// copyright.setFont(ft);
		// copyright.setForeground(Color.WHITE);

		weblb = new JLabel(getWebsite());
		weblb.setFont(ft);
		weblb.setForeground(Color.WHITE);
		weblb.setPreferredSize(new Dimension(fw, 20));

//		updatebtn = new CButton();
//		updatebtn.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				//updateAct.updateAction();
//				// cm.restartWhenMismatching(101);
//			}
//		});

		gp.add(leftPane);
		// gp.add(rightPane);
		// gp.add(copyright);
		gp.add(weblb);
		//if (pre.isOwon()) gp.add(updatebtn);
		cp.add(tp, BorderLayout.NORTH);
		cp.add(gp, BorderLayout.CENTER);

		frame.setSize(fw, fh);
		frame.setLocationRelativeTo(owner);

		localize(rb);
	}

	public void close() {
		if (frame != null) {
			frame.dispose();
			frame = null;
		}
		instance = null;
	}

	private String getWebsite() {
		String website = pre.getWebsite();// "http://www.owon.com.hk";
		return "<html><font><u>" + website + "<u></font></html>";
	}

	public void localize(ResourceBundle rb) {
		String ver = sc.getBoardVersion(), ser = sc.getBoardSeries();
		// ver = "V2.0.12";
		// ser = "VDS2062131252365";
		String titlelabel = rb.getString("About.Name") + " "
				+ pre.getManufacturerId() + " " + pre.getProductId() + " "
				+ pre.getVersionText();
		if (pre.isNeutral())
			titlelabel = rb.getString("About.Name") + " "
					+ pre.getVersionText();
		title.setText(titlelabel);
		version.setText(rb.getString("About.Version") + ver);
		vslbl.setText(rb.getString("About.SN") + ser);
		// machine.setText(rb.getString("About.Machine"));

		// copyright.setText(rb.getString("About.Copyright"));
		weblb.setText(getWebsite());
		//updatebtn.setText(rb.getString("Action.Update"));

	}
}
