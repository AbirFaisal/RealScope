package com.owon.uppersoft.dso.model;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import com.owon.uppersoft.dso.function.MarkCursorControl;
import com.owon.uppersoft.dso.function.PersistentDisplay;
import com.owon.uppersoft.dso.function.PersistentPropertyChangeListener;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.WorkBench;
import com.owon.uppersoft.dso.mode.control.TimeControl;
import com.owon.uppersoft.dso.util.ui.LineUtil;
import com.owon.uppersoft.dso.wf.ChartScreenSelectModel;
import com.owon.uppersoft.vds.core.aspect.Decorate;
import com.owon.uppersoft.vds.core.aspect.help.AreaImageHelper;
import com.owon.uppersoft.vds.core.paint.Background;
import com.owon.uppersoft.vds.core.paint.ScreenContext;
import com.owon.uppersoft.vds.data.LocRectangle;
import com.owon.uppersoft.vds.data.Point;

/**
 * ChartDecorater，Chart画图器
 * 
 */
public class ChartDecorater implements Decorate, AreaImageHelper {

	private ControlManager cm;
	private Background bg;
	private WaveFormManager wfm;
	private WorkBench wb;
	private DataHouse dh;

	public ChartDecorater(final ControlManager cm, WorkBench wb, DataHouse dh) {
		this.cm = cm;
		this.wb = wb;
		this.dh = dh;
		wfm = dh.getWaveFormManager();
		pd = new PersistentDisplay(cm.getIRuntime(), this,
				cm.getSupportChannelsNumber(), dh);
		cm.pcs.addPropertyChangeListener(new PersistentPropertyChangeListener(
				cm, pd));
		bg = new Background() {

			private int VpixPerDiv;

			@Override
			public void adjustView(Rectangle rect, boolean isScreenMode_3) {
				super.adjustView(rect, isScreenMode_3);
				VpixPerDiv = isScreenMode_3 ? 25 : 50;
				setYunitlen(isScreenMode_3 ? 5 : 10);
			}

			@Override
			protected void paintLines(Graphics2D g2d, Color co) {
				super.paintLines(g2d, co);

				// g2d.setColor(Color.red);
				/* 画4个十字印 */
				final int HpixPerDiv = 50;
				final int hp = HpixPerDiv * 5, vp = VpixPerDiv * 2;
				int px1 = xcenter - hp, px2 = xcenter + hp, py1 = ycenter - vp, py2 = ycenter
						+ vp;
				stampCross(g2d, px1, py1);
				stampCross(g2d, px2, py1);
				stampCross(g2d, px1, py2);
				stampCross(g2d, px2, py2);
			}

			private void stampCross(Graphics2D g2d, int origin_x, int origin_y) {
				final int range = 4;
				g2d.drawLine(origin_x - range, origin_y, origin_x + range,
						origin_y);
				g2d.drawLine(origin_x, origin_y - range, origin_x, origin_y
						+ range);
			}
		};
	}

	@Override
	public void adjustView(ScreenContext pc, Rectangle bound) {
		Insets its = pc.getChartInsets();
		drawsz.x = bound.width + its.left;// + its.right;
		drawsz.y = bound.height + its.top;// + its.bottom;

		bg.adjustView(bound, pc.isScreenMode_3());
		wfm.adjustView(pc, bound);
		pd.adjustView(pc, bound);
	}

	public Background getBackground() {
		return bg;
	}

	@Override
	/** TODO 打印预览也调用此方法，余晖开启时由于使用画布，打印预览波形背景即使关闭仍有黑色背景 */
	public void paintView(Graphics2D g2d, ScreenContext sc, Rectangle r) {
		//Insets ci = sc.getChartInsets();
		//AffineTransform at = g2d.getTransform();
		//g2d.scale(r.width / (double) (1000 + ci.left + ci.right), 1);
		AffineTransform at = g2d.getTransform();
		g2d.scale(dh.xRate, dh.yRate);

		if (pd.isUseCanvasBuffer()) {
			bg.paintView(g2d, sc);
			wfm.paintRulePoints(g2d, sc, r);

			pd.paintView(g2d, drawsz);

			/** 余辉的时候画参考和数学计算 */
			wfm.paintViewWithoutWaveForms(g2d, sc, r);
			// g2d.setColor(Color.pink);
			// g2d.drawLine(40, 50, 60, 70);
			// g2d.drawLine(20, 30, 50, 80);
			paintLabels(g2d, sc, r);
			paintMarks(g2d, sc);
		} else {
			// fillBackground(g2d);//这里填充背景会覆盖 打印预览的波形背景
			bg.paintView(g2d, sc);
			wfm.paintRulePoints(g2d, sc, r);
			wfm.paintView(g2d, sc, r);
			paintLabels(g2d, sc, r);
			paintMarks(g2d, sc);
		}
		g2d.setTransform(at);
	}

