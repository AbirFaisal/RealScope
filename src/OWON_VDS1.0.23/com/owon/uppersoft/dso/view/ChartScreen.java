package com.owon.uppersoft.dso.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import com.owon.uppersoft.dso.control.ChartScreenMouseGesture;
import com.owon.uppersoft.dso.control.FFTMouseAdapter;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.pref.Define;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.wf.ChartScreenSelectModel;
import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.core.aspect.Paintable;
import com.owon.uppersoft.vds.core.aspect.help.IPrintable;
import com.owon.uppersoft.vds.core.paint.PaintContext;
import com.owon.uppersoft.vds.core.usb.help.ContextMenuManager;
import com.owon.uppersoft.vds.ui.layout.LayoutManagerAdapter;

public class ChartScreen extends JPanel implements Localizable,
		PropertyChangeListener, Paintable, IPrintable {

	private ViewChart chart;
	private ViewChart v1, v2;
	private PaintContext pc;

	private DataHouse dh;
	private ChartScreenMouseGesture csmg;

	public ChartScreenMouseGesture getChartScreenMouseGesture() {
		return csmg;
	}

	private ChartScreenSelectModel cssm;

	public ChartScreenSelectModel getChartScreenSelectModel() {
		return cssm;
	}

	public void setScreenSelectWFidx(int idx) {
		cssm.setScreenSelectWFidx(idx);
	}

	public ChartScreen(MainWindow mw, DataHouse dh, Dimension sz) {
		cssm = new ChartScreenSelectModel();

		this.dh = dh;

		ControlManager cm = dh.controlManager;
		WaveFormManager wfm = dh.getWaveFormManager();

//		setPreferredSize(sz);
		setBackground(Color.BLACK);

		pc = cm.paintContext;

		if (1 == 0) {
			final Image bgimg = new ImageIcon("bg.jpg").getImage();
			chart = new ViewChart(dh.getGlobalDecorater(), pc, false) {
				@Override
				protected void paintComponent(Graphics g) {
					// g.fillRect(0, 0, 12, chart.getHeight());
					g.drawImage(bgimg, 12, 0, null);
					super.paintComponent(g);
				}
			};
		} else {
			chart = new ViewChart(dh.getGlobalDecorater(), pc);
		}

		// chart.setBackground(Color.darkGray);
		csmg = new ChartScreenMouseGesture(mw, chart, dh, chart, this);
		add(chart);

		v1 = new ViewChart(wfm.getXYView(), pc);
		v2 = new ViewChart(wfm.getFFTView(), pc);

		FFTMouseAdapter fftma = new FFTMouseAdapter(wfm, cm.fftctr, v2);
		v2.addMouseListener(fftma);
		v2.addMouseMotionListener(fftma);

		add(v1);
		add(v2);

		layout3in1(wfm.is3in1On());
		setLayout(new LayoutManagerAdapter() {
			public void layoutContainer(java.awt.Container parent) {
				resizelayout();
			}
		});

		ctxMenu = new JPopupMenu();
		add(ctxMenu);
		cmm = new ContextMenuManager(ctxMenu, this);
		cm.pcs.addPropertyChangeListener(this);

	}

	private JPopupMenu ctxMenu;
	private ContextMenuManager cmm;

	public ContextMenuManager getContextMenuManager() {
		return cmm;
	}

	private void enableChartScreenGesture(boolean b) {
		csmg.enableGesture(b);
	}

	public void resizelayout() {
		Dimension pfsz = getSize();
		int w = pfsz.width, h = pfsz.height;
		Rectangle r = pc.getChartRectangle();

		int lw =PaintContext.leftwrange, th = PaintContext.composite_height;
		lw *= DataHouse.xRate;
		th *= DataHouse.yRate;

		if (pc.isScreenMode_3()) {
			v1.setBounds(0, 0, lw, th);
			v2.setBounds(lw, 0, w - lw, th);
			chart.setBounds(0, th, w, h - th);
			v1.adjustView(pc, PaintContext.lrect);
			v2.adjustView(pc, PaintContext.rrect);
			chart.adjustView(pc, r);
		} else {
			if (dh.controlManager.getFFTControl().isFFTon()) {
				v2.setBounds(0, 0, w, h);
				v2.adjustView(pc, r);
			} else {
				chart.setBounds(0, 0, w, h);
				chart.adjustView(pc, r);
			}
		}
	}

	private void layout3in1(boolean b) {
		pc.setScreenMode_3(b);
		v1.setVisible(b);
		if (b) {
			v2.setVisible(b);
			chart.setVisible(b);
		} else {

			boolean ffton = dh.controlManager.getFFTControl().isFFTon();
			v2.setVisible(ffton);
			chart.setVisible(!ffton);
		}
		// resizelayout();
	}

	public void fft_re_paint() {
		v2.re_paint();
	}

	public void xy_re_paint() {
		v1.re_paint();
	}

	public boolean is3in1() {
		return pc.isScreenMode_3();
	}

	public void update3in1(boolean b) {
		Platform.getDataHouse().set3in1(b);
		layout3in1(b);
		re_paint();
	}

	protected void customTransform(Graphics2D g2, int width, int height) {
		AffineTransform transform = new AffineTransform();
		double scale = (double) width / (double) getWidth();
		transform.setToScale(scale, scale);
		g2.transform(transform);
	}

	public void printView(Graphics2D g2, int width, int height) {
		customTransform(g2, width, height);
		Rectangle rv1 = v1.getBounds();

		if (pc.isScreenMode_3()) {
			g2.translate(0, rv1.y + rv1.height);
			chart.paintView(g2, pc, chart.getLocInfo());
			// chart.adjustView(pc, pc.rect);
			g2.translate(0, -rv1.y - rv1.height);

			v1.paintView(g2, pc, v1.getLocInfo());
			g2.translate(rv1.x + rv1.width, rv1.y);
			v2.paintView(g2, pc, v2.getLocInfo());
			// g2.translate(0 - rv1.width, rv1.y + rv1.height);
			// v1.adjustView(pc, pc.lrect);
			// v2.adjustView(pc, pc.rrect);
		} else {
			if (dh.controlManager.getFFTControl().isFFTon())
				v2.paintView(g2, pc, chart.getLocInfo());
			else
				chart.paintView(g2, pc, chart.getLocInfo());
		}

	}

	public void updateGridColor() {
		re_paint();
	}

	public void updateXYView() {
		if (v1.isVisible())
			v1.repaint();
	}

	public void rebuffer() {
		dh.getGlobalDecorater().rebuffer();
		re_paint();
	}

	public void re_paint() {
		repaint();
	}

	/**
	 * @return 绘制上下文
	 */
	public PaintContext getPaintContext() {
		return pc;
	}

	/**
	 * @return 主绘图区
	 */
	public ViewChart getChart() {
		return chart;
	}

	@Override
	public void localize(ResourceBundle rb) {
		chart.localize(rb);
		v1.localize(rb);
		v2.localize(rb);
	}

	public BufferedImage getChartScreenBufferedImage() {
		// Rectangle r=cs.getPaintContext().currentLocInfo;
		/**
		 * 该保存图片大小为整个主屏，切3in1情况包括三个窗口
		 * 
		 * 若只需保存当前波形显示窗口(切3in1最下方窗口)，
		 * 大小由cs.getWidth(),cs.getHeight()分别改为r.width,r.height
		 */
		// System.out.println(getWidth() + "," + getHeight());
		int tph = Platform.getMainWindow().getTitlePane().getHeight();
		int csw = getWidth(), csh = getHeight();
		BufferedImage bi = new BufferedImage(Define.def.FRM_Width,
				Define.def.FRM_Height - tph, BufferedImage.TYPE_USHORT_565_RGB);
		Graphics2D g2 = bi.createGraphics();

		// g2.translate(x, y);//0,0
		Color tmp = g2.getColor();
		// g2.setColor(Color.black);
		// g2.fillRect(0, 0, csw, csh);

		g2.setStroke(new BasicStroke(0.8f, BasicStroke.CAP_SQUARE,
				BasicStroke.JOIN_ROUND));
		// 打印ToolPane各信息
		g2.translate(0, csh);
		Platform.getMainWindow().getToolPane().paintComponents(g2);
		// 打印chartScreen
		g2.translate(0, -csh);
		printView(g2, csw, csh);

		g2.setColor(tmp);
		return bi;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String p = evt.getPropertyName();
		if (p.equals(PropertiesItem.FFT_ON)) {
			repaint();
		} else if (p.equals(PropertiesItem.FFT_OFF)) {
			repaint();
		} else if (p.equals(PropertiesItem.DURINGDMFETCH)) {
			enableChartScreenGesture(!(Boolean) evt.getNewValue());
		} else if (p.equals(PropertiesItem.REPAINT_CHARTSCREEN)) {
			repaint();
		}
	}
}
