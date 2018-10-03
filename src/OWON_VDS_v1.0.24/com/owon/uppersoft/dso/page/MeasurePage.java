package com.owon.uppersoft.dso.page;

import com.owon.uppersoft.dso.view.pane.MeasurePane;
import com.owon.uppersoft.dso.view.pane.dock.ContentPane;
import com.owon.uppersoft.vds.ui.widget.custom.LContainer;

public class MeasurePage extends AbstractContentPage {

	public static final String Name = "M.Measure.Name";

	@Override
	public LContainer createPage(IContentPage beforeApply, ContentPane cp,
			int toward) {
		MeasurePane cnp = new MeasurePane(cp.getControlManager());
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
