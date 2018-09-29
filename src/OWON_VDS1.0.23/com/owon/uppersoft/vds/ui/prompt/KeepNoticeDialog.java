package com.owon.uppersoft.vds.ui.prompt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.JDialog;
import javax.swing.JPanel;

import com.sun.awt.AWTUtilities;

public class KeepNoticeDialog extends JDialog {
	public static final int arc = 6;
	public static final int msgNum = 2;

	public static final Font font = new Font("SansSerif", Font.BOLD, 15);
	public static final Color CO_FADE_BG = new Color(60, 60, 60);

	private String msg = "";

	public KeepNoticeDialog(Window owner) {
		super(owner);
		setLayout(new BorderLayout());

		if (owner != null)
			owner.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					dispose();
				}
			});

		setUndecorated(true);
		setAlwaysOnTop(true);

		JPanel jp = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				// super.paintComponent(g);

				Graphics2D g2d = (Graphics2D) g;
				int wc, hc, x, y, w = getWidth(), h = getHeight(), tw, th;
				wc = w >> 1;
				hc = h >> 1;
				g2d.setFont(font);

				FontMetrics fm = g2d.getFontMetrics();
				Rectangle2D r2d = fm.getStringBounds(msg, getGraphics());
				tw = (int) r2d.getWidth();
				th = (int) r2d.getHeight();
				x = wc - (tw >> 1);
				y = hc - (th >> 1);
				g2d.setColor(Color.LIGHT_GRAY);
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);

				g2d.setColor(CO_FADE_BG);
				g2d.fillRoundRect(2, 2, w - 5, h - 5, arc, arc);
				g2d.setColor(Color.WHITE);
				// g2d.drawRoundRect(2, 2, w - 5, h - 5, arc, arc);
				g2d.drawString(msg, x, y + fm.getAscent());
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_OFF);
			}
		};
		add(jp, BorderLayout.CENTER);
		setFont(font);

		AWTUtilities.setWindowOpaque(this, false);
		setLocationRelativeTo(null);

		/** 改为无渐变效果的 */
		AWTUtilities.setWindowOpacity(this, 0.75f);
	}

	private void recomputeDialgSize() {
		FontMetrics fm = getFontMetrics(font);
		Rectangle2D r2d = fm.getStringBounds(msg, getGraphics());
		setSize((int) r2d.getWidth() + 30, (int) r2d.getHeight() + 30);

		// WindowUtil.ShapeWindow(this, arc);
		setLocationRelativeTo(null);
	}

	public void setMessage(String m) {
		msg = m;
		recomputeDialgSize();
	}

	/**
	 * 可持续显示更新提示的窗口，并在显示最后一条一段时间后自动关闭
	 * 
	 * 各个提示独立线程，分别累加mstime，然后休眠，在醒来后减去各自时间，最后一个减的会使mstime到零，然后触发关闭动作，
	 * 其间除了休眠都对对话框加锁
	 * 
	 * 为避免关闭动作设计对话框的引用置零而引入复杂的加锁逻辑，对话框只是隐藏而不关闭
	 * */
	public void keepShow() {
		keepShow(2000);
	}

	private int mstime = 0;

	public void keepShow(final int time) {
		new Thread() {
			@Override
			public void run() {
				nofading(time);
			}
		}.start();
	}

	private void nofading(int time) {
		synchronized (this) {
			setVisible(true);
			repaint();
			mstime += time;
		}

		int t = time;
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		synchronized (this) {
			mstime -= t;
			if (mstime <= 0) {
				setVisible(false);
				mstime = 0;
			}
		}
	}

	private boolean out;

	private void fading() {
		float v = 0f;
		out = false;
		AWTUtilities.setWindowOpacity(this, v += 0.1f);
		this.setVisible(true);
		try {
			while (v < 0.85 && !out) {
				AWTUtilities.setWindowOpacity(this, v);
				v += 0.1f;
				Thread.sleep(170);
			}
			Thread.sleep(1000);
			v -= 0.2f;
			while (v > 0.5 && !out) {
				AWTUtilities.setWindowOpacity(this, v);
				v -= 0.06f;
				Thread.sleep(260);
			}

			while (v > 0 && !out) {
				AWTUtilities.setWindowOpacity(this, v);
				v -= 0.08f;
				Thread.sleep(180);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		dispose();
	}
}