package com.owon.uppersoft.dso.page.function;

import com.owon.uppersoft.dso.page.AbstractContentPage;
import com.owon.uppersoft.dso.page.IContentPage;
import com.owon.uppersoft.dso.view.pane.dock.ContentPane;
import com.owon.uppersoft.dso.view.pane.function.MachineSettingPane;
import com.owon.uppersoft.vds.ui.widget.custom.LContainer;

public class MachineSettingPage extends AbstractContentPage {

	public static final String Name = "M.Utility.MachineSetting";
	public static final Object[] MachTypes = { "Machine_VDS7104",
			"Oscilloscope 2.0.6.0" };
	private MachineSettingPane msp;

	@Override
	public LContainer createPage(IContentPage beforeApply, ContentPane cp,
			int toward) {

		// MachineNetPage mnp = (MachineNetPage) ;
		msp = new MachineSettingPane(cp.getControlManager(), this);
		LContainer lc = cp.createContainerWithBackWard(msp, this, cp
				.getPageManager().getContentPage(MachineNetPage.Name));
		return lc;
	}

	@Override
	public String getContentID() {
		return Name;
	}

	@Override
	public void beforeLeave() {
		msp.beforeLeave();
	}

	@Override
	public boolean canLeave() {
		return true;
	}
}
