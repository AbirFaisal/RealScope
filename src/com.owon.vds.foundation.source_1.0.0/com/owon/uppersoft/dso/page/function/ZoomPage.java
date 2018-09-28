package com.owon.uppersoft.dso.page.function;

import com.owon.uppersoft.dso.page.AbstractContentPage;
import com.owon.uppersoft.dso.page.IContentPage;
import com.owon.uppersoft.dso.view.pane.dock.ContentPane;
import com.owon.uppersoft.dso.view.pane.function.ZoomPane;
import com.owon.uppersoft.vds.ui.widget.custom.LContainer;

public class ZoomPage extends AbstractContentPage {

	public static final String Name = "M.Zoom.Name";

	@Override
	public LContainer createPage(IContentPage beforeApply, ContentPane cp,
			int toward) {
		ZoomPane cnp = new ZoomPane(cp.getControlManager());
		LContainer jp = cp.createContainerWithBackWard(cnp, this, beforeApply);
		return jp;
	}

	@Override
	public String getContentID() {
		return Name;
	}

	@Override
	public void beforeLeave() {
	}

	@Override
	public boolean canLeave() {
		return true;
	}
}
