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

//import com.sun.awt.AWTUtilities;

public class NoticeDialog extends JDialog {
	public static final int arc = 6;
	public static final int msgNum = 2;

	public static final Font font = new Font("SansSerif", Font.BOLD, 15);
	public static final Color CO_FADE_BG = new Color(60, 60, 60);

	private String msg = "";

	public NoticeDialog(Window owner) {
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

		//AWTUtilities.setWindowOpaque(this, false);
		this.setBackground(Color.GRAY);

		setLocationRelativeTo(null);

		/** Changed to no gradient effect */
		//AWTUtilities.setWindowOpacity(this, 0.75f);
		this.setOpacity(0.75f);
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

	public void keepShow() {
		keepShow(2000);
	}

	public void keepShow(final int time) {
		new Thread() {
			@Override
			public void run() {
				nofading(time);
			}
		}.start();
	}

	private void nofading(int time) {
		setVisible(true);
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		synchronized (this) {
			dispose();
		}
	}

	private boolean out;

	private void fading() {
		float v = 0f;
		out = false;
		//AWTUtilities.setWindowOpacity(this, v += 0.1f);
		this.setOpacity(0.1f);

		this.setVisible(true);
		try {
			while (v < 0.85 && !out) {
				//AWTUtilities.setWindowOpacity(this, v);
				this.setOpacity(v);

				v += 0.1f;
				Thread.sleep(170);
			}
			Thread.sleep(1000);
			v -= 0.2f;
			while (v > 0.5 && !out) {
				//AWTUtilities.setWindowOpacity(this, v);
				this.setOpacity(v);
				v -= 0.06f;
				Thread.sleep(260);
			}

			while (v > 0 && !out) {
				//AWTUtilities.setWindowOpacity(this, v);
				this.setOpacity(v);
				v -= 0.08f;
				Thread.sleep(180);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		dispose();
	}
}