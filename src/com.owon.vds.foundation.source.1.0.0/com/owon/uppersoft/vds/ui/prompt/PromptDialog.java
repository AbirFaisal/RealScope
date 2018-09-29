package com.owon.uppersoft.vds.ui.prompt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.LineBorder;

import com.owon.uppersoft.vds.util.format.SFormatter;

/**
 * 可实现渐进式的提示
 * 
 */
public class PromptDialog extends JPanel implements IPopDown {
	public static final int RESHAPEPEROID = 100;
	public static final int RESHAPETIMES = 8;

	private int ytop;

	private Dimension maxSize = new Dimension(500, 400);

	public PromptDialog(int yt) {
		ytop = yt;
		setOpaque(true);
		setLayout(new BorderLayout());
		// setPreferredSize(maxSize);
		// setBackground(Color.yellow);
		setVisible(false);
		removeAll();
	}

	public void promptUp() {
		Rectangle rec = getBounds();
		rec.height = 1;

		arrange(rec, new Runnable() {
			@Override
			public void run() {
				promptClose();
			}
		});
	}

	/**
	 * 渐进式提示
	 * 
	 * @param jp
	 * @param xloc
	 *            如为Integer.MIN_VALUE，则为居中
	 */
	public void prompt(JComponent jp, final Runnable r) {
		if (getComponentCount() > 0) {
			promptShift(jp, r);
		} else {
			setVisible(false);
			promptDown(jp, r);
		}
	}

	private void setContent(JComponent jp) {
		removeAll();
		add(jp, BorderLayout.CENTER);
		setPreferredSize(jp.getPreferredSize());
	}

	private void logln(Object o) {
		// System.out.println(o);
	}

	private void arrange(Rectangle dest, Runnable endMission) {
		if (dest.width > maxSize.width)
			dest.width = maxSize.width;
		if (dest.height > maxSize.height)
			dest.height = maxSize.height;

		doTime_reshape(dest, endMission);
	}

	private void doTime_reshape(final Rectangle dest, Runnable endMission) {
		synchronized (jo) {
			int seq = ++jo.seq;
			Rectangle src = getBounds();

			int srcW = src.width, srcH = src.height;
			int destW = dest.width, destH = dest.height;

			int srcX = src.x;

			int times = RESHAPETIMES;

			int destX = getXLoc(destW);
			logln(srcX + ", " + destX);
			double delx = destX - srcX, delh = destH - srcH;

			final int y = src.y;
			logln(delx + ", " + delh);
			final double xstep = delx / times;
			final double hstep = delh / times;
			final double wstep = -xstep * 2;

			Job curJob = new Job(times, dest, xstep, wstep, hstep, y,
					endMission, seq);
			Timer tr = new Timer(0, curJob);
			tr.setRepeats(false);
			tr.start();
		}
	}

	private JOBOBSERVER jo = new JOBOBSERVER();

	class JOBOBSERVER {
		int seq = 0;
	}

	class Job implements ActionListener {

		int times;
		Rectangle dest;
		double xstep, wstep, hstep;
		int y;
		Runnable endMission;

		public Job(int times, Rectangle dest, double xstep, double wstep,
				double hstep, int y, Runnable endMission, int seq) {
			this.times = times;
			this.dest = dest;
			this.xstep = xstep;
			this.wstep = wstep;
			this.hstep = hstep;
			this.y = y;
			this.endMission = endMission;
			t = seq;

			String msg = SFormatter.UIformat("times: %d, xstep: %.2f, wstep: %.2f, hstep: %.2f, y: %d, t: %d",
							times, xstep, wstep, hstep, y, t);
			logln(msg);
			logln(dest);
		}

		int t;

		@Override
		public void actionPerformed(ActionEvent e) {
			synchronized (jo) {
				if (jo.seq != t)
					return;

				boolean done = shapeUnit();
				if (done) {
					logln("done");
					doLayout();
					if (endMission != null)
						endMission.run();
					return;
				}

				logln("repeat");
				Timer tr = new Timer(RESHAPEPEROID / times, this);
				tr.setRepeats(false);
				tr.start();
			}
		}

