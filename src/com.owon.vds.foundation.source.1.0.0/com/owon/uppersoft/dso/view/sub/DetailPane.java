package com.owon.uppersoft.dso.view.sub;

import static com.owon.uppersoft.dso.pref.Define.def;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.JPanel;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.CoreControl;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.mode.control.DeepMemoryControl;
import com.owon.uppersoft.dso.mode.control.TimeControl;
import com.owon.uppersoft.dso.pref.Define;
import com.owon.uppersoft.dso.pref.Style;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.core.aspect.control.ITimeControl;
import com.owon.uppersoft.vds.core.sample.SampleRate;
import com.owon.uppersoft.vds.core.zoom.AssitControl;
import com.owon.uppersoft.vds.data.Point;
import com.owon.uppersoft.vds.data.Range;
import com.owon.uppersoft.vds.ui.prompt.FadeIOShell;
import com.owon.uppersoft.vds.ui.resource.SwingResourceManager;
import com.owon.uppersoft.vds.ui.slider.SliderAdapter;
import com.owon.uppersoft.vds.ui.slider.SliderBarLocation;
import com.owon.uppersoft.vds.ui.slider.SymmetrySliderBar;
import com.owon.uppersoft.vds.ui.widget.ComboBoxOwner;
import com.owon.uppersoft.vds.ui.widget.OwnedComboBox;

