package com.owon.uppersoft.dso.page.function;

import com.owon.uppersoft.dso.page.AbstractContentPage;
import com.owon.uppersoft.dso.page.IContentPage;
import com.owon.uppersoft.dso.view.pane.dock.ContentPane;
import com.owon.uppersoft.dso.view.pane.function.MachineNetPane;
import com.owon.uppersoft.vds.ui.widget.custom.LContainer;

public class MachineNetPage extends AbstractContentPage {

	public static final String Name = "M.Utility.MachineNet";
	private MachineNetPane mnp;

	@Override
	public LContainer createPage(IContentPage beforeApply, ContentPane cp,
			int toward) {
		mnp = new MachineNetPane(cp.getControlManager(), cp, this);
		LContainer lc = cp.createContainerWithBackWard(mnp, this, cp
				.getPageManager().getContentPage(UtilityPage.Name));
		return lc;
	}

	@Override
	public String getContentID() {
		return Name;
	}

	@Override
	public void beforeLeave() {
		mnp.beforeLeave();
	}

	@Override
	public boolean canLeave() {
		return true;
	}
}
