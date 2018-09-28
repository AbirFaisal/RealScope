package com.owon.uppersoft.dso.page.function;

import com.owon.uppersoft.dso.page.AbstractContentPage;
import com.owon.uppersoft.dso.page.IContentPage;
import com.owon.uppersoft.dso.view.pane.dock.ContentPane;
import com.owon.uppersoft.dso.view.pane.function.RecordPane;
import com.owon.uppersoft.vds.ui.widget.custom.LContainer;

public class RecordPage extends AbstractContentPage {

	public static final String Name = "M.Record.Name";
	private RecordPane cnp;

	@Override
	public LContainer createPage(IContentPage beforeApply, ContentPane cp,
			int toward) {
		cnp = new RecordPane(cp.getControlManager());
		LContainer jp = cp.createContainerWithBackWard(cnp, this, beforeApply);
		return jp;
	}

	public RecordPane getRecordPane() {
		return cnp;
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
		return cnp.canLeave();
	}
}
