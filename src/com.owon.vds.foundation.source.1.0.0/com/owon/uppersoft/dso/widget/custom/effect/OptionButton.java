package com.owon.uppersoft.dso.widget.custom.effect;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.vds.ui.paint.LineDrawTool;
import com.owon.uppersoft.vds.ui.widget.custom.LButton;
import com.owon.uppersoft.vds.ui.widget.help.ShareRollNStopInfoPane;

/**
 * @deprecated
 * 
 */
public class OptionButton extends LButton implements MouseListener {
	public static final ImageIcon op;
	static {
		int[] tia1 = { 12, 16, 18 }, tia2 = { 18, 24, 18 };
		BufferedImage pi = LineDrawTool.fillTriangle(30, 32, tia1, tia2,
				Color.RED);
		op = new ImageIcon(pi);
	}
	private ShareRollNStopInfoPane tp;
	private ControlManager dh;

	public OptionButton(ShareRollNStopInfoPane tp, ControlManager dh) {
		this.tp = tp;
		this.dh = dh;

		setEnabled(false);
		setIcon(op);
		setPreferredSize(new Dimension(15, 32));

		addMouseListener(this);
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		tp.setRsRollover(true);
		tp.repaint();
	}

	public void mouseExited(MouseEvent e) {
		tp.setRsRollover(false);
		tp.repaint();
	}

	public void mouseClicked(MouseEvent e) {
		if (!isEnabled())
			return;

		JPopupMenu pop = new JPopupMenu();
		boolean isRuntime = dh.isRuntime();
		String label;
		if (isRuntime) {
			label = I18nProvider.bundle().getString("Action.JustStop");
			JMenuItem juststop = new JMenuItem(label);
			pop.add(juststop);
			label = I18nProvider.bundle().getString("Action.StopNdm");
			JMenuItem stopdm = new JMenuItem(label);
			pop.add(stopdm);
			juststop.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					tp.askStop(false);
				}
			});
			stopdm.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					tp.askStop(true);
				}
			});
		} else {
			label = "";
//			if (dh.isDMDataGotAlready())
//				label = I18nProvider.bundle().getString("Action.ResetDM");
//			else
//				label = I18nProvider.bundle().getString("Action.GetDMdata");
			JMenuItem getdm = new JMenuItem(label);
			pop.add(getdm);
			getdm.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
//					if (dh.isDMDataGotAlready()) {
//						new DataImporter().openfile(dh, InterCommunicator.dmf);
//					} else {
//						InterCommunicator ic = (InterCommunicator) Platform
//								.getControlApps().interComm;
//						ic.getDMDataBackgroundWhenAsk();
//					}
				}
			});
		}
		pop.show(this, getX() - 50, getY() + getHeight());
	}

	@Override
	protected void paintComponent(Graphics g) {
		tp.paintForButton(this, g);
		super.paintComponent(g);
	}
}
