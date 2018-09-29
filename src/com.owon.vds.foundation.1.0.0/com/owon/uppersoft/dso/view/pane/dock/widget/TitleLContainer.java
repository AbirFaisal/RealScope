package com.owon.uppersoft.dso.view.pane.dock.widget;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

import com.owon.uppersoft.dso.page.IContentPage;
import com.owon.uppersoft.dso.page.function.HomePage;
import com.owon.uppersoft.dso.view.pane.dock.ContentPane;
import com.owon.uppersoft.dso.view.pane.dock.IconBar;
import com.owon.uppersoft.vds.ui.widget.custom.LContainer;

public class TitleLContainer extends LContainer {
	private IContentPage current;
	private TitleBar title;
	private FunctionPanel sub;
	private IconBar iconBar;
	private ContentPane cp;

	public TitleLContainer(final ContentPane cp) {
		setLayout(new BorderLayout());

		this.cp = cp;

		title = new TitleBar(cp);
		add(title, BorderLayout.NORTH);

		setFocusable(true);
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				int cd = e.getKeyCode();
				if (cd == KeyEvent.VK_BACK_SPACE) {
					title.goBack(cp);
				}
			}
		});
	}

	public TitleBar getTitleBar() {
		return title;
	}

	public IconBar getIconBar() {
		return iconBar;
	}

	public FunctionPanel getSub() {
		return sub;
	}

	protected IContentPage getCurrent() {
		return current;
	}

	/**
	 * 通过装载方法载入和界面无关的内容
	 * 
	 * @param cur
	 * @param back
	 * @param subp
	 */
	public void load(IContentPage cur, IContentPage back, FunctionPanel subp) {
		current = cur;
		title.load(cur, back);
		if (sub != null)
			remove(sub);
		sub = subp;
		add(sub, BorderLayout.CENTER);

		if (iconBar != null)
			remove(iconBar);
		if (!current.getContentID().equalsIgnoreCase(HomePage.Name)) {
			iconBar = new IconBar(cp);
			add(iconBar, BorderLayout.WEST);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		// System.out.print("finalize ");
		// System.out.println(current != null ? current.getContentID() : "");
		super.finalize();
	}

	@Override
	public void localize(ResourceBundle rb) {
		title.localize(rb);
		sub.localize(rb);
		iconBar.localize(rb);
	}
}