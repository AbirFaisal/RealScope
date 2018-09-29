package com.owon.uppersoft.dso.view.pane.dock;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.beans.PropertyChangeEvent;
import java.util.ResourceBundle;

import javax.swing.JPanel;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.page.IContentPage;
import com.owon.uppersoft.dso.page.MathPage;
import com.owon.uppersoft.dso.page.function.HomePage;
import com.owon.uppersoft.dso.page.function.PageManager;
import com.owon.uppersoft.dso.pref.Define;
import com.owon.uppersoft.dso.view.pane.dock.widget.FunctionPanel;
import com.owon.uppersoft.dso.view.pane.dock.widget.TitleLContainer;
import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.ui.layout.OneColumnLayout;
import com.owon.uppersoft.vds.ui.widget.custom.ICLButton;
import com.owon.uppersoft.vds.ui.widget.custom.LContainer;
import com.owon.uppersoft.vds.util.LocalizeCenter;

public class ContentPane extends JPanel implements Localizable {

	private static final long serialVersionUID = 879817008906914669L;
	private SwitchPane switchPane;

	private ControlManager controlManager;

	private BottomBar bb;
	private DockDialog dock;
	private String iconButtonId;

	public DockDialog getDockDialog() {
		return dock;
	}

	private PageManager pm;

	public ContentPane(DockDialog dd, ControlManager cm, PageManager pm) {
		this.dock = dd;
		this.controlManager = cm;
		iconButtonId = ICLButton.INIT_ID;
		setLayout(new OneColumnLayout(new Insets(2, 2, 2, 2), 0));

		switchPane = new SwitchPane();
		switchPane.setOpaque(false);
		switchPane.setPreferredSize(new Dimension(0, 456));
		add(switchPane);

		this.pm = pm;

		bb = new BottomBar(pm, this);

		add(bb);
		if (controlManager.getFFTControl().isFFTon())
			applyContent(HomePage.Name, MathPage.Name, IContentPage.Forward);
		else
			applyContent(null, HomePage.Name, IContentPage.Forward);

		LocalizeCenter lc = controlManager.getLocalizeCenter();
		lc.addLocalizable(this);
	}

	public BottomBar getBottomBar() {
		return bb;
	}

	public void minimize() {
		dock.dockDlgSetVisible(false);
	}

	@Override
	protected void paintComponent(Graphics g) {
		int w = getWidth(), h = getHeight();
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Define.def.CO_DockBorder);
		g2d.setStroke(Define.def.Stroke2);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.drawRoundRect(1, 1, w - 3, h - 3, 15, 15);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	public SwitchPane getSwitchPane() {
		return switchPane;
	}

	public PageManager getPageManager() {
		return pm;
	}

	public ControlManager getControlManager() {
		return controlManager;
	}

	/**
	 * 用于表示当前页面，使IconBar图标选中状态与当前页对应
	 * 
	 * @param id
	 */
	public void setIconButtonId(String id) {
		iconButtonId = id;
	}

	public String getIconButtonId() {
		return iconButtonId;
	}

	private TitleLContainer[] tlcs;
	private TitleLContainer curtlc;

	/**
	 * 在两个备用容器中选择一个提供出去
	 * 
	 * @return
	 */
	protected TitleLContainer getAvalableTitleLContainer() {
		if (tlcs == null) {
			tlcs = new TitleLContainer[2];
			tlcs[0] = new TitleLContainer(this);
			tlcs[1] = new TitleLContainer(this);
			curtlc = null;
		}

		if (curtlc == tlcs[0]) {
			return curtlc = tlcs[1];
		} else {
			return curtlc = tlcs[0];
		}
	}

	public LContainer createContainerWithBackWard(FunctionPanel sub,
			IContentPage current, IContentPage back) {
		TitleLContainer clc = getAvalableTitleLContainer();

		clc.load(current, back, sub);
		return clc;
	}

	@Override
	public void localize(ResourceBundle rb) {
		if (currentLC != null) {
			currentLC.localize(rb);
		}
	}

	public String getCurrentName() {
		return ((TitleLContainer) currentLC).getTitleBar().getCurrentName();
	}

	private LContainer currentLC;
	private IContentPage currentPage;

	public IContentPage getCurrentPage() {
		return currentPage;
	}

	public void apply2LContainer(PropertyChangeEvent evt) {
		TitleLContainer tlc = (TitleLContainer) currentLC;
		tlc.getSub().propertyChange(evt);
	}

	public void beforeLeave() {
		TitleLContainer tlc = (TitleLContainer) currentLC;
		tlc.getSub().beforeLeave();
	}

	public boolean isCurrentPage(String contentID) {
		return contentID.equals(currentPage.getContentID());
	}

	public void applyDestPane(String pageName) {
		if (dock == null) {
			return;
		} else if (isCurrentPage(pageName)) {
			return;
		} else {
			applyContent(HomePage.Name, pageName, IContentPage.Forward, false);
		}
	}

	public void applyContent(String beforeApply, String afterApply, int toward) {
		applyContent(beforeApply, afterApply, toward, true);
	}

	/**
	 * @param beforeApply
	 *            afterApply页面的返回页面
	 * @param afterApply
	 *            要跳转的页面
	 * @param toward
	 *            跳转方式
	 */
	public void applyContent(String beforeApply, String afterApply, int toward,
			boolean isSwitch) {
		IContentPage beforePage = pm.getContentPage(beforeApply);
		IContentPage afterPage = pm.getContentPage(afterApply);
		applyContent(beforePage, afterPage, toward, isSwitch);
	}

	/**
	 * @param beforePage
	 *            afterApply页面的返回页面
	 * @param afterPage
	 *            要跳转的页面
	 * @param toward
	 *            跳转方式
	 */
	public void applyContent(IContentPage beforePage, IContentPage afterPage,
			int toward, boolean isSwitch) {
		if (afterPage == null)
			return;

		/** 由currentPage来得到当前页面的判断和处理 */
		if (currentPage != null) {
			if (!currentPage.canLeave()) {
				return;
			}
			currentPage.beforeLeave();
		}

		iconButtonId = afterPage.getContentID();

		currentLC = afterPage.createPage(beforePage, this, toward);
		switchPane.doSwitch(currentLC, toward, isSwitch);

		/**
		 * both navigate and switch
		 */
		currentPage = afterPage;
	}
}