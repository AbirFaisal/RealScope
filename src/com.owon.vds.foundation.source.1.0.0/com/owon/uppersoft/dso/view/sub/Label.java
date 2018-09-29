package com.owon.uppersoft.dso.view.sub;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.model.trigger.AbsTrigger;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.model.trigger.TriggerSet;
import com.owon.uppersoft.dso.model.trigger.VoltsensableTrigger;
import com.owon.uppersoft.dso.model.trigger.common.Voltsensor;
import com.owon.uppersoft.dso.pref.Define;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.dso.view.trigger.VoltSliderAdapter;
import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.vds.data.Point;
import com.owon.uppersoft.vds.ui.resource.SwingResourceManager;
import com.owon.uppersoft.vds.ui.slider.SliderBarLocation;
import com.owon.uppersoft.vds.ui.slider.SliderDelegate;
import com.owon.uppersoft.vds.ui.slider.SymmetrySliderBar;

public class Label extends JComponent implements MouseListener,
		SliderBarLocation, MouseMotionListener, PropertyChangeListener {
	public static final String imageRoot = "/com/owon/uppersoft/dso/image/";
	public static final String ball = "/com/owon/uppersoft/dso/image/ball";
	public static final Dimension DefaultBlockSize = new Dimension(126, 18);

	public Label(DataHouse dh, TriggerSet ts, int idx) {
		cm = dh.controlManager;
		tc = cm.getTriggerControl();
		wfm = dh.getWaveFormManager();
		setPreferredSize(DefaultBlockSize);
		setFont(Define.def.alphafont);
		addMouseListener(this);
		addMouseMotionListener(this);
		setTriggerSet(ts, idx);

		cm.pcs.addPropertyChangeListener(this);
	}

	private TriggerControl tc;

	private Image img;
	private Image img2;
	private TriggerSet ts;
	private int idx;
	private ChannelInfo ci;

	public int getIdx() {
		return idx;
	}

	private void loadImage2(AbsTrigger at) {
		String imgp = at.getIconKey();
		if (imgp != null)
			img2 = SwingResourceManager.getIcon(Label.class, imageRoot + imgp)
					.getImage();
		else
			img2 = null;
	}

	private WaveFormManager wfm;
	private ControlManager cm;

	public void setTriggerSet(TriggerSet ts, int idx) {
		this.ts = ts;
		this.idx = idx;

		if (tc.isExtTrg(idx)) {
			this.idx = -1;
			ci = null;
			img = null;
		} else {
			ci = wfm.getWaveForm(idx).wfi.ci;
			img = SwingResourceManager.getIcon(Label.class,
					ball + (idx + 1) + ".png").getImage();
		}
		loadImage2(ts.getTrigger());
	}

	private String getText() {
		if (ci == null)
			return " EXT ";
		int voltbase = ci.getVoltageLabel().getValue();
		int pos0 = ci.getPos0();

		// System.err.println(ts.id() + " , " + ts.getTrigger());
		// System.err.println(ci.number);
		String txt = ts.getTrigger().getLabelText(ci.isInverse(), voltbase,
				pos0);
		return txt;
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		if (shouldLabelClose()) {
			g2d.setColor(Color.GRAY);
			g2d.drawOval(6, 2, 14, 14);
			String name;
			if (ci != null)
				name = String.valueOf(ci.getNumber() + 1);
			else {
				name = "E";
			}
			g2d.drawString(name, 9, 14);
			g2d.setColor(Color.GRAY);
		} else {
			if (ts.isVoltsenseSupport() && rolloverR) {
				g2d.setColor(Define.def.style.CO_INFOBLOCK_HIGHLIGHT);
				g2d.fillRect(hlstart, 1, borderwidth, 20);
			}
			if (rolloverMid) {
				g2d.setColor(Define.def.style.CO_INFOBLOCK_HIGHLIGHT);
				g2d.fillRect(trgmodstart, 1, trgmodstart - chstart - 4, 20);
			}
			if (rolloverL) {
				g2d.setColor(Define.def.style.CO_INFOBLOCK_HIGHLIGHT);
				g2d.fillRect(2, 1, trgmodstart - chstart, 16);
				// g2d.fillOval(4, 0, 18, 18);
			}

			if (img != null) {
				g2d.drawImage(img, chstart, 1, null);
			} else {
				Color tmp = g2d.getColor();
				g2d.setColor(Color.YELLOW);
				g2d.drawString("EXT", 2, 15);
				g2d.setColor(tmp);
			}

			if (img2 != null) {
				g2d.drawImage(img2, trgmodstart, 2, null);
				AbsTrigger at = ts.getTrigger();
				g2d.setColor(Color.WHITE);
				Font tmp = g2d.getFont();
				g2d.setFont(Define.def.insideFont);
				at.paintIcon(g2d);
				g2d.setFont(tmp);
			}
			g2d.setColor(Color.WHITE);
		}

		String txt = getText();
		if (txt != null) {
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.drawString(txt, 58, 14);
			// if (!ci.on)
			// g2d.drawLine(58, 10, 108, 10);

			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
			int x = e.getX();
			if (x >= hlstart && x < hlstart + borderwidth) {
				if (ts.isVoltsenseSupport()) {
					if (idx >= 0) {
						wfm.getWaveForm(idx).doubleClickOnLevel();
						/** 自己刷新自身的改变内容 */
						repaint();
					}
				}
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
		rolloverR = false;
		rolloverMid = false;
		rolloverL = false;
		// if (ts.isVoltsenseSupport()) {
		setCursor(Cursor.getDefaultCursor());
		// }
		repaint();
	}

	private boolean rolloverL = false, rolloverMid = false, rolloverR = false;
	public static final int hlstart = 55, borderwidth = 70, chstart = 5,
			trgmodstart = 30;

	@Override
	public void mouseMoved(MouseEvent e) {
		if (shouldLabelClose())
			return;
		int x = e.getX();
		if (x > hlstart + 2 && x < hlstart + borderwidth) {
			if (!tc.isTrgEnable())
				return;

			if (!rolloverR) {
				rolloverR = true;
				if (ts.isVoltsenseSupport()) {// &&!cm.triggerControl.isSingleTrg()
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					repaint();
				}
			}
			return;
		} else if (x > trgmodstart + 2 && x < hlstart - 2) {
			if (!rolloverMid) {
				rolloverMid = true;
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				repaint();
			}
			return;
		} else if (x > chstart && x < trgmodstart - 2) {
			if (!rolloverL) {
				if (tc.isSingleTrg()) {
					rolloverL = true;
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					repaint();
				}
			}
			return;
		}

		if (rolloverR) {
			rolloverR = false;
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			repaint();
		}
		if (rolloverMid) {
			rolloverMid = false;
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			repaint();
		}
		if (rolloverL) {
			rolloverL = false;
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			repaint();
		}
	}

	@Override
	public Point getSliderBarLocation(int x, int y, int xs, int ys, Component cp) {
		int bw = InfoBlock.BlockWidth;
		int scrWidth = MainWindow.MAX_WIDTH;
		x = xs - x + bw + 5;
		if (x >= scrWidth - sliderwidth)
			x -= bw + (sliderwidth);
		y = 25 + ys - y - sliderheight;
		return new Point(x, y);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		final int x = e.getX();
		final int y = e.getY();

		if (shouldLabelClose())
			return;
		if (x < hlstart && x >= trgmodstart) {
			AbsTrigger at = ts.getTrigger();
			at.nextStatus();
			tc.doSubmit();
			cm.pcs.firePropertyChange(PropertiesItem.NEXT_STATUS, null, null);
			loadImage2(at);
			repaint();
			return;
		}
		if (x >= hlstart && x < hlstart + borderwidth) {
			Point p = getSliderBarLocation(x, y, e.getXOnScreen(), e
					.getYOnScreen(), e.getComponent());

			if (ts.isVoltsenseSupport() && ci != null) {
				int hr = ts.getVoltsenseHalfRange();
				VoltsensableTrigger vt = (VoltsensableTrigger) ts.getTrigger();
				final Voltsensor vs = ts.getVoltsense();
				int value = vs.c_getVoltsense();
				// 附加反相
				value = ci.getInverseValue(value);
				SymmetrySliderBar.createSymmetrySliderFrame(Platform
								.getMainWindow().getFrame(), p.x, p.y, hr, ci
								.getPos0byRange(hr), hr - value, true,
						Color.LIGHT_GRAY, SliderDelegate.BtnStatusBoth,
						new VoltSliderAdapter(tc, ci, ts
								.getVoltsenseHalfRange(), vt), I18nProvider
								.bundle());

				Platform.getMainWindow().update_ChangeVoltsense(idx);
			}
			return;
		}

		if (x >= chstart && x < trgmodstart) {
			if (tc.isSingleTrg()) {
				tc.nextSingleChannel();

				/**
				 * KNOW 单一触发时，通道改动，影响触发电平的电压数值
				 *
				 * 因为触发电平的电压数值，受到通道的电压档位和零点位置的影响，而通道改变时，这些内容会改变，所以需要随着刷新
				 */
				cm.pcs.firePropertyChange(PropertiesItem.NEXT_SINGLE_CHANNEL,
						null, tc.singleTriggerSet.getChannel());

				tc.doSubmit();
				setTriggerSet(tc.singleTriggerSet, tc.getSingleTrgChannel());
				repaint();
			}
			return;
		}
	}

	public static final int BlockWidth = 130;

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String evtn = evt.getPropertyName();
		if (evtn.equals(PropertiesItem.SWITCH_SLOWMOVE)) {
			repaint();
		} else if (evtn.equals(PropertiesItem.SWITCH_NormalMOVE)) {
			repaint();
		} else if (evtn.equals(PropertiesItem.UPDATE_VOLTSENSE)) {
			int chl = (Integer) evt.getNewValue();
			if (chl == idx)
				repaint();
		} else if (evtn.equals(PropertiesItem.UPDATE_UPP_LOW)) {
			int chl = (Integer) evt.getNewValue();
			if (chl == idx)
				repaint();
		}
	}

	private boolean shouldLabelClose() {
		return (ci != null && !ci.isOn()) || cm.getFFTControl().isFFTon()
				|| !tc.isTrgEnable();
	}

}