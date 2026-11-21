package com.owon.uppersoft.dso.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ResourceBundle;

import javax.swing.JPanel;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.mode.control.DeepMemoryControl;
import com.owon.uppersoft.dso.mode.control.TimeControl;
import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.aspect.Localizable;

/**
 * StorageView, storage view
 * 
 */
public class StorageView extends JPanel implements Localizable {
	public static final int xborder = 5, yborder = 5, xb2 = xborder << 1,
			yb2 = yborder << 1;
	public static final int edgetall = 5;
	public static final int gap = 50, gap2 = gap << 1;
	public static final int linetall = 2;

	private Color bg = Color.BLACK;
	/**
	 * 
	 */
	private static final long serialVersionUID = 3836565395026946704L;
	private DataHouse dh;

	public StorageView(DataHouse dh) {
		setOpaque(false);
		this.dh = dh;
		cm = dh.controlManager;
		tc = cm.getTimeControl();
		dmc = cm.getDeepMemoryControl();
	}

	@Override
	public void localize(ResourceBundle rb) {
	}

	private Rectangle2D.Double r2d = new Rectangle2D.Double();

	private ControlManager cm;
	private TimeControl tc;
	private DeepMemoryControl dmc;

	@Override
	protected void paintComponent(Graphics g) {
		// super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		int w = getWidth(), h = getHeight(), hh = h >> 1, hw = w >> 1;
		/** wlen wlen is the line length representing the storage depth */
		int wlen = w - gap2, wb = w - xb2, hb = h - yb2;

		Shape tmp = g2d.getClip();
		r2d.setRect(xborder, xborder, w - xb2, h - yb2);
		g2d.setClip(r2d);

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(bg);
		g2d.fillRoundRect(xborder, yborder, wb, hb, 35, 35);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);

		boolean dmDraw = dh.isDMLoad() && dh.isDataComplete();

		if (dmDraw && !cm.isKeepGet()) {
			// TODO After matching the model, reloading a DM file reports null, because the object of virtualPoint has not been created yet when dmDraw is set to true
			// KNOW DM initial drawing is wrong
			cm.getWaveFormInfoControl().getWaveFormInfoForDM()
					.drawView_dm(g, gap, hh, wlen, tc.getHorTrgIdx());
		} else {
			/** While running, the T position changes with movement, and the brackets change with the timebase */
			g2d.setColor(Color.LIGHT_GRAY);
			int dl = dmc.getDeepDataLen();
			/** v_fsl v_fsl is the waveform length within the screen, i.e. the distance between the two brackets when running */
			int v_fsl = tc.getRTfullScreenNumber() * wlen / dl;
			// System.err.println(controlManager.timeControl.rtfls + ", " + wlen + ", "
			// + dl);

			/**
			 * xo:value of the left bracket relative to the origin in the left-end coordinate system, used to draw the left bracket.
			 * Half of the difference between the full waveform length and the on-screen waveform length is the waveform length beyond the left edge of the screen; since the overview waveform starts drawing after skipping the gap distance, gap must also be added to get the xo value of the left bracket
			 */
			int x0 = ((wlen - v_fsl) >> 1) + gap;
			if (cm.getCoreControl().isRunMode_slowMove()) {
				x0 = ((wlen - v_fsl) >> 1) + (gap << 1);
			}
			int x1 = x0 + v_fsl;

			/** Brackets */
			g2d.drawLine(x0, hh - edgetall, x0, hh + edgetall);
			g2d.drawLine(x1, hh - edgetall, x1, hh + edgetall);

			/** Horizontal line */
			g2d.drawLine(gap, hh, gap + wlen, hh);

			/** T */
			int hti = tc.getHorizontalTriggerPosition();
			// System.err.println(hti + ", " + (wlen>>1));
			/**
			 * tp:horizontal trigger position of the overview, with the origin (0,0) at the left end, used for drawing. hti * v_fsl /
			 * Define.def.AREA_WIDTH:horizontal trigger position, with the origin at the center; must be converted to the left-end coordinate system for drawing
			 */
			int tp = (int) (gap + (wlen >> 1) - (double) hti
					* (v_fsl > 0 ? v_fsl : 1) / GDefine.AREA_WIDTH);

			/** Limit the horizontal trigger position of the overview to within the full waveform length wlen */
			if (tp <= gap)
				tp = gap;
			else if (tp >= gap + wlen)
				tp = gap + wlen;
			g2d.setColor(Color.MAGENTA);
			/** Draw the horizontal trigger position of the overview */
			int y0 = hh - edgetall, y1 = hh - (edgetall << 1);
			g2d.drawLine(tp, y0, tp, y1);
			g2d.drawLine(tp - 1, y1 + 2, tp - 1, y1);
			g2d.drawLine(tp + 1, y1 + 2, tp + 1, y1);
		}

		g2d.setColor(Color.ORANGE);
		g2d.drawLine(hw, hh + 3, hw, hh - 3);

		g2d.setClip(tmp);
	}
}