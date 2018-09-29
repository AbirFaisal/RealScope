package com.owon.uppersoft.dso.page;

import com.owon.uppersoft.dso.view.pane.DisplayPane;
import com.owon.uppersoft.dso.view.pane.dock.ContentPane;
import com.owon.uppersoft.vds.ui.widget.custom.LContainer;

/**
 * 
 * @author Matt
 * 
 */
public class DisplayPage extends AbstractContentPage {

	public static final String Name = "M.Display.Name";

	@Override
	public LContainer createPage(IContentPage beforeApply, ContentPane cp,
			int toward) {
		DisplayPane cnp = createDisplayPane(cp);
		LContainer jp = cp.createContainerWithBackWard(cnp, this, beforeApply);
		return jp;
	}

	protected DisplayPane createDisplayPane(ContentPane cp) {
		return new DisplayPane(cp.getControlManager());
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
