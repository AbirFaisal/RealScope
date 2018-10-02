package com.owon.uppersoft.dso.page.function;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.owon.uppersoft.dso.page.ChannelPage;
import com.owon.uppersoft.dso.page.DisplayPage;
import com.owon.uppersoft.dso.page.IContentPage;
import com.owon.uppersoft.dso.page.MarkPage;
import com.owon.uppersoft.dso.page.MathPage;
import com.owon.uppersoft.dso.page.MeasurePage;
import com.owon.uppersoft.dso.page.SamplePage;
import com.owon.uppersoft.dso.page.TriggerPage;
import com.owon.uppersoft.dso.view.pane.dock.IconBar;
import com.owon.uppersoft.dso.view.pane.dock.widget.HomePageListCellRenderer;
import com.owon.uppersoft.dso.view.pane.function.HomePane;
import com.owon.uppersoft.vds.core.aspect.help.ILoadPersist;
import com.owon.uppersoft.vds.ui.resource.SwingResourceManager;
import com.owon.uppersoft.vds.util.Pref;
import com.owon.uppersoft.vds.util.ui.CListModel;
import sun.font.DelegatingShape;

public abstract class PageManager implements ILoadPersist {
	public LinkedList<Integer> linked;
	private List<IContentPage> pages;
	private List<IContentPage> s1, s2, s3, s4;
	private HomePage hp = new HomePage();

	public PageManager() {
		pages = new ArrayList<IContentPage>(12);

		pages.add(createTriggerPage());
		pages.add(new ChannelPage());
		pages.add(new MeasurePage());

		pages.add(new SamplePage());
		pages.add(new MarkPage());
		pages.add(createDisplayPane());
		pages.add(new MathPage());

		pages.add(new ZoomPage());
		pages.add(new RulePage());
		pages.add(new RecordPage());
		pages.add(createUtilityPage());

		prepare();

		int size = pages.size();
		pages.add(new CustomPage());
		pages.add(new MachineNetPage());
		pages.add(new MachineSettingPage());
		pages.add(new ReferenceWavePage());
		pages.add(hp);

		s1 = pages.subList(0, 3);
		s2 = pages.subList(3, 7);// 10
		s3 = pages.subList(7, size);
		s4 = pages.subList(0, size);
	}

	public HomePage getHomePage() {
		return hp;
	}

	protected abstract TriggerPage createTriggerPage();

	protected abstract DisplayPage createDisplayPane();

	protected abstract UtilityPage createUtilityPage();

	private void prepare() {
		HashMap<String, Image> cimap = SwingResourceManager.m_ClassImageMap;

		Iterator<IContentPage> it = pageIterator();
		while (it.hasNext()) {
			IContentPage icp = it.next();

			String n = icp.getContentID();

			int i1 = n.indexOf('.') + 1;

			int i2 = n.lastIndexOf('.');

			String p = n.substring(i1, i2);

			Image img = SwingResourceManager.getIcon(HomePageListCellRenderer.class, "/com/owon/uppersoft/dso/image/" + p + ".png").getImage();

			cimap.put(n, img);
		}
	}

	public void createLists(IconBar ib) {
		ib.createList(new CListModel(s4));
	}

	public int getPageNum() {
		return pages.size();
	}

	public Iterator<IContentPage> pageIterator() {
		return pages.iterator();
	}

	public IContentPage getContentPage(int index) {
		return pages.get(index);
	}

	public IContentPage getContentPage(String name) {
		for (IContentPage icp : pages) {
			if (icp.getContentID().equalsIgnoreCase(name)) {
				// System.out.println("PageManage.icp:" + icp.getContentID());
				return icp;
			}
		}
		// System.err.println("getContentPage null");
		return null;
	}

	public void load(Pref p) {
		linked = p.loadIntegerList("CustomizePages", ",");
	}

	public void persist(Pref p) {
		p.persistIntegerList("CustomizePages", linked, ",");
	}

	public void createLists(HomePane hp) {
		hp.createList(new CListModel(s1));
		hp.createList(new CListModel(s2));
		hp.createList(new CListModel(s3));
	}
}