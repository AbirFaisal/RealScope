package com.owon.uppersoft.dso.function.measure;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.pref.Define;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.dso.view.pane.dock.widget.TitleBar;
import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.core.measure.MeasureElem;
import com.owon.uppersoft.vds.core.measure.MeasureT;
import com.owon.uppersoft.vds.core.measure.VR;
import com.owon.uppersoft.vds.ui.layout.OneColumnLayout;
import com.owon.uppersoft.vds.ui.layout.OneRowLayout;
import com.owon.uppersoft.vds.ui.paint.LineDrawTool;
import com.owon.uppersoft.vds.ui.resource.ResourceCenter;
import com.owon.uppersoft.vds.ui.resource.SwingResourceManager;
import com.owon.uppersoft.vds.ui.widget.custom.CComboBox;
import com.owon.uppersoft.vds.ui.widget.custom.CLabel;
import com.owon.uppersoft.vds.ui.widget.custom.LButton;
import com.owon.uppersoft.vds.ui.window.ComponentMover;
import com.owon.uppersoft.vds.util.LocalizeCenter;
import com.owon.uppersoft.vds.util.format.SFormatter;
import com.sun.awt.AWTUtilities;

public class MeasureSnapshot implements Localizable {

	public static final int outorc = 25, inorc = 24, orcw = 15, orch = 15;

	private static MeasureSnapshot instance;

	public static void closeMeasureSnapshot() {
		if (instance != null) {
			instance.close();
			instance = null;
		}
	}

	public static void handleSnapshot(LocalizeCenter lc, int idx,
			CComboBox Snapshot, ControlManager cm) {
		closeMeasureSnapshot();
		if (idx > 0) {
			instance = new MeasureSnapshot(cm, Snapshot, idx);
			lc.addLocalizable(instance);
		}
	}

	private MainWindow mw;
	private JDialog frame;
	private CLabel lab;
	private static final String MsgKeyPrefix = "AutoMeasure.", LabStr = "  CH";
	private final int MeaTHorEndIdx = 7;
	private JLabel[] chLabels;
	private JLabel[] othLabels;
	private String[] chPageItems;
	private VR[] vrs;
	private LinkedList<MeasureElem> OthMTlinked;
	private final String ClosePath = (ResourceCenter.IMG_DIR + "mclose.png");

	public JDialog getSnapshotFrame() {
		return frame;
	}

	public boolean isVisible() {
		return frame.isVisible();
	}

	public CLabel getShowCurLab() {
		return lab;
	}

	private int SnapshotCH;
	private ControlManager cm;

	public MeasureSnapshot(final ControlManager cm, final CComboBox Snapshot,
			int SnapshotCH) {
		this.cm = cm;
		this.SnapshotCH = SnapshotCH;
		this.mw = Platform.getMainWindow();
		preUpdateMeasure();

		initialize(cm, Snapshot);
		new ComponentMover(frame, frame);
		frame.setUndecorated(true);
		frame.setResizable(false);

		localize(I18nProvider.bundle());
		frame.setVisible(true);
	}

	private void preUpdateMeasure() {
		WaveFormManager wfm = Platform.getDataHouse().getWaveFormManager();
		vrs = wfm.getWaveForm(SnapshotCH - 1).getMeasureADC().vrs;
		OthMTlinked = cm.measMod.othMTlinked;
		MeasureManager mm = cm.getMeasureManager();
		boolean tmp = mm.ison();
		mm.setMeasureOn(true);
		cm.measure(wfm);
		mm.setMeasureOn(tmp);
	}

