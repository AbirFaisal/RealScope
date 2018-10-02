package com.owon.uppersoft.dso.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ResourceBundle;

import javax.swing.JPanel;

import com.owon.uppersoft.vds.core.aspect.Decorate;
import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.core.paint.PaintContext;
import com.owon.uppersoft.vds.core.paint.ScreenContext;

/**
 * ViewChart is a graphics rendering area that is independent of location and size.
 * It can carry waveforms, FFTs or other data model related drawings.
 * 
 */
public class ViewChart extends JPanel implements Decorate, Localizable {

	private boolean superDraw = true;

	private Decorate dc;
	private PaintContext pc;
	private Rectangle locInfo = new Rectangle();

	public Rectangle getLocInfo() {
		return locInfo;
	}

	public ScreenContext getScreenContext() {
		return pc;
	}

	protected ViewChart(Decorate dc, PaintContext pc, boolean sd) {
		this.dc = dc;
		this.pc = pc;
		superDraw = sd;
		setBackground(Color.BLACK);
		setDoubleBuffered(false);
		/** Double buffering can be turned off */
	}

	public ViewChart(Decorate dc, PaintContext pc) {
		this(dc, pc, true);
	}

	@Override
	public void localize(ResourceBundle rb) {
	}

	/**
	 * Resize to prepare for updating to buffered image
	 */
	public void adjustView(ScreenContext pc, Rectangle bd) {
		/** Use copy here to prevent incoming reference object
		 * content from being modified in the future */
		locInfo.setBounds(bd);
		dc.adjustView(pc, locInfo);
	}

	public void paintView(Graphics2D g2d, ScreenContext pc, Rectangle r) {
		// pc.setCurrentLocInfo(locInfo);
		/** TODO Consider how to display the afterglow */
		dc.paintView(g2d, pc, r);
	}

	public void re_paint() {
		repaint();
	}

	public void re_paint(int x, int y, int width, int height) {
		repaint(x, y, width, height);
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (superDraw)
			super.paintComponent(g);
		paintView((Graphics2D) g, pc, locInfo);
	}
}
