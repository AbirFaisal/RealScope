package com.owon.uppersoft.vds.ui.widget.custom;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import com.owon.uppersoft.vds.ui.layout.OneRowLayout;
import com.owon.uppersoft.vds.ui.resource.SwingResourceManager;

public class ExcludeButtons extends JPanel {
	public static final String EXCLUDE = "exclude";
	private ButtonGroup buttonGroup = new ButtonGroup();
	private JToggleButton[] jtbs;
	private int sel;

	public ExcludeButtons(final Object[] os, final PropertyChangeListener pcl,
			final int j, int tabw, int tabh, Font f) {
		setLayout(new OneRowLayout());
		setOpaque(false);

		int i = 0;
		jtbs = new JToggleButton[os.length];
		for (Object o : os) {
			final int k = i;
			JToggleButton jtb = new JToggleButton() {
				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					if (sel == k) {
						setBackground(Color.white);
						// g.setColor(Color.ORANGE);
						// g.drawRoundRect(3, 3, getWidth()-5,
						// getHeight()-5,5,5);
						g.drawImage(marker, 6, 7, null);
					} else {
						setBackground(Color.gray);
					}
				}
			};
			jtb.setText(o.toString());
			jtb.setPreferredSize(new Dimension(tabw, tabh));
			jtbs[i] = jtb;
			add(jtb);
			buttonGroup.add(jtb);
			jtb.setFont(f);
			CButton.setToolTip(jtb);
			jtb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (isFinalsel)
						return;
					// System.out.println(k + ", " + sel);
					int v = k;
					if (sel == v) {
						// System.out.println("k++");
						v = (v + 1) % os.length;
						setSelected(v);
					}
					sel = v;
					pcl.propertyChange(new PropertyChangeEvent(
							ExcludeButtons.this, EXCLUDE, null, v));
				}
			});

			if (i == j) {
				jtb.setSelected(true);
			}
			i++;
		}
		sel = j;
	}

	public void setSelected(int s) {
		if (s >= 0 && s < jtbs.length) {
			// JToggleButton last = null;
			// if (sel >= 0 && sel < jtbs.length) {
			// last = jtbs[sel];
			// }
			sel = s;
			jtbs[s].setSelected(true);
			// jtbs[s].setIcon(marker);
			// if (last != null) {
			// last.setIcon(null);
			// }
		}
	}

	// private int preSel = -1;
	private boolean isFinalsel;

	public void setFinalSelected(int s) {
		isFinalsel = true;
		if (s >= 0 && s < jtbs.length) {
			// preSel =sel ;
			// System.out.println("preSel:" + preSel);
			sel = s;
			for (int i = 0; i < jtbs.length; i++) {
				if (i == s) {
					jtbs[s].setSelected(true);
					continue;
				}
				jtbs[i].setEnabled(false);
			}
		}
	}

	public void undoFinalSelected(int s) {
		isFinalsel = false;
		sel = s;
		for (int i = 0; i < jtbs.length; i++) {
			if (i == sel)
				jtbs[i].setSelected(true);
			jtbs[i].setEnabled(true);
		}
	}
	public static final String ImageDirectory = "/com/owon/uppersoft/vds/ui/widget/custom/image/";
	public static final String Path = ImageDirectory
			+ "marker.png";

	public static final Image marker = SwingResourceManager.getImage(
			ExcludeButtons.class, Path);

	public static void main(String[] args) {
		JFrame jf = new JFrame();
		jf.setBounds(500, 500, 500, 100);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		String[] s = new String[] { "Single", "Alternate", "test" };
		ExcludeButtons eb = new ExcludeButtons(s, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				System.out.println("newValue:" + evt.getNewValue());
			}
		}, 0, 100, 60, new Font(Font.SANS_SERIF, Font.PLAIN, 14));
		jf.add(eb, BorderLayout.CENTER);
		jf.setVisible(true);

		eb.setFinalSelected(1);

	}
}
