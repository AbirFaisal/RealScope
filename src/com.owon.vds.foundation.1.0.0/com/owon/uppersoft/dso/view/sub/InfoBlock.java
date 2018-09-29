package com.owon.uppersoft.dso.view.sub;

import static com.owon.uppersoft.dso.pref.Define.def;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.pref.Define;
import com.owon.uppersoft.dso.pref.Style;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.core.wf.WaveFormInfo;
import com.owon.uppersoft.vds.ui.resource.SwingResourceManager;
import com.owon.uppersoft.vds.ui.widget.ComboBoxOwner;

public class InfoBlock extends JPanel implements MouseListener,
		MouseMotionListener, MouseWheelListener, Localizable,
		PropertyChangeListener {

	public static final String sel = (Define.def.style.path + Style.selname);
	public static final String uns = (Define.def.style.path + Style.unsname);

	public static final int floor0 = 30;
	public static final int floor1 = 45;
	public static final int floor2 = 61;
	public static final int floor3 = 77;

	public static final int BlockHeight = 82;
	public static final int BlockWidth = 124;
	public static final Dimension DefaultBlockSize = new Dimension(
			BlockWidth + 1, BlockHeight + 1);

	public String vblbl, pos0lbl, freqlbl;

	private WaveForm wf;
	private boolean select = false;
	private int name;

	public void setSelected(boolean b) {
		this.select = b;
		String p;
		if (select)
			p = sel + name + ".png";
		else
			p = uns + name + ".png";
		icon = SwingResourceManager.getIcon(InfoBlock.class, p);
		// unsicon = SwingResourceManager.getIcon(InfoBlock.class, uns + name+
		// ".png");
	}

	public WaveForm getWaveForm() {
		return wf;
	}

	private ChannelInfo getChannelInfo() {
		return wf.wfi.ci;
	}

	public int getChannelNumber() {
		return getChannelInfo().getNumber();
	}

	private String getDivUnit() {
		return cm.getDivUnit();
	}

	private String getDivUnits() {
		return cm.getDivUnits();
	}

	public void updateVolt() {
		ChannelInfo ci = getChannelInfo();
		vblbl = ci.getVoltageLabel() + " /" + getDivUnit();
	}

	private void updateVBLabel() {
		ChannelInfo ci = getChannelInfo();
		vblbl = ci.getVoltageLabel() + " /" + getDivUnit();
	}

	public void updatePos0() {
		ChannelInfo ci = getChannelInfo();
		pos0lbl = ci.getPos0() / (double) GDefine.PIXELS_PER_DIV
				+ getDivUnits();
	}

	public void updateFreq() {
		freqlbl = cm.getFreqLabel(getChannelInfo());
	}

	private static final String onPath = "/com/owon/uppersoft/dso/image/on.png";
	private static final String offPath = "/com/owon/uppersoft/dso/image/off.png";
	public static final Image onImage, offImage;
	static {
		offImage = SwingResourceManager.getIcon(InfoBlock.class, offPath)
				.getImage();
		onImage = SwingResourceManager.getIcon(InfoBlock.class, onPath)
				.getImage();
	}

	private void updateONOFFImage() {
		onoffImage = getChannelInfo().isOn() ? offImage : onImage;
	}

	public void localize(ResourceBundle bd) {
		updateVolt();
		updatePos0();
	}

	public void setWaveForm(WaveForm wf) {
		this.wf = wf;
		WaveFormInfo wfi = wf.wfi;
		ChannelInfo ci = wfi.ci;
		vblbl = ci.getVoltageLabel() + " /" + getDivUnit();
		// System.out.println("	vblbl:"+vblbl);
		pos0lbl = ci.getPos0() / (double) GDefine.PIXELS_PER_DIV
				+ getDivUnits();
		freqlbl = cm.getFreqLabel(ci);
		name = ci.getNumber() + 1;
		// datanum = wf.getDatalen();

		updateONOFFImage();
	}

	private InfoPane inp;
	private ImageIcon icon;
	private ControlManager cm;

	/**
	 * Create the panel
	 */
	public InfoBlock(InfoPane inp, WaveForm wf1, ControlManager cm) {
		ibd = new InfoBlockAction(cm, this);
		this.cm = cm;
		this.inp = inp;
		setWaveForm(wf1);
		setPreferredSize(DefaultBlockSize);
		setLayout(null);
		setOpaque(false);
		setFont(Define.def.alphafont);

		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
	}

	private InfoBlockAction ibd;

	public InfoBlockAction getInfoBlockDelegate() {
		return ibd;
	}

	public ComboBoxOwner getComboBoxOwner() {
		return ibd;
	}

	public void returnVbFloor() {
		Point mousePoint = MouseInfo.getPointerInfo().getLocation();
		Point ibPoint = this.getLocationOnScreen();
		if (mousePoint.x >= ibPoint.x + hlstart
				&& mousePoint.x <= ibPoint.x + hllen
				&& mousePoint.y >= ibPoint.y + floor0
				&& mousePoint.y <= ibPoint.y + floor1) {
			linePos = floor1;
		}
		repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON1)
			return;
		ChannelInfo ci = getChannelInfo();
		boolean on = ci.isOn();
		if (!select) {
			inp.notify_a(this);
			ibd.onBlockPressed2Select(ci);
		}

		int x = e.getX();
		int y = e.getY();

		if (linePos == floor2) {
			if (on)
				ibd.f2_pos0Pressed(wf, e);
		} else if (linePos == floor1) {
			if (on)
				ibd.f1_vbPressed(ci, floor1);
		} else if (y < floor0) {// &&x > onoffstart && x <onoffend
			if (x > iconend && x < onoffstart) {
				// if (on && y > (floor0 >> 1) && x > ratestart && x < rateend)
				// {
				// ibd.f0_frontPressed(ci, e);
				// }
			} else if (x >= onoffstart) {
				if (cm.getFFTControl().isFFTon())
					return;
				ibd.f0_back_uncheck_Pressed(wf);
			}
		}

		if (linePos != floor1) {
			ibd.f1_notPressed();
		}

		if (leftlinePos == floor1) {
			if (on)
				ibd.f1_couplingPressed(ci);
		}

	}

	private boolean onVBline;

	@Override
	public void mouseMoved(MouseEvent e) {

		int l1, l2;
		int x = e.getX();
		int y = e.getY();
		int c = Cursor.HAND_CURSOR;
		boolean on = wf.isOn();
		boolean rp = false;
		onVBline = false;

		if (y > floor0) {// ||x > hlstart + hllen
			if (!on) {
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				return;
			}

			if (x < hlstart) {
				linePos = -1;
				rp = true;

				if (y < floor1) {
					l1 = floor1;
				} else if (y >= floor1 && y < floor2) {
					l1 = floor2;
					c = Cursor.DEFAULT_CURSOR;
				} else {
					l1 = floor3;
					c = Cursor.DEFAULT_CURSOR;
				}

				if (leftlinePos != l1) {
					leftlinePos = l1;
					rp = true;
					setCursor(Cursor.getPredefinedCursor(c));
				}
			} else {// if(x>hlstart)
				leftlinePos = -1;
				rp = true;

				if (y < floor1) {
					l2 = floor1;
					onVBline = true;
				} else if (y >= floor1 && y < floor2) {
					l2 = floor2;
				} else {
					l2 = floor3;
					c = Cursor.DEFAULT_CURSOR;
				}

				if (linePos != l2) {
					linePos = l2;
					rp = true;
					setCursor(Cursor.getPredefinedCursor(c));
				}
			}
		} else {
			l2 = floor0;
			c = Cursor.DEFAULT_CURSOR;
			// if (x > ratestart && x < rateend && y > (floor0 >> 1) && on) {
			// c = Cursor.HAND_CURSOR;
			// }
			if (linePos != l2) {
				leftlinePos = linePos = l2;
				rp = true;
			}

			setCursor(Cursor.getPredefinedCursor(c));
		}

		if (rp)
			repaint();
	}

	public static final int txtstart = 40, hlstart = 35, hllen = 85,
			hlhigh = 15, onoffstart = 102, iconend = 25;

	// public static final int ratestart = 37, rateend = 78;

	@Override
	public void mouseExited(MouseEvent e) {
		mouseover = false;
		linePos = -1;
		leftlinePos = -1;
		repaint();
	}

	private int leftlinePos = -1;
	private int linePos = -1;
	private boolean mouseover = false;

	private Image onoffImage;

	@Override
	protected void paintComponent(Graphics g) {
		// super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		ChannelInfo ci = getChannelInfo();
		boolean on = ci.isOn();
		if (on && icon != null) {
			g2d.drawImage(icon.getImage(), 0, 0, null);
		}

		if (linePos > floor0 && linePos < floor3 && on) {
			// g2d.setComposite(def.composite40);
			g2d.setColor(def.style.CO_INFOBLOCK_HIGHLIGHT);
			g2d.fillRect(hlstart, linePos - hlhigh, hllen, hlhigh);
			// g2d.setComposite(def.composite100);
		}
		// if (linePos == floor0 && on) {
		// g2d.setColor(def.style.CO_INFOBLOCK_HIGHLIGHT);
		// Rectangle2D r2 = g.getFontMetrics().getStringBounds(
		// ci.getProbeLabel(), g);
		// g2d.fillRect(ratestart, 13, (int) r2.getWidth() + 5, (int) r2
		// .getHeight());
		// }

		if (leftlinePos == floor1) {
			g2d.setColor(def.style.CO_INFOBLOCK_HIGHLIGHT);
			g2d.fillRect(0, floor0, hlstart, hlhigh);
		}

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		if (!on) {
			g2d.setColor(Color.GRAY);
			g2d.drawOval(5, 2, 20, 20);
			g2d.drawString(String.valueOf(ci.getNumber() + 1), 11, 18);
		}

		// g2d.drawString("p: " + datanum, 30, floor0 - 2);
		if (mouseover) {
			g2d.setColor(Color.WHITE);
			// g2d.drawOval(onoffstart, 4, g2d.getFont().getSize() << 1, g2d
			// .getFont().getSize());
			Font tmp = g2d.getFont();
			g2d.setFont(Define.def.OnOffFont);
			// g2d.drawString(onoff, onoffstart, floor0 - 14);

			if (!cm.getFFTControl().isFFTon())
				g2d.drawImage(onoffImage, onoffstart, 5, null);
			g2d.setFont(tmp);
			if (on) {
				Rectangle2D r2 = g.getFontMetrics().getStringBounds(
						ci.getProbeLabel(), g);
				// g2d.drawLine(ratestart, 27, ratestart + (int) r2.getWidth() +
				// 3, 27);
				g2d.drawLine(hlstart + 5, floor2, hlstart + hllen - 10, floor2);
				g2d.drawLine(5, floor1, 30, floor1);
				g2d.drawLine(hlstart + 5, floor1, hlstart + hllen - 10, floor1);
			}

		}

		// paintLightText(g2d);

		g2d.setColor(on ? Color.WHITE : Color.DARK_GRAY);
		{// if(on||mouseover)
			// g2d.drawString(ci.getProbeLabel(), 40, 25);
			if (ci.isBandlimit() && cm.isBandLimit())
				g2d.drawString("(b)", 40, 25);
			g2d.drawString(vblbl, txtstart, floor1 - 2);
			g2d.drawString(pos0lbl, txtstart, floor2 - 2);
			g2d.drawString(freqlbl, txtstart, floor3 - 2);
			g2d.drawString((ChannelInfo.COUPLINGCHARS[ci.getCouplingIdx()]), 5,
					floor1 - 2);
		}

		g2d.setColor((mouseover || select) ? ci.getColor()
				: def.CO_INFOBLOCK_BORDER);

		g2d.drawRoundRect(0, 0, BlockWidth - 1, BlockHeight - 1, 11, 11);

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	private void paintLightText(Graphics2D g2d) {
		if (mouseover) {
			Font tmp = g2d.getFont();
			g2d.setComposite(AlphaComposite.getInstance(
					AlphaComposite.SRC_OVER, 0.15f));
			g2d.setFont(Define.def.LightTextFont);
			g2d.setColor(wf.getColor());
			g2d.drawString(vblbl, txtstart - 2, floor1 - 2);

			g2d.setComposite(AlphaComposite.getInstance(
					AlphaComposite.SRC_OVER, 1f));
			g2d.setFont(tmp);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		ChannelInfo ci = getChannelInfo();
		if (!ci.isOn())
			return;
		int x = e.getX();
		int y = e.getY();
		if (y < floor0) {
			if (x > iconend && x < onoffstart)
				ibd.onClicked(e.getClickCount());
			else if (x < iconend)
				ibd.dockDialogQuickAct();
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		mouseover = true;
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		// System.err.println(onVBline);
		if (onVBline) {
			int t = e.getWheelRotation();
			// System.err.println(t);
			ibd.incretSelect(t);
			repaint();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String pn = evt.getPropertyName();
		if (pn.equals(PropertiesItem.CHANNEL_OPTION)) {
			updateONOFFImage();
		} else if (pn.equals(PropertiesItem.TUNE_VBBCHANGE)) {
			updateVBLabel();
		} else if (pn.equals(PropertiesItem.UPDATE_CHLVOLT)) {
			updateVolt();
			repaint();
		}
	}
}
