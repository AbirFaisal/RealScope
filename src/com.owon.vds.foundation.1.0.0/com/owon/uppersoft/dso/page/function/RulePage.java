package com.owon.uppersoft.dso.page.function;

import com.owon.uppersoft.dso.data.LObject;
import com.owon.uppersoft.dso.page.AbstractContentPage;
import com.owon.uppersoft.dso.page.IContentPage;
import com.owon.uppersoft.dso.view.pane.dock.ContentPane;
import com.owon.uppersoft.dso.view.pane.function.RulePane;
import com.owon.uppersoft.vds.ui.widget.custom.LContainer;

public class RulePage extends AbstractContentPage {

	public static final String Name = "M.Rule.Name";
	public static final LObject[] pf = { new LObject("M.Rule.f"),
			new LObject("M.Rule.p") };

	@Override
	public LContainer createPage(IContentPage beforeApply, ContentPane cp,
			int toward) {
		RulePane cnp = new RulePane(cp.getControlManager());
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
