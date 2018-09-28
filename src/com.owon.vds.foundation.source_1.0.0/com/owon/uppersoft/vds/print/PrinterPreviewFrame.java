package com.owon.uppersoft.vds.print;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.core.aspect.help.IPrintable;

public class PrinterPreviewFrame implements Localizable {

	private static PrinterPreviewFrame instance;

	public static void handlePrinterPreview(MainWindow mw, ControlManager cm) {
		if (instance == null || !instance.isDisplayable()) {
			instance = new PrinterPreviewFrame(mw.getChartScreen(),
					mw.getWindow(), cm.ppc);
			cm.getLocalizeCenter().addLocalizable(instance);
		} else {
			instance.toFront();
		}
	}

	public static void selfRepaint() {
		if (instance != null)
			instance.getPFrame().repaint();
	}

	private static final String PRINT_MESSAGE = "Print.Message";
	private static final String PRINT_SET_PRE_PAGE_BACK = "Print.SetPrePageBack";
	private static final String PRINT_SHOW_WAVE_BACK = "Print.ShowWaveBack";
	private static final String PRINT__DEFAULT_SCALE = "Print._DefaultScale";
	private static final String PRINT_FACE_SIZE = "Print.FaceSize";
	private static final String PRINT_WHOLE_PAGE = "Print.WholePage";
	private static final String PRINT_PAGE_TRANSFORM = "Print.PageTransform";
	private static final String PRINT_EXIT = "Print.Exit";
	private static final String PRINT_PRINT = "Print.Print";
	private static final String PRINT_PAGE_SET = "Print.PageSet";
	private static final String PRINT_VIEW = "Print.View";
	private static final String PRINT_FILE = "Print.File";
	private static final String PRINT_PRINT_PREVIEW_FRAME = "Print.PrintPreviewFrame";

	private JFrame frame;
	private CustomScaleWindow customScaWin;
	private PageEdgeSetup pageEdgeSet;
	private IPrintable chartScreen;
	private Window mw;
	public PrinterPreviewControl ppc;

	/**
	 * Create the application
	 */
	public PrinterPreviewFrame(IPrintable chartScreen, Window mw,
			PrinterPreviewControl ppc) {
		this.mw = mw;
		this.chartScreen = chartScreen;
		this.ppc = ppc;
		initialize();
		frame.setLocationRelativeTo(null);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		localize(PrinterPreviewControl.bundle());
		frame.setVisible(true);
	}

	private JMenuBar jMenuBarOne;
	private JMenu fileMenu, viewMenu;

	private JCheckBoxMenuItem showWFBGmenuItem;// show Wave BackGround !!!!
												// checkMenuItem
	private JMenuItem paperBGMenuItem;// set paper BackGround
	private JMenuItem customizeMenuItem;// Default Size
	private JMenuItem realPageMenuItem; // face Size
	private JMenuItem wholePageMenuItem;
	private JCheckBoxMenuItem hvtransformMenuItem;// horizontal、vertical
													// transform !!!!
													// checkMenuItem

	private JMenuItem pageSetupMenuItem;
	private JMenuItem printMenuItem;
	private JMenuItem exitMenuItem;

	double dpiScale;
	int dpi;
	double scale = 1;
	double defaultEdgeSpace = 20;// 默认页边距为20毫米
	public static final double inchPerMM = 25.4;
	public static final int A4PaperXPixels = 2480;
	public static final int A4PaperYPixels = 3508;
	public static final int A4PaperBorderTrim = 75;
	public static final int DefaultPrinterDPI = 300;
	public static final int A4PaperClientXPixels = 2330;
	public static final int A4PaperClientYPixels = 3358;
	private int A4EdgeSpace, A4PageWidth, A4PageHeight;
	private String message, title;

