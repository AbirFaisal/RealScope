package com.owon.uppersoft.dso.source.comm.detect;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.vds.core.usb.IDevice;
import com.owon.uppersoft.vds.util.ui.CListModel;

public class CheckUSBPortsPane extends JPanel {
	public Font alphafont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);

	private CListModel plm;
	private JList jlist;
	private JScrollPane jsp;

	public CheckUSBPortsPane(final List<IDevice> ids, final USBLoopChecker uch) {
		setLayout(null);
		final JPanel t = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				int w = getWidth();
				g.setColor(Color.black);
				g.drawString("[ X ]", w - 30, 20);
				g.setFont(alphafont);
				g.drawString(
						I18nProvider.bundle().getString(
								"M.Connection.chooseUSB"), 10, 20);
			}
		};
		t.setBounds(0, 0, 400, 30);
		MouseAdapter ma = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int w = t.getWidth(), x = e.getX();
				if (x > w - 30 && x < w) {
					uch.ignoreCheckResult();
				}
			}

			public void mouseMoved(MouseEvent e) {
				int w = t.getWidth(), x = e.getX();
				if (x > w - 30 && x < w) {
					t.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				} else {
					t.setCursor(Cursor
							.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			};
		};
		t.addMouseListener(ma);
		t.addMouseMotionListener(ma);
		add(t);

		jsp = new JScrollPane();
		plm = new CListModel(ids) {
			@Override
			public Object getElementAt(int index) {// super.getElementAt(index).toString()
				return "device (" + (index + 1) + ')';
			}
		};
		jlist = new JList(plm);
		jlist.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JList jl = (JList) e.getSource();
				final int idx = jl.locationToIndex(e.getPoint());
				if (idx < plm.getSize()) {
					final IDevice id = ids.get(idx);
					if (id != null) {
						clickToConnect(id);
					}
				}
			}
		});
		jsp.add(jlist);
		jsp.setViewportView(jlist);
		jsp.setBounds(0, 30, 400, 170);
		// jsp.setPreferredSize(new Dimension(400,170));
		add(jsp);// new JLabel("ddd")
		final Dimension psz = new Dimension(400, 200);
		// setLayout(new LayoutManagerAdapter() {
		// private Dimension sz = new Dimension();
		//
		// @Override
		// public void layoutContainer(Container parent) {
		// // System.out.println("layoutContainer");
		// parent.getSize(sz);
		// int w = sz.width, h = sz.height, y = 30;
		//
		// /** 这里用BorderLayout失效？ */
		// t.setBounds(0, 0, w, y);
		// jsp.setBounds(0, y, w, Math.max(h - y, 0));
		// jsp.doLayout();
		// }
		//
		// @Override
		// public Dimension preferredLayoutSize(Container parent) {
		// return psz;
		// }
		// });

		setPreferredSize(psz);
	}

	public void clickToConnect(IDevice id) {
	}

	public void updateList(List<IDevice> ids) {
		plm.setList(ids);
		jlist.setModel(plm);
		jlist.repaint();
	}

}