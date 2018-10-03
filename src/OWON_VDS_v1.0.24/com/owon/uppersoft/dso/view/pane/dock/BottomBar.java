package com.owon.uppersoft.dso.view.pane.dock;

import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.page.IContentPage;
import com.owon.uppersoft.dso.page.function.CustomPage;
import com.owon.uppersoft.dso.page.function.HomePage;
import com.owon.uppersoft.dso.page.function.PageManager;
import com.owon.uppersoft.dso.pref.Define;
import com.owon.uppersoft.vds.ui.layout.OneRowLayout;
import com.owon.uppersoft.vds.ui.paint.LineDrawTool;
import com.owon.uppersoft.vds.ui.resource.ImagePaintUtil;
import com.owon.uppersoft.vds.ui.resource.SwingResourceManager;
import com.owon.uppersoft.vds.ui.widget.custom.LButton;
import com.owon.uppersoft.vds.ui.window.ComponentMover;

/**
 * BottomBar，移动侧边栏底部
 * 
 */
public class BottomBar extends JPanel {
	public static final int rw = 10, rh = 10, H = 40;
	public static final String CustomPath = "/com/owon/uppersoft/dso/image/Custom.png";
	public static final String Custom_Path = "/com/owon/uppersoft/dso/image/Custom_p.png";
	// (Define.def.style.path + Style.Custom_name);

	public static final TexturePaint GradientTexturePaint;
	static {
		GradientPaint p = new GradientPaint(0, 0,
				Define.def.style.CO_TitleBarTop, 0, H,
				Define.def.style.CO_TitleBarBottom, true);
		GradientTexturePaint = ImagePaintUtil.getTexturePaint(p, 1, H);
	}

	private PageManager pm;
	private ContentPane cp;

	public BottomBar(final PageManager pm, final ContentPane cp) {
		this.pm = pm;
		this.cp = cp;

		custombt = new LButton();
		custombt.setPreferredSize(new Dimension(45, 31));
		ImageIcon i = SwingResourceManager.getIcon(ContentPane.class,
				Custom_Path);
		custombt.setIcon(i);
		custombt.setRolloverIcon(LineDrawTool.getRolloverIcon(i));
		// custombt.setText(I18nProvider.bundle().getString("M.Custom.Custom"));

		custombt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean onCustom = cp.getCurrentName().equals(
						I18nProvider.bundle().getString("M.Custom.Name"));

				if (onCustom) {
					cp.applyContent(CustomPage.Name, HomePage.Name,
							IContentPage.Backward);
					setCustomBtnIconOriginal();
				} else {
					cp.applyContent(HomePage.Name, CustomPage.Name,
							IContentPage.Forward);
					ImageIcon i = SwingResourceManager.getIcon(BottomBar.class,
							CustomPath);
					custombt.setIcon(i);
					custombt.setRolloverIcon(LineDrawTool.getRolloverIcon(i));
				}
			}
		});

		setLayout(new OneRowLayout(new Insets(2, 2, 2, 2), 0));
		setPreferredSize(new Dimension(0, 40));
		setOpaque(false);

		new ComponentMover(cp.getDockDialog().getDialog(), this);
		/** KNOW 不再提供侧边栏下方的快捷按钮 */
		// updateButtonItems();
	}

	public void setCustomBtnIconOriginal() {
		// 为了保证点击快捷和后退按钮时，自定义图标的正确显示，做了以下事情
		ImageIcon i = SwingResourceManager
				.getIcon(BottomBar.class, Custom_Path);
		custombt.setIcon(i);
		custombt.setRolloverIcon(LineDrawTool.getRolloverIcon(i));
	}

	@Override
	protected void paintComponent(Graphics g) {
		int w = getWidth(), h = getHeight();
		Graphics2D g2d = (Graphics2D) g;
		g2d.setPaint(GradientTexturePaint);
		g2d.fillRect(0, 0, 15, 15);
		g2d.fillRect(w - 15, 0, 15, 15);

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.fillRoundRect(0, 0, w, h, 15, 15);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);

	}

	private LButton custombt;

	public void updateButtonItems() {
		removeAll();
		for (Integer i : pm.linked) {
			if (i == null)
				return;

			final LButton jb = new LButton();
			jb.setPreferredSize(new Dimension(45, 31));// 42, 31
			final IContentPage afterp = pm.getContentPage(i);
			ImageIcon icon = new ImageIcon(
					SwingResourceManager.m_ClassImageMap.get(afterp
							.getContentID()));

			ImageIcon ricon = LineDrawTool.getRolloverIcon(icon);

			jb.setIcon(icon);
			jb.setRolloverIcon(ricon);
			jb.setPressedIcon(icon);
			// jb.setText(i + 1 + "");
			jb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (afterp.getContentID().equals(
							cp.getCurrentPage().getContentID()))
						return;

					cp.applyContent(HomePage.Name, afterp.getContentID(),
							IContentPage.Forward);

				}
			});
			add(jb);
		}
		add(custombt);
		doLayout();
	}
}