public class DetailPane extends JPanel implements MouseListener,
		MouseMotionListener, MouseWheelListener, Localizable,
		SliderBarLocation, PropertyChangeListener {
	public static final String horposPath = (Define.def.style.path + Style.DetailPname);
	private static Image img = SwingResourceManager.getIcon(InfoBlock.class,
			horposPath).getImage();
	public static final int BlockHeigth = 103;
	public static final int BlockWidth = 126;

	public static final int floor0 = 22;
	public static final int floor1 = 47;
	public static final int floor2 = 72;
	public static final int floor3 = 96;

	private String timebase, trigPos, memdepth, sampling;
	private OwnedComboBox tbcbb, dmcbb;
	private ControlManager cm;
	private DataHouse dh;
	protected CoreControl cc;
	private TimeControl tc;
	private DeepMemoryControl dmc;

	public DetailPane(DataHouse dh) {
		this.dh = dh;
		cm = dh.controlManager;
		cc = cm.getCoreControl();
		tc = cc.getTimeControl();
		dmc = cc.getDeepMemoryControl();
		setLayout(null);
		setOpaque(false);
		setFont(Define.def.alphafont);

		// setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		updateInfo();
	}

	private String ht;

	public void updateInfo() {
		AssitControl ac = cm.getZoomAssctr();
		if (ac.isonZoom())
			ht = "W     ";
		else
			ht = "M      ";
		timebase = ht + tc.getTimebaseLabel() + " /" + cm.getDivUnit();
		trigPos = "T      " + tc.getHorizontalTriggerLabel();
		memdepth = "D      " + dmc.getDeepLabel();
		updateSampleRate();
	}

	public void updateDM_Sample() {
		memdepth = "D      " + dmc.getDeepLabel();
		updateSampleRate();

		repaint();
	}

	@Override
	public void localize(ResourceBundle rb) {
		timebase = ht + tc.getTimebaseLabel() + " /" + cm.getDivUnit();
	}

	public void doUpdateSampleRate() {
		updateSampleRate();
		repaint();
	}

	private void updateSampleRate() {
		String sr = cc.getCurrentSampleRate_Text();
		if (sr == null || sr.length() == 0) {
			sampling = "";
		} else {
			sampling = "S      " + sr;
		}
	}

	private int linePos = -1;

	@Override
	public void mouseMoved(MouseEvent e) {
		if (!enable)
			return;
		int y = e.getY();
		int l2 = -1;
		if (y >= 0 && y < floor0) {
			l2 = floor0;
		} else if (y >= floor0 && y < floor1) {
			l2 = floor1;
		} else if (y >= floor1 && y < floor2) {
			l2 = floor2;
		} else {
			l2 = floor3;
		}
		if (l2 != linePos) {
			linePos = l2;
			applyDetailPaneToolTip(linePos);

			// if (linePos == floor0 || linePos == floor1 || linePos == floor2)
			// setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			// else
			// setCursor(Cursor.getDefaultCursor());

			repaint();
		}
	}

	private void applyDetailPaneToolTip(int linePos) {
		ResourceBundle rb = I18nProvider.bundle();
		switch (linePos) {
		case floor0:
			setToolTipText(rb.getString("ToolTip.Timebase"));
			break;
		case floor1:
			setToolTipText(rb.getString("ToolTip.HorTrgPos"));
			break;
		case floor2:
			setToolTipText(rb.getString("ToolTip.DeepMemory"));
			break;
		case floor3:
			setToolTipText(rb.getString("ToolTip.Sampling"));
			break;
		default:
			break;
		}

	}

	/**
	 * 供快捷键使用的简单增减
	 * 
	 * @param del
	 */
	public void nextTimeBase(int del) {
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		boolean operatable = cm.isTimeOperatable();
		if (!operatable)
			return;

		int idx = tc.getTimebaseIdx();

		idx += del;
		changeTimeBase(idx);
	}

	public void removeTBOwnedComboBox() {
		if (tbcbb != null) {
			remove(tbcbb);
			tbcbb = null;
			repaint();
		}
	}

	public void changeTimeBase(int idx) {
		/** 暂时不在这里判断是否可操作，外层基本都有判断了 */
		boolean operatable = Platform.getControlApps().interComm
				.isTimeOperatableNTryGetDM();
		if (!operatable)
			return;
		// System.err.println(idx);
		tc.c_setTimebaseIdx(idx, true);
	}

	public void changeFFTTimeBase(int idx) {
		/** 这个给fft使用,不去获取深存储 */
		tc.c_setTimebaseIdx(idx, true);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (!enable)
			return;
		final int x = e.getX();
		final int y = e.getY();
		AssitControl ac = cm.getZoomAssctr();
		if (linePos == floor0) {
			if (ac.isonAssistSet()) {
				FadeIOShell pv = new FadeIOShell();
				pv.prompt(I18nProvider.bundle().getString("M.Zoom.Warn"),
						getWindow());
				return;
			}

			if (x < 30) {
				if (!AssitControl.FastMWstwich)
					return;
				/**
				 * KNOW A状态和Z状态下若调大Z时基，大到M时基跟着改变时， 存的mtbIdx 、mhtpz需重算;
				 * 已在astbcbb和zmtbcbb的keepZtbNotbiggerthanMtb()判断中重算。
				 */
				int dst = ac.isonZoom() ? AssitControl.MAIN_STATUS
						: AssitControl.ZOOM_STATUS;
				ac.changeZoomstatus(cm, dst);
				cm.pcs.firePropertyChange(PropertiesItem.ZOOMSELECTED, null,
						dst);
				if (tbcbb != null)
					tbcbb.onReleaseFocus();
				updateInfo();
				repaint();

			} else {
				// 如果连接暂停，调动tb,波形将变动，需要载入DM
				boolean operatable = Platform.getControlApps().interComm
						.isTimeOperatableNTryGetDM();
				if (!operatable)
					return;
				if (ac.isonZoom() && !AssitControl.FastMWstwich)
					return;
				requestFocus();
				if (tbcbb == null) {
					tbcbb = new OwnedComboBox(cc.getMachineInfo().TIMEBASE,
							tc.getTimebaseIdx(), new ComboBoxOwner() {
								@Override
								public void selected(int idx) {
									changeTimeBase(idx);
								}

								@Override
								public void removeOwnedComboBox() {
									removeTBOwnedComboBox();
								}

								@Override
								public void afterRemoved() {
									cm.pcs.firePropertyChange(
											PropertiesItem.DOCK_REPAINT, null,
											null);
								}
							}, true);
					tbcbb.setMaximumRowCount(12);
					add(tbcbb);
					tbcbb.setBounds(35, floor0 - 18, 80, 22);

					tbcbb.setSelectedIndex(tc.getTimebaseIdx());
					tbcbb.requestFocus();
					tbcbb.showPopup();
				}
			}
		} else if (linePos == floor2) {
			if (dmcbb == null && cc.getDeepProvider().getDeepNumber() > 1) {
				dmcbb = new OwnedComboBox(cc.getMachineInfo().DEEP,
						dmc.getDeepIdx(), new ComboBoxOwner() {
							@Override
							public void selected(int idx) {
								dmselected(idx);// dmc,
							}

							@Override
							public void removeOwnedComboBox() {
								if (dmcbb != null) {
									DetailPane.this.remove(dmcbb);
									dmcbb = null;
									DetailPane.this.repaint();
								}
							}

							@Override
							public void afterRemoved() {
							}
						}, true);
				add(dmcbb);
				dmcbb.setBounds(35, floor2 - 18, 80, 22);
				dmcbb.setSelectedIndex(dmc.getDeepIdx());
				dmcbb.requestFocus();
				dmcbb.showPopup();
			}
		} else if (linePos == floor1) {
			if (tbcbb != null)
				tbcbb.onReleaseFocus();
			if (dmcbb != null)
				dmcbb.onReleaseFocus();
			if (!cm.shouldAdjustHorTrgPos())
				return;

			Range p = tc.getHorTrgRange();

			Point point = getSliderBarLocation(x, y, e.getXOnScreen(),
					e.getYOnScreen(), e.getComponent());

			SymmetrySliderBar.createUnsymmetrySliderFrame(getWindow(), point.x,
					point.y, -p.left, -p.right, false,
					-tc.getHorizontalTriggerPosition(), new SliderAdapter() {

						@Override
						public void valueChanged(int oldV, int newV) {
							int v = -newV;
							tc.c_setHorizontalTriggerPosition(v);
							trigPos = "T       "
									+ tc.getHorizontalTriggerLabel();
							repaint();
							cm.mcctr.computeXValues();
							// Zoom下更新zhtp
							cm.getZoomAssctr().updateZoomHtp();
						}

						public void actionOff() {
							// 预留 System.err.println("value");
						}

					}, I18nProvider.bundle());
		} else {
		}

		if (linePos != floor2 && dmcbb != null)
			dmcbb.onReleaseFocus();
		if (linePos != floor0 && tbcbb != null)
			tbcbb.onReleaseFocus();
	}

	public static final int hllen = 120, hlhigh = 19;

	@Override
	protected void paintComponent(Graphics g) {
		// super.paintComponent(g);
		g.drawImage(img, 0, 0, null);
		int x = 10;

		if (linePos >= floor0 && linePos < floor3) {
			g.setColor(def.style.CO_INFOBLOCK_HIGHLIGHT);

			g.fillRect(3, linePos - 17, hllen, hlhigh);
		}
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		if (!enable)
			g.setColor(Color.gray);
		else
			g.setColor(Color.white);
		g.drawString(timebase, x, floor0);
		g.drawString(trigPos, x, floor1);
		g.drawString(memdepth, x, floor2);
		g.drawString(sampling, x, floor3);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// linePos = -1;
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (linePos == floor0) {
			int t = e.getWheelRotation();
			nextTimeBase(t);
		} else if (linePos == floor2) {
			int t = e.getWheelRotation();
			nextDMselected(t);
		}
	}

	private void nextDMselected(int del) {
		int idx = dmc.getDeepIdx();
		dmselected(dmc.restrictDMidx(idx + del));// dmc,
	};

	public void dmselected(int idx) {// DeepMemoryControl dmc,
		if (cm.displayControl.isXYModeOn()) {
			boolean should = DeepMemoryControl.restrictShouldChange(idx);
			if (should) {
				FadeIOShell pv = new FadeIOShell();
				pv.prompt(
						I18nProvider.bundle().getString(
								"Info.XYModeForceDeepMemory"), getWindow());

				return;
			}
		}

		dmc.c_setDeepIdx(idx);
		updateDM_Sample();

	}

	private Window getWindow() {
		return Platform.getMainWindow().getWindow();
	}

	@Override
	public Point getSliderBarLocation(int x, int y, int xs, int ys, Component cp) {
		x = xs - x - 25;
		y = ys - y + DetailPane.floor1 + 5;
		return new Point(x, y);
	}

	public void setDetailPaneEnable(boolean b) {
		// setCursor(Cursor.getPredefinedCursor(b ? Cursor.HAND_CURSOR
		// : Cursor.DEFAULT_CURSOR));
		enable = b;
		repaint();
	}

	private boolean enable;

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String pn = evt.getPropertyName();
		if (pn.equals(PropertiesItem.FFT_ON)) {
			setDetailPaneEnable(false);
			doUpdateSampleRate();
		} else if (pn.equals(PropertiesItem.FFT_OFF)) {
			setDetailPaneEnable(true);
			doUpdateSampleRate();
		} else if (pn.equals(ITimeControl.onTimebaseUpdated)) {
			updateInfo();
			repaint();
		} else if (pn.equals(ITimeControl.onHTPChanged)) {
			updateInfo();
			repaint();
		} else if (pn.equals(ITimeControl.onHorTrgPosChangedByTimebase)) {
			updateInfo();
			repaint();
		} else if (pn.equals(SampleRate.sampleRateUpdated)) {
			updateInfo();
			repaint();
		}
	}
}