		private boolean shapeUnit() {
			Rectangle2D r = r2d;
			double x, w, h;
			x = r.getX();
			w = r.getWidth();
			h = r.getHeight();
			x += xstep;
			w += wstep;
			h += hstep;
			logln(x + ", " + w + ", " + h);
			setBounds(x, y, w, h);
			// doLayout();

			boolean b = Math.abs(dest.width - w) < 1
					&& Math.abs(dest.height - h) < 1;
			// System.out.println(x + ", " + w + ", " + h + ", " + b + ","
			// + dest.width + ", " + dest.height);

			if (Math.abs(w) > maxSize.width
					|| Math.abs(h) > maxSize.height)
				return true;

			return b;
		}
	}

	private void promptDown(JComponent jp, final Runnable r) {
		setContent(jp);

		final Rectangle rec = new Rectangle();
		Dimension sz = jp.getPreferredSize();
		rec.width = sz.width;
		rec.height = 1;

		rec.x = getXLoc(rec.width);
		rec.y = ytop;

		setBounds(rec);
		rec.height = sz.height;

		setVisible(true);
		arrange(rec, r);
	}

	private Rectangle2D r2d = new Rectangle2D.Double();
	public static final String LastPromptDispose = "LastPromptDispose";

	public void setBounds(double x, double y, double w, double h) {
		r2d.setRect(x, y, w, h);
		super.setBounds((int) x, (int) y, (int) w, (int) h);
	}

	public void setBounds(Rectangle r) {
		super.setBounds(r);
		r2d.setRect(r);
	}

	/**
	 * @param jp
	 * @param xloc
	 *            如为Integer.MIN_VALUE，则为居中
	 */
	public void promptShift(final JComponent jp, final Runnable r) {
		fireLastComponent();

		setContent(jp);
		doLayout();

		final Rectangle rec = new Rectangle();
		Dimension sz = jp.getPreferredSize();
		rec.width = sz.width;
		rec.height = sz.height;

		rec.x = getXLoc(rec.width);
		rec.y = ytop;

		arrange(rec, r);

	}

	private int getXLoc(int w) {
		Container owner = getParent();
		int v;
		int ow = owner.getWidth();
		// System.err.println(ow);
		v = (ow - w) >> 1;

		return v;
	}

	private void fireLastComponent() {
		int cc = getComponentCount();
		// System.err.println("cc: " + cc);
		if (cc > 0) {
			JComponent curr = (JComponent) getComponent(0);
			// 将控件移除出容器的信息传递出去
			curr.firePropertyChange(PromptDialog.LastPromptDispose, false, true);
		}
	}

	public void promptClose() {
		fireLastComponent();
		// 将当前控件的引用置空，用于新控件进入判断是展开还是切换
		removeAll();
		synchronized (jo) {
			jo.seq = 0;
		}

		Container owner = getParent();
		owner.setVisible(false);
		setVisible(false);
	}

	public static void main(String[] args) {
		final JFrame fr = new JFrame();
		fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container c = fr.getContentPane();

		JButton btn = new JButton("do");
		c.add(btn, BorderLayout.NORTH);
		c.add(new JPanel(), BorderLayout.CENTER);
		JButton btn2 = new JButton("do2");
		c.add(btn2, BorderLayout.SOUTH);

		final JPanel p1 = new JPanel();

		final JLabel lbl = new JLabel("fdsfsd");
		lbl.setBorder(new LineBorder(Color.RED, 1));
		p1.setBackground(Color.LIGHT_GRAY);
		p1.setPreferredSize(new Dimension(600, 400));
		p1.add(lbl, BorderLayout.CENTER);

		final JPanel p2 = new JPanel();

		final JLabel lbl2 = new JLabel("fdsfsd2");
		lbl2.setBorder(new LineBorder(Color.BLACK, 1));
		p2.setBackground(Color.WHITE);
		p2.setPreferredSize(new Dimension(200, 100));
		p2.add(lbl2, BorderLayout.CENTER);

		JPanel cen = new JPanel();
		c.add(cen, BorderLayout.CENTER);
		fr.setSize(800, 600);

		final PromptDialog pp = new PromptDialog(60);
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pp.prompt(p1, null);// lbl
			}
		});
		btn2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pp.promptShift(p2, null);// lbl2
			}
		});
		fr.setVisible(true);
	}
}