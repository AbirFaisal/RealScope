package com.owon.uppersoft.dso.global;

import java.awt.Window;
import java.beans.PropertyChangeSupport;

import javax.swing.JComponent;

import com.owon.uppersoft.dso.page.IContentPage;
import com.owon.uppersoft.dso.page.function.HomePage;
import com.owon.uppersoft.dso.page.function.PageManager;
import com.owon.uppersoft.dso.page.function.UtilityPage;
import com.owon.uppersoft.dso.view.pane.dock.ContentPane;
import com.owon.uppersoft.dso.view.pane.dock.DockDialog;

public abstract class DefaultDockControl implements DockControl {
	private DockDialog iosf;
	private JComponent toolbtn;

	public DefaultDockControl() {
	}

	@Override
	public void init(ControlManager cm) {
		this.cm = cm;

		// 在这里创建pm，方便在load properties之前可以添加到plList中去
		pm = createPageManager();
		cm.lpList.add(pm);
	}

	protected abstract PageManager createPageManager();

	private Window wnd;
	private PropertyChangeSupport pcs;
	private ControlManager cm;

	private PageManager pm;

	public void initialize(Window wnd, PropertyChangeSupport pcs,
			JComponent toolbtn) {
		this.wnd = wnd;
		this.pcs = pcs;
		this.toolbtn = toolbtn;
	}

	public void dockDlgOnOff() {
		if (iosf == null) {
			iosf = new DockDialog(wnd, cm, pcs, toolbtn, pm);
		}
		iosf.dockDlgSetVisible(!iosf.isVisible());
	}

	public void dockDialogQuickOpenHide(String pageName) {
		if (iosf == null) {
			iosf = new DockDialog(wnd, cm, pcs, toolbtn, pm);
			iosf.dockDlgSetVisible(true);
		}
		ContentPane cp = iosf.getContentPane();
		boolean isCurrent = cp.isCurrentPage(pageName);
		if (iosf.isVisible() && isCurrent) {
			iosf.dockDlgSetVisible(false);// cp.minimize();
		} else {
			iosf.dockDlgSetVisible(true);
			cp.applyDestPane(pageName);
		}
	}

	public void dockDialog2HomePage() {
		if (iosf != null) {
			ContentPane cp = iosf.getContentPane();
			cp.applyContent(UtilityPage.Name, HomePage.Name,
					IContentPage.Forward);
		}
	}

}