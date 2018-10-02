package com.owon.uppersoft.dso.view;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.ResourceBundle;

import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import com.owon.uppersoft.dso.function.measure.MeasureManager;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.model.WaveFormInfoControl;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.page.MeasurePage;
import com.owon.uppersoft.dso.pref.Define;
import com.owon.uppersoft.dso.util.ui.FloatGlassP;
import com.owon.uppersoft.dso.view.sub.InfoBlock;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.measure.MeasureElem;
import com.owon.uppersoft.vds.core.measure.MeasureModel;
import com.owon.uppersoft.vds.core.measure.MeasureT;
import com.owon.uppersoft.vds.core.measure.VR;
import com.owon.uppersoft.vds.ui.resource.SwingResourceManager;
import com.owon.uppersoft.vds.util.format.SFormatter;

public class ValuePane extends JPanel implements PropertyChangeListener,
		MouseInputListener {
	private VR[] vrs;
	private WaveFormManager wfm;
	public MeasureModel measMod;
	private final String MsgKeyPrefix = "AutoMeasure.";
	private ControlManager cm;

	private boolean mouseOver, dragDelete;
	private Image addbtnImg, minusbtnImg;
	private final String addPath = "/com/owon/uppersoft/dso/image/add.png";
	private final String minusPath = "/com/owon/uppersoft/dso/image/minus.png";
	private final int ROWGAP = 15, COLGAP = 90, HalfCOLGAP = COLGAP >> 1,
			inorc = 24;
	private int selCol = -1, dragTarget = -1;
	public static final int Height = 90;
	private JPanel gp;

	public ValuePane(DataHouse dh) {
		setOpaque(false);

		wfm = dh.getWaveFormManager();
		cm = dh.controlManager;
		measMod = dh.controlManager.measMod;
		addMouseListener(this);
		addMouseMotionListener(this);
		addbtnImg = SwingResourceManager.getIcon(InfoBlock.class, addPath)
				.getImage();
		minusbtnImg = SwingResourceManager.getIcon(InfoBlock.class, minusPath)
				.getImage();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String pn = evt.getPropertyName();
		if (pn.equals(MeasureManager.RefreshMeasureResult)) {
			repaint();
		}
	}

	private String getMeasureType(int mtidx, ResourceBundle rb) {
		String mt = MsgKeyPrefix + MeasureT.VALUES[mtidx];
		String type = rb.getString(mt);
		// 限定字符
		type = SFormatter.getRestrictSubString(type, 12);
		return type;
	}

	@Override
	protected void paintComponent(Graphics g) {
		// super.paintComponent(g);
		// if (!controlManager.getMeasureManager().ison())
		// return;
		Graphics2D g2d = (Graphics2D) g;
		boolean isCH_empty = measMod.isCHlinkEmpty();
		boolean isCHon = !isCH_empty;
		g.setColor(Color.LIGHT_GRAY);
		int cox = 5, coy = 15;

		if (mouseOver) {
//			Graphics2D g2 = (Graphics2D) g;
			g2d.drawImage(addbtnImg, 15, 0, null);
		}

		/** 列标题显示 */
		g.drawString("", cox, coy);
		ResourceBundle rb = I18nProvider.bundle();
		for (Integer mtidx : measMod.MTlinked) {
			String type = getMeasureType(mtidx, rb);
			if (mtidx.equals(measMod.MTlinked.getFirst()))
				cox += COLGAP >> 1;
			else
				cox += COLGAP;
			if (isCHon)
				g.drawString(type, cox, coy);// MeasureT列名
		}
		/** 延迟标题 */
		if (measMod.getOthLinkedCount() > 0) {
			int x;
			if (isCHon)
				x = cox + 2 * COLGAP;
			else
				x = COLGAP;
			g.drawString(rb.getString("AutoMeasure.Delay"), x, coy);
		}

		/** 测量值 */
		g.setColor(Color.white);
		coy += ROWGAP;

		WaveFormInfoControl wfic = wfm.getWaveFormInfoControl();
		Iterator<WaveForm> iwf = wfic.getLowMachineWFIterator();
		while (iwf.hasNext()) {
			WaveForm wf = iwf.next();
			cox = 10;

			vrs = wf.getMeasureADC().vrs;
			Integer integer = wf.wfi.ci.getNumber();
			if (measMod.hasCHlinkElem(integer)) {
				g.drawString(wf.toString(), cox, coy);
				cox = 5;
				// for (int i = 0; i < measMod.MTlinked.size(); i++) {
				for (Integer i : measMod.MTlinked) {
					if (i.equals(measMod.MTlinked.getFirst()))
						cox += COLGAP >> 1;
					else
						cox += COLGAP;
					// int idx = measMod.MTlinked.get(i);
					int idx = (int) i;
					if (wf.isOn()) {
						g.drawString(vrs[idx].vu, cox, coy);// vrs[idx].vu
					} else {
						g.drawString("--", cox, coy);
					}
				}
				coy += ROWGAP;
			}
		}

		/** 延迟的值 */
		if (measMod.getOthLinkedCount() > 0) {
			int x, y;
			if (isCHon)
				x = COLGAP * measMod.MTlinked.size() - 40;
			else
				x = -50;
			y = 15 + ROWGAP;

			for (MeasureElem me : measMod.othMTlinked) {
				if (me.on) {
					String txt = rb.getString(me.label);
					txt = SFormatter.getRestrictSubString(txt, 12);
					if (wfic.isSupportDelay1_2())
						// y = y+ROWGAP*me.idx;
						g.drawString(txt + ":  " + me.vu, x + COLGAP, y);
					else
						g.drawString(txt + ":  --", x + COLGAP, y);

					if (measMod.othMTlinked.size() > 2
							&& measMod.othMTlinked.size() <= 4) {
						if (wfic.isSupportDelay3_4())
							// y = y+ROWGAP*me.idx;
							g.drawString(txt + ":  " + me.vu, x + COLGAP, y);
						else
							g.drawString(txt + ":  --", x + COLGAP, y);
					}

					y += ROWGAP;
				}
				// System.out.println(y);
			}
		}

		if (isCH_empty && !measMod.MTlinked.isEmpty()) {
			g.drawString(rb.getString("M.Measure.UncheckedCHs"), 50, 40);
		}

		if (dragTarget != -1 || selCol != -1) {
			final int w = COLGAP, h = getHeight();
			int x1 = getselColLocation(selCol);
			// int x2 = getselColLocation(dragTarget);
//			Graphics2D g2d = (Graphics2D) g;
			g2d.setComposite(AlphaComposite.getInstance(
					AlphaComposite.SRC_OVER, (float) 0.5));
			g2d.setColor(Define.def.style.CO_DockContainer);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.fillRoundRect(x1, 0, w - 4, h - 8, inorc, inorc);
			g2d.drawRoundRect(x1, 0, w - 4, h - 8, inorc, inorc);
			// g2d.drawRoundRect(x2, 0, w - 4, h - 15, inorc, inorc);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);

			if (dragDelete) {
				Graphics2D g2 = (Graphics2D) g;
				g2.drawImage(minusbtnImg, x1 + COLGAP - 20, 0, null);
			}
		}

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (measMod.isCHlinkEmpty() || measMod.isMTlinkEmpty())
			return;
		/* 设置列删除状态 */
		int py = e.getY();
		boolean isOut = py < 0 || py > getHeight();
		if (dragDelete != isOut) {
			dragDelete = isOut;
			repaint();
		}
		/* 列交换效果1 */
		int tar = getPointColumn(e.getX());
		if (tar != dragTarget) {
			cm.measMod.moveMtElem(selCol, tar);
			selCol = dragTarget = tar;
			// System.out.println("selCol:" + selCol + "target:" + dragTarget);
			repaint();
		}
		/* 列拖拽效果2 */
		moveGlassPaneAmongValuePane(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		doMouseAction('M', e.getX(), e.getY());
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		doMouseAction('C', e.getX(), e.getY());
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (measMod.isCHlinkEmpty() || measMod.isMTlinkEmpty())
			return;
		int x = e.getX();
		selCol = getPointColumn(x);
		repaint();// 选中效果1
		initGlassPane();// 选中效果2
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		/* 移动 */
		// int x = e.getX();
		// int insCol = getPointColumn(x);
		// controlManager.measMod.moveMTlinkedElem(selCol, insCol);
		/* 删除 */
		if (dragDelete) {
			cm.measMod.removeSelCol(selCol);
			cm.pcs.firePropertyChange(
					MeasureModel.Refresh_MeasurePane_Selected, null, null);
			dragDelete = false;
		}
		releaseSelect();
		repaint();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		mouseOver = true;
		repaint();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		mouseOver = false;
		repaint();
	}

	private void openHideMeasurePane() {
		cm.getDockControl().dockDialogQuickOpenHide(MeasurePage.Name);
	}

	private int getPointColumn(int x) {
		int selCol = -1;
		if (x > HalfCOLGAP && x < COLGAP)
			selCol = 0;
		else if (x > COLGAP) {
			x -= HalfCOLGAP;

			selCol = x / COLGAP;
			if (selCol > measMod.getColumnMax() - 1
					|| selCol > measMod.getColumnCount() - 1)
				selCol = -1;

		}
		return selCol;
	}

	private int getselColLocation(int selCol) {
		return HalfCOLGAP + selCol * COLGAP;
	}

	private void releaseSelect() {
		/* 关闭选中效果 */
		releaseGlassPane();
		selCol = dragTarget = -1;
	}

	private FloatGlassP seljf;
	boolean isSelFrameOn = false;

	private void initGlassPane() {
		gp = Platform.getMainWindow().getGlassPane();
		// 选中效果2
		if (isSelFrameOn && selCol >= 0) {
			Point l = getselColScreenLocation(selCol);
			final int w = COLGAP, h = getHeight();// controlManager.measMod.getRowCount()
			seljf = new FloatGlassP(l.x, l.y, w, h);
			gp.add(seljf);
			gp.setVisible(true);
		}
	}

	private void moveGlassPaneAmongValuePane(MouseEvent e) {
		// 拖拽效果2
		if (isSelFrameOn) {
			if (seljf == null)
				return;
			int x = e.getX() - (seljf.getWidth() >> 1);
			int y = seljf.getY();
			// 拖拽过程效果，TODO 需优化bl的获取
			int bl = 0// Platform.getMainWindow().getFrame().getLocation().x
			+ HalfCOLGAP;
			int br = bl + measMod.getColumnCount() * COLGAP - seljf.getWidth();
			if (x < bl)
				x = bl;
			if (x > br)
				x = br;
			seljf.setLocation(x, y);
		}
	}

	private void releaseGlassPane() {
		if (isSelFrameOn && seljf != null) {
			gp.remove(seljf);
			gp.setVisible(false);
		}
	}

	private Point getselColScreenLocation(int selCol) {
		int x = 0, y = 0;
		Point l = new Point(0, 0);// Platform.getMainWindow().getFrame().getLocation();
		x = l.x + (HalfCOLGAP + (selCol) * COLGAP);
		y = l.y + 557;
		return new Point(x, y);
	}

	private void doMouseAction(char act, int x, int y) {
		boolean enterable = x < 42 && y < 16;
		switch (act) {
		case 'M':
			if (enterable)
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			else
				setCursor(Cursor.getDefaultCursor());
			break;
		case 'C':
			if (enterable)
				openHideMeasurePane();
			break;
		}
	}

}