package com.owon.uppersoft.dso.page.function;

import com.owon.uppersoft.dso.page.AbstractContentPage;
import com.owon.uppersoft.dso.page.IContentPage;
import com.owon.uppersoft.dso.view.pane.dock.ContentPane;
import com.owon.uppersoft.dso.view.pane.function.HomePane;
import com.owon.uppersoft.vds.ui.widget.custom.LContainer;

public class HomePage extends AbstractContentPage {

	public static final String Name = "M.Home.Name";

	HomePage() {
	}

	@Override
	public LContainer createPage(IContentPage beforeApply, ContentPane cp,
			int toward) {
		HomePane hp = new HomePane(this, cp, cp.getControlManager());
		LContainer jp = cp.createContainerWithBackWard(hp, this, null);
		return jp;
	}

	@Override
	public void beforeLeave() {
	}

	@Override
	public boolean canLeave() {
		return true;
	}

	@Override
	public String getContentID() {
		return Name;
	}

}
