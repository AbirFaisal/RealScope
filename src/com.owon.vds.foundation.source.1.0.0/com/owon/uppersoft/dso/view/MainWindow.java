package com.owon.uppersoft.dso.view;

import static com.owon.uppersoft.dso.pref.Define.def;
import static com.owon.uppersoft.vds.ui.window.WindowUtil.ShapeWindow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

import com.owon.uppersoft.dso.control.ChartScreenMouseGesture;
import com.owon.uppersoft.dso.control.RightScreenGesture;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.WorkBench;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.model.trigger.TrgCheckType;
import com.owon.uppersoft.dso.pref.Define;
import com.owon.uppersoft.dso.pref.Style;
import com.owon.uppersoft.dso.source.comm.TrgStatus;
//import com.owon.uppersoft.dso.update.action.UpdateAction;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.view.pane.dock.MarkValueBulletin;
import com.owon.uppersoft.dso.wf.ChartScreenSelectModel;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.core.aspect.control.ITimeControl;
import com.owon.uppersoft.vds.socket.server.ServerControl;
import com.owon.uppersoft.vds.ui.layout.OneColumnLayout;
import com.owon.uppersoft.vds.ui.prompt.PromptDialog;
import com.owon.uppersoft.vds.ui.prompt.Promptable;
import com.owon.uppersoft.vds.ui.resource.FontCenter;
import com.owon.uppersoft.vds.ui.resource.ResourceCenter;
import com.owon.uppersoft.vds.ui.resource.SwingResourceManager;
import com.owon.uppersoft.vds.ui.window.ExtendedStataJFrame;
import com.owon.uppersoft.vds.util.LocalizeCenter;

/**
 * MainWindow，界面主框架
 * 
 */
public class MainWindow implements Localizable, Promptable {

	public static final String bgPath = (Define.def.style.path + Style.MWBGimgname);
	public static final Image bg = SwingResourceManager.getIcon(
			MainWindow.class, bgPath).getImage();

	public static final int MAX_HEIGHT;
	public static final int MAX_WIDTH;

	static {
		Dimension sz = Toolkit.getDefaultToolkit().getScreenSize();
		MAX_WIDTH = sz.width;
		MAX_HEIGHT = sz.height;
	}

	private JFrame frame;
	private JPanel basePane;

	private ToolPane toolPane;

	private WorkBench wb;
	private DataHouse dh;
	private ChartScreen chartScreen;
	private TitlePane titlePane;

	@Override
	public Window getWindow() {
		return frame;
	}

	@Override
	public boolean isVisible() {
		return frame.isVisible();
	}

	private ControlManager cm;

	public MainWindow(WorkBench wb, DataHouse dh) {
		this.wb = wb;
		this.dh = dh;
		cm = dh.controlManager;

		initialize();

		// updateAct = new UpdateAction(cm, frame);
		// cm.getLocalizeCenter().addLocalizable(updateAct);
		//UpdateAction.handleUpdateAction(cm, frame);
	}

	public DataHouse getDataHouse() {
		return dh;
	}

	/**
	 * @return 顶级框架
	 */
	public JFrame getFrame() {
		return frame;
	}

	public void onDispose() {
		wb.exit();
		if (dh != null) {
			dh.closePersistence();// 关闭余辉线程
		}
		cm.scpiServer.destroyServer();
	}

	/**
	 * 大小改变时的调整
	 */
	public void exchangeSize() {
		if (frame.getExtendedState() != Frame.MAXIMIZED_BOTH) {
			frame.setExtendedState(Frame.MAXIMIZED_BOTH);
			ShapeWindow(frame, 0);
		} else {
			frame.setExtendedState(Frame.NORMAL);
			ShapeWindow(frame, def.WND_SHAPE_ARC);
		}
	}

	public void minSize() {
		frame.setExtendedState(Frame.ICONIFIED);
	}

	private void initialize() {
		frame = new ExtendedStataJFrame();
		keyAdapter = new CKeyAdapter(this, dh.controlManager);
		// frame.addKeyListener(keyAdapter);
		frame.setFocusable(true);

		final KeyEventDispatcher ked = new KeyEventDispatcher() {
			@Override
			public boolean dispatchKeyEvent(KeyEvent e) {
				if (e.getSource() instanceof JTextComponent)
					return false;
				if (e.getID() == KeyEvent.KEY_PRESSED && frame.isDisplayable()) {
					keyAdapter.keyPressed(e);
				}
				// System.err.println(e.paramString());
				// System.err.println(e.toString());
				return false;
			}
		};

		KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.addKeyEventDispatcher(ked);

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onDispose();
				KeyboardFocusManager.getCurrentKeyboardFocusManager()
						.removeKeyEventDispatcher(ked);
				super.windowClosing(e);
			}

