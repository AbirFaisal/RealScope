package com.owon.uppersoft.vds.ui.widget.help;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.owon.uppersoft.vds.ui.layout.OneRowLayout;
import com.owon.uppersoft.vds.ui.widget.custom.CButton;

public class RadioButtonGroup extends JPanel {

	public static final String RADIO = "radio";

	private ButtonGroup buttonGroup = new ButtonGroup();
	private JRadioButton[] radioButtons;

	public static void main(String args[]) {
		try {
			JFrame jf = new JFrame();
			jf.setBounds(100, 100, 500, 90);
			jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			Integer[] t = new Integer[4];
			t[0] = 1;
			t[2] = 3;
			t[1] = 2;
			t[3] = 4;
			RadioButtonGroup rbg = new RadioButtonGroup(t,
					new PropertyChangeListener() {
						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							RadioButtonGroup rbg = (RadioButtonGroup) evt
									.getSource();
							rbg.setSelected((Integer) evt.getNewValue());
						}
					}, 3, 62, 30);
			jf.add(rbg, BorderLayout.CENTER);
			jf.setVisible(true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the frame
	 */
	public RadioButtonGroup(Object[] os, final PropertyChangeListener pcl,
			int j, int tabw, int tabh) {
		setLayout(new OneRowLayout());
		sel = j;
		int i = 0;
		radioButtons = new JRadioButton[os.length];
		for (Object o : os) {
			final int k = i;
			final JRadioButton radioButton = new JRadioButton() {

				@Override
				protected void paintComponent(Graphics g) {
					Graphics2D g2d = (Graphics2D) g;
					int w = getWidth(), h = getHeight();
					g.setColor(Color.DARK_GRAY);
					g.fillRect(0, 0, w, h);
					if (isSelected()) {
						g2d.setStroke(new BasicStroke(3));
						g.setColor(Color.ORANGE);
						g.drawRect(2, 2, w - 5, h - 5);
					}
					g.setColor(Color.WHITE);
					String n = getText();
					FontMetrics fm = g.getFontMetrics();
					Rectangle2D r2 = fm.getStringBounds(n, g);
					int tw = (int) r2.getWidth();
					int th = (int) r2.getHeight();
					g.drawString(n, (w - tw) >> 1,
							((h - th) >> 1) + fm.getAscent());
				}
			};
			radioButtons[i] = radioButton;
			add(radioButton);
			buttonGroup.add(radioButton);
			String n = o.toString();
			radioButton.setText(n);
			// radioButton.setIcon(defaultIcon);
			radioButton.setPreferredSize(new Dimension(tabw, tabh));
			CButton.setToolTip(radioButton);
			radioButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					pcl.propertyChange(new PropertyChangeEvent(
							RadioButtonGroup.this, RADIO, null, k));
					// System.err.println(k + ", " + getSelection());
					setSelected(k);
				}
			});
			if (i == j) {
				radioButton.setSelected(true);
			}
			i++;
		}
	}

	private int sel;

	public int getSelection() {
		return sel;
	}

	public void setSelected(int s) {
		/** 可查找并关掉外部由RADIO_fire出去的调用 */
		if (s >= 0 && s < radioButtons.length && sel != s) {
			radioButtons[s].setSelected(true);
			sel = s;
		}
	}

}