	private void initialize(ControlManager cm, final CComboBox Snapshot) {
		ResourceBundle rb = I18nProvider.bundle();
		final JFrame jf = mw.getFrame();
		frame = new JDialog(jf);
		frame.setTitle(rb.getString("M.Measure.SnapshotTitle"));
		mw.getFrame().addWindowListener(new WindowAdapter() {
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
		frame.setUndecorated(true);

		// contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JPanel tp = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				int w = getWidth(), h = getHeight();
				Graphics2D g2d = (Graphics2D) g;
				g2d.setPaint(TitleBar.GradientTexturePaint);
				// g2d.setColor(Define.def.style.CO_TitleBarButton);
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
		lab = new CLabel();
		lab.setPreferredSize(new Dimension(485, 30));

		tp.add(lab);

		LButton closecb = new LButton();

		ImageIcon close = SwingResourceManager.getIcon(MeasureSnapshot.class,
				ClosePath);
		closecb.setIcon(close);
		close = LineDrawTool.getRolloverIcon(close);
		closecb.setRolloverIcon(close);
		tp.add(closecb);
		closecb.setPreferredSize(new Dimension(38, 24));

		closecb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				Snapshot.setSelectedIndex(0);
			}
		});

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
		gp.setLayout(new OneColumnLayout(new Insets(20, 30, 20, 10), 10));

		chLabels = new JLabel[MeasureT.VALUES.length];
		chPageItems = new String[MeasureT.VALUES.length];

		Dimension mtsz = new Dimension(250, 30);

		JPanel horPane = new JPanel();
		horPane.setOpaque(false);
		horPane.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		horPane.setPreferredSize(new Dimension(500, 120));

		JPanel verPane = new JPanel();
		verPane.setOpaque(false);
		verPane.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		verPane.setPreferredSize(new Dimension(500, 150));

		int i = 0;
		for (MeasureT mt : MeasureT.VALUES) {
			String type = mt.toString();
			chPageItems[i] = MsgKeyPrefix + type;
			// type = rb.getString(chPageItems[i]);
			chLabels[i] = new JLabel();
			chLabels[i].setPreferredSize(mtsz);
			chLabels[i].setBackground(Color.GRAY);
			chLabels[i].setForeground(Color.WHITE);
			if (i <= MeaTHorEndIdx) {
				horPane.add(chLabels[i]);
			} else {
				verPane.add(chLabels[i]);
			}
			i++;
		}
		gp.add(horPane);
		gp.add(verPane);

		if (cm.getSupportChannelsNumber() >= 2) {
			JPanel othPane = new JPanel();
			othPane.setOpaque(false);
			othPane.setPreferredSize(new Dimension(420, 60));
			othPane.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

			int othMeaTypSize = cm.measMod.getOthLinkedCount();
			othLabels = new JLabel[othMeaTypSize];
			for (int p = 0; p < othMeaTypSize; p++) {
				othLabels[p] = new JLabel();
				othLabels[p].setPreferredSize(mtsz);
				othLabels[p].setBackground(Color.GRAY);
				othLabels[p].setForeground(Color.WHITE);
				othPane.add(othLabels[p]);
			}
			gp.add(othPane);
		}
		lab.setText(LabStr + SnapshotCH);
		cp.add(tp, BorderLayout.NORTH);
		cp.add(gp, BorderLayout.CENTER);

		frame.setSize(540, 370 + 40 + 20);
		frame.setLocationRelativeTo(null);

		// WindowUtil.ShapeWindow(frame, Define.def.WND_SHAPE_ARC_2);
		AWTUtilities.setWindowOpaque(frame, false);
	}

	public void localize(ResourceBundle rb) {
		frame.setTitle(rb.getString("M.Measure.SnapshotTitle"));
		Font ft = Define.def.snapshotfont_en;
		if (cm.isChineseLocale()) {
			ft = Define.def.snapshotfont;
		}
		for (int i = 0; i < chLabels.length; i++) {
			String type = rb.getString(chPageItems[i]);
			type = SFormatter.getRestrictSubString(type, 12);// 限定字符
			chLabels[i].setText(type + " = " + vrs[i].vu);
			chLabels[i].setFont(ft);
		}
		List<MeasureElem> othPageItems = cm.measMod.othMTlinked;
		if (othLabels != null)
			for (int i = 0; i < othLabels.length; i++) {
				String type = rb.getString(othPageItems.get(i).label);
				type = SFormatter.getRestrictSubString(type, 14);// 限定字符
				othLabels[i].setText(type + " = " + OthMTlinked.get(i).vu);
				othLabels[i].setFont(ft);
			}
	}

	public void close() {
		if (frame != null) {
			frame.dispose();
			frame = null;
		}
	}
}
