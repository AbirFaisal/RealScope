package com.owon.uppersoft.dso.page.function;

import com.owon.uppersoft.dso.page.AbstractContentPage;
import com.owon.uppersoft.dso.page.IContentPage;
import com.owon.uppersoft.dso.view.pane.dock.ContentPane;
import com.owon.uppersoft.dso.view.pane.function.CustomPane;
import com.owon.uppersoft.vds.ui.widget.custom.LContainer;

public class CustomPage extends AbstractContentPage {
	public static final String Name = "M.Custom.Name";
	private CustomPane cnp;

	@Override
	public LContainer createPage(IContentPage beforeApply, ContentPane cp,
			int toward) {
		cnp = new CustomPane(cp, cp.getControlManager(), cp.getBottomBar());
		LContainer jp = cp.createContainerWithBackWard(cnp, this, beforeApply);
		return jp;
	}

	@Override
	public String getContentID() {
		return Name;
	}

	@Override
	public void beforeLeave() {
		cnp.beforeLeave();
	}

	@Override
	public boolean canLeave() {
		return true;
	}
}
