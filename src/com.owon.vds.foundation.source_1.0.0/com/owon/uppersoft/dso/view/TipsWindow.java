package com.owon.uppersoft.dso.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.LineBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.pref.Define;
import com.owon.uppersoft.dso.view.pane.dock.widget.TitleBar;
import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.ui.layout.OneRowLayout;
import com.owon.uppersoft.vds.ui.resource.FontCenter;
import com.owon.uppersoft.vds.ui.resource.ImagePaintUtil;
import com.owon.uppersoft.vds.ui.resource.ResourceCenter;
import com.owon.uppersoft.vds.ui.resource.SwingResourceManager;
import com.owon.uppersoft.vds.ui.widget.custom.CCheckBox;
import com.owon.uppersoft.vds.ui.widget.custom.CLabel;
import com.owon.uppersoft.vds.ui.window.ComponentMover;
import com.sun.awt.AWTUtilities;

public class TipsWindow implements Localizable {

	private static TipsWindow instance;

	public static void showTipsWin(MainWindow mw, ControlManager cm) {
		if (cm.istipsWindowShow) {
			onoffTipsWin(mw, cm);
		}
	}

	public static void onoffTipsWin(MainWindow mw, ControlManager cm) {
		if (instance == null) {
			instance = new TipsWindow(mw);
			cm.getLocalizeCenter().addLocalizable(instance);
		} else {
			instance.close();
		}
	}

	public static TexturePaint getGradualTexturePaint(int x0, int y0,
			int width, int height) {
		int x1 = 0, y1 = height;
		GradientPaint p = new GradientPaint(x0, y0,
				Define.def.style.CO_TitleBarTop, x1, y1,
				Define.def.style.CO_TitleBarBottom, true);
		TexturePaint gtp = ImagePaintUtil.getTexturePaint(p, width, height);
		return gtp;
	}

	private JDialog tipsDlg;
	private CLabel empty, title;
	private CCheckBox hiddenCb;
	private final int windowWidth = 600, windowHeight = 580,
			titlePaneHeight = 62, basePaneHeight = windowHeight
					- titlePaneHeight, titlelblWidth = 330, leftgap = 15,
			rightgap = 41;
	private final int emptyHeight = 22, titleHeight = 15;
	private Color Co_Title, Co_Tabbed, Co_Content, Co_EditorPaneBG;
	private ImageIcon lightIcon = SwingResourceManager.getIcon(TitlePane.class,
			ResourceCenter.IMG_DIR + "light.png");
	private ImageIcon closeIcon = SwingResourceManager.getIcon(TitlePane.class,
			ResourceCenter.IMG_DIR + "close.png");

	private String locale;
	private final String CHLHTML = "chl1.htm";
	private final String DETHTML = "per1.htm";
	private final String TRGHTML = "trg1.htm";
	private final String SCUTSHTML = "shortcuts.htm";

	private ControlManager cm;

	public TipsWindow(MainWindow mw) {
		this.cm = mw.getDataHouse().controlManager;
		prepare();
		creatDlg(mw);
		init();
		new ComponentMover(tipsDlg, tipsDlg);
		AWTUtilities.setWindowOpaque(tipsDlg, false);
		// WindowUtil.ShapeWindow(tipsDlg, 27);
	}

	private void prepare() {
		Co_Title = Define.def.style.CO_DockTitle;
		Co_Tabbed = Define.def.CO_DockBorder;
		Co_Content = Define.def.style.CO_DockContainer;
		Co_EditorPaneBG = Color.WHITE;
		// locale = Locale.getDefault().toString() + "/";
	}

	private void creatDlg(MainWindow mw) {
		if (mw == null) {
			tipsDlg = new JDialog();
			tipsDlg.setBounds(300, 350, windowWidth, windowHeight);
		} else {
			tipsDlg = new JDialog(mw.getFrame(), false);// 模式化true
			tipsDlg.setSize(windowWidth, windowHeight);
		}
		tipsDlg.setLayout(new BorderLayout());
		tipsDlg.setUndecorated(true);
		tipsDlg.setAlwaysOnTop(true);
		tipsDlg.setResizable(false);
		tipsDlg.setLocationRelativeTo(null);
	}

	private void init() {
		JPanel titlePane = getTitlePane();
		JPanel basePane = getBasePane();
		tipsDlg.add(titlePane, BorderLayout.NORTH);
		tipsDlg.add(basePane, BorderLayout.CENTER);

		localize(I18nProvider.bundle());
		tipsDlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		tipsDlg.setVisible(true);
	}

