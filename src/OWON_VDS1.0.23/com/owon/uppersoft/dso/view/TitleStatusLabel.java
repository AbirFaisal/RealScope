package com.owon.uppersoft.dso.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.pref.Define;
import com.owon.uppersoft.dso.source.comm.InfiniteDaemon;
import com.owon.uppersoft.dso.source.comm.TrgStatus;
import com.owon.uppersoft.vds.ui.resource.FontCenter;

public class TitleStatusLabel extends JLabel implements MouseListener {
	private int status_icon_xloc = 0;

	private int counter = 0;
	private boolean overstatus = false;

	public TitleStatusLabel(int status_icon_xloc) {
		this.status_icon_xloc = status_icon_xloc;

		setForeground(Define.def.CO_DockFore);

		setHorizontalAlignment(SwingConstants.CENTER);
		setFont(Define.def.commonStatusfont);

		setPreferredSize(new Dimension(98, 25));

		addMouseListener(this);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		int mouseBtn = e.getButton();
		// KOWN This sentence has an empty error, do the following processing
		if (Platform.getControlApps() == null)
			return;
		InfiniteDaemon dae = Platform.getControlApps().getDaemon();
		dae.onClickStatus(status_icon_xloc, mouseBtn);
	}

	public void mouseEntered(MouseEvent e) {
		if (!overstatus) {
			overstatus = (true);
			repaint();
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (overstatus) {
			overstatus = (false);
			repaint();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	private TrgStatus ts = TrgStatus.Offline;

	public TrgStatus getTrgStatus() {
		return ts;
	}

	private boolean tempStatus = false;// Used to temporarily change to autoseting...

	public void setTempStatus(boolean tempStatus) {
		this.tempStatus = tempStatus;

		TrgStatus ts;
		if (!tempStatus) {
			ts = this.ts;
		} else {
			setForeground(Color.WHITE);
			ts = TrgStatus.AutoSetting;
		}
		// System.err.println(tempStatus);
		setText(ts.toString());
		repaint();
	}

	public void updateTrgStatus(int c, TrgStatus ts) {
		this.ts = ts;

		if (ts.equals(TrgStatus.Stop))
			setForeground(Color.RED);
		else
			setForeground(Color.WHITE);
		String t = ts.toString();

		/** Since it is still possible to take a frame and start automatic setting
		 * after using tempStatus, it may be changed to Trg'd, which needs to be masked. */
		if (!tempStatus)
			setText(t);

		// System.err.println(t);

		int tmp = counter;
		counter = Math.abs(c);
		if (tmp != counter && !tempStatus)
			repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		int w = getWidth(), h = getHeight();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(Color.BLACK);
		g2d.fillRoundRect(5, 5, w - 10, h - 10, 30, 30);

		if (overstatus) {
			g2d.setColor(Color.ORANGE);
			g2d.setStroke(Define.def.Stroke2);
			g2d.drawRoundRect(5, 5, w - 10, h - 10, 30, 30);
		}

		super.paintComponent(g);

		if (counter > 0) {
			g2d.setColor(Color.RED);
			g2d.fillOval(w - 12, 6, 10, 10);
			g2d.setColor(Color.WHITE);
			g2d.drawOval(w - 12, 6, 10, 10);
			g2d.setFont(Define.def.statusCounterfont);
			g2d.drawString(String.valueOf(counter), w - 9, 15);
		}

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	public void localize(ResourceBundle rb) {
		setFont(FontCenter.getLabelFont());
		setToolTipText(rb.getString("ToolTip.Statuslbl"));
	}
}