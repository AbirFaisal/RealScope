package com.owon.uppersoft.dso.view.pane.dock;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.page.IContentPage;
import com.owon.uppersoft.dso.page.function.HomePage;
import com.owon.uppersoft.dso.page.function.PageManager;
import com.owon.uppersoft.dso.pref.Define;
import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.ui.layout.OneColumnLayout;
import com.owon.uppersoft.vds.ui.paint.LineDrawTool;
import com.owon.uppersoft.vds.ui.resource.FontCenter;
import com.owon.uppersoft.vds.ui.resource.SwingResourceManager;
import com.owon.uppersoft.vds.ui.widget.custom.ICLButton;
import com.owon.uppersoft.vds.util.ui.CListModel;

public class IconBar extends JPanel implements Localizable {
	private ContentPane cp;
	private ICLButton[] b;
	private int buttonSize;
	private String[] s;

	public IconBar(final ContentPane cp) {
		setLayout(new OneColumnLayout(new Insets(8, 0, 8, 0), 0));
		setPreferredSize(new Dimension(Define.ICON_BAR_WIDTH, 0));
		setBackground(Define.def.style.CO_DockHomeCellBack);

		this.cp = cp;
		PageManager pm = cp.getPageManager();
		buttonSize = 0;

		pm.createLists(this);
	}

	public void createList(CListModel os) {
		buttonSize = os.getSize();
		b = new ICLButton[buttonSize];
		s = new String[buttonSize];
		for (int i = 0; i < buttonSize; i++) {
			final IContentPage icp = (IContentPage) os.getElementAt(i);
			s[i] = icp.getContentID();
			Image img = SwingResourceManager.m_ClassImageMap.get(s[i]);
			Icon icon = SwingResourceManager.getIcon(img);
			b[i] = new ICLButton();
			b[i].setPreferredSize(new Dimension(0, 35));
			b[i].setIcon(icon);
			b[i].setRolloverIcon(LineDrawTool.getRolloverIcon((ImageIcon) icon));
			b[i].setId(s[i]);
			b[i].setBackgroundColor(Color.gray);
			b[i].setBorderColor(Color.gray);
			if (cp.getIconButtonId() == s[i]) {
				b[i].setNotEnabledColor(Define.def.style.CO_DockHomeBack);
				b[i].setDisabledIcon(icon);
				b[i].setNotEnabled();
			}

			b[i].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cp.applyContent(HomePage.Name, icp.getContentID(),
							HomePage.Forward);
				}
			});
			add(b[i]);
		}
		localize(I18nProvider.bundle());
	}

	@Override
	public void localize(ResourceBundle rb) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < buttonSize; i++) {
			sb.setLength(0);
			sb.append("<html><span style=font-family:"
					+ FontCenter.getLabelFont().getFamily()
					+ ";font-size:11px;font-weight:bold;>" + rb.getString(s[i])
					+ "</span></html>");
			b[i].setToolTipText(sb.toString());
		}
	}
}
