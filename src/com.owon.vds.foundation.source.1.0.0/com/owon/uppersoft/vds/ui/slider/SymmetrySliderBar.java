package com.owon.uppersoft.vds.ui.slider;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ResourceBundle;

import javax.swing.JDialog;
import javax.swing.JPanel;

import com.owon.uppersoft.vds.ui.paint.LineDrawTool;
import com.owon.uppersoft.vds.ui.resource.SwingResourceManager;
import com.sun.awt.AWTUtilities;

/**
 * SymmetryCSliderBar，The special scroll bar at the center of the slider,
 * the speed at which the positive and negative offset control values change
 * 
 * @author Matt
 * 
 */
public class SymmetrySliderBar extends JPanel implements MouseListener,
		MouseMotionListener, Runnable {
	public static final AlphaComposite composite80 = LineDrawTool
			.getAlphaComposite(0.8f);
	public static final AlphaComposite composite100 = LineDrawTool
			.getAlphaComposite(1f);

	public static final String ImageDirectory = "/com/owon/uppersoft/vds/ui/slider/image/";
	
	public static final Image thumbv = SwingResourceManager.getIcon(
			SymmetrySliderBar.class, ImageDirectory + "thumbv.png").getImage();

	public static final Image sliderbgv = SwingResourceManager.getIcon(
			SymmetrySliderBar.class, ImageDirectory + "sliderbgv.png")
			.getImage();
	public static final Image sliderbgh = SwingResourceManager.getIcon(
			SymmetrySliderBar.class, ImageDirectory + "sliderbgh.png")
			.getImage();
	public static final Image thumbh = SwingResourceManager.getIcon(
			SymmetrySliderBar.class, ImageDirectory + "thumbh.png")
			.getImage();

	public static final int sliderheight = 178, sliderwidth = 19,
			thumbwidth = 16, thumbheight = 24;

	private Dimension sz;
	private Color sco = Color.DARK_GRAY;
	private boolean sliderSelected = false;

	private int rangeLength;
	private int thumbLength;
	private int tPos;
	private int thumbRange;

	private int lastv;
	private int middlePos;
	private int middle0;

	private ISliderView sv;
	private boolean vertical;

	public SymmetrySliderBar(ISliderView sv, Dimension sz, boolean vertical) {
		this.sz = new Dimension(sz);
		setPreferredSize(sz);
		this.vertical = vertical;
		setOpaque(false);
		this.sv = sv;

		addMouseListener(this);
		addMouseMotionListener(this);

		rangeLength = vertical ? sz.height : sz.width;
		thumbLength = thumbheight;
		thumbRange = rangeLength - thumbLength;
		middle0 = rangeLength >> 1;
		middlePos = (thumbRange) >> 1;
		resetSpos();
	}

	private final void resetSpos() {
		tPos = middlePos;
	}

	private final int getMousePos(MouseEvent e) {
		return vertical ? e.getY() : e.getX();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (!sliderSelected)
			return;
		int v = getMousePos(e);

		int pos = tPos + v - lastv;

		if (pos < 0)
			pos = 0;
		else if (pos > thumbRange)
			pos = thumbRange;

		lastv = lastv + pos - tPos;
		tPos = pos;

		repaint();

		if (t == null) {
			t = new Thread(this);
			t.start();
		}
	}

	/**
	 *
	 * Use the sliderSelected variable to distinguish whether the slider is clicked or not, and if it is selected,
	 * it will monitor the drag event and accumulate the displacement of the drag.
	 * Unchecked automatically accumulates the mouse's offset from the center until increment is set to zero.
	 * Both operations have their own threads running mutually exclusive, using the same variable.
	 * When the mouse is released, the thread referenced by this variable is joined.
	 *
	 */

	private int increment = 0;

	@Override
	public void mousePressed(MouseEvent e) {
		int v = getMousePos(e);
		sliderSelected = false;
		if (v < middlePos) {
			increment = -1;
		} else if (v >= (middlePos + thumbLength)) {
			increment = 1;
		} else {
			increment = 0;
			sliderSelected = true;
		}

		if (sliderSelected) {
			lastv = v;
		} else {
			if (t == null) {
				final int inc = increment;
				t = new Thread() {
					@Override
					public void run() {
						try {
							if (inc != 0) {
								sv.adjustAdd(inc);
								repaint();
								Thread.sleep(500);
							}
							while (increment != 0) {
								Thread.sleep(100);
								// System.out.println("inc:"+inc);
								sv.adjustAdd(inc);
								repaint();
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				};
				t.start();
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		increment = 0;
		sliderSelected = false;
		if (t != null) {
			try {
				t.join();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			t = null;
		}
		resetSpos();

		sv.actionOff();

		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		if (vertical)
			g.drawImage(sliderbgv, 0, 0, null);
		else {
			g.drawImage(sliderbgh, 0, 0, null);
		}

		sv.paintSelf(g2d);

		g2d.setComposite(composite80);
		g.setColor(sco);
		if (vertical)
			g.drawImage(thumbv, 1, tPos, null);// sz.width, thumbLength
		else
			g.drawImage(thumbh, tPos, 1, null);
		g2d.setComposite(composite100);
	}

	public void run() {
		try {
			while (sliderSelected) {
				Thread.sleep(50);

				int del = tPos - middlePos;
				// System.out.println("del"+del);
				sv.adjustAdd(del);
				repaint();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private Thread t = null;

	@Override
	public void mouseClicked(MouseEvent e) {
		boolean b = e.getClickCount() == 2
				&& e.getButton() == MouseEvent.BUTTON1;
		if (!b)
			return;
		int v;
		if (vertical) {
			v = e.getY();
		} else {
			v = e.getX();
		}
		if (v >= middlePos && v <= middlePos + thumbLength) {
			resetSpos();
			sv.setDefault();
			repaint();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * 给视窗设定或视窗扩展使用的View，由外部提供
	 * 
	 * @param wd
	 * @param x
	 * @param y
	 * @param vertical
	 * @param isv
	 * @param pcl
	 * @return
	 */
	public static final JDialog createAssistZoomViewSliderFrame(Window wd,
			int x, int y, boolean vertical, ISliderView isv, ResourceBundle rb) {
		return createCViewSliderFrame(wd, x, y, vertical, isv, rb);
	}

	/**
	 * 由外部指派VIew的控件
	 * 
	 * @param wd
	 * @param x
	 * @param y
	 * @param vertical
	 * @param isv
	 * @param pcl
	 * @return
	 */
	public static final JDialog createCViewSliderFrame(Window wd, int x, int y,
			boolean vertical, ISliderView isv, ResourceBundle rb) {
		int w, h;
		if (vertical) {
			w = sliderwidth;
			h = sliderheight;
		} else {
			w = sliderheight;
			h = sliderwidth;
		}
		final JDialog dlg = new JDialog(wd);
		dlg.setUndecorated(true);
		dlg.setLayout(null);
		dlg.setBounds(x, y, w, h);
		Dimension sz = new Dimension(w, h);

		final SymmetrySliderBar csb = new SymmetrySliderBar(isv, sz, vertical);

		csb.setBounds(0, 0, dlg.getWidth(), dlg.getHeight());

		FocusListener fa = FocusControlButton.addShareFocusControlButtons(isv,
				csb, dlg, false, rb);

		dlg.addFocusListener(fa);
		AWTUtilities.setWindowOpaque(dlg, false);
		dlg.setVisible(true);
		dlg.requestFocus();

		return dlg;
	}

	/**
	 * 非对称控件，快捷按钮只有归零
	 * 
	 * @param wd
	 * @param x
	 * @param y
	 * @param min
	 * @param max
	 * @param vertical
	 * @param v
	 * @param pcl
	 * @return
	 */
	public static final JDialog createUnsymmetrySliderFrame(Window wd, int x,
			int y, int min, int max, boolean vertical, int v,
			final SliderDelegate pcl, ResourceBundle rb) {
		int w, h;
		if (vertical) {
			w = sliderwidth;
			h = sliderheight;
		} else {
			w = sliderheight;
			h = sliderwidth;
		}
		final JDialog dlg = new JDialog(wd);
		dlg.setUndecorated(true);
		dlg.setBounds(x, y, w << 1, h);

		Dimension sz = new Dimension(w, h);
		SliderView sv = new SliderView(min, max, v, pcl);
		final SymmetrySliderBar csb = new SymmetrySliderBar(sv, sz, vertical);
		csb.setBounds(0, 0, w, h);
		FocusListener fa = FocusControlButton.addShareFocusControlButtons(sv,
				csb, dlg, SliderDelegate.BtnStatus0, pcl, false, rb);

		dlg.addFocusListener(fa);
		AWTUtilities.setWindowOpaque(dlg, false);
		dlg.setVisible(true);
		dlg.requestFocus();

		return dlg;
	}

	/**
	 * 对称控件
	 * 
	 * @param wd
	 * @param x
	 * @param y
	 * @param halfRange
	 *            对称View中的半长
	 * @param defaultValue
	 *            默认值
	 * @param value
	 * @param vertical
	 * @param co
	 * @param BtnStatus
	 * @param pcl
	 * @return
	 */
	public static final JDialog createSymmetrySliderFrame(Window wd, int x,
			int y, int halfRange, int defaultValue, int value,
			boolean vertical, Color co, int BtnStatus,
			final SliderDelegate pcl, ResourceBundle rb) {
		int w, h;
		if (vertical) {
			w = sliderwidth;
			h = sliderheight;
		} else {
			w = sliderheight;
			h = sliderwidth;
		}
		final JDialog dlg = new JDialog(wd);
		dlg.setUndecorated(true);
		dlg.setBounds(x, y, w, h);
		Dimension sz = new Dimension(w, h);

		SymmetrySliderView sv = new SymmetrySliderView(sz,
				(halfRange << 1) + 1, defaultValue, value, vertical, co, pcl);
		final SymmetrySliderBar csb = new SymmetrySliderBar(sv, sz, vertical);
		csb.setBounds(0, 0, w, h);

		FocusListener fa = FocusControlButton.addShareFocusControlButtons(sv,
				csb, dlg, BtnStatus, pcl, true, rb);

		dlg.addFocusListener(fa);
		AWTUtilities.setWindowOpaque(dlg, false);
		dlg.setVisible(true);
		dlg.requestFocus();
		return dlg;
	}

	private static final int INFOBLOCK = 1, LABEL = 2, EDGEPANE = 3,
			PULSEPANE = 4, SLOPEPANE = 5, DETAILPANE = 7;

	// private static Point getSliderBarLocation(MouseEvent e, int type) {
	// // int x, int y, int xOnScreen, int yOnScreen, int width
	// int x = e.getX();
	// int y = e.getY();
	// int xs = e.getXOnScreen(), ys = e.getYOnScreen();
	// int w = e.getComponent().getWidth();
	// int bw = InfoBlock.BlockWidth;
	//
	// switch (type) {
	// case INFOBLOCK:
	// x = bw + xs - x;
	// y = InfoBlock.floor2 + ys - y - sliderheight + 10;
	// break;
	// case LABEL:
	// int scrWidth = MainWindow.MAX_WIDTH;
	// x = xs - x + bw + 5;
	// if (x >= scrWidth - sliderwidth)
	// x -= bw + (sliderwidth);
	// y = 25 + ys - y - sliderheight;
	// break;
	// case EDGEPANE:
	// x = w + xs - x + 5;
	// y = ys - y - sliderheight + 20;
	// break;
	// case PULSEPANE:
	// x = w + xs - x + 5;
	// y = ys - y - sliderheight + 20;
	// break;
	// case SLOPEPANE:
	// x = w + xs - x - 20;
	// y = ys - y - sliderheight + 20;
	// break;
	// case DETAILPANE:
	// x = xs - x - 25;
	// y = ys - y + DetailPane.floor1 + 5;
	// break;
	// }
	//
	// return new Point(x, y);
	// }

	/**
	 * Values from one coordinate (in vertical center, up and down negative)
	 * to values in another coordinate (from top to 0, increasing downward)
	 * 
	 * @param halfRange
	 * @param v
	 * @return
	 */
	private static final int symmetry2zeroDown(int halfRange, int v) {
		return halfRange - v;
	}

	// /**
	// * 从一种坐标上的数值(以垂直中心，上正下负)到另一种坐标上的数值(从顶端为0，向下递增)
	// *
	// * 供通道零点转换使用
	// *
	// * @param halfRange
	// * @param ci
	// * @return
	// */
	// private static final int s2z_Pos0(int halfRange, ChannelInfo ci) {
	// return symmetry2zeroDown(halfRange, ci.getPos0());
	// }

	/**
	 * Values from one coordinate (in vertical center, up and down negative) to values in another coordinate (from top to 0, increasing downward)
	 * 
	 * For channel trigger level or threshold upper and lower limit conversion
	 * 
	 * @param ts
	 * @param ci
	 * @return
	 */
	// private static final int s2z_Voltsense(int halfRange, TriggerSet ts) {
	// return symmetry2zeroDown(halfRange, ts.getVoltsense().);
	// }
	public static void main(String args[]) {
		// createSymmetrySliderFrame(null, 20, 20, 100, 80, 1000, false,
		// Color.RED, BtnStatusNO, new SliderAdapter() {
		// });
	}
}
