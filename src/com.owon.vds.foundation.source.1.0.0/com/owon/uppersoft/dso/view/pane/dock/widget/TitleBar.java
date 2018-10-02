package com.owon.uppersoft.dso.view.pane.dock.widget;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.page.IContentPage;
import com.owon.uppersoft.dso.pref.Define;
import com.owon.uppersoft.dso.view.pane.dock.ContentPane;
import com.owon.uppersoft.dso.view.pane.dock.DockDialog;
import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.ui.paint.LineDrawTool;
import com.owon.uppersoft.vds.ui.resource.FontCenter;
import com.owon.uppersoft.vds.ui.resource.ImagePaintUtil;
import com.owon.uppersoft.vds.ui.resource.SwingResourceManager;
import com.owon.uppersoft.vds.ui.widget.custom.LButton;
import com.owon.uppersoft.vds.ui.widget.help.ComponentChaser;
import com.owon.uppersoft.vds.ui.window.ComponentMover;

public class TitleBar extends JPanel implements Localizable {

	public static final int
			H = 40,
			BackW = 42,
			XAlign = 3,
			RoundOffset = 10,
			W = Define.Dock_Width + Define.ICON_BAR_WIDTH - 4 - XAlign - BackW;

	public static final Color CO_MainContainer = Color.DARK_GRAY;
	public static final int rw = 10, rh = 10;
	public static final TexturePaint GradientTexturePaint;
	static {
		GradientPaint p = new GradientPaint(0, 0,
				Define.def.style.CO_TitleBarTop, 0, H,
				Define.def.style.CO_TitleBarBottom, true);
		GradientTexturePaint = ImagePaintUtil.getTexturePaint(p, 1, H);
	}

	//public static final String BackPath = "/com/owon/uppersoft/dso/image/back.png";
	//public static final String MinimizePath = "/com/owon/uppersoft/dso/image/minimize.png";

	public TitleBar(final ContentPane cp) {
		setPreferredSize(new Dimension(W, H - 5));
		setLayout(null);

		backBtn = new JButton("< Menu");
		backBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				goBack(cp);
			}
		});
		add(backBtn);
		backBtn.setBounds(XAlign, 3, 75, H - 10);


		miniBtn = new LButton("X");
		miniBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cp.minimize();
			}
		});
		add(miniBtn);
		miniBtn.setBounds(W, 3, BackW, H - 10);

		setFont(FontCenter.getBigtitlefont());

		DockDialog dd = cp.getDockDialog();
		new ComponentMover(dd.getDialog(), this);
		new ComponentChaser(dd.getJFrame(), dd.getDialog(), this,
				new Runnable() {
					@Override
					public void run() {
						cp.minimize();
					}
				});
	}

	public void goBack(ContentPane cp) {
		cp.applyContent(current.getContentID(), back.getContentID(),
				IContentPage.Backward);
	}

	private IContentPage current, back;
	private JButton backBtn;
	private LButton miniBtn;

	/**
	 *Load interface-independent content with load methods
	 * 
	 * @param current
	 * @param back
	 */
	public void load(final IContentPage current, final IContentPage back) {
		this.current = current;
		this.back = back;
		backBtn.setVisible(back != null);
		tlbl = I18nProvider.bundle().getString(current.getContentID());
	}

	public String getCurrentName() {
		return tlbl;
	}

	private String tlbl;

	@Override
	public void localize(ResourceBundle rb) {
		tlbl = rb.getString(current.getContentID());
		Font f = FontCenter.getBigtitlefont();
		setFont(f);
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		int w = getWidth(), h = getHeight();
		g2d.setPaint(GradientTexturePaint);
		g2d.fillRect(0, h - 15, 15, 15);
		g2d.fillRect(w - 15, h - 15, 15, 15);

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.fillRoundRect(0, 0, w, h, 15, 15);

		g2d.setStroke(Define.def.Stroke2);
		g2d.setColor(Color.WHITE);
		Rectangle2D r2d = g2d.getFontMetrics().getStringBounds(tlbl, g2d);
		int x = (w - (int) r2d.getWidth()) >> 1;
		x = Math.max(x, XAlign + BackW);
		int y = ((h - (int) r2d.getHeight()) >> 1)
				+ g2d.getFontMetrics().getAscent();
		g2d.drawString(tlbl, x, y);

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
	}
}