package com.owon.uppersoft.dso.page;

import com.owon.uppersoft.dso.view.pane.dock.ContentPane;
import com.owon.uppersoft.dso.view.trigger.TriggerPane;
import com.owon.uppersoft.vds.ui.widget.custom.LContainer;

public abstract class TriggerPage extends AbstractContentPage {

	public static final String Name = "M.Trg.Name";

	@Override
	public LContainer createPage(IContentPage beforeApply,
			final ContentPane cp, int toward) {
		TriggerPane tp = createTriggerPane(cp);
		LContainer jp = cp.createContainerWithBackWard(tp, this, beforeApply);
		return jp;
	}

	protected abstract TriggerPane createTriggerPane(final ContentPane cp);

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