	private JPanel getTitlePane() {
		JPanel titlePane = new JPanel();
		titlePane.setLayout(new BorderLayout());
		titlePane.setPreferredSize(new Dimension(windowWidth, titlePaneHeight));
		titlePane.setOpaque(false);

		final int x0 = 15, y0 = emptyHeight;
		CLabel icon = new CLabel() {
			@Override
			protected void paintComponent(Graphics g) {
				int width = getWidth(), height = getHeight();
				Graphics2D g2d = (Graphics2D) g;
				g2d.setColor(Co_Tabbed);
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.fillRoundRect(x0, y0, width + 10, height + 10, 50, 50);
				g2d.setPaint(TipsWindow.getGradualTexturePaint(0, y0, 1, y0
						+ TitleBar.H));// 设置画笔颜色渐变
				g2d.fillRoundRect(x0 + 2, y0 + 2, width + 10, height + 10, 50,
						50);
				g2d.drawImage(lightIcon.getImage(), 0, 0, null);
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_OFF);
				g2d.dispose();
			}
		};
		CLabel close = new CLabel() {
			@Override
			protected void paintComponent(Graphics g) {
				int width = getWidth(), height = getHeight();
				Graphics2D g2d = (Graphics2D) g;
				g2d.setColor(Co_Tabbed);
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.fillOval(0, 5, 32, 32);
				g2d.fillRect(0, y0, width - 16, height);
				g2d.setPaint(TipsWindow.getGradualTexturePaint(0, y0, 1, y0
						+ TitleBar.H));// 设置画笔颜色渐变
				g2d.fillRect(0, y0 + 2, width - 18, height);

				g2d.drawImage(closeIcon.getImage(), 0, 5, null);
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_OFF);
				g2d.dispose();
			}
		};
		empty = new CLabel();
		title = new CLabel();
		hiddenCb = new CCheckBox();
		icon.setIcon(lightIcon);
		close.setIcon(closeIcon);

		empty.setPreferredSize(new Dimension(0, emptyHeight));
		title.setPreferredSize(new Dimension(titlelblWidth, titleHeight));
		// hiddenCb.setOpaque(false);
		// hiddenCb.setFocusable(false);

		JPanel tipContainer = new JPanel();
		tipContainer.setLayout(new BorderLayout());
		tipContainer.setOpaque(false);
		JPanel tipp = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				int width = getWidth(), height = getHeight();
				Graphics2D g2d = (Graphics2D) g;
				g2d.setColor(Co_Tabbed);
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.fillRect(0, 0, width, 5);
				g2d.setPaint(TitleBar.GradientTexturePaint);// 设置画笔颜色渐变
				g2d.fillRect(0, 2, width, height);
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_OFF);
			}
		};
		tipp.setLayout(new OneRowLayout());
		tipp.setBackground(Co_Title);

		tipp.add(title);
		tipp.add(hiddenCb);
		tipContainer.add(empty, BorderLayout.NORTH);
		tipContainer.add(tipp, BorderLayout.CENTER);

		titlePane.add(close, BorderLayout.EAST);
		titlePane.add(icon, BorderLayout.WEST);
		titlePane.add(tipContainer, BorderLayout.CENTER);
		close.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				close();
			}
		});
		hiddenCb.setSelected(!cm.istipsWindowShow);
		hiddenCb.setForeground(Color.WHITE);
		hiddenCb.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean b = hiddenCb.isSelected();
				cm.istipsWindowShow = !b;
				if (b)
					close();
			}
		});

		return titlePane;
	}

	private String chlbl, dtlbl, trglbl, stclbl;
	private JTabbedPane tbp;
	private JEditorPane chlp, detp, trgp, stcp;

	private JPanel getBasePane() {

		JPanel basePane = new JPanel();
		basePane.setPreferredSize(new Dimension(windowWidth, basePaneHeight));
		basePane.setLayout(new FlowLayout(FlowLayout.LEFT, leftgap, 0));
		basePane.setOpaque(false);

		JPanel tabbedContainer = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {

				int width = getWidth(), height = getHeight();
				Graphics2D g2d = (Graphics2D) g;
				g2d.setColor(Co_Tabbed);
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.fillRoundRect(0, 0, width, height, 25, 25);
				g2d.fillRect(0, 0, 10, 10);
				g2d.fillRect(width - 10, 0, 10, 10);
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_OFF);
			}
		};
		tabbedContainer.setBackground(Co_Tabbed);

		tbp = new JTabbedPane();
		tbp.setPreferredSize(new Dimension(windowWidth - rightgap,
				basePaneHeight - 10));
		chlp = creatEditorpane(getHtmlPath() + CHLHTML);
		detp = creatEditorpane(getHtmlPath() + DETHTML);
		trgp = creatEditorpane(getHtmlPath() + TRGHTML);
		stcp = creatEditorpane(getHtmlPath() + SCUTSHTML);

		chlp.setBackground(Co_Content);
		detp.setBackground(Co_Content);
		trgp.setBackground(Co_Content);
		stcp.setBackground(Co_Content);

		tbp.addTab(chlbl, chlp);
		tbp.addTab(dtlbl, detp);
		tbp.addTab(trglbl, trgp);
		tbp.addTab(stclbl, stcp);

		tabbedContainer.add(tbp);
		basePane.add(tabbedContainer);
		return basePane;
	}

	private JEditorPane creatEditorpane(final String url) {
		JEditorPane chlp = null;
		chlp = new JEditorPane();
		chlp.setSelectionColor(Co_EditorPaneBG);
		chlp.setBorder(new LineBorder(Define.def.style.CO_DockTitle) {
			@Override
			public void paintBorder(Component c, Graphics g, int x, int y,
					int width, int height) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(Co_Tabbed);
				g2.fillRect(x, y + height - 5, 5, 5);
				g2.fillRect(x + width - 5, y + height - 5, 5, 5);
				g2.setColor(Co_EditorPaneBG);
				g2.fillRoundRect(x, y + height - 15, 15, 15, 15, 15);
				g2.fillRoundRect(x + width - 15, y + height - 15, 15, 15, 15,
						15);
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_OFF);
				g2.dispose();
			}
		});
		// chlp.setContentType("text/html");
		// setEditorPaneContent(chlp, url);//localize会加入
		/*-
		 * 利用setEditable()方法将JEditorPane设为不可编辑.请注意，这行是相当重要的，若是我们将这个方法设为true,
		 * 我们将会失去HTML文件本身的特性，如超级链接的功能等等。因此一般都会将编辑的功能取消(设置false).目前这个超
		 * 级链接功能并没有作用，这部份将在JEditorPane的事件处理中介绍.
		 */
		chlp.setEditable(false);
		final JEditorPane edp = chlp;
		chlp.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					try {
						// System.out.println(e.getDescription());
						// System.out.println(e.getURL());
						edp.setPage(e.getURL());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		return chlp;
	}

	private void setEditorPaneContent(JEditorPane edp, final String htmlURL) {
		String contentURL = getHtmlPath() + htmlURL;
		File file = new File(contentURL);
		if (!file.exists()) {
			setLocalePath(Locale.ENGLISH);
			contentURL = getHtmlPath() + htmlURL;
			file = new File(contentURL);
		}

		String str = file.getAbsolutePath();// 取得文件位置的绝对路径
		str = "file:" + str;// 将绝对路径加入URL.protocol合成一完整的URL输入字符串
		/* 利用setPage()方法将字符串中路径所指的文件加载JEditorPane. */
		try {
			edp.setPage(str);
		} catch (IOException ioe) {
			// ioe.printStackTrace();
			// System.exit(0);
			// DBG.outprintln("file:" + contentURL + " is not found");
		}
	}

	public void close() {
		tipsDlg.dispose();
		tipsDlg = null;
		instance = null;
	}

	@Override
	public String toString() {
		return super.toString();
	}

	public void localize(ResourceBundle rb) {
		title.setFont(FontCenter.getBigtitlefont());
		title.setText(rb.getString("M.TipsWindow.Tips"));

		hiddenCb.setFont(FontCenter.getLabelFont());
		hiddenCb.setText(rb.getString("M.TipsWindow.Hidden"));

		chlbl = rb.getString("M.Channel.Name");
		dtlbl = rb.getString("M.TipsWindow.Detail");
		trglbl = rb.getString("M.Trg.Name");
		stclbl = rb.getString("M.TipsWindow.Shortcuts");

		tbp.setFont(Define.def.tipsTabbed);
		tbp.setTitleAt(0, chlbl);
		tbp.setTitleAt(1, dtlbl);
		tbp.setTitleAt(2, trglbl);
		tbp.setTitleAt(3, stclbl);

		setLocalePath(Locale.getDefault());
		setEditorPaneContent(chlp, CHLHTML);
		setEditorPaneContent(detp, DETHTML);
		setEditorPaneContent(trgp, TRGHTML);
		setEditorPaneContent(stcp, SCUTSHTML);
	}

	private void setLocalePath(Locale l) {
		locale = l.toString();
		// System.out.println("setlocale:" + locale);
	}

	private String getHtmlPath() {
		String folder = cm.docManager.getExternalDoc();
		String s = folder + locale + "/";
		// System.out.println("html:" + s);
		return s;
	}

	public static void main(String[] args) {
		// Define.prepare(0);
		// Locale loc = new Locale("en");
		// Locale.setDefault(loc);
		// new TipsWindow(null);

		// String local=Locale.GERMANY.toString() ;
		// System.out.println(local);
	}
}
