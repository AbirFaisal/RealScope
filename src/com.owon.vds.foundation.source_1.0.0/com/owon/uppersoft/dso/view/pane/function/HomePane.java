package com.owon.uppersoft.dso.view.pane.function;

import java.awt.Component;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;

import javax.swing.JList;
import javax.swing.ListCellRenderer;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.page.IContentPage;
import com.owon.uppersoft.dso.page.function.HomePage;
import com.owon.uppersoft.dso.pref.Define;
import com.owon.uppersoft.dso.view.pane.dock.ContentPane;
import com.owon.uppersoft.dso.view.pane.dock.widget.FunctionPanel;
import com.owon.uppersoft.dso.view.pane.dock.widget.HomePageListCellRenderer;
import com.owon.uppersoft.vds.ui.layout.OneColumnLayout;
import com.owon.uppersoft.vds.ui.resource.SwingResourceManager;
import com.owon.uppersoft.vds.util.ui.CListModel;

public class HomePane extends FunctionPanel {

	private HomePage hp;

	public HomePane(HomePage hp, ContentPane cp, ControlManager cm) {
		super(cm);
		this.cp = cp;
		this.hp = hp;

		setLayout(new OneColumnLayout(new Insets(8, 8, 8, 8), 8));
		setBackground(Define.def.style.CO_DockHomeBack);

		cp.getPageManager().createLists(this);
	}

	public void createList(CListModel os) {
		final JList list = new JList(os);
		list.setCellRenderer(new ListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				IContentPage icp = (IContentPage) value;// list.getModel().getElementAt(index);
				String id = icp.getContentID();
				Image img = SwingResourceManager.m_ClassImageMap.get(id);

				String n = (I18nProvider.bundle().getString(id));
				int type = HomePageListCellRenderer.HomeListCenter;
				if (list.getModel().getSize() - 1 == index) {
					type = HomePageListCellRenderer.HomeListDown;
				} else if (index == 0) {
					type = HomePageListCellRenderer.HomeListUp;
				}

				cp.setIconButtonId(id);

				return new HomePageListCellRenderer(type, n, cellHasFocus, img);
			}
		});
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				/** 也可把处理放在Pressed或Clicked中，寻找更好的体验 */
				JList jl = (JList) e.getSource();
				int idx = jl.locationToIndex(e.getPoint());
				cp.applyContent(hp.getContentID(), ((IContentPage) jl
						.getModel().getElementAt(idx)).getContentID(),
						HomePage.Forward);
			}
		});
		add(list);
	}

	@Override
	public void localize(ResourceBundle rb) {
		super.localize(rb);
		repaint();
	}

	private ContentPane cp;

}