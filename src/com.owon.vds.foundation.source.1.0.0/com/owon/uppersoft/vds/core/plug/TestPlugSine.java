package com.owon.uppersoft.vds.core.plug;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import com.owon.uppersoft.vds.core.paint.Background;
import com.owon.uppersoft.vds.core.paint.ColorProvider;

/**
 * 在界面上测试正弦插值的效果
 * 
 * @author Matt
 * 
 */
public class TestPlugSine {
	private byte[] raw_data;
	private byte[] output_data;

	public static final int plugLength = 80, rate = 10, arrayLength = 90,
			xgap = 1, xoffset = 12;

	public TestPlugSine() {
		raw_data = VDSource.genSine(100, 15, rate - 1, arrayLength);
		// VDSource.genSimulateSine(PlugValueUtil_X.PlugArgs_dbl.length);

		output_data = new byte[raw_data.length * rate];
		// unp.length - (PlugValueUtil_X.PlugArgs_dbl.length - 1)
		load();
	}

	private void paint(Graphics2D g2d) {
		g2d.setColor(Color.orange);

		int x0 = xoffset;
		int px0 = x0 + 40;
		byte[] bb = output_data;
		int gap = xgap, y0, beg;

		int len = bb.length;
		int x1 = px0, y1 = 0, x2 = x1 + gap, y2 = 0;

		beg = 0;
		y0 = 140;
		// 连线
		y1 = y0 - bb[beg++];
		for (int i = beg; i < len; i++) {
			y2 = y0 - bb[i];
			g2d.drawLine(x1, y1, x2, y2);
			x1 += gap;
			x2 += gap;
			y1 = y2;
		}

		// 点
		beg = 0;
		y0 = 290;
		x1 = px0;
		y1 = y0 - bb[beg++];
		g2d.drawLine(x1, y1, x1, y1);
		x1 += gap;
		for (int i = beg; i < len; i++) {
			y1 = y0 - bb[i];
			g2d.drawLine(x1, y1, x1, y1);
			x1 += gap;
		}

		// 原始点
		beg = 0;
		// PlugValueUtil_X.PlugArgs_dbl.length;
		y0 = 410;
		gap = gap * rate;
		len = raw_data.length;// num;//
		x1 = x0;
		x2 = x1 + gap;
		y1 = y0 - raw_data[beg++];
		for (int i = beg; i < len; i++) {
			y2 = y0 - raw_data[i];
			g2d.drawLine(x1, y1, x2, y2);
			x1 += gap;
			x2 += gap;
			y1 = y2;
		}
	}

	public void load() {
		// int i = 0, j = 0, len = unp.length;

		// pr = PlugValueUtil.PlugRate;
		// for (i = 0, j = 0; i < len; i++, j += pr) {
		// PlugValueUtil.fill(ped, j, pr, (byte) (unp[i]));
		// }
		for (int i = 0; i < raw_data.length; i++) {
			System.out.print(raw_data[i] + ", ");
		}
		System.out.println();
		Plug10.sine_interpolate(output_data, raw_data, plugLength, 0, 0);
		// Plug5.sine_interpolate(output_data, raw_data, plugLength);
		for (int i = 0; i < output_data.length; i++) {
			System.out.print(output_data[i] + ", ");
		}
		// PlugValueUtil_X.fill_dbl(ped, unp, len,
		// PlugValueUtil_X.PlugArgs_dbl);

		// System.out.println();
		// for (i = 0; i < len; i++) {
		// System.out.print(unp[i] + " ");
		// }
		// System.out.println();
		// for (i = 0; i < j; i++) {
		// System.out.print(ped[i] + " ");
		// }
	}

	public static void main(String[] args) {
		JFrame jf = new JFrame();
		final Background bg = new Background();
		final TestPlugSine tps = new TestPlugSine();
		final ColorProvider cp = new ColorProvider() {
			@Override
			public Color getGridColor() {
				return Color.gray;
			}
		};
		JPanel jp = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				bg.paintView((Graphics2D) g, cp);
				tps.paint((Graphics2D) g);
			}
		};
		jp.setBackground(Color.BLACK);
		final Rectangle r = new Rectangle(10, 30, 1000, 500);
		jp.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				bg.adjustView(r, false);
			}
		});

		jf.setLayout(new BorderLayout());
		jf.add(jp, BorderLayout.CENTER);
		jf.setBounds(20, 20, 1200, 700);
		jf.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		jf.setVisible(true);
	}
}