			@Override
			public void windowClosed(WindowEvent e) {
				onDispose();
				KeyboardFocusManager.getCurrentKeyboardFocusManager()
						.removeKeyEventDispatcher(ked);
			}
		});

		ImageIcon imgIcon = new ImageIcon(getClass().getResource(
				ResourceCenter.IMG_DIR + "launcher.png"));
		frame.setIconImage(imgIcon.getImage());
		new DropTarget(frame, DnDConstants.ACTION_COPY_OR_MOVE,
				new CDropTargetAdapter(dh));

		// final int b = Define.def.Frame_Border_Width, w =
		// Define.def.FRM_Width, h = Define.def.FRM_Height;
		// , bgh = bg.getHeight(null), delh = h - bgh;

		/* 在根容器之上的唯一基础容器 */
		basePane = new JPanel();
		basePane.setBackground(Define.def.CO_FRM_Border);
		basePane.setLayout(new BorderLayout());
		basePane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {

				int cw = chartScreen.getWidth(), ch = chartScreen.getHeight();
				double dfw = GDefine.AREA_WIDTH + def.AREA_W_BOUNDREST, dfh = def.CHART_HEIGHT;
				dh.xRate = cw / dfw;
				dh.yRate = ch / dfh;

				chartScreen.resizelayout();
				basePane.repaint();
				cm.pcs.firePropertyChange(
						PropertiesItem.UPDATE_MARKBULLETIN_BOUND, null, null);
			}
		});

		frame.setContentPane(basePane);
		// new ShellResizer(frame, basePane);

		titlePane = new TitlePane(this, new Dimension(0, def.TITLE_HEIGHT));
		basePane.add(titlePane, BorderLayout.NORTH);
		chartScreen = new ChartScreen(this, dh, new Dimension(0, 521));
		// chartScreen.setSize(new Dimension(GDefine.AREA_WIDTH +
		// def.AREA_W_BOUNDREST, def.CHART_HEIGHT));
		basePane.add(chartScreen, BorderLayout.CENTER);
		toolPane = createToolPane(new Dimension(0, 173), cm);
		basePane.add(toolPane, BorderLayout.SOUTH);

		LocalizeCenter lc = cm.getLocalizeCenter();
		lc.addLocalizable(titlePane);
		lc.addLocalizable(chartScreen);
		lc.addLocalizable(toolPane);
		lc.addLocalizable(this);

		/** 暂时关闭，因为存储深度的combobox的滚轮事件也会被接收 */
		// frame.addMouseWheelListener(new MouseWheelListener() {
		// @Override
		// public void mouseWheelMoved(MouseWheelEvent e) {
		// if (dh.isDMLoad() && !dh.isRuntime()) {
		// int i = e.getWheelRotation();
		// toolPane.getDetailPane().nextTimeBase(i);}}});
		cm.fireFFTonoff2EnableMainWindow();

		cm.pcs.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				String pn = evt.getPropertyName();
				if (pn.equals(ITimeControl.onTimebaseUpdated)) {
					update_Timebase();
				}
			}
		});

		lc.reLocalize(I18nProvider.bundle());
		frame.setSize(def.PreferredSize);
		// ShapeWindow(frame, def.WND_SHAPE_ARC);
		// AWTUtilities.setWindowOpacity(frame, 0.93f);
		frame.setLocationRelativeTo(null);
		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		glassPane = new GlassPane();
		frame.setGlassPane(glassPane);
		glassPane.setVisible(false);

		// frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		// ShapeWindow(frame, 0);
	}

	protected ToolPane createToolPane(Dimension sz, ControlManager cm) {
		return new ToolPane(this, sz, cm);
	}

	public JPanel getGlassPane() {
		return glassPane;
	}

	private GlassPane glassPane;

	public void prompt(JComponent jp, Runnable r) {
		glassPane.prompt(jp, r);
	}

	public void promptUp() {
		glassPane.promptUp();
	}

	public void promptClose() {
		glassPane.promptClose();
	}

	private final class GlassPane extends JPanel {
		public GlassPane() {
			setOpaque(false);
			setLayout(null);

			pp = new PromptDialog(Define.def.TITLE_HEIGHT);
			add(pp);
		}

		PromptDialog pp;

		public void prompt(JComponent jp, Runnable r) {
			setVisible(true);
			pp.prompt(jp, r);
		}

		public void promptUp() {
			if (isVisible())
				pp.promptUp();
		}

		public void promptClose() {
			pp.promptClose();
			setVisible(false);
		}
	}

	public ChartScreenSelectModel getChartChannelSelectModel() {
		return chartScreen.getChartScreenSelectModel();
	}

	protected final ChartScreenMouseGesture getChartScreenMouseGesture() {
		return chartScreen.getChartScreenMouseGesture();
	}

	protected final RightScreenGesture getRightScreenGesture() {
		return getChartScreenMouseGesture().getRightScreenGesture();
	}

	private boolean isRuntime() {
		return cm.isRuntime();
	}

	public void update_HorTrg() {
		titlePane.updateView();
		if (isRuntime()) {
			re_paint();
		} else {
			chartScreen.rebuffer();
		}
	}

	private void update_Timebase() {
		titlePane.updateView();
		if (isRuntime()) {
			re_paint();
		} else {
			chartScreen.rebuffer();
		}
	}

	public void update_Pos0() {
		if (isRuntime()) {
			re_paint();
		} else {
			chartScreen.rebuffer();
		}
	}

	public void update_ChangeVoltsense(int chl) {
		ChartScreenSelectModel rsg = getChartChannelSelectModel();
		rsg.update_ChangeLevel(chl, TrgCheckType.NotOver);

		WaveFormManager wfm = dh.getWaveFormManager();
		if (wfm.setSelectedWaveForm(chl)) {
			toolPane.getInfoPane().updateSelected(chl);
		}
		re_paint();
	}

	public void update_DoneVoltsense() {
		ChartScreenSelectModel rsg = getChartChannelSelectModel();
		rsg.update_DoneLevelChange();

		re_paint();
	}

	public void update_ChangeUppLow(int chl, TrgCheckType type) {
		ChartScreenSelectModel rsg = getChartChannelSelectModel();
		rsg.update_ChangeLevel(chl, type);

		WaveFormManager wfm = dh.getWaveFormManager();
		if (wfm.setSelectedWaveForm(chl)) {
			toolPane.getInfoPane().updateSelected(chl);
		}
		re_paint();
	}

	public void update_DoneUppLow() {
		ChartScreenSelectModel rsg = getChartChannelSelectModel();
		rsg.update_DoneLevelChange();

		re_paint();
	}

	/**
	 * 重新缓冲画图
	 * 
	 * 新的数据内容引起的更新，数据来得慢也能及时刷新
	 */
	public void updateShow() {
		chartScreen.rebuffer();
	}

	public void re_paint() {
		chartScreen.re_paint();
	}

	public void updateGridColor() {
		chartScreen.updateGridColor();
	}

	private MarkValueBulletin mvb;

	/**
	 * 显示
	 */
	public void show() {
		frame.setVisible(true);
		// System.err.println(frame.getBounds());
		TipsWindow.showTipsWin(this, cm);
		mvb = new MarkValueBulletin(this);
	}

	public void channelOnOffRepaint(WaveForm wf) {
		toolPane.getInfoPane().updateInfo(wf);
		toolPane.updateTrgVolt();
		toolPane.getDetailPane().doUpdateSampleRate();
		re_paint();
	}

	/**
	 * @return 工具区
	 */
	public ToolPane getToolPane() {
		return toolPane;
	}

	public MarkValueBulletin getMarkValueBulletin() {
		return mvb;
	}

	public void updateInfo() {
		// toolPane.updateChannels();
		toolPane.getInfoPane().updateInfos2(dh.getWaveFormManager());
		toolPane.updateTrgVolt();
	}

	public void updateStatus(int c, TrgStatus ts) {
		titlePane.updateTrgStatus(c, ts);
	}

	public void updateStatus(TrgStatus ts) {
		titlePane.updateTrgStatus(ts);
	}

	public void updateDefaultAll() {
		re_paint();// chart
		updateInfo();// Channels , updateTrgVolt
		cm.pcs.firePropertyChange(ITimeControl.onTimebaseUpdated, null, null);
		// toolPane.getDetailPane().doupdateSampleRate();//SampleRate
		// updateTrgInfoPane();
	}

	/**
	 * @return 标题栏
	 */
	public TitlePane getTitlePane() {
		return titlePane;
	}

	/**
	 * @return 标题栏
	 */
	public ITitleStatus getITitleStatus() {
		return titlePane;
	}

	/**
	 * @return 主绘图区
	 */
	public ChartScreen getChartScreen() {
		return chartScreen;
	}

	@Override
	public void localize(ResourceBundle rb) {
		// fft/xy string then repaint
		basePane.repaint();

		// updateUI();
	}

	private CKeyAdapter keyAdapter;

	public CKeyAdapter getKeyAdapter() {
		return keyAdapter;
	}

}