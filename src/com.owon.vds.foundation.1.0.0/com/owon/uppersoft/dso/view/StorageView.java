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
 * StorageView，存储试图
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
		/** wlen 代表存储深度的线长度 */
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
			// TODO 匹配机型后重载DM文件会报空,因dmDraw设true时,virtualPoint还没建对象
			// KNOW DM初始画图不对
			cm.getWaveFormInfoControl().getWaveFormInfoForDM()
					.drawView_dm(g, gap, hh, wlen, tc.getHorTrgIdx());
		} else {
			/** 运行时T位置随移动而改变，括弧随时基而改变 */
			g2d.setColor(Color.LIGHT_GRAY);
			int dl = dmc.getDeepDataLen();
			/** v_fsl 代表屏幕内波形长度，运行状态下即两括号之间的距离 */
			int v_fsl = tc.getRTfullScreenNumber() * wlen / dl;
			// System.err.println(cm.timeControl.rtfls + ", " + wlen + ", "
			// + dl);

			/**
			 * xo:左括号相对于原点在左端坐标系的值，用于画左括号。
			 * 波形全长扣去屏幕内波形长，差值的一半即是屏幕左界以外波形长，因总览区波形跳过gap距离开始画，故还需加gap才是左括号xo值
			 */
			int x0 = ((wlen - v_fsl) >> 1) + gap;
			if (cm.getCoreControl().isRunMode_slowMove()) {
				x0 = ((wlen - v_fsl) >> 1) + (gap << 1);
			}
			int x1 = x0 + v_fsl;

			/** 括弧 */
			g2d.drawLine(x0, hh - edgetall, x0, hh + edgetall);
			g2d.drawLine(x1, hh - edgetall, x1, hh + edgetall);

			/** 横线 */
			g2d.drawLine(gap, hh, gap + wlen, hh);

			/** T */
			int hti = tc.getHorizontalTriggerPosition();
			// System.err.println(hti + ", " + (wlen>>1));
			/**
			 * tp:总览图的水平触发位置，原点(0,0)在左端，用于画图。 hti * v_fsl /
			 * Define.def.AREA_WIDTH:水平触发位置，原点在中央，画图须转成左端坐标系
			 */
			int tp = (int) (gap + (wlen >> 1) - (double) hti
					* (v_fsl > 0 ? v_fsl : 1) / GDefine.AREA_WIDTH);

			/** 限制总览图的水平触发位置在 波形全长wlen范围内 */
			if (tp <= gap)
				tp = gap;
			else if (tp >= gap + wlen)
				tp = gap + wlen;
			g2d.setColor(Color.MAGENTA);
			/** 画出总览图的水平触发位置 */
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