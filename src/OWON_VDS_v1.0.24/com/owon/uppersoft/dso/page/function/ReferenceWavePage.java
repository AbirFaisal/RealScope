package com.owon.uppersoft.dso.page.function;

import com.owon.uppersoft.dso.page.AbstractContentPage;
import com.owon.uppersoft.dso.page.IContentPage;
import com.owon.uppersoft.dso.view.pane.dock.ContentPane;
import com.owon.uppersoft.dso.view.pane.function.ReferenceWavePane;
import com.owon.uppersoft.vds.ui.widget.custom.LContainer;

public class ReferenceWavePage extends AbstractContentPage {

	public static final String Name = "M.Utility.ReferenceWave.Name";
	public static Object[] objectsforsave;

	@Override
	public LContainer createPage(IContentPage beforeApply, ContentPane cp,
			int toward) {
		ReferenceWavePane rwp = new ReferenceWavePane(cp.getControlManager());
		LContainer lc = cp.createContainerWithBackWard(rwp, this, cp
				.getPageManager().getContentPage(UtilityPage.Name));
		return lc;
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