	/**
	 * @param pc
	 */
	protected void paintLabels(Graphics2D g2d, ScreenContext pc, Rectangle r) {
		Insets its = pc.getChartInsets();
		int x0 = its.left, y0 = its.top, rw = r.width, rh = r.height;
		int y = y0;
		int x1 = x0 + rw, y1 = y0 + rh;

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		ChartScreenSelectModel cssm = wb.getMainWindow()
				.getChartChannelSelectModel();
		if (!cm.getCoreControl().isRunMode_slowMove()) {

			/** 非慢扫情况下，画水平触发位置 */
			int pos;
			// if (dh.isDMLoad()) {
			// pos = -wfm.getHorTrgPos(tc);
			// } else {
			TimeControl tc = cm.getTimeControl();
			pos = tc.getHorizontalTriggerPosition();
			// }

			// System.out.println(String.format("DM:%d, %s, %d, %d",
			// isDMLoad() ? 1 : 0, tc.getHorizontalTriggerLabel(this), tc
			// .getHorizontalTriggerPosition(), pos));
			int x = x0 + (rw >> 1) - pos;
			g2d.setColor(Color.red);
			if (!dh.isHorTrgPosFrozen()) {
				LineUtil.paintHorTrg(x, y, x0, x1, g2d);
				LineUtil.paintHorTrgDetail(x, y, r, g2d,
						tc.getHorizontalTriggerLabel(),
						cssm.isOnShowHtpDetail());
			}
		}
		lr.set(x0, x1, y0, y1);
		/** 画通道标签 */
		wfm.paintWaveFormInfo(g2d, pc, r, cm, lr, cssm);

		wfm.paintPFLabel(g2d, pc);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	private LocRectangle lr = new LocRectangle();

	protected void paintMarks(Graphics2D g2d, ScreenContext pc) {
		g2d.setColor(MarkCursorControl.Hue);

		Rectangle r = pc.getChartRectangle();

		/** 光标测量线 */
		cm.mcctr.drawMarkCursor(g2d, r);

		/** 视窗扩展线 */
		cm.getZoomAssctr().drawMarks(g2d, r);
	}

	public static final void fillBackground(BufferedImage pesistbi, int w, int h) {
		Graphics2D g2d = (Graphics2D) pesistbi.getGraphics();
		// 普通的图像缓存绘制背景色
		AlphaComposite composite = AlphaComposite.getInstance(
				AlphaComposite.CLEAR, 0.0f);
		g2d.setComposite(composite);
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, w, h);
		// g2d.clearRect(0, 0, bw, bh);

		// MainWindow mw = Platform.getMainWindow();
		// PaintContext pc = mw.getChartScreen().getPaintContext();
		// bg.paintView(g2d, pc);
		g2d.dispose();
	}

	private Point drawsz = new Point();

	public Point getDrawSize() {
		return drawsz;
	}

	public void resetARGBBufferImage(BufferedImage pesistbi) {
		fillBackground(pesistbi, drawsz.x, drawsz.y);
	}

	public BufferedImage createARGBScreenBufferedImage() {
		BufferedImage bi = new BufferedImage(drawsz.x, drawsz.y,
				BufferedImage.TYPE_INT_ARGB);
		return bi;
	}

	private PersistentDisplay pd;

	public PersistentDisplay getPersistentDisplay() {
		return pd;
	}

	@Deprecated
	public void fadeThdOn_Off_UI(int idx) {
		pd.fadeThdOn_Off_UI(idx);
	}

	public void rebuffer() {
		if (pd.isUseCanvasBuffer() && pd.isRuntime())
			pd.resetPersistBufferImage();
	}
}