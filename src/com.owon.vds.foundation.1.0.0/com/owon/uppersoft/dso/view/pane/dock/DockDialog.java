package com.owon.uppersoft.dso.view.pane.dock;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JComponent;
import javax.swing.JDialog;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.page.function.PageManager;
import com.owon.uppersoft.dso.pref.Define;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.vds.ui.resource.FontCenter;
import com.owon.uppersoft.vds.ui.window.MaxMinAnimator;
import com.owon.uppersoft.vds.ui.window.WindowChaser;
import com.sun.awt.AWTUtilities;

/**
 * 事件监听在 WaveFormManager.pcs 上
 * 
 * KNOW 由于这个侧边栏在java7下收起再打开会无法显示内容，所以java7暂无法使用
 * 自定义dialog弹出后再缩回去以后，再次弹出无法显示其中的内容，疑为无调用画图方法
 * 
 */
public class DockDialog implements PropertyChangeListener {

	private JDialog dlg;
	private Window wnd;

	public Window getJFrame() {
		return wnd;
	}

	private PropertyChangeSupport host;

	private JComponent jc;
	private ControlManager cm;
	private PageManager pm;

	public DockDialog(Window mw, ControlManager cm, PropertyChangeSupport pcs,
			JComponent jc, PageManager pm) {
		this.wnd = mw;
		this.cm = cm;
		this.jc = jc;
		this.pm = pm;
		host = pcs;

		if (host != null) {
			host.addPropertyChangeListener(this);
		}
	}

	public void dockDlgSetVisible(boolean visible) {
		Point p = jc.getLocationOnScreen();
		setVisible(new Rectangle(p.x, p.y, jc.getWidth(), 80), visible);
	}

	protected void onClosing() {
		if (host != null)
			host.removePropertyChangeListener(this);
	}

	public JDialog getDialog() {
		return dlg;
	}

	public void close() {
		if (dlg != null) {
			dlg.dispose();
		}
	}

	private MaxMinAnimator mu = new MaxMinAnimator();

	public void setVisible(Rectangle bloc, boolean b) {
		if (dlg == null) {
			init();
		}
		Rectangle dloc = dlg.getBounds();
		mu.setFontNColor(FontCenter.getTitleFont(),
				Define.def.style.CO_DockTitle);
		if (b) {
			mu.max(bloc, dloc, dlg);
		} else {
			mu.min(bloc, dloc, dlg);
		}
	}

	public boolean isVisible() {
		if (dlg == null)
			return false;
		return dlg.isVisible();
	}

	public ContentPane getContentPane() {
		return cp;
	}

	// public boolean isInMath() {
	// String id = cp.getCurrentPage().getContentID();
	// String mathid = mw.getDataHouse().getControlManager().getHomepage()
	// .getMathPage().getContentID();
	// return id.equals(mathid);
	// }

	private void init() {
		dlg = new JDialog(wnd) {
			@Override
			public String getName() {
				return cp.getCurrentName();
			}
		};

		dlg.setUndecorated(true);
		cp = new ContentPane(this, cm, pm);
		dlg.setContentPane(cp);
		cp.setOpaque(true);

		dlg.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onClosing();
				dlg = null;
			}

			public void windowClosed(WindowEvent e) {
				cp.beforeLeave();
			}
		});

		Point l = wnd.getLocation();
		dlg.setBounds(l.x + 1000 - 265 - +Define.ICON_BAR_WIDTH, l.y + 47,
				Define.Dock_Width + Define.ICON_BAR_WIDTH, Define.Dock_Heigth);
		wnd.addComponentListener(new WindowChaser(wnd, dlg));

		// WindowUtil.ShapeWindow(dlg, Define.def.WND_SHAPE_ARC);
		AWTUtilities.setWindowOpaque(dlg, false);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (cp != null)
			cp.apply2LContainer(evt);
		if (dlg != null
				&& evt.getPropertyName().equals(PropertiesItem.DOCK_REPAINT)) {
			dlg.repaint();
		}
	}

	private ContentPane cp;

}
