package com.owon.uppersoft.dso.view.pane.dock;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.owon.uppersoft.dso.function.MarkCursorControl;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.page.MarkPage;
import com.owon.uppersoft.dso.pref.Define;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.view.ChartScreen;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.vds.core.paint.PaintContext;
import com.owon.uppersoft.vds.ui.resource.SwingResourceManager;
import com.owon.uppersoft.vds.ui.window.WindowChaser;
import com.sun.awt.AWTUtilities;

public class MarkValueBulletin extends JDialog implements MouseListener,
		PropertyChangeListener {

	private JFrame jf;
	private ControlManager cm;
	private MarkCursorControl mcc;
	private PaintContext pc;
	private ChartScreen cs;
	private boolean mouseOver;

	private Color background;
	private float diaphaneity;
	int width = 420, height = 55;

	public MarkValueBulletin(MainWindow mw) {
		super(mw.getFrame());
		this.cm = mw.getDataHouse().getControlManager();
		pc = cm.paintContext;
		mcc = cm.mcctr;
		jf = mw.getFrame();
		cs = mw.getChartScreen();

		// if (!jf.isVisible())//隐藏主界面
		// return;

		final int w = width, h = height;
				String maxBtnPath = "/com/owon/uppersoft/dso/image/markvalueshow.png";
		final Image maxBtnImg = SwingResourceManager.getIcon(
				MarkValueBulletin.class, maxBtnPath).getImage();
		background = Define.def.style.CO_DockContainer;
		diaphaneity = (float) 0.5;

		JPanel cp = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D) g;
				if (!mcc.ison) {
					g2d.drawImage(maxBtnImg, 3, h - 23, null);
					return;
				}
				paintBackGround(g2d, w, h);
				paintCloseIcon(g2d, getWidth());
				g2d.setColor(MarkCursorControl.Hue);
				mcc.updateMarkValue(g2d);
			}
		};
		setBound();
		setContentPane(cp);
		setUndecorated(true);
		setVisible(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		AWTUtilities.setWindowOpaque(this, false);

		addMouseListener(this);
		// addMouseMotionListener(this);
		cm.pcs.addPropertyChangeListener(this);
		jf.addComponentListener(new WindowChaser(jf, this));
		jf.requestFocus();
	}

	public void paintCloseIcon(Graphics2D g2d, int w) {
		int x0 = w - 16, x1 = w - 9;
		g2d.setColor(Color.gray);
		if (mouseOver) {
			g2d.setColor(Color.red);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			// g2d.fillOval(x0 - 2, 3, 12, 11);
			g2d.fillRoundRect(x0 - 2, 3, 12, 11, 5, 5);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);
			g2d.setColor(Color.white);
		}
		g2d.drawLine(x0, 5, x1, 10);
		g2d.drawLine(x1, 5, x0, 10);
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		int x0 = getWidth() - 18;
		int y0 = getHeight() - 25;
	if (x > x0 && y < 15) {
			mcc.closeMarkCursor();
		} else if (x < 25 && y > y0) {
			cm.getDockControl().dockDialogQuickOpenHide(MarkPage.Name);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		this.diaphaneity = (float) 0.7;
		mouseOver = true;
		repaint();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		this.diaphaneity = (float) 0.5;
		mouseOver = false;
		repaint();
	}

	public void setDiaphaneity(float v) {
		this.diaphaneity = v;
	}

	public void setBound() {
		Rectangle r = pc.getChartRectangle();
		Point lcs = cs.getLocationOnScreen();
		int x = lcs.x + (int) ((r.x) * DataHouse.xRate) - 2;
		int y = lcs.y + (int) ((r.height) * DataHouse.yRate) - 38;
		// System.out.println(x + "," + lc.y + "," + cs.getHeight());

		if (mcc.is3in1())
			y += (int) ((r.height + 5) * DataHouse.yRate);
		setBounds(x, y, width, height);
	}

	public void paintBackGround(Graphics2D g2d, int w, int h) {
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				diaphaneity));
		g2d.setColor(background);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.fillRoundRect(2, 0, w - 4, h - 8, 5, 5);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String name = evt.getPropertyName();
		if (name.equals(PropertiesItem.UPDATE_MARKBULLETIN)) {
			repaint();
		} else if (name.equals(PropertiesItem.UPDATE_MARKBULLETIN_BOUND)) {
			setBound();
		}
//		else if (name.equals(PropertiesItem.TURN_ON_MARKBULLETIN)) {
//			boolean on = (Boolean) evt.getNewValue();
//			mcc.turnOnMarkCursor(on);
//		}
	}

}