	/**
	 * Initialize the contents of the frame
	 */
	private void initialize() {
		frame = new JFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				instance = null;
			}
		});
		mw.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				if (frame != null) {
					frame.dispose();
					frame = null;
				}
				instance = null;
			}
		});
		frame.setBounds(0, 20, 1000, 728);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		dpi = Toolkit.getDefaultToolkit().getScreenResolution();
		dpiScale = (double) dpi / (double) DefaultPrinterDPI;

		A4EdgeSpace = (int) (defaultEdgeSpace / inchPerMM * dpi);
		// (int)(defaultEdgeSpace*inchPerMM* dpi);

		if (ppc.isVertical) {
			// vertical
			A4PageWidth = (int) (A4PaperXPixels * dpiScale);
			A4PageHeight = (int) (A4PaperYPixels * dpiScale);
		} else {
			// horizontal
			A4PageWidth = (int) (A4PaperYPixels * dpiScale);
			A4PageHeight = (int) (A4PaperXPixels * dpiScale);
		}

		jMenuBarOne = new JMenuBar();
		/*
		 * 
		 * 创建几个JMenu对象
		 */

		fileMenu = new JMenu();
		viewMenu = new JMenu();
		frame.setJMenuBar(jMenuBarOne);
		jMenuBarOne.add(fileMenu);
		jMenuBarOne.add(viewMenu);

		pageSetupMenuItem = new JMenuItem();
		pageSetupMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pageEdgeSet = new PageEdgeSetup(getPrinterPreviewFrame());
			}
		});
		printMenuItem = new JMenuItem();
		printMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (ppc.isPaintWFBG) {
					int response = JOptionPane.showConfirmDialog(frame,
							message, title, JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if (response == 0) {
						ppc.isPaintWFBG = true;
					} else if (response == 1) {
						ppc.isPaintWFBG = false;
						scrollPanel.repaint();
					} else if (response == 2 || JOptionPane.CLOSED_OPTION < 0) {
						return;
					}
				}
				Printer p = new Printer(getPrinterPreviewFrame());
			}
		});
		exitMenuItem = new JMenuItem();
		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});

		fileMenu.add(pageSetupMenuItem);
		fileMenu.add(printMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(exitMenuItem);

		hvtransformMenuItem = new JCheckBoxMenuItem();
		hvtransformMenuItem.setSelected(!ppc.isVertical);
		hvtransformMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setHVPageTransform();
			}
		});

		wholePageMenuItem = new JMenuItem();
		wholePageMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				double temp = (double) (scrollPanel.getSize().height)
						/ A4PageHeight;
				setScale(temp);
			}
		});
		realPageMenuItem = new JMenuItem();
		realPageMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setScale(1);
			}
		});
		customizeMenuItem = new JMenuItem();
		customizeMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				customScaWin = new CustomScaleWindow(getPrinterPreviewFrame());
			}
		});
		showWFBGmenuItem = new JCheckBoxMenuItem();
		showWFBGmenuItem.setSelected(ppc.isPaintWFBG);
		showWFBGmenuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				ppc.isPaintWFBG = !ppc.isPaintWFBG;
				scrollPanel.repaint();
			}

		});
		paperBGMenuItem = new JMenuItem();
		paperBGMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setA4PageBackground();
				scrollPanel.repaint();
			}
		});

		viewMenu.add(hvtransformMenuItem);
		viewMenu.addSeparator();
		viewMenu.add(wholePageMenuItem);
		viewMenu.add(realPageMenuItem);
		viewMenu.add(customizeMenuItem);
		viewMenu.addSeparator();
		viewMenu.add(showWFBGmenuItem);
		viewMenu.add(paperBGMenuItem);

		scrollPanel = new MyJScrollPane(frame);

		fileMenu.setMnemonic('F');
		viewMenu.setMnemonic('V');
		pageSetupMenuItem.setMnemonic('S');
		printMenuItem.setMnemonic('P');
		exitMenuItem.setMnemonic('E');
		hvtransformMenuItem.setMnemonic('T');
		// wholePageMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_A);
		// realPageMenuItem.setMnemonic('R');
		customizeMenuItem.setMnemonic('D');
		showWFBGmenuItem.setMnemonic('W');
		// paperBGMenuItem.setMnemonic('P');

		reScroll();
	}

	public PrinterPreviewFrame getPrinterPreviewFrame() {
		return this;
	}

	public void setPageEdgeSpace(Rectangle r) {
		ppc.leftEdgeLength = r.x;
		ppc.rightEdgeLength = r.y;
		ppc.upEdgeLength = r.width;
		ppc.downEdgeLength = r.height;
		reScroll();
	}

	public void toFront() {
		frame.toFront();
		reScroll();
	}

	public boolean isDisplayable() {
		return frame.isDisplayable();
	}

	public JFrame getPFrame() {
		return frame;
	}

	public Rectangle getPageEdgeSpace() {
		return new Rectangle(ppc.leftEdgeLength, ppc.rightEdgeLength,
				ppc.upEdgeLength, ppc.downEdgeLength);
	}

	public void setScale(double value) {
		scale = value;
		reScroll();
	}

	public double getScale() {
		return scale;
	}

	public void setA4PageBackground() {
		Color c = JColorChooser
				.showDialog(this.frame, "Demo", A4PageBackground);
		if (c != null)
			A4PageBackground = c;
	}

	public void setHVPageTransform() {
		if (ppc.isVertical == true) {
			ppc.isVertical = false;
		} else {
			ppc.isVertical = true;
		}
		frame.dispose();
		this.initialize();
		frame.setLocationRelativeTo(null);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		localize(PrinterPreviewControl.bundle());
		frame.setVisible(true);
	}

	public void localize(ResourceBundle bdl) {
		frame.setTitle(bdl.getString(PRINT_PRINT_PREVIEW_FRAME));
		fileMenu.setText(bdl.getString(PRINT_FILE));
		viewMenu.setText(bdl.getString(PRINT_VIEW));
		pageSetupMenuItem.setText(bdl.getString(PRINT_PAGE_SET));
		printMenuItem.setText(bdl.getString(PRINT_PRINT));
		exitMenuItem.setText(bdl.getString(PRINT_EXIT));
		hvtransformMenuItem.setText(bdl.getString(PRINT_PAGE_TRANSFORM));
		wholePageMenuItem.setText(bdl.getString(PRINT_WHOLE_PAGE));
		realPageMenuItem.setText(bdl.getString(PRINT_FACE_SIZE));
		customizeMenuItem.setText(bdl.getString(PRINT__DEFAULT_SCALE));
		showWFBGmenuItem.setText(bdl.getString(PRINT_SHOW_WAVE_BACK));
		paperBGMenuItem.setText(bdl.getString(PRINT_SET_PRE_PAGE_BACK));
		message = bdl.getString(PRINT_MESSAGE);

		if (customScaWin != null)
			customScaWin.localize(bdl);
		if (pageEdgeSet != null)
			pageEdgeSet.localize(bdl);
	}

	private void reScroll() {
		scrollPanel.reScroll();
		scrollPanel.repaint();
	}

	Color A4PageBackground = Color.white;
	MyJScrollPane scrollPanel;

	class Mypanel extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7620249394827072345L;

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;

			// 画最外圈的黑色矩形框
			g2d.drawRect((getWidth() - (int) (A4PageWidth * scale)) / 2, 0,
					(int) (A4PageWidth * scale), (int) (A4PageHeight * scale));

			// 画最打印纸张背景色
			g2d.setColor(A4PageBackground);
			g2d.fillRect(
					(scrollPanel.getPanel().getSize().width - (int) (A4PageWidth * scale)) / 2 + 1,
					0 + 1, (int) (A4PageWidth * scale) - 1,
					(int) (A4PageHeight * scale) - 1);

			// 设置红色虚线
			float[] dash2 = { 6.0f, 4.0f, 2.0f, 4.0f, 2.0f, 4.0f };
			BasicStroke bs = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
					BasicStroke.JOIN_MITER, 10.0f, dash2, 0.0f);
			g2d.setStroke(bs);
			g2d.setColor(Color.red);

			// 画虚线
			int x = (int) (scrollPanel.getPanel().getSize().width / 2 - scale
					* A4PageWidth / 2 + scale
					* (ppc.leftEdgeLength / inchPerMM * dpi));
			int y = (int) (ppc.upEdgeLength / inchPerMM * dpi * scale);
			int width = (int) (A4PageWidth * scale - scale
					* (ppc.leftEdgeLength / inchPerMM * dpi + ppc.rightEdgeLength
							/ inchPerMM * dpi));
			int height = (int) (A4PageHeight * scale - scale
					* (ppc.upEdgeLength / inchPerMM * dpi + ppc.downEdgeLength
							/ inchPerMM * dpi));
			g2d.drawRect(x, y, width, height);

			// 移动原点到波形图区域
			g2d.translate(x, y);
			// 绘画波形背景
			drawChartViewBG(g2d, width);
			// 绘画波形
			float stroke;
			// if (scale < 0.5)
			// stroke = 4.0f;
			// else
			stroke = 0.0f;
			g2d.setStroke(new BasicStroke(stroke, BasicStroke.CAP_SQUARE,
					BasicStroke.JOIN_ROUND));
			chartScreen.printView(g2d, width, height);
		}
	}

	public void printChartView(Graphics2D g2d, int width, int height) {
		chartScreen.printView(g2d, width, height);
	}

	public void drawChartViewBG(Graphics2D g2d, int width) {
		if (ppc.isPaintWFBG) {
			g2d.setColor(Color.black);
			int curheight = width * chartScreen.getHeight()
					/ chartScreen.getWidth();
			g2d.fillRect(0, 0, width, curheight);
		}
	}

	public class MyJScrollPane extends JScrollPane {

		/**
		 * 
		 */
		private static final long serialVersionUID = 584209475191317835L;
		private Mypanel panel = new Mypanel();

		public MyJScrollPane(JFrame jf) {
			jf.getContentPane().add(this, BorderLayout.CENTER);
			this.setPreferredSize(new Dimension(100, 100));
			this.setViewportView(panel);
			setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			// panel.setLayout(null);
			setVisible(true);
		}

		public JPanel getPanel() {
			return panel;
		}

		private void reScroll() {
			int w = 0;
			if (A4PageWidth * scale < scrollPanel.getSize().width)
				w = scrollPanel.getSize().width;
			else
				w = (int) (A4PageWidth * scale);

			int h = 0;
			if (A4PageHeight * scale < scrollPanel.getSize().height)
				h = scrollPanel.getSize().height;
			else
				h = (int) (A4PageHeight * scale);
			panel.setPreferredSize(new Dimension(w, h));
			scrollPanel.doLayout();
		}
	}
}
